/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uni.sc.radicioni.rocchio;

import uni.sc.util.tools.MMToolbox;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe per calcolare la cosine similarity
 * @author Matteo Madeddu
 */
public class MMCosineSimilarity {

    // Toolbox per la gestione di stampe
    public static MMToolbox toolsbox = new MMToolbox();

    /**
     * Metodo per calcolare la cosine similarity tra due vettori di Double
     * @param docVector1 : document vector 1 (a)
     * @param docVector2 : document vector 2 (b)
     * @return cosineSimilarity: double value
     */
    public double cosineSimilarity(ArrayList<Double> docVector1, ArrayList<Double> docVector2) {

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        // I vettori devono avere la stessa lunghezza
        for (int i = 0; i < docVector1.size(); i++) {
            dotProduct += docVector1.get(i) * docVector2.get(i);    // a.b
            magnitude1 += Math.pow(docVector1.get(i), 2);           // (a^2)
            magnitude2 += Math.pow(docVector2.get(i), 2);           // (b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);                         // sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);                         // sqrt(b^2)
        if (magnitude1 != 0.0 || magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            cosineSimilarity = 0.0;
        }

        return cosineSimilarity;
    }

    public static HashMap<String, Double> diffDoubleCentroidHashMapVector
            (HashMap<String, Double> pos_part, HashMap<String, Double> neg_part) {

        HashMap<String, Double> vector_diff = new HashMap<String, Double>();
        for (String dim : pos_part.keySet()) {
            vector_diff.put(dim, pos_part.get(dim) - neg_part.get(dim));
        }

        return vector_diff;
    }

}
