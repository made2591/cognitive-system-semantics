package uni.sc.radicioni.rocchio;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe per calcolare TF-IDF
 * @author Matteo
 */
public class MMTFIDF {

    /**
     * Calcolo della TF di ogni termine presente all'interno di un set di documenti
     * (array di stringhe all'interno di un hashmap)
     * @param all_documents_term : mapping nome documento => lista di termini del documento
     * @return HashMap nome documento => Hashmap Stringa => TF value
     */
    public HashMap<String, HashMap<String, Double>> DocsetTFCalculator(HashMap<String, ArrayList<String>> all_documents_term) {
        HashMap<String, HashMap<String, Double>> tf = new HashMap<String, HashMap<String, Double>>();
        for (String doc_name : all_documents_term.keySet()) {
            tf.put(doc_name, TFCalculator(all_documents_term.get(doc_name)));
        }
        return tf;
    }

    /**
     * Calcolo della TF di ogni termine presente all'interno di un documento (array di stringhe)
     * @param document_terms : array di stringhe
     * @return Hashmap Stringa => TF value
     */
    public HashMap<String, Double> TFCalculator(ArrayList<String> document_terms) {

        HashMap<String, Double> tf_doc = new HashMap<String, Double>();
        for (String term_actual : document_terms) {
            if(tf_doc.containsKey(term_actual)) {
                Double d = tf_doc.get(term_actual);
                tf_doc.put(term_actual, d.intValue()+1.0);
            } else {
                tf_doc.put(term_actual, 1.0);
            }
        }
        for (String term_actual : document_terms) {
            tf_doc.put(term_actual, (double) tf_doc.get(term_actual) / document_terms.size());
        }
        return tf_doc;

    }

    /**
     * Calcolo della IDF di un termine di un documento
     * @param dictionary_terms : termini del dizionario di cui calcolare la idf (ordinato lessicograficamente)
     * @param all_documents_term : mapping nome documento => lista di termini del documento
     * @return HashMap<String, Double> : valori di idf per ogni termine del dizionario
     */
    public HashMap<String, Double> DocumentIDFCalculator(ArrayList<String> dictionary_terms, HashMap<String, ArrayList<String>> all_documents_term) {
        HashMap<String, Double> idf_vector = new HashMap<String, Double>();
        // per ogni parola nel dizionario (ordinato)
        for (String dictionary_term : dictionary_terms) {
            double count = 0.0;
            // per ogni documento
            for (ArrayList<String> document_terms : all_documents_term.values()) {
                // se il termine del dizionario preso in esame
                // è presente nella lista di termini del documento, incremento
                if (document_terms.contains(dictionary_term)) count++;
            }
            if(count == 0.0) idf_vector.put(dictionary_term, Math.log(all_documents_term.keySet().size() / count + 1));
            else idf_vector.put(dictionary_term, Math.log(all_documents_term.keySet().size() / count));
        }
        return idf_vector;
    }

}