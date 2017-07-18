package uni.sc;

import uni.sc.radicioni.babelnet.MMMainEsercitazioneDue;
import uni.sc.radicioni.rdf.MMMainEsercitazioneQuattro;
import uni.sc.radicioni.rocchio.MMMainEsercitazioneTre;
import uni.sc.radicioni.wordnet.MMMainEsercitazioneUno;
import uni.sc.util.tools.MMToolbox;

public class Main {

    public static void main(String[] args) {

        MMToolbox toolsbox = new MMToolbox();

        System.out.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "~~~~~~~~~ Radicioni: Esercitazioni ~~~~~~~~~~\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

        // richiedo quale file di stopwords usare
        System.out.print("Inserisci quale file esercitazione avviare:" +
                "\n\t1: WordNet;" +
                "\n\t2: BabelNet;" +
                "\n\t3: Rocchio;" +
                "\n\t4: RDF;" +
                "\nScelta: ");
        int sentence = toolsbox.getInputInt(1, 4, 1);
        switch (sentence) {

            case 1:
                MMMainEsercitazioneUno.main(null);
                break;
            case 2:
                MMMainEsercitazioneDue.main(null);
                break;
            case 3:
                MMMainEsercitazioneTre.main(null);
                break;
            case 4:
                MMMainEsercitazioneQuattro.main(null);
                break;
        }
    }

}
