package uni.sc.radicioni.babelnet;

import uni.sc.util.MMConfig;
import uni.sc.util.tools.MMItalianUtil;
import uni.sc.util.tools.MMToolbox;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import static uni.sc.util.logger.MMLogger.*;
import static uni.sc.util.tools.MMItalianUtil.cleanSentence;

/**
 * Questa classe rappresenta il core dell'esercizio 2.
 * Implementa la versione estesa di Lesk che fa utilizzo di BabelNet
 * per disambiguare. Fa riferimento alle classi MMBabelNet e di utilità
 * presenti anche nell'esercitazione 1.
 *
 * Created by Matteo on 13/04/15.
 */
public class MMCoreEsercitazioneDue {

    // babelnet connection
    public static MMBabelNet database;
    // package utility
    public static MMToolbox toolsbox;
    // utilità per l'italiano
    public static MMItalianUtil itautil;
    // configurazioni
    public static MMConfig config;
    // relazioni da considerare
    public static ArrayList<String> relations_wanted = new ArrayList<String>();

    // per creare il contesto una sola volta senza dover ogni volta stemmatizzare
    private static ArrayList<String> context = new ArrayList<String>();

    public MMCoreEsercitazioneDue(ArrayList<String> relations) {
        config = new MMConfig();
        database = new MMBabelNet();
        toolsbox = new MMToolbox();
        itautil = new MMItalianUtil();
        relations_wanted = new ArrayList<String>();
        for(String r : relations) {
            relations_wanted.add(r);
        }
    }

    /**
     * Implementazione dell'algoritmo di Lesk esteso sfruttando glosse e esempi
     * @param w : Stringa, Termine da disambiguare
     * @param s : Stringa, Sentence in cui è presente il termine
     * @return HashMap, Termine => senso migliore trovato per il termine
     */
    public HashMap<String, String> extendedLeskWithGloss(String w, String s) {

        logTitle("Sentence in ingresso: '" + s + "'");

        ArrayList<String> sentence_cleaned = cleanSentence(s);

        s = toolsbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(sentence_cleaned, " ");

        // booleano per capire se voglio i sensi di tutte le parole
        boolean single_word = true;

        //int best_sense_id = -1;
        String best_sense = "NO_SENSE_FOUND";
        HashMap<String, String> best_senses = new HashMap<String, String>();

        // se il primo termine è null o una stringa vuota, allora disambiguo tutte le parole
        if(w == null || w.trim().length() == 0) {
            logTitle("Eseguo Lesk Extended su tutti i termini della sentence (ancora da stemmatizzare): '" + s + "'");
            single_word = false;
        } else {
            try {
                // recupero la definizione migliore da babelnet (la prima possibile glossa)
                ArrayList<String> temp = MMBabelNet.getBabelSynset(w, null, null);
                // se la trovo setto un default
                if(temp.size() > 0) {
                    ArrayList<String> temp_def = MMBabelNet.getBabelSynsetDefinition(temp.get(0), "IT");
                    if(temp_def.size() > 0) {
                        best_sense = temp_def.get(0);
                    }
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            best_senses = new HashMap<String, String>();
            logTitle("Eseguo Lesk Extended sul termine '" + w + "' della sentence: '" + s + "'");
        }

        // costruisco l'array di termini per la sentence passata come parametro
        ArrayList<String> sentence = toolsbox.splitSentenceAsArrayListOfString(s);

        int index_of_actual_analized_word = 1;
        int number_of_words_in_sentence = sentence.size();

        if(single_word) {
            // salvo nella stringa il best_sense per quella termine
            best_sense = extendedLeskWithGlossInternal(w, sentence);
            best_senses.put(w, best_sense);

        } else {

            // per ogni termine della sentence
            for(String word : sentence) {

                logSection("Analizzo il termine '" + word + "' - " +
                    index_of_actual_analized_word + " di " + number_of_words_in_sentence);

                // salvo nella stringa (la uso come temporaneo) il best_sense per la termine
                best_sense = extendedLeskWithGlossInternal(word, sentence);
                best_senses.put(word, best_sense);

                index_of_actual_analized_word += 1;

            }

        }

        // svuoto il contesto
        context = new ArrayList<String>();
        logTitle("Termino la chiamata di LESK ESTESO");

        return best_senses;

    }

    /**
     * Esegue l'algoritmo di Lesk Extended vero e proprio: procedura interna che lavora su un termine e una frase
     * @param word : Stringa, Termine
     * @param sentence : ArrayList<String>, Sentence da cui è estratto il termine
     * @return int : Best sense-id per il termine
     */
    private String extendedLeskWithGlossInternal(String word, ArrayList<String> sentence) {

        // se non è ancora stato fatto, lemmatizzo la sentence per creare il contesto
        if(context.size() == 0) {
            logSection("Creo il contesto a partire dalla sentence");
            for(String w : sentence) {
                context.add(MMItalianUtil.italianLemmer(w));
            }
        }
        logSection("Creato il contesto: " + toolsbox.arrayOfWordsToStringSeparatedBySemiColumn(context));

        // inizializzo il massimo overlap
        int max_overlap = -1;

        String best_sense = "NO_SENSE";

        // carico tutti i possibili sensi della termine
        ArrayList<String> all_word_senses = new ArrayList<String>();
        try {
            all_word_senses = MMBabelNet.getBabelSynset(word, null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        logSection("Analizzo ogni senso del termine "+word);
        // per ogni senso della termine

        int index_of_actual_analized_sense = 1;
        int number_of_sense_for_word = all_word_senses.size();

        for(String sense_id : all_word_senses) {

            if(sense_id != null) {

                logSubSection("Analizzo il senso '" + sense_id + "' - " +
                        index_of_actual_analized_sense + " di " + number_of_sense_for_word);

                // trovo la glossa e la pulisco e concateno le sue termini in unico array
                String sense_gloss_sentence = "NO_SENSE_FOUND";
                try {
                    ArrayList<String> temp = MMBabelNet.getBabelSynsetDefinition(sense_id, "IT");
                    if(temp.size() > 0) {
                        sense_gloss_sentence = temp.get(0);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                ArrayList<String> sense_gloss = cleanSentence(sense_gloss_sentence);

                // trovo gli esempi e li pulisco e li concateno in unico array di termini (tutti i termini di ogni esempio ripulito)
                ArrayList<String> sense_examples = new ArrayList<String>();
                try {
                    ArrayList<String> temp = MMBabelNet.getBabelSynsetDefinition(sense_id, "IT");
                    if(temp.size() > 0) {
                        temp.remove(0);
                        sense_examples = temp;
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                ArrayList<String> sense_examples_cleaned = new ArrayList<String>();
                for (String example : sense_examples) {
                    sense_examples_cleaned.addAll(cleanSentence(example));
                }
                sense_examples = sense_examples_cleaned;

                ArrayList<String> sense_extended = null;
                ///////////////////////////////////////////////////////////////////////////////////////////////////
                // PASSO AGGIUNGO PER IL LESK ESTESO: PER OGNI POSSIBILE SENSO CORRELATO CARICO GLOSSE E ESEMPI ///
                ArrayList<String> sense_gloss_examples_extended = extendWithStemmedGlossAndRelatedExample(sense_id);
                ///////////////////////////////////////////////////////////////////////////////////////////////////

                sense_extended = sense_gloss_examples_extended;

                // stemmatizzo ogni termine nella glossa pulita e rimpiazzo l'array dei termini con l'array degli stemmi
                ArrayList<String> sense_gloss_stemmed = new ArrayList<String>();
                for (String gloss_word : sense_gloss) {
                    if(!gloss_word.equalsIgnoreCase(word) &&
                            !gloss_word.equalsIgnoreCase(MMItalianUtil.italianLemmer(word))) {
                        logSubSubParag("Termine di glossa da tenere: " + gloss_word);
                        // calcolo l'overlap tra la signature del senso e il contesto
                        // per overlap = 0, il primo overlap viene mantenuto (il confronto è che sia strettamente maggiore)
                        // inoltre nel calcolo dell'overlap non considero il termine di cui sto valutando l'ambiguità
                        // semantica: in assenza di informazione ulteriore, non ha senso farlo. Infatti, questa potrebbe comparire più volte
                        // all'interno di esempi di un senso che non è quello che vogliamo valutare. E' ragionevole, quindi quando la incontro
                        // non la stemmatizzo (e di conseguenza non sarà presente nella signature
                        sense_gloss_stemmed.add(MMItalianUtil.italianLemmer(gloss_word));
                        logSubSubParag("Lemma per il termine di glossa da tenere: " + MMItalianUtil.italianLemmer(word));
                    } else {
                        logSubSubParag("Termine di glossa da escludere: "+gloss_word);
                        logSubSubParag("Lemma per il termine di glossa da escludere: "+ MMItalianUtil.italianLemmer(word));
                    }
                }
                sense_gloss = sense_gloss_stemmed;

                // stemmatizzo ogni termine nell'array di termini (tutti i termini presenti in ogni esempio) e
                // rimpiazzo l'array dei termini degli esempi con l'array degli stemmi dei termini degli esempi
                ArrayList<String> sense_example_stemmed = new ArrayList<String>();
                for (String example_word : sense_examples) {
                    if(!example_word.equalsIgnoreCase(word) &&
                            !example_word.equalsIgnoreCase(MMItalianUtil.italianLemmer(word))) {
                        logSubSubParag("Termine d'esempio da tenere: "+example_word);
                        logSubSubParag("Lemma per il termine d'esempio da tenere: "+ MMItalianUtil.italianLemmer(word));
                        // idem come sopra
                        sense_example_stemmed.add(MMItalianUtil.italianLemmer(example_word));
                    } else {
                        logSubSubParag("Termine d'esempio da escludere: "+example_word);
                        logSubSubParag("Lemma per il termine d'esempio da escludere: "+ MMItalianUtil.italianLemmer(word));
                    }
                }
                sense_examples = sense_example_stemmed;

                // stemmatizzo ogni termine nell'array di termini (tutti i termini presenti in ogni glossa e esempio di
                // ogni senso correlato) e rimpiazzo l'array dei termini degli esempi con l'array degli stemmi delle
                // termini degli esempi
                ArrayList<String> sense_extended_stemmed = new ArrayList<String>();
                for (String example_word : sense_extended) {
                    if(!example_word.equalsIgnoreCase(word) &&
                            !example_word.equalsIgnoreCase(MMItalianUtil.italianLemmer(word))) {
                        logSubSubParag("Termine di glossa o esempio di senso correlato da tenere: "+example_word);
                        logSubSubParag("Lemma per il termine di glossa o esempio di senso correlato da tenere: "+ MMItalianUtil.italianLemmer(word));
                        // idem come sopra
                        sense_example_stemmed.add(MMItalianUtil.italianLemmer(example_word));
                    } else {
                        logSubSubParag("Termine di glossa o esempio di senso correlato da escludere: "+example_word);
                        logSubSubParag("Lemma per il termine di glossa o esempio di senso correlato: "+ MMItalianUtil.italianLemmer(word));
                    }
                }
                sense_extended = sense_extended_stemmed;

                // creo la signature come concatenazione dei termini presenti negli esempi e glossa ripuliti e stemmatizzati
                ArrayList<String> signature_of_sense = new ArrayList<String>();
                signature_of_sense.addAll(sense_gloss);
                signature_of_sense.addAll(sense_examples);
                signature_of_sense.addAll(sense_extended);

                int actual_sense_overlap = computeOverlap(signature_of_sense, context);

                logSubSection("Overlap trovato per la signature del senso: " + actual_sense_overlap);

                // se è maggiore del maggiore trovato fin'ora
                if (actual_sense_overlap > max_overlap) {
                    logSubSubSection("Senso migliore trovato: vecchio overlap = " + max_overlap + ", nuovo = " + actual_sense_overlap);
                    // mi salvo il massimo overlap e il senso associato
                    max_overlap = actual_sense_overlap;
                    try {
                        ArrayList<String> temp = MMBabelNet.getBabelSynsetDefinition(sense_id, "IT");
                        if(temp.size() > 0)
                            best_sense = temp.get(0);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
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
     * Estendo il contesto aggiungendo glosse ed esempi di sensi correllati
     * @param synset_id : BabelNet Synset richiesto
     * @return ArrayList<String> lista di glosse ed esempi di sensi correlati
     */
    public ArrayList<String> extendWithStemmedGlossAndRelatedExample(String synset_id) {

        ArrayList<String> results_all = new ArrayList<String>();

        ArrayList<ArrayList<String>> results_related = new ArrayList<ArrayList<String>>();
        try {
            results_related = MMBabelNet.getRelatedSynset(synset_id, relations_wanted);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        for(ArrayList<String> related : results_related) {

            ArrayList<String> results_defintion_and_examples_related = new ArrayList<String>();
            try {
                results_defintion_and_examples_related = MMBabelNet.getBabelSynsetDefinition(related.get(0), synset_id);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < results_defintion_and_examples_related.size(); i++) {
                if(i == 0) {
                    results_all.add(results_defintion_and_examples_related.get(i));
                }
                else {
                    results_all.add(results_defintion_and_examples_related.get(i));
                }
            }

        }

        ArrayList<String> results_all_no_stop = new ArrayList<String>();

        for(String elem : results_all) {
            results_all_no_stop.addAll(cleanSentence(elem));
        }

        return results_all_no_stop;

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
