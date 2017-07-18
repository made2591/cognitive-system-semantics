package uni.sc.util;

import java.io.File;

/**
 * Classe per mantenere i path delle configurazioni
 * Created by Matteo on 15/04/15.
 */
public class MMConfig {

    public String ROOT_PROJECT_DIR = "YOUR_ABSOLUTE_PATH_TO_RES";

    public String ROOT_PROJECT_LOG_DIR = "YOUR_ABSOLUTE_PATH_TO_LOG";

    public String ROCCHIO_ITALIAN_DOCUMENT_ROOT = ROOT_PROJECT_DIR + "rocchio_res/all_doc";

    public String STOP_WORDS_PATH = ROOT_PROJECT_DIR+ "stop_words";

    public String STOP_WORDS_FILE_1 = "stop_words_1.txt";

    public String STOP_WORDS_FILE_2 = "stop_words__ frakes_baeza-yates.txt";

    public String STOP_WORDS_FILE_3 = "stop_words_FULL.txt";

    public String ITALIAN_STOP_WORDS = ROOT_PROJECT_DIR+ "bnet_res/stopwords.txt";

    public String WORDNET_PATH = ROOT_PROJECT_DIR + "wordnet/dict";

    public String HTML_LOG_FILE = ROOT_PROJECT_LOG_DIR;

    public String TXT_LOG_FILE = ROOT_PROJECT_LOG_DIR;

    public String LEMMARIO = ROOT_PROJECT_DIR+ "bnet_res/morph-it.txt";

    public String BABELNET_CACHE_FILE = ROOT_PROJECT_DIR + "bnet_res/cache/babelnet.dat";

    public String ROCCHIO_CACHE_FILE_ITA = ROOT_PROJECT_DIR + "rocchio_res/cache/";

    public String WORDNET_DICT = ROOT_PROJECT_DIR + "wordnet/dict";

    public String RDF_RES_DOC = ROOT_PROJECT_DIR + "rdf_res/news_collection";

    public String RDF_DEFAULT_FILENAME = ROOT_PROJECT_DIR + "rdf_res/collection.rdf";

    public String getRDF_DEFAULT_FILENAME() {
        return RDF_DEFAULT_FILENAME;
    }

    public void setRDF_DEFAULT_FILENAME(String RDF_DEFAULT_FILENAME) {
        this.RDF_DEFAULT_FILENAME = RDF_DEFAULT_FILENAME;
    }

    public String getRDF_RES_DOC() {
        return RDF_RES_DOC;
    }

    public void setRDF_RES_DOC(String RDF_RES_DOC) {
        this.RDF_RES_DOC = RDF_RES_DOC;
    }

    public String getSTOP_WORDS_FILE_2() {
        return STOP_WORDS_FILE_2;
    }

    public void setSTOP_WORDS_FILE_2(String STOP_WORDS_FILE_2) {
        this.STOP_WORDS_FILE_2 = STOP_WORDS_FILE_2;
    }

    public String getSTOP_WORDS_FILE_3() {
        return STOP_WORDS_FILE_3;
    }

    public void setSTOP_WORDS_FILE_3(String STOP_WORDS_FILE_3) {
        this.STOP_WORDS_FILE_3 = STOP_WORDS_FILE_3;
    }

    public String getSTOP_WORDS_FILE_1() {
        return STOP_WORDS_FILE_1;
    }

    public void setSTOP_WORDS_FILE_1(String STOP_WORDS_FILE_1) {
        this.STOP_WORDS_FILE_1 = STOP_WORDS_FILE_1;
    }

    public String getWORDNET_DICT() {
        return WORDNET_DICT;
    }

    public void setWORDNET_DICT(String WORDNET_DICT) {
        this.WORDNET_DICT = WORDNET_DICT;
    }

    public String getROOT_PROJECT_DIR() {
        return ROOT_PROJECT_DIR;
    }

    public void setROOT_PROJECT_DIR(String ROOT_PROJECT_DIR) {
        this.ROOT_PROJECT_DIR = ROOT_PROJECT_DIR;
    }

    public String getROOT_PROJECT_LOG_DIR() {
        return ROOT_PROJECT_LOG_DIR;
    }

    public void setROOT_PROJECT_LOG_DIR(String ROOT_PROJECT_LOG_DIR) {
        this.ROOT_PROJECT_LOG_DIR = ROOT_PROJECT_LOG_DIR;
    }

    public String getROCCHIO_ITALIAN_DOCUMENT_ROOT() {
        return ROCCHIO_ITALIAN_DOCUMENT_ROOT;
    }

    public void setROCCHIO_ITALIAN_DOCUMENT_ROOT(String ROCCHIO_ITALIAN_DOCUMENT_ROOT) {
        this.ROCCHIO_ITALIAN_DOCUMENT_ROOT = ROCCHIO_ITALIAN_DOCUMENT_ROOT;
    }

    public String getBABELNET_CACHE_FILE() {
        return BABELNET_CACHE_FILE;
    }

    public void setBABELNET_CACHE_FILE(String BABELNET_CACHE_FILE) {
        this.BABELNET_CACHE_FILE = BABELNET_CACHE_FILE;
    }

    public String getITALIAN_STOP_WORDS() {
        return ITALIAN_STOP_WORDS;
    }

    public void setITALIAN_STOP_WORDS(String ITALIAN_STOP_WORDS) {
        this.ITALIAN_STOP_WORDS = ITALIAN_STOP_WORDS;
    }

    public String getHTML_LOG_FILE() {
        return HTML_LOG_FILE;
    }

    public void setHTML_LOG_FILE(String HTML_LOG_FILE) {
        this.HTML_LOG_FILE = HTML_LOG_FILE;
    }

    public String getTXT_LOG_FILE() {
        return TXT_LOG_FILE;
    }

    public void setTXT_LOG_FILE(String TXT_LOG_FILE) {
        this.TXT_LOG_FILE = TXT_LOG_FILE;
    }

    public String getSTOP_WORDS_PATH() {
        return STOP_WORDS_PATH;
    }

    public void setSTOP_WORDS_PATH(String STOP_WORDS_PATH) {
        this.STOP_WORDS_PATH = STOP_WORDS_PATH;
    }

    public String getSTOP_WORDS_FILE(int val) {
        String swf = STOP_WORDS_FILE_1;
        switch (val) {
            case 1:
                swf = STOP_WORDS_FILE_1;
                break;
            case 2:
                swf = STOP_WORDS_FILE_2;
                break;
            case 3:
                swf = STOP_WORDS_FILE_3;
                break;
            default:
                swf = STOP_WORDS_FILE_1;
                break;
        }
        return swf;
    }

    public String getWORDNET_PATH() {
        return WORDNET_PATH;
    }

    public void setWORDNET_PATH(String WORDNET_PATH) {
        this.WORDNET_PATH = WORDNET_PATH;
    }

    public String getLEMMARIO() {
        return LEMMARIO;
    }

    public void setLEMMARIO(String LEMMARIO) {
        this.LEMMARIO = LEMMARIO;
    }

    public String getROCCHIO_CACHE_FILE_ITA() {
        return ROCCHIO_CACHE_FILE_ITA;
    }

    public void setROCCHIO_CACHE_FILE_ITA(String ROCCHIO_CACHE_FILE_ITA) {
        this.ROCCHIO_CACHE_FILE_ITA = ROCCHIO_CACHE_FILE_ITA;
    }

}