package uni.sc.radicioni.wordnet;

import uni.sc.util.MMConfig;
import uni.sc.util.logger.MMLogger;
import uni.sc.util.tools.MMToolbox;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import static uni.sc.util.logger.MMLogger.*;

/**
 * Main Esercitazione 1 di 5 - Parte di Radicioni
 *
 * Created by Matteo on 15/04/15.
 */
public class MMMainEsercitazioneUno {

    // istanzio il logger
    private final static MMLogger logger = new MMLogger();

    public void start(String prj_name, String title, String filename) throws IOException {

        // creo la toolsbox
        MMToolbox toolsbox = new MMToolbox();

        System.out.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                         "~~~~~~~~ Radicioni: Esercitazione 1 ~~~~~~~~~\n" +
                         "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

        // richiedo una sentence in ingresso
        System.out.print("Inserisci una frase (Vuoto = default: 'The house was burnt to ashes while the owner returned.'): ");
        String sentence = toolsbox.getInputString(true);
        if(sentence.trim().length() == 0) {
            sentence = "The house was burnt to ashes while the owner returned.";
            System.out.print("Frase inserita: default\n");
        } else {
            System.out.print("Frase inserita: '" + sentence + "'\n");
        }

        // richiedo una word da disambiguare
        System.out.print("Inserisci quale termine disambiguare (vuoto = disambigua tutti i termini): ");
        String word = toolsbox.getInputString(true);
        if(word.trim().length() == 0) {
            System.out.print("Disambiguo tutte le parole\n");
        } else {
            word = "";
            System.out.print("Parola da disambiguare: '"+word+"'\n");
        }

        // imposto il livello di verbosità del logger
        System.out.print("Inserisci il livello di verbosità del logging su console (1-7; Default => 3): ");
        int clog = toolsbox.getInputInt(1, 7, 3);
        System.out.print("Logging su console: verbosità "+clog+"\n");
        System.out.print("Inserisci il livello di verbosità del logging su file    (1-7; Default => 5): ");
        int flog = toolsbox.getInputInt(1, 7, 5);
        System.out.print("Logging su file: verbosità "+flog+"\n");

        // richiedo quale stemmer usare
        System.out.print("Avvio Lesk utilizzando:" +
                "\n\t1: Stemmer RiTa;" +
                "\n\t2: Stemmer Esterno;" +
                "\n\t3: Lemmer StanfordNLP Parser (Default);" +
                "\nScelta: ");
        int stemmer = toolsbox.getInputInt(1, 3, 3);
        if(stemmer == 1) {
            System.out.print("Eseguo con: stemmer RiTa;\n");
        } else if(stemmer == 2) {
            System.out.print("Eseguo con: stemmer esterno;\n");
        } else if(stemmer == 3) {
            System.out.print("Eseguo con: lemmer StanfordNLP Parser;\n");
        }

        // richiedo quale file di stopwords usare
        System.out.print("Inserisci quale file di stopwords usare:" +
                "\n\t1: stop_words_1.txt (Default);" +
                "\n\t2: stop_words__ frakes_baeza-yates.txt;" +
                "\n\t3: stop_words_FULL.txt;" +
                "\nScelta: ");
        int stopwords = toolsbox.getInputInt(1, 3, 1);
        if(stopwords == 1) {
            System.out.print("Eseguo con: file_1;\n");
        } else if(stopwords == 2) {
            System.out.print("Eseguo con: file_2;\n");
        } else if(stopwords == 3) {
            System.out.print("Eseguo con: file_3;\n");
        }

        String stem = "stemmer";
        if(stemmer < 3) stem += "_"+stemmer;
        else stem = "stanford_lemmer";
        filename += "WSD_" + stem + "_swords_" + stopwords;

        String description = "Questo file contiene il log dell'Esercitazione 1. " +
                "Per maggiori informazioni sulle scelte "+
                "implementative fare riferimento alla relazione. "+
                "<br/><br/>" +
                "I file di log di questo esercizio si trovano in:<br/>"+
                filename + "<br/>Con il significato di: WSD_[stemmer_val/lemmer]_[swords_val].[html && txt]";

        MMLogger.setup(prj_name, title, description, filename);

        setConsoleLogVerbosity(clog);
        setFileLogVerbosity(flog);

        // costruisco il core dell'applicazione passandogli il logger
        MMCoreEsercitazioneUno core = new MMCoreEsercitazioneUno(stemmer, stopwords);

        // chiamo il la prima versione del Lesk Alghoritm
        HashMap<String, String> wordsBestSenses =  core.simplifiedLesk(word, sentence);

        // stampo i sensi migliori per ogni parola
        for(String w : wordsBestSenses.keySet()) {
            logTitle("Senso migliore per la parola '" + w + "': " + wordsBestSenses.get(w));
        }

    }

    public static void main(String[] args) {

        MMMainEsercitazioneUno tester = new MMMainEsercitazioneUno();

        try {

            MMConfig config = new MMConfig();
            String prj_name = "WSD";
            String title = "Esercitazione 1 - Radicioni WSD";
            String filename = "wordnet/";

            tester.start(prj_name, title, filename);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problemi a creare i file di log");
        }

        try {
            Desktop.getDesktop().open(MMLogger.fileHtmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
