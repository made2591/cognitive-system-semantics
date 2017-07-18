package uni.sc.radicioni.rocchio;

import uni.sc.radicioni.babelnet.MMBabelNet;
import uni.sc.util.tools.MMItalianUtil;
import uni.sc.util.MMConfig;
import uni.sc.util.tools.MMToolbox;
import static uni.sc.radicioni.rocchio.MMCosineSimilarity.diffDoubleCentroidHashMapVector;
import static uni.sc.util.logger.MMLogger.*;
import static uni.sc.util.tools.MMItalianUtil.cleanSentence;
import java.io.*;
import java.util.*;

/**
 * Classe per costruire lo space vector model
 *
 * Created by Matteo on 23/04/15.
 */
public class MMCoreEsercitazioneTre implements Serializable {

    // Database per la connessione a Babelnet
    public static MMBabelNet database;
    // Toolbox per la gestione di stampe
    public static MMToolbox toolsbox;
    // Utilità per l'italiano (rimozione stopword, lemmatizzazione)
    public static MMItalianUtil itautil;
    // Configurazioni varie
    public static MMConfig config;

    // Directory di default per i documenti
    public static String documents_dir = "EMPTY_DIR";
    // Valore di beta di default (solo positivi)
    public static Double beta = 1.0;
    // Valore di gamma di default (zero negativi)
    public static Double gamma = 0.0;
    public static int version = -1;
    public static String filename = "default_file_name";

    // Valori di documenti da inserire in percentuale per training,
    // developing
    // e testing
    public Double percentage_training_set = 0.8;
    public Double percentage_developing_set = 0.0;
    public Double percentage_testing_set = 0.2;

    // Numero di documenti totali presenti nella cartella
    public int number_of_documents = -1;
    // Mapping nome documento (nome file) => lista di parole (pulite e lemmatizzate)
    public HashMap<String, ArrayList<String>> documents = new HashMap<String, ArrayList<String>>();
    // Mapping nome documento => classe di appartenenza
    public HashMap<String, String> documents_classes = new HashMap<String, String>();
    // Mapping nome classe => training_doc => #
    //                     => developing_doc => #
    //                     => testing_doc => #
    public HashMap<String, HashMap<String, Integer>> classes_counting = new HashMap<String, HashMap<String, Integer>>();
    // Lista di classi recuperate dalla directory
    public ArrayList<String> classes = new ArrayList<String>();
    // Mapping nome classe => centroide (vettore dimensione => valore)
    public HashMap<String, HashMap<String, Double>> classes_centroids = new HashMap<String, HashMap<String, Double>>();
    // Array con i nomi di file dei documenti usati per il training, developing e testing
    public ArrayList<String> training_documents = new ArrayList<String>();
    public ArrayList<String> developing_documents = new ArrayList<String>();
    public ArrayList<String> testing_documents = new ArrayList<String>();

    // Dimensioni dello spazio vettoriale (parole, lemmatizzate, ordinate lessicograficamente)
    public ArrayList<String> dimensions = new ArrayList<String>();
    // Mapping contenente i valori IDF di ogni parola contenuta in dimensions
    public HashMap<String, Double> idf = new HashMap<String, Double>();
    // Mapping contenente i valori TD di ogni documento usato durante la fase di training
    public HashMap<String, HashMap<String, Double>> tf = new HashMap<String, HashMap<String, Double>>();

    public static Boolean near_pos_defined = false;
    public static int near_pos_mode = 1;
    public HashMap<String, ArrayList<String>> near_pos = new HashMap<String, ArrayList<String>>();

    /**
     * Costruttore del core dell'applicazione
     * @param doc_dir : directory con i documenti
     * @param b : valore di beta per il calcolo dei centroidi con formula
     * @param g : valore di alfa per il calcolo dei centroidi con formula
     * @param train : valore in percentuale 0.0-1.0 di documenti da usare nel training
     * @param dev : valore in percentuale 0.0-1.0 di documenti da usare nel developing
     * @param tes : valore in percentuale 0.0-1.0 di documenti da usare nel testing
     * @param v : versione del core da mandare in esecuzione
     * @param nps : versione dell'algoritmo per calcolare i NEAR POSITIVE
     * @param fn : nome del file da usare per fare caching
     */
    public MMCoreEsercitazioneTre(String doc_dir, Double b, Double g, Double train, Double dev, Double tes, int v, int nps, String fn) {

        config = new MMConfig();
        database = new MMBabelNet();
        toolsbox = new MMToolbox();
        itautil = new MMItalianUtil();
        documents_dir = doc_dir;
        beta = b;
        gamma = g;
        version = v;
        filename = fn;
        percentage_training_set = train;
        percentage_developing_set = dev;
        percentage_testing_set = tes;
        near_pos_mode = nps;

    }

    /**
     * Metodo per il training dello spazio
     * @param mode :
     *             0 => legge da un dump nella posizione definita nella configurazione
     *             1 => rigenera lo spazio senza salvare
     *             2 => rigenera lo spazio salvando un dump nella posizione definita nella configurazione
     * @return core dell'applicazione
     */
    public MMCoreEsercitazioneTre training(boolean random_test_mode, int mode) {
        MMCoreEsercitazioneTre ret = this;
        logTitle("Inizio il training");
        try {
            if (mode > 0) {
                trainingInternal(random_test_mode, mode);
                if (mode > 1) {
                    logSection("Salvo un dump del core dell'applicazione");
                    FileOutputStream fout = new FileOutputStream(config.getROCCHIO_CACHE_FILE_ITA()+"ROCCHIO.dat");
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(this);
                }
            } else {
                if(!random_test_mode) {
                    logSection("Carico un dump del core dell'applicazione per non rieseguire il training");
                    FileInputStream fin = new FileInputStream(config.getROCCHIO_CACHE_FILE_ITA() + "ROCCHIO.dat");
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    ret = (MMCoreEsercitazioneTre) ois.readObject();
                } else {
                    logSection("Carico un dump E RANDOMIZZO l'ordine dei documenti per training/developing/testing");
                    FileInputStream fin = new FileInputStream(config.getROCCHIO_CACHE_FILE_ITA() + "ROCCHIO.dat");
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    ret = (MMCoreEsercitazioneTre) ois.readObject();
                    this.documents = ret.documents;
                    trainingInternal(random_test_mode, mode);
                    ret = this;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logTitle("Fine del training");
        return ret;
    }

    /**
     * Metodo per il training dello spazio
     * @return core dell'applicazione
     */
    private MMCoreEsercitazioneTre trainingInternal(boolean random_test_mode, int mode) {

        // istanzio la classe che calcolerà i valori idf per ogni dimensione
        // e tf per ogni termine in ogni documento usato per il training
        MMTFIDF tfidf_calculator = new MMTFIDF();

        try {
            // inizializzo lo spazio vettoriale
            this.initDocumentsMapAndSpaceVectorDimensions(random_test_mode, mode);
            logSection("Calcolo il vettore TF per i "+documents.size()+" documenti usati per il training");
            // #################################################################################### //
            // IMPORTANTE: a questo punto documents contiene solo i documenti in training           //
            // Infatti in fase di creazione dello spazio (initDocumentsMapAndSpaceVectorDimensions) //
            // sono stati puliti e lemmatizzati solo i documenti necessari al training dello spazio //
            // Nel metodo NEAR POS tuttavia succedono cose brutte: quindi per sicurezza, passo un   //
            // elenco filtrato di quelli che sono i documenti su cui bisogna calcolare il TF e l'id //
            // #################################################################################### //
            HashMap<String, ArrayList<String>> temp_train_doc = new HashMap<>();
            for(String train_doc_name : training_documents) {
                temp_train_doc.put(train_doc_name, documents.get(train_doc_name));
            }
            this.tf = tfidf_calculator.DocsetTFCalculator(temp_train_doc);
            logSection("Calcolo il vettore IDF per i "+dimensions.size()+" termini (dimensioni) dello spazio");
            this.idf = tfidf_calculator.DocumentIDFCalculator(dimensions, temp_train_doc);
            logSection("Calcolo il centroide per le "+classes.size()+" classi individuate");
            computeClassCentroid();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;

    }

    // Implementing Fisher–Yates shuffle
    private static void shuffleArray(File[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            File a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    /**
     * Il metodo esegue il testing basandosi sulla collezione di documenti utilizzati durante il training: nella
     * fase di costruzione, vengono isolate porzioni relative per ogni classe al fine di mantenere per ognuna un
     * adeguato numero di documenti di training, di developing (NEAR POSITIVE) e di testing.
     */
    public void testing() {

        try {
            if(near_pos_defined) {
                // salvo il numero di documenti che riesco a classificare correttamente
                HashMap<String, HashMap<String, Double>> classes_errors_map = new HashMap<String, HashMap<String, Double>>();
                if(near_pos_mode != 4) {
                    if (developing_documents.size() == 0) {
                        logSection("Calcolo i NEAR POSITIVE facendo testing e guardando gli errori sui documenti di training");
                        classes_errors_map = testing(training_documents);
                    } else {
                        logSection("Calcolo i NEAR POSITIVE facendo testing e guardando gli errori sui documenti di developing");
                        classes_errors_map = testing(developing_documents);
                    }
                } else {
                    logSection("Calcolo i NEAR POSITIVE considerando i centroidi");
                }
                logErrorMap(classes_errors_map);
                near_pos = analyzeErrorMap(classes_errors_map);
            } else {
                logSection("Testing sui testing documents");
                HashMap<String, HashMap<String, Double>> classes_errors_map = testing(testing_documents);
                logErrorMap(classes_errors_map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Logga la error_map dopo testing;
     *
     * @param classes_errors_map
     */
    public void logErrorMap(HashMap<String, HashMap<String, Double>> classes_errors_map) {

        for(String klass_1 : classes_errors_map.keySet()) {
            logSubSubParag("Classe '" + klass_1 + "' testata su " + classes_counting.get(klass_1).get("testing") + " documenti");
            for(String klass_2 : classes_errors_map.keySet()) {
                logSubSubParag("Classe '" + klass_1 + "' confusa con '" + klass_2 + "': " + classes_errors_map.get(klass_1).get(klass_2));
            }
        }

    }

    /**
     * Analizza l'error map ed estrappola delle liste di possibili near_positive class
     *
     * @param classes_errors_map
     * @return
     */
    public HashMap<String, ArrayList<String>> analyzeErrorMap(HashMap<String, HashMap<String, Double>> classes_errors_map) {

        HashMap<String, ArrayList<String>> class_near_pos = new HashMap<String, ArrayList<String>>();

        // Viene considerata NEAR POSITIVE solo la classe con cui sono stati fatti più errori
        // (se sono uguali più classi per numero, viene presa una sola classe in modo arbitrario
        if(near_pos_mode == 1) {
            logSubSection("Viene considerata NEAR POSITIVE solo la classe con cui sono stati fatti più errori. 1 sola classe arbitraria se ci sono più classi con pari # errori");
            for (String klass_1 : classes) {
                Double maxValueInMap = (Collections.max(classes_errors_map.get(klass_1).values()));
                for (Map.Entry<String, Double> entry : classes_errors_map.get(klass_1).entrySet()) {
                    if (entry.getValue().equals(maxValueInMap)) {
                        ArrayList<String> temp = new ArrayList<String>();
                        temp.add(entry.getKey());
                        class_near_pos.put(klass_1, temp);
                    }
                }
            }
        }
        // Vengono considerate NEAR POSITIVE solo le classi con cui è stato fatto almeno un errore
        // Se una classe A non è mai stata confusa con nessuna, NESSUNA CLASSE è considerata NEAR POSITIVE di A
        else if(near_pos_mode == 2) {
            logSubSection("Vengono considerate NEAR POSITIVE solo le classi con cui è stato fatto almeno un errore. Nessuna classe è NPOS se non ci sono errori.");
            for (String klass_1 : classes) {
                ArrayList<String> temp = new ArrayList<String>();
                for (String klass_2 : classes) {
                    if(classes_errors_map.get(klass_1).get(klass_2) > 0.0) {
                        temp.add(klass_2);
                    }
                }
                class_near_pos.put(klass_1, temp);
                logSubSection("Classe: " + klass_1 + ", " + class_near_pos.get(klass_1).size() + " ha NEAR POSITIVE: " + toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(class_near_pos.get(klass_1), "; "));
            }
        }
        // Vengono considerate NEAR POSITIVE solo le classi con cui è stato fatto almeno un errore.
        // Se una classe A non è mai stata confusa con nessuna, OGNI CLASSE è considerata NEAR POSITIVE di A
        else if(near_pos_mode == 3) {
            logSubSection("Vengono considerate NEAR POSITIVE solo le classi con cui è stato fatto almeno un errore. Tutte le classi sono NPOS se non ci sono errori.");
            for (String klass_1 : classes) {
                ArrayList<String> temp = new ArrayList<String>();
                for (String klass_2 : classes) {
                    if(classes_errors_map.get(klass_1).get(klass_2) > 0.0) {
                        temp.add(klass_2);
                    }
                }
                if(temp.size() == 0) {
                    for(String klass_2 : classes) {
                        temp.add(klass_2);
                    }
                }
                class_near_pos.put(klass_1, temp);
                logSubSection("Classe: " + klass_1 + ", " + class_near_pos.get(klass_1).size() + " ha NEAR POSITIVE: " + toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(class_near_pos.get(klass_1), "; "));
            }

        }
        // Vengono considerate NEAR POSITIVE solo le classi con centroide più vicino alla classe presa in esame
        // Per ogni classe, esiste una sola classe NEAR POSITIVE ed è quella con centroide più vicino (cosSim)
        else if(near_pos_mode == 4) {
            logSubSection("Inizio il confronto di ogni centroide con ogni centroide.");
            MMCosineSimilarity cossimUtil = new MMCosineSimilarity();
            for (String klass_1 : classes_centroids.keySet()) {
                ArrayList<String> temp = new ArrayList<String>();
                double max_similarity = -1.0;
                String nearest_class = "NOT_FOUND";
                // per ogni altro centroide
                for (String klass_2 : classes_centroids.keySet()) {
                    // escludo il centroide della classe stessa
                    if(!klass_2.equals(klass_1)) {
                        // trasformazione di tipo che serve a java (collection to arraylist)
                        ArrayList<Double> klass_1_centroid = new ArrayList<Double>(classes_centroids.get(klass_1).values());
                        ArrayList<Double> klass_2_centroid = new ArrayList<Double>(classes_centroids.get(klass_2).values());
                        Double res = cossimUtil.cosineSimilarity(klass_1_centroid, klass_2_centroid);
                        if (res > max_similarity) {
                            nearest_class = klass_2;
                            max_similarity = res;
                        }
                    }
                }
                temp.add(nearest_class);
                logSubSection("Classe: " + klass_1 + " ha NEAR POSITIVE: " + nearest_class + ";");
                class_near_pos.put(klass_1, temp);
            }

        }

        return class_near_pos;

    }

    /**
     * Eseguo il testing sul set di documenti con nome presente nella lista
     *
     * @param testing_docs : lista nomi di documenti di testing
     */
    public HashMap<String, HashMap<String, Double>> testing(ArrayList<String> testing_docs) {

        logTitle("Inizio il testing");
        // costruisco un utility per calcolare la cosine similarity tra due vettori
        MMCosineSimilarity cossimUtil = new MMCosineSimilarity();
        // istanzio l'oggetto per calcolare tf e idf
        MMTFIDF tfidf_calculator = new MMTFIDF();

        // hashmap con i risultati del testing
        HashMap<String, HashMap<String, Double>> classes_errors_map = new HashMap<String, HashMap<String, Double>>();
        for (String klass_one : classes) {
            HashMap<String, Double> error_for_class = new HashMap<String, Double>();
            for (String klass_two : classes) {
                error_for_class.put(klass_two, 0.0);
            }
            classes_errors_map.put(klass_one, error_for_class);
        }

        // per contare i test che hanno successo per ogni classe
        int correct = 0;
        // counter per il logging
        int tested_docs = 1;
        // per ogni documento di test
        for (String testing_doc_name : testing_docs) {
            logSection("Nome documento: '" + testing_doc_name + "' - (" + (tested_docs++) + " / " + testing_docs.size() + ")");
            // aggiungo ai documenti dell'applicazione il documento di testing: l'operazione eseguita è quella di
            // rimozione delle stopwords e lemmatizzazione già avvenuta sui documenti di training in fase di costruzione
            // dello spazio vettoriale. Il tutto avviene attraverso l'utilizzo del metodo createObjectDocument
            ArrayList<String> original_doc = documents.get(testing_doc_name);
            // inizio l'operazione di proiezione del documento di testing nello spazio creato in fase di training:
            // le parole in esso contenute potrebbero non far parte del dizionario dello spazio, quindi devono essere ignorate.
            // Discorso analogo per le parole che descrivono lo spazio e non sono presenti nel documento, per le quali il tf
            // corrispettivo verrà impostato a zero
            HashMap<String, Double> projected_doc = new HashMap<String, Double>();

            logSubSection("Calcolo del vettore tf per il documento");
            // calcolo tf per ogni parola del documento
            HashMap<String, Double> tf_testing_doc = tfidf_calculator.TFCalculator(original_doc);

            // per ogni dimensione dello spazio
            for (String dimension : dimensions) {
                // inserisco un default a zero
                projected_doc.put(dimension, 0.0);
                // se la dimensione è presente nel documento
                if (original_doc.contains(dimension)) {
                    projected_doc.put(dimension, tf_testing_doc.get(dimension) * idf.get(dimension));
                }
            }

            // similarità massima tra il documento di testing in esame e un centroide
            double max_similarity = -1.0;
            // la classe migliore per il documento
            String best_class = "NOT_FOUND";

            // dummy var per il logging (contare il numero di classi usate)
            int count_class = 0;

            logSubSection("Inizio confronto per ogni classe");
            for (String klass : classes_centroids.keySet()) {
                count_class++;

                // trasformazione di tipo che serve a java (collection to arraylist)
                ArrayList<Double> klass_centroid = new ArrayList<Double>(classes_centroids.get(klass).values());
                ArrayList<Double> test_doc_term = new ArrayList<Double>(projected_doc.values());

                // calcolo la cosine similarity tra il documento di testing e il centroide della classe in esame
                double cosineSim = cossimUtil.cosineSimilarity(klass_centroid, test_doc_term);
                logSubSubParag("Confronto centroide classe '" + klass + "' (" + count_class + "/" + classes_centroids.size() + ") => valore: " + cosineSim);
                if (cosineSim > max_similarity) {
                    max_similarity = cosineSim;
                    best_class = klass;
                }
            }

            // isolo la clase reale del documento
            String doc_real_class = documents_classes.get(testing_doc_name);
            // se la best class predetta è corretta incremento il numero di documenti corretti
            if (doc_real_class.equalsIgnoreCase(best_class)) correct++;
            else {
                // recupero la mappa per identificare gli errori di una classe rispetto a un'altra
                HashMap<String, Double> error_between_klass = classes_errors_map.get(doc_real_class);
                // incremento il numero di errori per la classe predetta
                logSubSubParag("Error between klass: " + best_class);
                logSubSubParag("Error between klass: " + toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(new ArrayList<String>(classes_centroids.keySet()), "; "));
                error_between_klass.put(best_class, error_between_klass.get(best_class) + 1.0);
                // salvo la coppia classe_vera => classe_sbagliata => #numero errori (+1 sopra)
                classes_errors_map.put(doc_real_class, error_between_klass);
            }
            logSubSection("Documento di testing: " + testing_doc_name + ". " +
                    "Classe migliore: " + best_class + ", " +
                    "Classe reale: " + documents_classes.get(testing_doc_name));
            logSubSubSection("Corretti su totali: " + correct + "/" + testing_docs.size());
            flushLogger();
        }

        // CALCOLO DELLE'ERRORE RELATIVO
        //        for(String class_error : classes_errors_map.keySet()) {
        //            // recupero la mappa per identificare gli errori di una classe rispetto a un'altra
        //            HashMap<String, Double> error_bet_klass = classes_errors_map.get(class_error);
        //            for(String error_made : error_bet_klass.keySet()) {
        //                error_bet_klass.put(error_made, error_bet_klass.get(error_made)/classes_counting.get(class_error).get("testing"));
        //            }
        //            // salvo la coppia classe_vera => classe_sbagliata => #numero errori relativi al numero di documenti di quella classe
        //            classes_errors_map.put(class_error, error_bet_klass);
        //        }

        logTitle("Fine test. Corretti su totali: " + correct + "/" + testing_docs.size());
        return classes_errors_map;

    }

    /**
     * Restituisce una lista ordinata alfabeticamente in ordine lessicografico di termini presenti dentro
     * la cartella di documenti specificata nel path passato come parametro e inizializza il mapping tra documenti e
     * termini (anche la variabile che mantiene il numero di documenti)
     * @throws IOException
     * @throws NullPointerException
     */
    public void initDocumentsMapAndSpaceVectorDimensions(boolean random_test_mode, int mode) {

        try {
            logTitle("Inizio la costruzione dello space vector model");
            // conto totale per ogni documento
            HashMap<String, Integer> total_counts = new HashMap<String, Integer>();
            // conto totale per ogni documento usato per il training, developing e testing
            HashMap<String, Integer> percentage_training_counts = new HashMap<String, Integer>();
            HashMap<String, Integer> percentage_developing_counts = new HashMap<String, Integer>();
            HashMap<String, Integer> percentage_testing_counts = new HashMap<String, Integer>();
            Set<String> set_of_class = new HashSet<String>();
            // creo una lista di file presenti nella directory
            File[] allfiles = new File(documents_dir).listFiles();
            if(random_test_mode) shuffleArray(allfiles);
            // se riesco ad aprire la directory
            if (allfiles != null) {
                logSection("Numero di file presenti: " + allfiles.length);
                // per ogni file
                for (File f : allfiles) {
                    // se è un file di testo
                    if (f.getName().endsWith(".txt")) {
                        String klass_name = f.getName().split("_")[0];
                        // se il numero totale di file per quella data classe non è ancora stato inizializzato, init a 0
                        if (total_counts.get(klass_name) == null)
                            total_counts.put(klass_name, 0);
                        // incremento il numero di file totali classificati con quella classe
                        total_counts.put(klass_name, total_counts.get(klass_name) + 1);
                        // salvo il mapping nome documento => classe di appartenenza del documento
                        documents_classes.put(f.getName(), klass_name);
                        // aggiungo all'insieme delle classi la classe trovata (senza ripetizioni)
                        set_of_class.add(klass_name);
                    }
                }
                // per ogni classe, calcolo la percentuale di documenti di training, developing e testing da usare
                // in funzione del numero di documenti classificati con la classe in esame
                for (String klass : total_counts.keySet()) {
                    int temp_total_counts = total_counts.get(klass);
                    percentage_training_counts.put(klass, (int) Math.round(temp_total_counts * percentage_training_set));
                    percentage_developing_counts.put(klass, (int) Math.round(temp_total_counts * percentage_developing_set));
                    percentage_testing_counts.put(klass, (int) Math.round(temp_total_counts * percentage_testing_set));
                    HashMap<String, Integer> class_counting = new HashMap<String, Integer>();
                    class_counting.put("training", percentage_training_counts.get(klass));
                    class_counting.put("developing", percentage_developing_counts.get(klass));
                    class_counting.put("testing", percentage_testing_counts.get(klass));
                    classes_counting.put(klass, class_counting);
                    logSubSection("Classe: '" + klass + "', Totale documenti: " + temp_total_counts + ". Utilizzo training: " + percentage_training_counts.get(klass) +
                            " / developing: " + percentage_developing_counts.get(klass) +
                            " / testing: " + percentage_testing_counts.get(klass));

                }
                // inizializzo un insieme in cui accumulare i termini che descrivono le dimensioni dello spazio
                Set<String> set_of_term = new HashSet<String>();
                // per ogni file presente nella directory
                int number_of_file = 0;
                int total_training_number = 0;
                for (String kn : percentage_training_counts.keySet()) {
                    total_training_number += percentage_training_counts.get(kn);
                }
                double percent_x = 0.0;
                for (File f : allfiles) {
                    float percent = number_of_file * 100f / total_training_number;
                    if (Math.round(percent) != percent_x) {
                        logSubSection("Training al "+percent_x+"% ("+(number_of_file+1) + " di " + total_training_number+")");
                        flushLogger();
                        percent_x = Math.round(percent);
                    }
                    // se è un file di testo
                    if (f.getName().endsWith(".txt")) {
                        // isolo il nome del file e della classe di appartenenza
                        String document_file_name = f.getName();
                        String klass_name = f.getName().split("_")[0];
                        // se rispettivamente al numero di documenti per quella classe, il numero di documenti di
                        // training da usare in percentuale al totale non è stato raggiunto
                        if (percentage_training_counts.get(klass_name) > 0) {
                            number_of_file++;
                            logSubSubSection("Analizzo file " + number_of_file + " di " + total_training_number);
                            logSubSubParag("Aggiungo ai documenti di training: "+document_file_name);
                            training_documents.add(document_file_name);
                            // diminuisco il numero di documenti totali da usare come doc di training per quella classe
                            percentage_training_counts.put(klass_name, percentage_training_counts.get(klass_name) - 1);
                            // lemmatizzo il documento
                            ArrayList<String> lemmedTermOfDoc = new ArrayList<>();
                            if(mode > 0) {
                                lemmedTermOfDoc = createObjectDocument(document_file_name);
                            } else {
                                lemmedTermOfDoc = documents.get(document_file_name);
                            }
                            // aggiungo ogni termine trovato nel documento al set di termini che descrive lo spazio.
                            for (String term : lemmedTermOfDoc) {
                                set_of_term.add(term);
                            }
                        } else if (percentage_developing_counts.get(klass_name) > 0) {
                            logSubSubParag("Aggiungo ai documenti di developing: " + document_file_name);
                            developing_documents.add(document_file_name);
                            percentage_developing_counts.put(klass_name, percentage_developing_counts.get(klass_name) - 1);
                            if(mode > 0) {
                                createObjectDocument(document_file_name);
                            } else {
                                documents.get(document_file_name);
                            }
                        } else if (percentage_testing_counts.get(klass_name) > 0) {
                            logSubSubParag("Aggiungo ai documenti di testing: " + document_file_name);
                            testing_documents.add(document_file_name);
                            percentage_testing_counts.put(klass_name, percentage_testing_counts.get(klass_name) - 1);
                            if(mode > 0) {
                                createObjectDocument(document_file_name);
                            } else {
                                documents.get(document_file_name);
                            }
                        }
                    }
                }
                logParag(toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(training_documents, "; "));
                logParag(toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(developing_documents, "; "));
                logParag(toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(testing_documents, "; "));

                // trasformo il set in una lista ordinata
                ArrayList<String> list_of_terms_in_all_documents = new ArrayList<String>();
                for (String term : set_of_term) {
                    list_of_terms_in_all_documents.add(term);
                }

                java.util.Collections.sort(list_of_terms_in_all_documents);
                logSection("Inizializzo le dimensioni dello spazio: "+list_of_terms_in_all_documents.size());
                dimensions = list_of_terms_in_all_documents;
                // trasformo il set in una lista ordinata
                ArrayList<String> list_of_classes_of_all_documents = new ArrayList<String>();
                for (String term : set_of_class) {
                    list_of_classes_of_all_documents.add(term);
                }

                java.util.Collections.sort(list_of_classes_of_all_documents);
                logSection("Inizializzo le classi dello spazio: " + list_of_classes_of_all_documents.size());
                classes = list_of_classes_of_all_documents;
            } else {
                throw new NullPointerException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Questo metodo lemmatizza e rimuove le stopwords da un documento con contenuto in italiano
     * e restituisce una lista di stringhe delle parole del documento (ordinate lessicograficamente).
     * Del documento, pulito e lemmatizzato, viene mantenuta una lista di termini della proprietà
     * di classe documents
     * @param document_file_name : string del nome del file
     * @return array di stringhe del documento stemmatizzato
     */
    public ArrayList<String> createObjectDocument(String document_file_name) {

        ArrayList<String> lemmedTermOfDoc = new ArrayList<String>();

        try {
            logSubSubParag("Analizzo il documento: " + document_file_name);
            // System.out.println("Analizzo il documento: " + document_file_name);
            number_of_documents += 1;
            BufferedReader in = null;
            in = new BufferedReader(new FileReader(new File(documents_dir+File.separator+document_file_name)));
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            // pulisco la frase rimuovendo le stopwords
            ArrayList<String> str = cleanSentence(sb.toString());
            // calcolo i lemmi delle parola nella frase
            lemmedTermOfDoc = MMItalianUtil.italianSentenceLemmer(str);
            java.util.Collections.sort(lemmedTermOfDoc);
            documents.put(document_file_name, lemmedTermOfDoc);
            // per ogni lemma trovato lo inserisco nell'insieme di elementi
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lemmedTermOfDoc;
    }

    /**
     * Crea i centroidi per le classi del modello
     */
    public void computeClassCentroid() {

        int klass_number = 1;
        // prima versione di Rocchio: calcolo dei centroidi come media aritmetica
        if (version == 1) {
            for (String klass : classes) {
                logSubSection("Calcolo centroide per la classe '" + klass + "' (" + (klass_number++) + "/" + classes.size() + ")");
                // istanzio il centroide che poi aggiungerò ai centroidi di ogni classe
                HashMap<String, Double> klass_centroid = new HashMap<String, Double>();
                for (String dim : dimensions) {
                    klass_centroid.put(dim, 0.0);
                }
                for (String training_document_name : training_documents) {
                    // se è un documento di training sulla classe di cui sto calcolando il centroide
                    if (documents_classes.get(training_document_name).equalsIgnoreCase(klass)) {
                        for (String term_in_training_document : tf.get(training_document_name).keySet()) {
                            klass_centroid.put(term_in_training_document,
                                    klass_centroid.get(term_in_training_document) +
                                            tf.get(training_document_name).get(term_in_training_document) * idf.get(term_in_training_document));
                        }
                    }
                }
                for (String dim : klass_centroid.keySet()) {
                    klass_centroid.put(dim, klass_centroid.get(dim) / classes_counting.get(klass).get("training"));
                }
                classes_centroids.put(klass, klass_centroid);
            }
        }
        // seconda versione di rocchio: calcolo dei centroidi con negativi e positivi
        else if (version == 2) {
            for (String klass : classes) {
                logSubSection("Calcolo centroide per la classe '" + klass + "' (" + (klass_number++) + "/" + classes.size() + ")");
                // istanzio il centroide che poi aggiungerò ai centroidi di ogni classe
                HashMap<String, Double> klass_centroid_pos_part = new HashMap<String, Double>();
                HashMap<String, Double> klass_centroid_neg_part = new HashMap<String, Double>();
                for (String dim : dimensions) {
                    klass_centroid_pos_part.put(dim, 0.0);
                    klass_centroid_neg_part.put(dim, 0.0);
                }
                int pos_doc_for_klass_i = 0;
                int neg_doc_for_klass_i = 0;

                // calcolo somma dei positivi e dei negativi
                for (String training_document_name : training_documents) {
                    // se è un documento di training sulla classe di cui sto calcolando il centroide
                    if (documents_classes.get(training_document_name).equalsIgnoreCase(klass)) {
                        logSubSubParag("Esecuzione standard: pos trovato");
                        pos_doc_for_klass_i++;
                        for (String term_in_training_document : tf.get(training_document_name).keySet()) {
                            klass_centroid_pos_part.put(term_in_training_document,
                                    klass_centroid_pos_part.get(term_in_training_document) +
                                            tf.get(training_document_name).get(term_in_training_document) *
                                                    idf.get(term_in_training_document));
                        }
                    } else {
                        // SE NON E' definito un insieme di NEAR POSITIVE (modalità di esecuzione: versione 2)
                        // allora considero come negativi tutti gli elementi delle altre classi
                        if(!near_pos_defined) {
                            logSubSubParag("Esecuzione standard: neg trovato");
                            neg_doc_for_klass_i++;
                            for (String term_in_training_document : tf.get(training_document_name).keySet()) {
                                klass_centroid_neg_part.put(term_in_training_document,
                                        klass_centroid_neg_part.get(term_in_training_document) +
                                                tf.get(training_document_name).get(term_in_training_document) *
                                                        idf.get(term_in_training_document));
                            }
                        }
                        // SE E' DEFINITO UN INSIEME DI NEAR POSITIVE PER LA CLASSE IN ESAME guardo se la classe
                        // del documento che sto prendendo in esame è definita NEAR POSITIVE della classe di cui
                        // sto calcolando il centroide
                        else {
                            logSubSubParag("Esecuzione con i NEAR POSITIVE: sfrutto computazione precedente");
                            if(near_pos.get(klass).contains(documents_classes.get(training_document_name))) {
                                logSubSubParag("La classe " + documents_classes.get(training_document_name) + " E' una NEAR POSITIVE di " + klass);
                                neg_doc_for_klass_i++;
                                for (String term_in_training_document : tf.get(training_document_name).keySet()) {
                                    klass_centroid_neg_part.put(term_in_training_document,
                                            klass_centroid_neg_part.get(term_in_training_document) +
                                                    tf.get(training_document_name).get(term_in_training_document) *
                                                            idf.get(term_in_training_document));
                                }
                            } else {
                                logSubSubParag("La classe " + documents_classes.get(training_document_name) + " NON E' una NEAR POSITIVE di " + klass);
                                //logSubSubSection("La classe "+documents_classes.get(training_document_name)+" NON E' una NEAR POSITIVE di "+klass);
                            }
                        }
                    }
                }
                // moltiplico per beta e divido per il numero di positivi
                for (String dim : klass_centroid_pos_part.keySet()) {
                    klass_centroid_pos_part.put(dim, klass_centroid_pos_part.get(dim) * beta / pos_doc_for_klass_i);
                }
                // moltiplico per gamma e divido per il numero di negativi
                for (String dim : klass_centroid_neg_part.keySet()) {
                    klass_centroid_neg_part.put(dim, klass_centroid_neg_part.get(dim) * gamma / neg_doc_for_klass_i);
                }
                // calcolo la differenza tra i positivi medi per beta meno i negativi medi per gamma
                HashMap<String, Double> klass_centroid =
                        diffDoubleCentroidHashMapVector(klass_centroid_pos_part, klass_centroid_neg_part);
                //TODO ORIGINALE
//                // inserisco il centroide calcolato nella hashmap dei centroidi per le classi
//                classes_centroids.put(klass, klass_centroid);

                //TODO NUOVA MODIFICA
                // se c'è stata una variazione
                if(near_pos_defined && near_pos.get(klass).size() > 0) {
                    // inserisco il centroide calcolato nella hashmap dei centroidi per le classi
                    classes_centroids.put(klass, klass_centroid);
                } else {
                    if(!near_pos_defined) {
                        // inserisco il centroide calcolato nella hashmap dei centroidi per le classi
                        classes_centroids.put(klass, klass_centroid);
                    }
                }

            }
        }
        // terza versione di rocchio: calcolo dei centroidi con negativi e positivi
        else if (version == 3) {
            //TODO: calcolo attraverso i NEAR POSITIVE
            // Calcolo con versione 2: mando faccio testing sui developing, e implemento algoritmo
            // di scelta in base all'errore commesso per ogni classe dentro al developing
            version = 2;
            this.computeClassCentroid();
            logSection("Inizio il testing per il calcolo dei NEAR POSITIVE (distanzio centroidi più vicini)");
            near_pos_defined = true;
            this.testing();
            logSection("Ricalcolo i centroidi sfruttando i NEAR POSITIVE");
            version = 2;
            this.computeClassCentroid();
            near_pos_defined = false;
            version = 3;
        }
    }

}