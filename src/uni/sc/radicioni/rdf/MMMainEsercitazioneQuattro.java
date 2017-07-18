package uni.sc.radicioni.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import uni.sc.util.MMConfig;
import uni.sc.util.logger.MMLogger;
import uni.sc.util.tools.MMToolbox;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static uni.sc.util.logger.MMLogger.*;

/**
 * Questa classe rappresenta il core dell'esercizio 4.
 * Implementa RDF. Fa logging su file e console.
 *
 * Created by Matteo on 15/04/15.
 */
public class MMMainEsercitazioneQuattro {

    // istanzio il logger
    private final static MMLogger logger = new MMLogger();

    public void start(String prj_name, String title, String filename) throws IOException {

        MMConfig config = new MMConfig();

        // creo la toolsbox
        MMToolbox toolsbox = new MMToolbox();

        System.out.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "~~~~~~~~ Radicioni: Esercitazione 4 ~~~~~~~~~\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

        // imposto il livello di verbosità del logger
        System.out.print("Inserisci il livello di verbosità del logging su console (1-7; Default => 5): ");
        int clog = toolsbox.getInputInt(1, 7, 5);
        System.out.print("Logging su console: verbosità " + clog + "\n");
        System.out.print("Inserisci il livello di verbosità del logging su file    (1-7; Default => 5): ");
        int flog = toolsbox.getInputInt(1, 7, 5);
        System.out.print("Logging su file: verbosità " + flog + "\n");

        // richiedo la directory dei documenti
        System.out.print("Inserisci la posizione dei documenti. Default: " + config.getRDF_RES_DOC());
        String document_dir = toolsbox.getInputString(true);
        if(document_dir.trim().length() == 0) {
            document_dir = config.getRDF_RES_DOC();
            System.out.print("Posizione inserita: default\n");
        } else {
            System.out.print("Posizione inserita: "+document_dir+"\n");
        }

        System.out.print("Inserisci il nome del file che conterrà la collezione RDF. Default: " + config.getRDF_DEFAULT_FILENAME());
        String nome_file = toolsbox.getInputString(true);
        if(document_dir.trim().length() == 0) {
            nome_file = config.getRDF_DEFAULT_FILENAME();
            System.out.print("Nome file inserito: default\n");
        } else {
            System.out.print("Nome file inserito: "+nome_file+"\n");
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

        String description = "Questo file contiene il log dell'Esercitazione 4" +
                "<br/><br/>" +
                "I file di log di questo esercizio si trovano in:<br/>" +
                filename+"_"+stopwords + "<br/>Con il significato di: RDF_[swords_val].[html && txt]";

        MMLogger.setup(prj_name, title, description, filename);

        setConsoleLogVerbosity(clog);
        setFileLogVerbosity(flog);

        // costruisco il core dell'applicazione passandogli il logger
        MMCoreEsercitazioneQuattro core = new MMCoreEsercitazioneQuattro(document_dir, nome_file, stopwords);

        // richiedo quale versione usare per il sistema
        System.out.print("Inserisci l'opzione per il caricamento del modello:" +
                "\n\t0 => Se presente, carica il modello dal file specificato prima;" +
                "\n\t1 => Ricostruisce il modello e lo mostra a video, senza salvare su file;" +
                "\n\t2 => Ricostruisce il modello e lo mostra a video e lo salva su file;" +
                "\nScelta:\n");
        int mode = toolsbox.getInputInt(0, 2, 0);

        Model model = core.getRDFModel(mode);

        System.out.print("Modello caricato con successo. Premi un tasto per stampare a video...\n");
        toolsbox.getInputString(true);
        System.out.print("\n");
        model.write(System.out);
        System.out.print("\n");

        boolean go_on = true;
        while(go_on) {
            flushLogger();
            // richiedo quale versione usare per il sistema
            System.out.print("Inserisci il tipo di query:" +
                    "\n\t1 => 1 Proprietà = Valore esatto;" +
                    "\n\t2 => 1 Proprietà = Contiene Valore;" +
                    "\n\t3 => Stampa il documento RDF "+nome_file+
                    "\n\t4 => Esci" +
                    "\nScelta:\n");
            flushLogger();
            int choice = toolsbox.getInputInt(1, 4, 1);
            String prop = "";
            String val = "";
            ArrayList<String> res = new ArrayList<>();
            switch (choice) {
                case 1:
                    flushLogger();
                    System.out.print("Proprietà ammesse:\ntitle, subject, description, date, creator, publisher\n");
                    prop = toolsbox.getInputString(false);
                    System.out.print("Valore esatto:\n");
                    val = toolsbox.getInputString(false);
                    res = core.getDocumentWithSpecificPropertyValue(model, prop, val);
                    if(res.size() == 0) logSubSection("Nessuno documento trovato con i parametri impostati!");
                    else {
                        for(String e : res) {
                            logSubSection("Documento: " + e);
                            flushLogger();
                        }
                    }
                    flushLogger();
                    break;
                case 2:
                    flushLogger();
                    System.out.print("Proprietà ammesse:\ntitle, subject, description, date, creator, publisher\n");
                    prop = toolsbox.getInputString(false);
                    System.out.print("Valore contenuto:\n");
                    val = toolsbox.getInputString(false);
                    res = core.getDocumentWithSubstringPropertyValue(model, prop, val);
                    if(res.size() == 0) logSubSection("Nessuno documento trovato con i parametri impostati!");
                    else {
                        for(String e : res) {
                            logSubSection("Documento: " + e);
                            flushLogger();
                        }
                    }
                    flushLogger();
                    break;
                case 3:
                    flushLogger();
                    System.out.print("\n");
                    model.write(System.out);
                    System.out.print("\n");
                    flushLogger();
                    break;
                case 4:
                    flushLogger();
                    System.out.print("Esco dal programma.\n");
                    go_on = false;
                    flushLogger();
                    break;
                default:
                    flushLogger();
                    System.out.print("Esco dal programma.\n");
                    go_on = false;
                    flushLogger();
                    break;
            }

        }

    }

    public static void main(String[] args) {

        MMMainEsercitazioneQuattro tester = new MMMainEsercitazioneQuattro();

        try {

            MMConfig config = new MMConfig();
            String prj_name = "RDF";
            String title = "Esercitazione 4 - Radicioni RDF";
            String filename = "rdf/RDF";

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