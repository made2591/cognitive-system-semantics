package uni.sc.util.logger;

import uni.sc.util.MMConfig;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 * Classe per effettuare logging su file e su console
 * Created by Matteo on 15/04/15.
 */
public class MMLogger {

    static private FileHandler fileTxt;
    static public File fileTxtFile;
    static private MMLogFormatter formatterTxt;

    static private FileHandler fileHTML;
    static public File fileHtmlFile;
    static private Formatter formatterHTML;

    static private Logger logger;
    static private ConsoleHandler consoleHandler;
    static private FileHandler fileTxtHandler;
    static private FileHandler fileHTMLHandler;

    public MMLogger() {
        // ottiene il logger globale per configurarlo
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    static public void setup(String prj_name, String title, String description, String filename) throws IOException {

        // istanzio l'oggetto che contiene le configurazioni
        MMConfig config = new MMConfig();

        // istanzio il logger
        Logger logger = MMLogger.logger;

        // Sopprimo ogni forma di handler di default per logging
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        // creo i puntatori ai file per il log su file
        fileTxtHandler = new FileHandler(config.getTXT_LOG_FILE()+filename+".txt");
        fileTxtFile = new File(config.getTXT_LOG_FILE()+filename+".txt");
        fileHTMLHandler = new FileHandler(config.getHTML_LOG_FILE()+filename+".html");
        fileHtmlFile = new File(config.getHTML_LOG_FILE()+filename+".html");

        // creo l'handler per il log su file txt
        formatterTxt = new MMLogFormatter();
        fileTxtHandler.setFormatter(formatterTxt);
        logger.addHandler(fileTxtHandler);

        // creo l'handler per il log in console
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatterTxt);
        logger.addHandler(consoleHandler);

        // creo l'handler per il log su file HTML
        MMHTMLFormatterForLog html_formatter = new MMHTMLFormatterForLog();
        html_formatter.setPrj_name(prj_name);
        html_formatter.setTitle(title);
        html_formatter.setDescription(description);
        formatterHTML = html_formatter;
        fileHTMLHandler.setFormatter(formatterHTML);
        logger.addHandler(fileHTMLHandler);
    }

    // set-in della verbosità su console (DEFAULT = 7)
    static public void setConsoleLogVerbosity(int level) {

        switch (level) {
            case 1:
                consoleHandler.setLevel(Level.SEVERE);
                break;
            case 2:
                consoleHandler.setLevel(Level.WARNING);
                break;
            case 3:
                consoleHandler.setLevel(Level.INFO);
                break;
            case 4:
                consoleHandler.setLevel(Level.CONFIG);
                break;
            case 5:
                consoleHandler.setLevel(Level.FINE);
                break;
            case 6:
                consoleHandler.setLevel(Level.FINER);
                break;
            case 7:
                consoleHandler.setLevel(Level.FINEST);
                break;
            default:
                consoleHandler.setLevel(Level.SEVERE);
                break;
        }

    }

    // set-in della verbosità su file (DEFAULT = 5)
    static public void setFileLogVerbosity(int level) {

        switch (level) {
            case 1:
                fileTxtHandler.setLevel(Level.SEVERE);
                fileHTMLHandler.setLevel(Level.SEVERE);
                break;
            case 2:
                fileTxtHandler.setLevel(Level.WARNING);
                fileHTMLHandler.setLevel(Level.WARNING);
                break;
            case 3:
                fileTxtHandler.setLevel(Level.INFO);
                fileHTMLHandler.setLevel(Level.INFO);
                break;
            case 4:
                fileTxtHandler.setLevel(Level.CONFIG);
                fileHTMLHandler.setLevel(Level.CONFIG);
                break;
            case 5:
                fileTxtHandler.setLevel(Level.FINE);
                fileHTMLHandler.setLevel(Level.FINE);
                break;
            case 6:
                fileTxtHandler.setLevel(Level.FINER);
                fileHTMLHandler.setLevel(Level.FINER);
                break;
            case 7:
                fileTxtHandler.setLevel(Level.FINEST);
                fileHTMLHandler.setLevel(Level.FINEST);
                break;
            default:
                fileTxtHandler.setLevel(Level.FINE);
                fileHTMLHandler.setLevel(Level.FINE);
                break;
        }

    }

    // ############################################################################# //
    // ############################################################################# //
    // ################### METODI PER SCRIVERE SU FILE DI LOG ###################### //
    // ############################################################################# //
    // ############################################################################# //

    static public void logTitle(String sentence) {
        logger.severe(sentence);
    }

    static public void logSection(String sentence) {
        logger.warning(sentence);
    }

    static public void logSubSection(String sentence) {
        logger.info(sentence);
    }

    static public void logSubSubSection(String sentence) {
        logger.config(sentence);
    }

    static public void logParag(String sentence) {
        logger.fine(sentence);
    }

    static public void logSubParag(String sentence) {
        logger.finer(sentence);
    }

    static public void logSubSubParag(String sentence) {
        logger.finest(sentence);
    }

    public static void flushLogger() {
        fileHTMLHandler.flush();
        fileTxtHandler.flush();
    }

}

