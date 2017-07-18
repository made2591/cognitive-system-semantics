package uni.sc.radicioni.wordnet;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/**
 * Created by Matteo on 17/04/15.
 */
public class MMStanfordUtil {

    // creates a StanfordCoreNLP object, with POS tagging,
    // lemmatization, NER, parsing, and coreference resolution
    static public Properties props = new Properties();

    public MMStanfordUtil() {

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

    }

    public String getLemmaOfWord(String w) {

        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(w);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                return token.lemma();
            }

        }

        return w;

    }

    public String getPosTagOfWord(String w) {

        Properties props = new Properties();

        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(w);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                return token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            }

        }

        return "NN";

    }


}
