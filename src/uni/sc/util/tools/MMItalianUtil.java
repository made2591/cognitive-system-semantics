package uni.sc.util.tools;

import uni.sc.util.MMConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import static uni.sc.util.logger.MMLogger.logSubSubSection;

/**
 * Questa classe lavora con lemmari dell'italiano per lemmatizzare contenuto di documenti italiani
 *
 * Created by Matteo on 18/04/15.
 */
public class MMItalianUtil {

    /**
     * Lemmatizzatore di termine italiano attraverso l'utilizzo di lemmario su file (MORPH-IT)
     * @param word : parola da lemmatizzare
     * @return lemma String : parola lemmatizzata
     */
    static public String italianLemmer(String word) {

        MMConfig config = new MMConfig();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(config.getLEMMARIO()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] infos = line.split("\t");
                if(infos.length > 1 && infos[0].equalsIgnoreCase(word)) {
                    return infos[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return word;

    }

    /**
     * Questo metodo pulisce una stringa facendo utilizzo di uno stopwords file
     * @param s : Stringa, Sentence da cui rimuovere le stopwords
     * @return ArrayList<String> infos : sentence
     */
    public static ArrayList<String> cleanSentence(String s) {

        MMToolbox toolsbox = new MMToolbox();
        MMConfig config = new MMConfig();

        String slog = s;
        if(slog.length() >= 30) { slog = s.substring(0, 30); }
        logSubSubSection("Pulisco la stringa '" + slog + "..." + "' tramite espressione regolare");
        s = s.replaceAll("[^a-zA-Z ]", "").toLowerCase();

        // creo variabili per computazione e risultato
        ArrayList<String> s_array = toolsbox.splitSentenceAsArrayListOfString(s);
        ArrayList<String> s_result = toolsbox.splitSentenceAsArrayListOfString(s);

        // buffer reader per leggere il file di stopwords
        BufferedReader br = null;
        try {
            br = new BufferedReader(
                    new FileReader(
                            new File(config.getITALIAN_STOP_WORDS())
                    )
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String line;
            try {
                if (br != null) {
                    String stop_words_removed = "";
                    ArrayList<String> already_removed = new ArrayList<>();
                    while ((line = br.readLine()) != null) {
                        for (String w : s_array) {
                            if (line.trim().length() > 0) {
                                line = line.split(" ", 2)[0].trim();
                                if (line.trim().length() > 0) {
                                    if (w.equalsIgnoreCase(line)) {
                                        if(!already_removed.contains(line)) {
                                            stop_words_removed += line + "; ";
                                            already_removed.add(line);
                                        }
                                        while (s_result.remove(w)) {
                                            s_result.remove(w);
                                        }
                                    }
                                // System.out.print("w: " + w + "; line: " + line + "\n");
                                }
                            }
                        }
                    }
                    if (!stop_words_removed.trim().isEmpty()) {
                        logSubSubSection("Rimuovo le stopwords '" + stop_words_removed + "'");
                    } else {
                        logSubSubSection("Nessuna stowords rimossa!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return s_result;

    }

    /**
     * Lemmatizzatore di frase italiana attraverso l'utilizzo di lemmario su file (MORPH-IT)
     * @param sentence: frase da lemmatizzare
     * @return lemma ArrayList<String>: frase lemmatizzata
     */
    static public ArrayList<String> italianSentenceLemmer(ArrayList<String> sentence) {

        MMConfig config = new MMConfig();

        ArrayList<String> lemmed_sentence = new ArrayList<String>();
        HashMap<String, String> ordered_lemmed_found = new HashMap<String, String>();

        for(String lemma : sentence) {
            ordered_lemmed_found.put(lemma, lemma);
        }


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(config.getLEMMARIO()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] infos = line.split("\t");
                for(String word : ordered_lemmed_found.keySet()) {
                    if(infos.length > 1 && infos[0].equalsIgnoreCase(word)) {
                        ordered_lemmed_found.put(word.toLowerCase(), infos[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(String lemma : ordered_lemmed_found.keySet()) {
            lemmed_sentence.add(ordered_lemmed_found.get(lemma));
        }

        return lemmed_sentence;

    }

}