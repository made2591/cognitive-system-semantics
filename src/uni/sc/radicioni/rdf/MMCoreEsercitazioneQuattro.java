package uni.sc.radicioni.rdf;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import uni.sc.radicioni.babelnet.MMBabelNet;
import uni.sc.radicioni.rocchio.MMTFIDF;
import uni.sc.radicioni.wordnet.MMStanfordUtil;
import uni.sc.util.MMConfig;
import uni.sc.util.tools.MMDocumentsUtil;
import uni.sc.util.tools.MMToolbox;
import java.io.*;
import java.util.*;

import static uni.sc.util.logger.MMLogger.*;

/**
 * Classe per costruire il tool RDF
 *
 * Created by Matteo on 23/04/15.
 */
public class MMCoreEsercitazioneQuattro implements Serializable {

    // Database per la connessione a Babelnet
    public static MMBabelNet database;
    // Toolbox per la gestione di stampe
    public static MMToolbox toolsbox;
    // Utilità per l'italiano (rimozione stopword, lemmatizzazione)
    public static MMDocumentsUtil docutil;
    // Configurazioni varie
    public static MMConfig config;
    // file da utilizzare per rimuovere le stopwords
    public static int stopwords_file = 1;
    // Stanford Parser
    public static MMStanfordUtil stanfordutil;

    // Directory di default per i documenti
    public static String documents_dir = "EMPTY_DIR";
    public static String filename = "DEFAULT_FILE_NAME";

    // Mapping nome documento (nome file) => lista di parole (lemmatizzate)
    public HashMap<String, ArrayList<String>> documents = new HashMap<String, ArrayList<String>>();
    // Mapping nome documento => classe di appartenenza
    public HashMap<String, String> documents_classes = new HashMap<String, String>();

    /**
     * Costruttore del core dell'applicazione
     * @param doc_dir : directory con i documenti
     * @param fn : nome del file da usare per fare caching
     */
    public MMCoreEsercitazioneQuattro(String doc_dir, String fn, int swf) {

        config = new MMConfig();
        database = new MMBabelNet();
        toolsbox = new MMToolbox();
        docutil = new MMDocumentsUtil();
        documents_dir = doc_dir;
        filename = config.getRDF_DEFAULT_FILENAME();
        stopwords_file = swf;
        stanfordutil = new MMStanfordUtil();
        System.clearProperty("wordnet.database.dir");
        System.setProperty("wordnet.database.dir", config.getWORDNET_DICT());

    }

    /**
     * Recupera il modello, ricreandolo salvandolo o caricandolo a seconda del parametro mode
     * @param mode :
     *             0 => legge da un dump nella posizione definita nella configurazione
     *             1 => rigenera il documento senza salvare
     *             2 => rigenera il documento salvando un dump nella posizione definita nella configurazione
     * @return Model : modello
     */
    public Model getRDFModel(int mode) {
        Model model = null;
        try {
            if (mode > 0) {
                model = createRDFModel();
                logTitle("Creo il modello per la directory: " + documents_dir);
                if (mode > 1) {
                    logTitle("Salvo il modello in posizione: " + filename);
                    RDFWriter fasterWriter = model.getWriter("RDF/XML");
                    try {
                        FileOutputStream fout = new FileOutputStream(filename);
                        fasterWriter.write(model, fout, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                logTitle("Carico il modello in posizione: "+filename);
                model = loadRDFModel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * Crea il modello RDF basandosi sulle conosenze della struttura dei documenti stessi
     * @return Model : modello
     */
    private Model createRDFModel() throws IOException {

        MMTFIDF tfidf_calc = new MMTFIDF();

        // crea un modello RDF
        Model model = ModelFactory.createOntologyModel();
        String base = "http://purl.org/metadata/dublin_core#";
        model.setNsPrefix("dc", base);

        File[] allfiles = new File(documents_dir).listFiles();
        // se riesco ad aprire la directory
        if (allfiles != null) {
            logSection("Numero di file presenti: " + allfiles.length);
            int count_document = 2;
            // per ogni file
            for (File f : allfiles) {
                // se è un file di testo
                if (f.getName().endsWith(".txt")) {

                    logSubSubParag("Valuto contenuto: " + f.getName());
                    String doc_content = docutil.getDocumentContent(f.getAbsolutePath());
                    ArrayList<String> cleaned_content = this.cleanSentence(doc_content);
                    ArrayList<String> lemmed_words = new ArrayList<String>();
                    for(String cleaned_word : cleaned_content) {
                        lemmed_words.add(stanfordutil.getLemmaOfWord(cleaned_word));
                    }

                    // crea una risorsa per ogni file presente nella directory
                    logSubSubParag("Creo la risorsa: " + f.getName());
                    Resource doc = model.createResource(doc_content.split("\n")[1].replace("# ", ""));

                    // aggiunge la proprietà titolo
                    String title = doc_content.split("\n")[4];
                    logSubSubParag("Aggiungo la proprietà title: " + title);
                    Property hasTitle = model.createProperty(base + "title");
                    model.add(doc, hasTitle, title);

                    HashMap<String, Double> tf_doc = tfidf_calc.TFCalculator(lemmed_words);

                    List<Map.Entry<String, Double>> best = getGreatest(tf_doc, 3);
                    ArrayList<String> most_rappr_words = new ArrayList<>();
                    for(Map.Entry<String, Double> entry : best) {
                        most_rappr_words.add(entry.getKey());
                    }

                    // aggiunge la proprietà subjecta
                    String subject = toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(most_rappr_words, ", ");
                    logSubSubParag("Aggiungo la proprietà subject (3 parole con più alto TF): " + subject);
                    Property hasSubject = model.createProperty(base + "subject");
                    model.add(doc, hasSubject, subject);

                    // aggiunge la proprietà description
                    String description = doc_content.split("\n")[6];
                    logSubSubParag("Aggiungo la proprietà description (prima riga non nulla dopo il titolo): " + description);
                    Property hasDescription = model.createProperty(base + "description");
                    model.add(doc, hasDescription, description);

                    // aggiunge la proprietà date
                    String date = doc_content.split("\n")[doc_content.split("\n").length-1];
                    logSubSubParag("Aggiungo la proprietà date (ultima riga non nulla): " + date);
                    Property hasDate = model.createProperty(base + "date");
                    model.add(doc, hasDate, date);

                    // aggiunge la proprietà creator
                    String creator = "Matteo Madeddu";
                    if(Math.random() > 0.5 && count_document > 0) {
                        // aggiunge la proprietà creator
                        creator = "Anakin Skywalker";
                        count_document--;
                    }
                    logSubSubParag("Aggiungo la proprietà soggetto: " + creator);
                    Property hasCreator = model.createProperty(base + "creator");
                    model.add(doc, hasCreator, creator);

                    // aggiunge la proprietà publisher
                    String publisher = "BBC";
                    logSubSubParag("Aggiungo la proprietà publisher: " + publisher);
                    Property hasPublisher = model.createProperty(base + "publisher");
                    model.add(doc, hasPublisher, publisher);

                }
            }
        }

        return model;

    }

    /**
     * Carica un modello RDF scritto su file nella posizione filename
     * @return Model : modello
     */
    private Model loadRDFModel() {

        // create an empty model
        Model model = ModelFactory.createDefaultModel();

        // use the FileManager to find the input file
        InputStream in = FileManager.get().open(filename);
        if (in == null) {
            throw new IllegalArgumentException("File: " + filename + " not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        return model;
    }

    /**
     * Esegue una query per uguaglianza di valore. Cerca i documenti la cui proprietà
     * specificata ha valore esattamente uguale a quello passato come parametro.
     * @param model : modello
     * @param prop : nome prop
     * @param val : valore
     */
    public ArrayList<String> getDocumentWithSpecificPropertyValue(Model model, String prop, String val) {

        logSubSection("Cerco tutti i documenti con proprietà: " + prop + " = (uguale) '"+val+"'");
        String queryString = "PREFIX dc: <http://purl.org/metadata/dublin_core#>\n" +
                "SELECT ?tit\n" +
                "WHERE  {\n" +
                "    ?document dc:title ?tit.\n" +
                "    ?document dc:"+prop+" ?val.\n" +
                "    FILTER( ?val = '"+val+"').\n" +
                "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ArrayList<String> res = new ArrayList<>();
        try {
            com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                res.add(soln.getLiteral("tit").getString());
            }
        } finally {
            qexec.close();
        }
        return res;

    }

    /**
     * Esegue una query per uguaglianza di valore. Cerca i documenti la cui proprietà
     * specificata ha valore che contiene quello passato come parametro.
     * E' equivalente al LIKE sql.
     * @param model : modello
     * @param prop : nome prop
     * @param val : valore
     */
    public ArrayList<String> getDocumentWithSubstringPropertyValue(Model model, String prop, String val) {

        logSubSection("Cerco tutti i documenti con proprietà: " + prop + " c (incluso) '"+val+"'");
        String queryString = "PREFIX dc: <http://purl.org/metadata/dublin_core#>\n" +
                "SELECT ?tit\n" +
                "WHERE  {\n" +
                "    ?document dc:title ?tit.\n" +
                "    ?document dc:"+prop+" ?val.\n" +
                "    FILTER(contains(?val, '"+val+"')).\n" +
                "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ArrayList<String> res = new ArrayList<>();
        try {
            com.hp.hpl.jena.query.ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                res.add(soln.getLiteral("tit").getString());
            }
        } finally {
            qexec.close();
        }
        return res;

    }

    /**
     * Tira fuori le tre Entry di una Map con i valori più alti. Si. Sembra incredibile
     * anche a me, ma Java fa proprio schifo. Si si. Fa schifo. djsakjdjlkadjklsajkldsjklasdjkl
     * @param map
     * @param n
     * @param <K>
     * @param <V>
     * @return
     */
    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> getGreatest(Map<K, V> map, int n) {
        Comparator<? super Map.Entry<K, V>> comparator =
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
                        V v0 = e0.getValue();
                        V v1 = e1.getValue();
                        return v0.compareTo(v1);
                    }
                };
        PriorityQueue<Map.Entry<K, V>> highest =
                new PriorityQueue<Map.Entry<K, V>>(n, comparator);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            highest.offer(entry);
            while (highest.size() > n) {
                highest.poll();
            }
        }

        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
        while (highest.size() > 0) {
            result.add(highest.poll());
        }
        return result;
    }

    /**
     * Questo metodo pulisce una stringa facendo utilizzo di uno stopwords file
     * @param s : Stringa, Sentence da cui rimuovere le stopwords
     * @return ArrayList<String> infos : sentence
     */
    public ArrayList<String> cleanSentence(String s) {

        logSection("Pulisco la stringa '" + s.substring(0, 30) + "..." + "' tramite espressione regolare");
        s = s.replaceAll("[^a-zA-Z ]", "").toLowerCase();

        // creo variabili per computazione e risultato
        ArrayList<String> s_array = toolsbox.splitSentenceAsArrayListOfString(s);
        ArrayList<String> s_result = toolsbox.splitSentenceAsArrayListOfString(s);

        // buffer reader per leggere il file di stopwords
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader(
                            new File(config.getSTOP_WORDS_PATH()+File.separator+config.getSTOP_WORDS_FILE(stopwords_file))
                    )
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String line;
            try {
                if(br != null) {
                    String stop_words_removed = "";
                    ArrayList<String> already_removed = new ArrayList<>();
                    while ((line = br.readLine()) != null) {
                        for(String w : s_array) {
                            if(w.equalsIgnoreCase(line)) {
                                if(line.trim().length() != 0) {
                                    if(!already_removed.contains(line)) {
                                        stop_words_removed += line + "; ";
                                        already_removed.add(line);
                                    }
                                }
                                while(s_result.remove(w)) {s_result.remove(w);}
                            }
                        }
                    }
                    if(!stop_words_removed.trim().isEmpty()) {
                        logSection("Rimuovo le stopwords '" + stop_words_removed + "'");
                    } else {
                        logSection("Nessuna stowords rimossa!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> real_res = new ArrayList<>();
        for(String elem : s_result) {
            if(elem.trim().length() > 0) real_res.add(elem);
        }

        return real_res;

    }

}