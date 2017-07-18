package uni.sc.radicioni.rocchio;

import uni.sc.util.MMConfig;
import uni.sc.util.logger.MMLogger;
import uni.sc.util.tools.MMToolbox;
import java.awt.*;
import java.io.IOException;
import static uni.sc.util.logger.MMLogger.setConsoleLogVerbosity;
import static uni.sc.util.logger.MMLogger.setFileLogVerbosity;

/**
 * Questa classe rappresenta il core dell'esercizio 3.
 * Implementa Rocchio con tanti parametri richiesti a runtime.
 * Fa logging su file e console e può salvare il training e la creazione
 * dello spazio vettoriale su file per una più veloce esecuzione
 * se si vuole rieseguire il testing. Fa riferimento alle classi di utilità
 * delle esercitazioni 1 e 2, in aggiunta alle classi presenti nel package
 * in cui è contenuta.
 *
 * Created by Matteo on 15/04/15.
 */
public class MMMainEsercitazioneTre {

    // istanzio il logger
    private final static MMLogger logger = new MMLogger();

    public void start(String prj_name, String title, String filename) throws IOException {

        MMConfig config = new MMConfig();


        // DEFAULT CONSOLE LOG
        int default_clog = 5;
        // DEFAULT FILE LOG
        int default_flog = 5;
        // DEFAULT POSIZIONE DOCSET
        String default_docset_position = config.getROCCHIO_ITALIAN_DOCUMENT_ROOT();
        // DEFAULT VERSION SOFTWARE
        int default_version = 3;
        // DEFAULT NPOS ALGORITHM
        int default_npos_alg = 2;
        // DEFAULT BETA VALUE
        double default_beta = 16.0;
        // DEFAULT GAMMA VALUE
        double default_gamma = 40.0;
        // DEFAULT TRAINING PERC
        double default_train_perc = 0.8;
        // DEFAULT DEVELOPING PERC
        double default_dev_perc = 0.0;
        // DEFAULT TESTING PERC
        double default_test_perc = 0.2;
        // DEFAULT SAVING OPTION
        int default_saving_option = 0;
        // DEFAULT RANDOM OPTION
        int default_random_option = 0;

        // creo la toolsbox
        MMToolbox toolsbox = new MMToolbox();

        System.out.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "~~~~~~~~ Radicioni: Esercitazione 3 ~~~~~~~~~\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

        // imposto il livello di verbosità del logger
        System.out.print("Inserisci il livello di verbosità del logging su console (1-7; Default => "+default_clog+"): ");
        int clog = toolsbox.getInputInt(1, 7, default_clog);
        System.out.print("Logging su console: verbosità " + clog + "\n");
        System.out.print("Inserisci il livello di verbosità del logging su file    (1-7; Default => "+default_flog+"): ");
        int flog = toolsbox.getInputInt(1, 7, default_flog);
        System.out.print("Logging su file: verbosità " + flog + "\n");

        // richiedo la directory dei documenti
        System.out.print("Inserisci la posizione dei documenti. Default " + default_docset_position +": ");
        String document_dir = toolsbox.getInputString(true);
        if(document_dir.trim().length() == 0) {
            document_dir = default_docset_position;
            System.out.print("Posizione inserita: default\n");
        } else {
            System.out.print("Posizione inserita: "+document_dir+"\n");
        }

        // richiedo quale versione usare per il sistema
        System.out.print("Inserisci quale versione vuoi utilizzare del sistema:" +
                           "\n\t1 => Calcolo centroidi attraverso media dei documenti appartenenti alla classe;" +
                           "\n\t2 => Calcolo centroidi attraverso utilizzo dei POS e dei NEG;" +
                           "\n\t3 => Calcolo centroidi attraverso utilizzo dei POS e dei NEAR POSITIVE;" +
                           "\nScelta (Vuoto = Default: "+default_version+"): ");
        int version = toolsbox.getInputInt(1, 3, default_version);
        System.out.print(version+"\n");
        int near_pos_mode = 0;
        if(version > 2) {
            // richiedo quale versione usare per il sistema
            System.out.print("Inserisci come calcolare i NEAR POSITIVE: per ogni opzione, è segnata la cardinalità (min-max) di classi che possono\n" +
                    "essere considerate come NEAR POSITIVE, dove * indica tutte e 0 indica che la formula, per quella classe, manterrà il vecchio centroide:" +
                    "\n\t1 => Classe (1-1) con cui è stato fatto maggior numero di errori (developing/training);" +
                    "\n\t2 => Classi (0-*) classi con cui è stato fatto almeno un errore (developing/training);" +
                    "\n\t3 => Classi (1-*) classi con cui è stato fatto almeno un errore (developing/training): se è l'insieme è vuoto, tutte le classi;" +
                    "\n\t4 => Classe (1-1) classe con centroide più vicino alla classe presa in esame;" +
                    "\nScelta (Vuoto = Default: "+default_npos_alg+"): ");
            near_pos_mode = toolsbox.getInputInt(1, 4, default_npos_alg);
            System.out.print(near_pos_mode+"\n");
        }

        Double beta = 16.0;
        Double gamma = 4.0;
        if (version > 2) {
            // richiedo i valori di gamma e beta
            System.out.print("Inserisci il valore di beta  (Range: 0.0-100.0, Default: "+default_beta+"): ");
            beta  = toolsbox.getInputDouble(0.0, 100.0, default_beta);
            System.out.print("Valore di beta: "+beta+"\n");
            System.out.print("Inserisci il valore di gamma (Range: 0.0-100.0, Default: "+default_gamma+"): ");
            gamma = toolsbox.getInputDouble(0.0, 100.0, default_gamma);
            System.out.print("Valore di gamma: "+gamma+"\n");
            if(near_pos_mode != 4) {
                System.out.print("\nPer calcolare i NEAR POS si può fare utilizzo di quattro algoritmi diversi: una delle varianti prevede" +
                        "\nl'esecuzione dell'algoritmo nella versione in cui considera tutti i NEG, una fase di testing per capire dove" +
                        "\nsi confonde di più e una fase di ricalibrazione dei centroidi sulla base degli errori più frequenti. Per compiere" +
                        "\nla fase di testing intermedia di testing, si può decidere di rinunciare a una porzione di elementi in fase di training/testing" +
                        "\ne mantenerli per la fase intermedia di 'developing' (ovvero tuning) dei centroidi. Se questa percentuale (detta di developing)" +
                        "\nviene impostata a 0.0, per la fase di testing intermedia vengono usati gli stessi documenti di training della fase di addestramento." +
                        "\nNell'esecuzione che non necessita di calcolare i NEAR POSITIVE, il document set viene diviso solo in due parti e la percentuale" +
                        "\ndi developing non viene richiesta (non viene utilizzato un developing set, poiché il training avviene tutto in una volta).\n\n");
            }
        }
        Double train = 0.8;
        Double dev = 0.0;
        Double tes = 0.2;

        // richiedo i valori per calcolare le percentuali di training, developing e testing
        System.out.print("Inserisci la percentuale di training  (Range: 0.1-0.9, Default: "+default_train_perc+", 16 documenti su 20 per ogni classe): ");
        train  = toolsbox.getInputDouble(0.1, 0.9, default_train_perc);
        System.out.print("Documenti di training: "+train*100+"%\n");
        if(version > 2 && near_pos_mode != 4) {
            System.out.print("Inserisci la percentuale di developing (Range: 0.0-"+(Math.round((1 - (train)) * 10.0) / 10.0)+", Default: "+default_dev_perc+", 2 documenti su 20 per ogni classe): ");
            dev = toolsbox.getInputDouble(0.0, (Math.round((1 - (train)) * 10.0) / 10.0), default_dev_perc);
            System.out.print("Documenti di developing: "+dev*100+"%\n");
        }
        if(version > 2 && near_pos_mode != 4) {
            System.out.print("Inserisci la percentuale di testing    (Range: 0.1-" + (Math.round((1 - (train + dev)) * 10.0) / 10.0) + ", Default: "+default_test_perc+", 2 documenti su 20 per ogni classe): ");
            tes = toolsbox.getInputDouble(0.1, (Math.round((1 - (train + dev)) * 10.0) / 10.0), default_test_perc);
            System.out.print("Documenti di testing: " + tes * 100 + "%\n");
        } else {
            System.out.print("Inserisci la percentuale di testing    (Range: 0.1-" + (Math.round((1 - (train)) * 10.0) / 10.0) + ", Default: "+(Math.round((1 - (train)) * 10.0) / 10.0)+", "+((Math.round((1 - (train)) * 10.0) / 10.0)*10)+" documenti su 20 per ogni classe): ");
            tes = toolsbox.getInputDouble(0.1, (Math.round((1 - (train)) * 10.0) / 10.0), (Math.round((1 - (train)) * 10.0) / 10.0));
            System.out.print("Documenti di testing: " + tes * 100 + "%\n");
        }

        // Not implemented.
        boolean done = false;
        if(done) {
            // richiedo su quale docset lavorare
            System.out.print("Inserisci su quale docset lavorare (ita o eng): ");
            String language_docset = toolsbox.getInputString(true);
            if (language_docset.trim().length() == 0) language_docset = "ita";
            System.out.print("Docset: " + language_docset + "\n");

            // richiedo in quale modalità lavorare
            System.out.print("Inserisci in quale modalità lavorare (lemma o babel):");
            String working_mode = toolsbox.getInputString(true);
            if (working_mode.trim().length() == 0) working_mode = "lemma";
            System.out.print("Docset: " + working_mode + "\n");
        }

        // filename+"_[numero_versione]_[beta_gamma]_[ita_eng]_[working_mode]
        filename = filename + "_" + version
                            + "_" + beta
                            + "_" + gamma
                            + "_" + near_pos_mode
                            + "_" + train
                            + "_" + dev
                            + "_" + tes;
        //                    + "_" + language_docset
        //                    + "_" + working_mode;

        String description = "Questo file contiene il log dell'Esercitazione 3" +
                "<br/><br/>" +
                "I file di log di questo esercizio si trovano in:<br/>" +
                filename + "<br/>Con il significato di: BABELNET_[numero_versione]_[beta_val]_[gamma_val]_[near_pos_mode_val]_[train]_[dev]_[test]"; //[ita/eng]_[lemma/babel]

        MMLogger.setup(prj_name, title, description, "rocchio/" + filename);

        setConsoleLogVerbosity(clog);
        setFileLogVerbosity(flog);

        // costruisco il core dell'applicazione passandogli il logger
        MMCoreEsercitazioneTre core = new MMCoreEsercitazioneTre(document_dir, gamma, beta, train, dev, tes, version, near_pos_mode, filename);

        // richiedo quale versione usare per il sistema
        System.out.print("Inserisci l'opzione per il salvataggio dello space vector model:" +
                "\n\t0 => Se presente, carica l'ultima i documenti da file (Default);" +
                "\n\t1 => Esegue il training, costruisce il vector model senza salvare;" +
                "\n\t2 => Esegue il training, costruisce il vector model salvando su file;" +
                "\nScelta (Vuoto = Default: "+default_saving_option+"): ");
        int mode = toolsbox.getInputInt(0, 2, default_saving_option);
        System.out.print(mode+"\n");
        // richiedo quale versione usare per il sistema
        System.out.print("Inserisci l'opzione per la randomizzazione dello space vector model:" +
                "\n\t0 => Randomizza l'ordine dei documenti (rieseguo training sfruttando altri documenti, utile per mediare risultati) (Default);" +
                "\n\t1 => Utilizza l'ordine naturale di estrazione dei documenti (sempre uguale, per confronti puntuali);" +
                "\nScelta (Vuoto = Default: "+default_random_option+"): ");
        int bool = toolsbox.getInputInt(0, 1, default_random_option);
        System.out.print(bool+"\n");
        boolean random_test_mode = true;
        if(bool == 1) random_test_mode = false;
        core = core.training(random_test_mode, mode);

        core.testing();


    }

    public static void main(String[] args) {

        MMMainEsercitazioneTre tester = new MMMainEsercitazioneTre();

        try {

            MMConfig config = new MMConfig();
            String prj_name = "Rocchio";
            String title = "Esercitazione 3 - Radicioni Rocchio";
            String filename = "ROCCHIO";

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
