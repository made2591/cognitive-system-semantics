package uni.sc.radicioni.babelnet;

import uni.sc.util.MMConfig;
import uni.sc.util.logger.MMLogger;
import uni.sc.util.tools.MMToolbox;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import static uni.sc.util.logger.MMLogger.*;

/**
 * Main Esercitazione 2 di 5 - Parte di Radicioni
 *
 * Created by Matteo on 15/04/15.
 */
public class MMMainEsercitazioneDue {

    // istanzio il logger
    private final static MMLogger logger = new MMLogger();

    public void start(String prj_name, String title, String filename) throws IOException {

        //TODO lemmatizzatore italiano usando morph-it

        //TODO PRATICAMENTE l'algoritmo è quello di prima solo che per ogni
        // senso devo calcolare più di un singolo overlap e sommarli. Ogni singolo addendo
        // del conto dell'overlap è dato dall'intersezione (come prima quindi, la somma diciamo)
        // tra IL CONTESTO IN CUI SI TROVA LA termine (come prima => ok) e una signature
        // che è data dai termini nella glossa del senso dove che sto analizzando in quel dato
        // momento. Ma quali sensi considero? Non solo quelli della termine, bensi volta che prendo
        // un senso della termine, analizzo anche i sensi ad esso correlati e prendo le glosse e gli esempi
        // di ogni senso correlato al senso che sto analizzando. Sommo il tutto e ho trovato l'overlap.
        // PRATICAMENTE tutto uguale a prima, MA devo trovarmi glosse ed esempi dei sensi correlati
        // al senso che sto prendendo in esame.


        // creo la toolsbox
        MMToolbox toolsbox = new MMToolbox();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "~~~~~~~~ Radicioni: Esercitazione 2 ~~~~~~~~~\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        // richiedo una sentence in ingresso
        System.out.print("Inserisci una frase (Vuoto = default: 'Non riesce ad appoggiare la pianta del piede perché ha un profondo taglio vicino all'alluce.'): ");
        String sentence = toolsbox.getInputString(true);
        if(sentence.trim().length() == 0) {
            sentence = "Non riesce ad appoggiare la pianta del piede perché ha un profondo taglio vicino all'alluce.";
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
            System.out.print("Parola da disambiguare: '"+word+"'\n");
        }

        // imposto il livello di verbosità del logger
        System.out.print("Inserisci il livello di verbosità del logging su console (1-7; Default => 3): ");
        int clog = toolsbox.getInputInt(1, 7, 3);
        System.out.print("Logging su console: verbosità " + clog + "\n");
        System.out.print("Inserisci il livello di verbosità del logging su file    (1-7; Default => 5): ");
        int flog = toolsbox.getInputInt(1, 7, 5);
        System.out.print("Logging su file: verbosità " + flog + "\n");

        // specifico le relazioni desiderate
        ArrayList<String> relations_wanted = new ArrayList<String>();
        relations_wanted.add("meronym");
        relations_wanted.add("hyponym");
        relations_wanted.add("hypernym");
        relations_wanted.add("synonyms");

        String description = "Questo file contiene il log dell'Esercitazione 2" +
                "<br/><br/>" +
                "I file di log di questo esercizio si trovano in:<br/>"+
                filename;

        MMLogger.setup(prj_name, title, description, filename);

        setConsoleLogVerbosity(clog);
        setFileLogVerbosity(flog);

        // costruisco il core dell'applicazione passandogli il logger
        MMCoreEsercitazioneDue core = new MMCoreEsercitazioneDue(relations_wanted);

        // chiamo il la prima versione del Lesk Alghoritm
        HashMap<String, String> wordsBestSenses =  core.extendedLeskWithGloss(word, sentence);

        // stampo i sensi migliori per ogni termine
        for(String w : wordsBestSenses.keySet()) {
            logTitle("Senso migliore per il termine '" + w + "': " + wordsBestSenses.get(w));
        }

    }

    public static void main(String[] args) {

        MMMainEsercitazioneDue tester = new MMMainEsercitazioneDue();

        try {

            MMConfig config = new MMConfig();
            String prj_name = "BabelNet";
            String title = "Esercitazione 2 - Radicioni BabelNet";
            String filename = "babelnet/BABELNET";

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