package uni.sc.radicioni.wordnet;

import edu.smu.tspell.wordnet.*;
import uni.sc.util.tools.MMToolbox;

/**
 * @deprecated
 * Classe di test per la libreria JAWS
 * Displays word forms and definitions for synsets containing the word form
 * specified on the command line. To use this application, specify the word
 * form that you wish to view synsets for, as in the following example which
 * displays all synsets containing the word form "airplane":
 * <br>
 * java TestJAWS airplane
 * Created by Matteo on 18/04/15.
 */
public class MMJawsTest {
    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */
    public static void main(String[] args) {

        System.setProperty("wordnet.database.dir", "/usr/local/WordNet-3.0/dict");

                // creo la toolsbox
        MMToolbox toolsbox = new MMToolbox();

        System.out.println("Inserisci una frase:\n");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "~~~~~~~~ Radicioni: esercitazione 1 ~~~~~~~~~\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Esercizio 1\nPasso 1 di 3");

        // richiedo una sentence in ingresso
        System.out.println("\nInserisci una frase:");
        String wordForm = toolsbox.getInputString(false);

        //  Get the synsets containing the wrod form
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wordForm);
        //  Display the word forms and definitions for synsets retrieved
        if (synsets.length > 0) {
            System.out.println("The following synsets contain '" +
                    wordForm + "' or a possible base form " +
                    "of that text:");
            for (int i = 0; i < synsets.length; i++) {
                System.out.println("");
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++) {
                    System.out.print((j > 0 ? ", " : "") +
                            wordForms[j]);
                }
                System.out.println(": " + synsets[i].getDefinition());
            }
        } else {
            System.err.println("No synsets exist that contain " +
                    "the word form '" + wordForm + "'");
        }
    }

}
