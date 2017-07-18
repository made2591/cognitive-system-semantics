package uni.sc.radicioni.wordnet;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import rita.RiWordNet;
import uni.sc.radicioni.lib.Stemmer;
import uni.sc.util.MMConfig;
import uni.sc.util.tools.MMToolbox;
import java.io.*;
import java.util.*;
import static uni.sc.util.logger.MMLogger.*;
import static uni.sc.util.logger.MMLogger.logSubParag;


/**
 * Questa classe rappresenta il core dell'esercizio 1. Utilizza la classe di configurazione uni.sc.util.MMConfig
 * e usa la libreria RiTa per collegarsi a WordNet. Utilizza lo stemmer contenuto nel package
 * lib (file "Stemmer.java") e implementa la versione di base dell'algoritmo Lesk.
 *
 * Created by Matteo on 13/04/15.
 */
public class MMCoreEsercitazioneUno {

    // riferimento a rita_word_net
    public static RiWordNet rita_word_net;
    // package utility
    public static MMToolbox toolsbox;
    // versione di stemmer esterno
    public static int stemmer = 3;
    // configurazione
    public static MMConfig config;
    // file da utilizzare per rimuovere le stopwords
    public static int stopwords_file = 1;
    // WordNetDatabase cui fa riferimento lo standford Parser
    public static WordNetDatabase database;
    // Stanford Parser
    public static MMStanfordUtil stanfordutil;

    // per creare il contesto una sola volta senza dover ogni volta stemmatizzare
    private static ArrayList<String> context = new ArrayList<String>();

    public MMCoreEsercitazioneUno(int stemmer_type, int swf) {
        config = new MMConfig();
        database = WordNetDatabase.getFileInstance();
        rita_word_net = new RiWordNet(config.getWORDNET_PATH());
        toolsbox = new MMToolbox();
        stemmer = stemmer_type;
        stopwords_file = swf;
        stanfordutil = new MMStanfordUtil();
        System.clearProperty("wordnet.database.dir");
        System.setProperty("wordnet.database.dir", config.getWORDNET_DICT());
    }

    /**
     * Wrapper per il due esercizi: esegue lesk tramite lemming o stemming
     * @param w : Stringa, Termine da disambiguare
     * @param s : Stringa, Sentence in cui è presente il termine
     * @return HashMap, Termine => senso migliore trovato per il termine
     */
    public HashMap<String, String> simplifiedLesk(String w, String s) {
        if(stemmer == 1 || stemmer == 2) {
            return simplifiedLeskUsingStemming(w, s);
        } else if(stemmer == 3) {
            return simplifiedLeskUsingLemming(w, s);
        }
        return null;
    }

    /**
     * Implementazione dell'algoritmo di Lesk semplificato sfruttando la stemmatizzazione
     * @param w : Stringa, Termine da disambiguare
     * @param s : Stringa, Sentence in cui è presente il termine
     * @return HashMap, Termine => senso migliore trovato per il termine
     */
    public HashMap<String, String> simplifiedLeskUsingStemming(String w, String s) {

        logTitle("Sentence in ingresso: '" + s + "'");

        // pulisco la sentence in ingresso chiamando i
        ArrayList<String> sentence_cleaned = this.cleanSentence(s);

        s = toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(sentence_cleaned, " ");

        // booleano per indicare se voglio i sensi di tutti i termini
        boolean single_word = true;

        // variabili di ciclo
        int best_sense_id = -1;
        String best_sense = "SENSE_NOT_FOUND";

        // HashMap con i sensi migliori per il termine / i termini
        HashMap<String, String> best_senses = new HashMap<String, String>();

        // se la prima termine è null o una stringa vuota, allora disambiguo tutti i termini
        if(w == null || w.trim().length() == 0) {
            // memorizzo che i termini da disambiguare sono tutti i termini della frase
            single_word = false;
        } else {
            // recupero il best sense del termine come il primo di tutto l'elenco
            // (SOLO TEMPORANEO => la chiamata alla procedura interna di Lesk penserà a trovare il migliore)
            best_sense_id = this.getAllSensesOfWord(w)[0];
            best_sense = "SENSE_NOT_FOUND";
            // se ho trovato almeno un senso
            if(best_sense_id != -1) {
                // recupero la glossa per il best_sense id per il termine
                best_sense = rita_word_net.getGloss(best_sense_id);
            }
            best_senses = new HashMap<String, String>();
        }

        // costruisco l'array di termini per la sentence passata come parametro
        ArrayList<String> sentence = toolsbox.splitSentenceAsArrayListOfString(s);

        // variabili dummy
        int index_of_actual_analized_word = 1;
        int number_of_words_in_sentence = sentence.size();

        if(single_word) {
            // salvo nella stringa il best_sense per quel termine
            logSection("Eseguo Lesk sul termine '" + w + "' della sentence: '" + s + "'");
            // chiamo la procedura effettiva che esegue Lesk, simplifiedLeskUsingStemmingInternal
            best_sense_id = simplifiedLeskUsingStemmingInternal(w, sentence);
            if (best_sense_id != -1)
                best_sense = rita_word_net.getGloss(best_sense_id);
            best_senses.put(w, best_sense);

        } else {
            logSection("Eseguo Lesk su tutti i termini della sentence (ancora da stemmatizzare): '" + s + "'");
            // per ogni termine della sentence
            for(String word : sentence) {

                logSection("Analizzo il termine '" + word + "' - " +
                        index_of_actual_analized_word + " di " + number_of_words_in_sentence);

                // salvo nella stringa (la uso come temporaneo) il best_sense per il termine
                best_sense_id = simplifiedLeskUsingStemmingInternal(word, sentence);
                // recupero la definizione del best_sense trovato per il termine disambiguato
                if(best_sense_id != -1)
                    best_sense = rita_word_net.getGloss(best_sense_id);
                best_senses.put(word, best_sense);

                index_of_actual_analized_word += 1;
            }
        }

        // svuoto il contesto
        context = new ArrayList<String>();
        logTitle("Termino la chiamata di LESK");

        // restituisco l'HashMap di sensi trovata
        return best_senses;

    }

    /**
     * Esegue l'algoritmo di Lesk con lemming vero e proprio: procedura interna che lavora su un termine e una frase
     * @param word : Stringa, Termine
     * @param sentence : ArrayList<String>, Sentence da cui è estratto il termine
     * @return int : Best sense-id per il termine
     */
    private int simplifiedLeskUsingStemmingInternal(String word, ArrayList<String> sentence) {

        // se non è ancora stato fatto, stemmatizzo la sentence per creare il contesto
        if(context.size() == 0) {
            if(stemmer == 1) logSubSection("Creo il contesto a partire dalla sentence, sfruttando lo stemmer RiTa (opzione: 1)");
            else logSubSection("Creo il contesto a partire dalla sentence, sfruttando lo stemmer esterno (opzione: 2)");
            for(String w : sentence) {
                if(stemmer == 1) {
                    context.add(this.getStemma_Rita_Based(w));
                } else if(stemmer == 2) {
                    context.add(this.getStemma_ExternalStemmer(w));
                }
            }
        }
        logSubSection("Contesto creato: " + toolsbox.arrayOfWordsToStringSeparatedBySemiColumn(context) + "\n");

        // inizializzo il massimo overlap
        int max_overlap = -1;
        // inizializzo il best_sense
        int best_sense_id = -1;
        // carico tutti i possibili sensi del termine
        int[] all_word_senses = this.getAllSensesOfWord(word);
        logSubSection("Analizzo ogni senso del termine");

        // variabili dummy
        int index_of_actual_analized_sense = 1;
        int number_of_sense_for_word = all_word_senses.length;

        // per ogni senso del termine
        for(int sense_id : all_word_senses) {
            // se il sense_id esiste
            if(sense_id != -1) {

                logSubSubSection("Analizzo il senso '" + sense_id + "' (" +
                        index_of_actual_analized_sense + " di " + number_of_sense_for_word + ")");

                // trovo la glossa e la pulisco e concateno i suoi termini in unico array
                String sense_gloss_sentence = rita_word_net.getGloss(sense_id);
                ArrayList<String> sense_gloss = this.cleanSentence(sense_gloss_sentence);

                // trovo gli esempi e li pulisco e li concateno in unico array di termini (tutti i termini di ogni esempio ripulito)
                ArrayList<String> sense_examples = toolsbox.castPrimitiveArrayToArrayList(rita_word_net.getExamples(sense_id));
                ArrayList<String> sense_examples_cleaned = new ArrayList<String>();
                for (String example : sense_examples) {
                    sense_examples_cleaned.addAll(this.cleanSentence(example));
                }
                sense_examples = sense_examples_cleaned;

                // stemmatizzo ogni termine nella glossa pulita e rimpiazzo l'array dei termini con l'array degli stemmi
                ArrayList<String> sense_gloss_stemmed = new ArrayList<String>();
                for (String gloss_word : sense_gloss) {
                    if(!gloss_word.equalsIgnoreCase(word) &&
                       !gloss_word.equalsIgnoreCase(this.getStemmaBasedOnStemmerValueForWord(word))) {
                        // logSubSubParag("Termine di glossa da tenere: " + gloss_word);
                        // calcolo l'overlap tra la signature del senso e il contesto
                        // per overlap = 0, il primo overlap viene mantenuto (il confronto è che sia strettamente maggiore)
                        // inoltre nel calcolo dell'overlap non considero il termine di cui sto valutando l'ambiguità
                        // semantica: in assenza di informazione ulteriore, non ha senso farlo. Infatti, questa potrebbe comparire più volte
                        // all'interno di esempi di un senso che non è quello che vogliamo valutare. E' ragionevole, quindi quando la incontro
                        // non la stemmatizzo (e di conseguenza non sarà presente nella signature).
                        sense_gloss_stemmed.add(this.getStemmaBasedOnStemmerValueForWord(gloss_word));
                    } else {
                        //logSubSubParag("Termine di glossa da escludere: "+gloss_word);
                    }
                }
                sense_gloss = sense_gloss_stemmed;

                // stemmatizzo ogni termine nell'array di termini (tutti i termini presenti in ogni esempio) e
                // rimpiazzo l'array dei termini degli esempi con l'array degli stemmi dei termini degli esempi
                ArrayList<String> sense_example_stemmed = new ArrayList<String>();
                for (String example_word : sense_examples) {
                    if(!example_word.equalsIgnoreCase(word) &&
                       !example_word.equalsIgnoreCase(this.getStemmaBasedOnStemmerValueForWord(word))) {
                        // logSubSubParag("Termine d'esempio da tenere: "+example_word);
                        // idem come sopra
                        sense_example_stemmed.add(this.getStemmaBasedOnStemmerValueForWord(example_word));
                    } else {
                        //logSubSubParag("Termine d'esempio da escludere: "+example_word);
                    }
                }
                sense_examples = sense_example_stemmed;

                // creo la signature come concatenazione dei termini presenti negli esempi e glossa ripuliti e stemmatizzati
                ArrayList<String> signature_of_sense = new ArrayList<String>();
                signature_of_sense.addAll(sense_gloss);
                signature_of_sense.addAll(sense_examples);

                // calcolo l'overlap tra la signature e il contesto
                int actual_sense_overlap = computeOverlap(signature_of_sense, context);

                logSubSubSection("Overlap trovato per la signature del senso: " + actual_sense_overlap);

                // se è maggiore del maggiore trovato fin'ora
                if (actual_sense_overlap > max_overlap) {
                    logSubSubSection("Senso migliore trovato: vecchio overlap = " + max_overlap + ", nuovo = " + actual_sense_overlap);
                    // salvo il massimo overlap e il senso associato
                    max_overlap = actual_sense_overlap;
                    best_sense_id = sense_id;
                }

            } else {
                logSubSection("Nessun senso (" + sense_id + " found)");
            }

            index_of_actual_analized_sense += 1;
        }

        logSubSection("Trovato senso migliore: '" + best_sense_id + "'");

        // restituisco il senso con overlap maggiore
        return best_sense_id;

    }

    /**
     * Implementazione dell'algoritmo di Lesk semplificato sfruttando la lemmatizzazione
     * @param w : Stringa, Termine da disambiguare
     * @param s : Stringa, Sentence in cui è presente il termine
     * @return HashMap, Termine => senso migliore trovato per il termine
     */
    public HashMap<String, String> simplifiedLeskUsingLemming(String w, String s) {

        logTitle("Sentence in ingresso: '" + s + "'");

        ArrayList<String> sentence_cleaned = this.cleanSentence(s);

        // pulisco la stringa da punteggiatura, articoli, spazi di troppo, stopwords
        // dalla posizione 1 in avanti ci sono le stop_words rimosse
        s = toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(sentence_cleaned, " ");

        // booleano per capire se voglio i sensi di tutti i termini
        boolean single_word = true;

        //int best_sense_id = -1;
        String best_sense;
        HashMap<String, String> best_senses = new HashMap<String, String>();

        // se il primo termine è null o una stringa vuota, allora disambiguo tutti i termini
        if(w == null || w.trim().length() == 0) {
            logTitle("Chiamo LESK su tutti i termini della sentence (ancora da stemmatizzare): '" + s + "'");
            single_word = false;
        } else {
            best_sense = database.getSynsets(w)[0].getDefinition();
            best_senses = new HashMap<String, String>();
            logTitle("Chiamo LESK sul termine '" + w + "' della sentence: '" + s + "'");
        }

        // costruisco l'array di termini per la sentence passata come parametro
        ArrayList<String> sentence = toolsbox.splitSentenceAsArrayListOfString(s);

        int index_of_actual_analized_word = 1;
        int number_of_words_in_sentence = sentence.size();

        if(single_word) {
            // salvo nella stringa il best_sense per quel termine
            best_sense = simplifiedLeskUsingLemmingInternal(w, sentence);
            best_senses.put(w, best_sense);

        } else {

            // per ogni termine della sentence
            for(String word : sentence) {

                logSection("Analizzo il termine '" + word + "' - " +
                        index_of_actual_analized_word + " di " + number_of_words_in_sentence);

                // salvo nella stringa (la uso come temporaneo) il best_sense per il termine
                best_sense = simplifiedLeskUsingLemmingInternal(word, sentence);
                best_senses.put(word, best_sense);

                index_of_actual_analized_word += 1;

            }

        }

        // svuoto il contesto
        context = new ArrayList<String>();
        logTitle("Termino la chiamata di LESK");

        return best_senses;

    }

    /**
     * Esegue l'algoritmo di Lesk con stemming vero e proprio: procedura interna che lavora su un termine e una frase
     * @param word : Stringa, Termine
     * @param sentence : ArrayList<String>, Sentence da cui è estratto il termine
     * @return int : Best sense-id per il termine
     */
    private String simplifiedLeskUsingLemmingInternal(String word, ArrayList<String> sentence) {

        // se non è ancora stato fatto, lemmatizzo la sentence per creare il contesto
        if(context.size() == 0) {
            logSection("Creo il contesto a partire dalla sentence");
            for(String w : sentence) {
                context.add(stanfordutil.getLemmaOfWord(w));
            }
        }
        logSection("Creato il contesto: " + toolsbox.arrayOfWordsToStringSeparatedBySemiColumn(context) + "\n");

        // inizializzo il massimo overlap
        int max_overlap = -1;

        String best_sense = "NO_SENSE";

        // carico tutti i possibili sensi del termine
        Synset[] all_word_senses = database.getSynsets(word);

        logSection("Analizzo ogni senso del termine");
        // per ogni senso del termine

        int index_of_actual_analized_sense = 1;
        int number_of_sense_for_word = all_word_senses.length;

        for(Synset sense_id : all_word_senses) {

            if(sense_id != null) {

                logSubSection("Analizzo il senso '" + sense_id + "' - " +
                        index_of_actual_analized_sense + " di " + number_of_sense_for_word);

                // trovo la glossa e la pulisco e concateno i suoi termini in unico array
                String sense_gloss_sentence = sense_id.getDefinition();
                ArrayList<String> sense_gloss = this.cleanSentence(sense_gloss_sentence);

                // trovo gli esempi e li pulisco e li concateno in unico array di termini (tutti i termini di ogni esempio ripulito)
                ArrayList<String> sense_examples = toolsbox.castPrimitiveArrayToArrayList(sense_id.getUsageExamples());
                ArrayList<String> sense_examples_cleaned = new ArrayList<String>();
                for (String example : sense_examples) {
                    sense_examples_cleaned.addAll(this.cleanSentence(sense_gloss_sentence));
                }
                sense_examples = sense_examples_cleaned;

                // stemmatizzo ogni termine nella glossa pulita e rimpiazzo l'array dei termini con l'array degli stemmi
                ArrayList<String> sense_gloss_stemmed = new ArrayList<String>();
                for (String gloss_word : sense_gloss) {
                    if(!gloss_word.equalsIgnoreCase(word) &&
                            !gloss_word.equalsIgnoreCase(stanfordutil.getLemmaOfWord(word))) {
                        logSubSubParag("Termine di glossa da tenere: " + gloss_word);
                        // calcolo l'overlap tra la signature del senso e il contesto
                        // per overlap = 0, il primo overlap viene mantenuto (il confronto è che sia strettamente maggiore)
                        // inoltre nel calcolo dell'overlap non considero il termine di cui sto valutando l'ambiguità
                        // semantica: in assenza di informazione ulteriore, non ha senso farlo. Infatti, questa potrebbe comparire più volte
                        // all'interno di esempi di un senso che non è quello che vogliamo valutare. E' ragionevole, quindi quando la incontro
                        // non la stemmatizzo (e di conseguenza non sarà presente nella signature
                        sense_gloss_stemmed.add(stanfordutil.getLemmaOfWord(gloss_word));
                        logSubSubParag("Lemma per il termine di glossa da tenere: " + stanfordutil.getLemmaOfWord(word));
                    } else {
                        logSubSubParag("Termine di glossa da escludere: "+gloss_word);
                        logSubSubParag("Lemma per il termine di glossa da escludere: "+stanfordutil.getLemmaOfWord(word));
                    }
                }
                sense_gloss = sense_gloss_stemmed;

                // stemmatizzo ogni termine nell'array di termini (tutti i termini presenti in ogni esempio) e
                // rimpiazzo l'array dei termini degli esempi con l'array degli stemmi dei termini degli esempi
                ArrayList<String> sense_example_stemmed = new ArrayList<String>();
                for (String example_word : sense_examples) {
                    if(!example_word.equalsIgnoreCase(word) &&
                            !example_word.equalsIgnoreCase(stanfordutil.getLemmaOfWord(word))) {
                        logSubSubParag("Termine d'esempio da tenere: "+example_word);
                        logSubSubParag("Lemma per il termine d'esempio da tenere: "+stanfordutil.getLemmaOfWord(word));
                        // idem come sopra
                        sense_example_stemmed.add(stanfordutil.getLemmaOfWord(example_word));
                    } else {
                        logSubSubParag("Termine d'esempio da escludere: "+example_word);
                        logSubSubParag("Lemma per il termine d'esempio da escludere: "+stanfordutil.getLemmaOfWord(word));
                    }
                }
                sense_examples = sense_example_stemmed;

                // creo la signature come concatenazione dei termini presenti negli esempi e glossa ripuliti e stemmatizzati
                ArrayList<String> signature_of_sense = new ArrayList<String>();
                signature_of_sense.addAll(sense_gloss);
                signature_of_sense.addAll(sense_examples);

                int actual_sense_overlap = computeOverlap(signature_of_sense, context);

                logSubSection("Overlap trovato per la signature del senso: " + actual_sense_overlap);

                // se è maggiore del maggiore trovato fin'ora
                if (actual_sense_overlap > max_overlap) {
                    logSubSubSection("Senso migliore trovato: vecchio overlap = " + max_overlap + ", nuovo = " + actual_sense_overlap);
                    // mi salvo il massimo overlap e il senso associato
                    max_overlap = actual_sense_overlap;
                    best_sense = sense_id.getDefinition();
                }

            } else {
                logSubSection("Nessun senso (" + sense_id + " found)");
            }

            index_of_actual_analized_sense += 1;
        }

        logSection("Trovato senso migliore: '" + best_sense + "'");

        // restituisco il senso con overlap maggiore
        return best_sense;

    }

    /**
     * Recupera il best PoS Tag per un dato termine
     * @param word : Stringa, Termine richiesto
     * @return pos : String, PoS migliore del termine (default: noun)
     */
    public String getBestPosOrNoun(String word) {

        logSubSection("Cerco il migliore POS TAG per il termine '" + word + "'");
        // chiedo a rita di recupera il best PoS per la word
        String pos = rita_word_net.getBestPos(word);
        // se non lo trovo assegno noun come default
        if (pos == null) pos = "n";
        return pos;

    }

    /**
     * Recupera tutti i sensi di un termine e restituisce un'array di interi sense-id di wn
     * @param word : String, Termine di cui trovare i sensi
     * @return int[] : sensi di wn
     */
    public int[] getAllSensesOfWord(String word) {

        logSection("Recupero tutti i sensi per il termine '" + word + "'");
        // chiedo a rita di recuerare tutti i sense-ids del termine passato come parametro
        // in accordo al best PoS assegnatoli dalla chiamata getBestPosOrNoun
        int[] sense_ids = rita_word_net.getSenseIds(word, this.getBestPosOrNoun(word));

        // variabili dummy
        int[] def = {-1};
        String stem = word;

        // se non sono trovati sensi per il termine in esame
        if(sense_ids.length == 0) {
            logSubParag("Non ho trovato sensi per il termine '" + word + "': cerco un senso per lo stemma '" + stem + "'");
            stem = this.getStemmaBasedOnStemmerValueForWord(word);
            sense_ids = rita_word_net.getSenseIds(stem, this.getBestPosOrNoun(word));
        }

        if (sense_ids.length == 0) {
            logSubParag("Non ho trovato sensi per lo stemma '" + stem + "'");
            return def;
        }

        return sense_ids;

    }

    /**
     * Recupera lo stemma basandosi su RiTa o lo stemmer esterno a seconda del valore del marker stemmer
     * @param word : String, Termine di cui trovare lo stemma
     * @return String : stemma del termine
     */
    public String getStemmaBasedOnStemmerValueForWord(String word) {

        logSubParag("Cerco il migliore POS TAG per il termine '" + word + "'");
        if(stemmer == 1) {
            logSubParag("Cerco lo stemma per il termine '" + word + "' usando RiTa");
            return getStemma_Rita_Based(word);
        } else if(stemmer == 2) {
            logSubParag("Cerco lo stemma per il termine '" + word + "' usando lo stemmer esterno");
            return getStemma_ExternalStemmer(word);
        }

        // NON CI DOVREBBE MAI ANDARE: marker solo per testing durante sviluppo
        logTitle("Errore di configurazione dello stemmer: valore " + stemmer + " non permesso!");
        return "STEMMER_CONFIGURATION_INVALID";
    }

    /**
     * Recupera lo stemma basandosi su RiTa
     * @param word : String, Termine di cui trovare lo stemma
     * @return String : stemma del termine
     */
    public String getStemma_Rita_Based(String word) {

        if(!(word.length() == 0 || word.trim().isEmpty() || word.replace(" ", "").isEmpty())) {
            String[] uno = rita_word_net.getStems(word, this.getBestPosOrNoun(word));
            int min = 30000;
            String min_stem = "";
            for (String e : uno) {
                if (e.length() < min) {
                    min = e.length();
                    min_stem = e;
                }
            }
            logSubSubParag("Stemma non trovato per '"+word+"': uso il termine intera");
            if (uno.length == 0) min_stem = word;
            return min_stem;
        }

        // NON CI DOVREBBE MAI ANDARE: marker solo per testing durante sviluppo
        logTitle("Errore nello stemmer RiTA");
        return "WORD_EMPTY";

    }

    /**
     * Recupera lo stemma basandosi su stemmer esterno
     * @param word : String, Termine di cui trovare lo stemma
     * @return String : stemma del termine
     */
    public String getStemma_ExternalStemmer(String word) {

        if (!(word.length() == 0 || word.trim().isEmpty() || word.replace(" ", "").isEmpty())) {
            return Stemmer.stemMyWord(word);
        }

        // NON CI DOVREBBE MAI ANDARE: marker solo per testing durante sviluppo
        logTitle("Errore nello stemmer esterno");
        return "WORD_EMPTY";

    }

    /**
     * Questo metodo pulisce una stringa facendo utilizzo di uno stopwords file
     * @param s : Stringa, Sentence da cui rimuovere le stopwords
     * @return ArrayList<String> infos : sentence
     */
    public ArrayList<String> cleanSentence(String s) {

        String slog = s;
        if(slog.length() >= 30) { slog = s.substring(0, 30); }
        logSection("Pulisco la stringa '" + slog + "..." + "' tramite espressione regolare");
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
                    while ((line = br.readLine()) != null) {
                        for(String w : s_array) {
                            if(w.equalsIgnoreCase(line)) {
                                if(line.trim().length() != 0) stop_words_removed += line + "; ";
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

    /**
     * Calcola l'overlap tra una signature e un contesto
     * @param signature : ArrayList<String> signature
     * @param context : ArrayList<String> context
     * @return overlap : int che rappresenta il livello di overlap
     */
    public int computeOverlap(ArrayList<String> signature, ArrayList<String> context) {

        int overlap = 0;

        for(String word_in : signature) {
            // il termine stessa non la considero perché tanto è ambigua, non ha senso tenerla da conto
            // ad esempio ash compare in un esempio che fa si che l'overlap vada a uno
            for (String word_context : context) {
                if (word_in.equalsIgnoreCase(word_context)) {
                    overlap += 1;
                }
            }
        }

        logSubParag("Calcolo overlap per la SIGNATURE:\n\t"+
                toolsbox.arrayOfWordsToStringSeparatedBySemiColumn(signature)+
                "\ncon il CONTESTO:\n\t"+
                toolsbox.arrayOfWordsToStringSeparatedBySemiColumn(context)+"\n\nRISULTATO: "+overlap);

        // restituisco il valore ottenuto
        return overlap;

    }

}