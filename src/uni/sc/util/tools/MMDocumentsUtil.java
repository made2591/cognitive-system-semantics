package uni.sc.util.tools;

import uni.sc.util.MMConfig;

import java.io.*;
import java.util.ArrayList;

import static uni.sc.util.tools.MMItalianUtil.cleanSentence;

/**
 * Classe per la gestione dei documenti
 * Created by Matteo on 22/04/15.
 */
public class MMDocumentsUtil {

    /**
     * Scorre tutti i file all'interno di una directory
     * @param directoryName : nome della directory (percorso)
     * @param files : array in cui vengono inseriti i puntatori ai file trovati
     */
    static public void listAllFileInsideDirectory(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listAllFileInsideDirectory(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * Restituisce una stringa con il contenuto del documento puntato dal parametro path (Stringa)
     * @param path : stringa che rappresenta il path del documento
     * @return str : stringa che rappresenta il contenuto del documento
     */
    public static String getDocumentContent(String path) {

        String str = "EMPTY_DOCUMENT";

        File file = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[(int) file.length()];
        try {
            if (fis != null) {
                fis.read(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            str = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str;

    }

    /**
     * @deprecated TODO: RIMUOVERE AL TERMINE DELLA SCRITTURA DI ROCCHIO
     * Main per testing
     * @param args
     */
    public static void main(String[] args) {

        MMConfig config = new MMConfig();

        MMToolbox toolbox = new MMToolbox();

        ArrayList<File> files = new ArrayList<File>();

        MMDocumentsUtil.listAllFileInsideDirectory(config.getROCCHIO_ITALIAN_DOCUMENT_ROOT(), files);

        for(File file : files) {

            System.out.println(file.getAbsolutePath());
            if(!file.isHidden() && file.canRead()) {
                ArrayList<String> content_cleaned = cleanSentence(MMDocumentsUtil.getDocumentContent(file.getAbsolutePath()));
                System.out.println(toolbox.arrayOfWordsToStringSeparatedBySpecifiedSeparator(content_cleaned, " "));
                String content_lemmer = "";
                for (String word : content_cleaned) {
                    content_lemmer += MMItalianUtil.italianLemmer(word) + " ";
                }
                System.out.println(content_lemmer+"\n");
            }

        }

    }

}
