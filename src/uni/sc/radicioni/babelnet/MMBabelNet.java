package uni.sc.radicioni.babelnet;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import uni.sc.util.MMConfig;
import uni.sc.util.tools.MMToolbox;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementa il wrapper per interrogare le Api di BabelNet
 * La classe è in grado di fare caching su file per risparmiare risorse
 *
 * Created by Matteo on 18/04/15.
 */
public class MMBabelNet {

    // user-agent per simulare le richieste da Browser
    static private final String USER_AGENT = "Mozilla/5.0";
    // key per interrogare le API di BabelNet
    static public final String KEY = "YOUR_BABELNET_API";
    static public final String LIST_OF_BABEL_IDS_URL = "http://babelnet.io/v1/getSynsetIds?";
    static public final String INFO_OF_GIVEN_SYNSET_URL = "http://babelnet.io/v1/getSynset";
    static public final String LIST_OF_EDGES_OF_SYNSET_ID_URL = "http://babelnet.io/v1/getEdges";
    static public final String LIST_OF_BABEL_IDS_WIKIPEDIA_TITLEPAGE_URL = "http://babelnet.io/v1/getSynsetIdsFromWikipediaTitle";
    static public final String SENSES_OF_GIVEN_WORD_URL = "http://babelnet.io/v1/getSenses";

    // gestisce oggetti in cache per limitare le richieste a BABELNET
    static public HashMap<String, Object> babelnetCache = new HashMap<String, Object>();

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // debug: ATTENZIONE. SE IMPOSTATO A TRUE NON USA LA CACHE QUINDI POTREBBE CONSUMARE LE RICHIESTE (1000 AL GIORNO) //
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    static public final boolean debug = false;

    /**
     * Ottiene una lista di liste del tipo "(Related BabelNet Synset ID, Relation Type (name of relation))
     * @param id : id del BabelNet Synset
     * @param relation_names : nomi di relazioni ammesse
     * @return ArrayList<ArrayList<String>> una lista di liste del tipo "(Related BabelNet Synset ID, Relation Type (name of relation))
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static ArrayList<ArrayList<String>> getRelatedSynset(String id, ArrayList<String> relation_names) throws URISyntaxException, MalformedURLException {

        ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
        String content;

        String hash_key = "related";
        hash_key += id;
        boolean new_content = false;

        if(babelnetCache.get(hash_key) != null && !debug) {
            content = (String)babelnetCache.get(hash_key);
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", id);
            params.put("key", KEY);
            URIBuilder b = new URIBuilder(LIST_OF_EDGES_OF_SYNSET_ID_URL);
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                b.addParameter((String)pair.getKey(), (String)pair.getValue());
                it.remove();
            }
            content = makeHTTPRequest(b.build().toURL(), "");
            new_content = true;

        }

        if(content != null) {

            org.json.JSONArray arr = new JSONArray(content);

            for (int i = 0; i < arr.length(); i++) {
                String target = arr.getJSONObject(i).getString("target");
                String language = arr.getJSONObject(i).getString("language");
                String relation_founded = arr.getJSONObject(i).getJSONObject("pointer").getString("fName");

                if(language.equalsIgnoreCase("it")) {
                    for (String relation_wanted : relation_names) {
                        if (relation_founded.trim().equalsIgnoreCase(relation_wanted)) {
                            ArrayList<String> related_example_id_and_relation_type = new ArrayList<String>();
                            related_example_id_and_relation_type.add(target);
                            related_example_id_and_relation_type.add(relation_founded);
                            result.add(related_example_id_and_relation_type);
                        }
                    }
                }
            }

            babelnetCache.put(hash_key, result);

        }

        return result;

    }

    /**
     * Recupera una lista di glosse dell'id e aggiunte glosse ed esempi per una data lingua (ITALIANO - NON PARAMETRIZZATO)
     * @param id : id del Babel Synset
     * @param lang : lingua da considerare
     * @return ArrayList<String> lista di glosse
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static ArrayList<String> getBabelSynsetDefinition(String id, String lang) throws URISyntaxException, MalformedURLException {

        ArrayList<String> result = new ArrayList<String>();
        String content = "";

        String hash_key = "definition";
        hash_key += id;
        String l = lang;
        if(lang == null) l = "IT";
        hash_key += l;

        boolean new_content = false;

        if(babelnetCache.get(hash_key) != null && !debug) {
            result = (ArrayList<String>) babelnetCache.get(hash_key);
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", id);
            params.put("key", KEY);

            URIBuilder b = new URIBuilder(INFO_OF_GIVEN_SYNSET_URL);
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                b.addParameter((String)pair.getKey(), (String)pair.getValue());
                it.remove();
            }
            content = makeHTTPRequest(b.build().toURL(), "");
            new_content = true;

        }

        if(new_content) {

            org.json.JSONArray arr_glosses = new org.json.JSONObject(content).getJSONArray("glosses");
            org.json.JSONArray all_examples = new org.json.JSONObject(content).getJSONArray("examples");

            for (int i = 0; i < arr_glosses.length(); i++) {
                try {
                    String language_of_sense = arr_glosses.getJSONObject(i).getString("language");
                    if(language_of_sense.equalsIgnoreCase(l)) {
                        String gloss_of_sense = arr_glosses.getJSONObject(i).getString("gloss");
                        result.add(gloss_of_sense);
                    }
                } catch (Exception e) {
                    System.out.print(e+"\n");
                }
            }

            for (int i = 0; i < all_examples.length(); i++) {
                try {
                    String language_of_sense = all_examples.getJSONObject(i).getString("language");
                    if(language_of_sense.equalsIgnoreCase(l)) {
                        String example_of_sense = all_examples.getJSONObject(i).getString("example");
                        result.add(example_of_sense);
                    }
                } catch (Exception e) {
                    System.out.print(e+"\n");
                }
            }

            babelnetCache.put(hash_key, result);

        }

        return result;

    }

    /**
     * Ottiene il Babel Synset
     * @param word : String parola da prendere in esame
     * @param lang : String lingua della parola
     * @param pos : String PoS della parola
     * @return ArrayList<String> Babel Synset
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static ArrayList<String> getBabelSynset(String word, String lang, String pos) throws URISyntaxException, MalformedURLException {

        ArrayList<String> result = new ArrayList<String>();
        String content = "";

        String hash_key = "synset";
        hash_key += word.toLowerCase().trim();
        String l = lang;
        if(lang == null) l = "IT";
        hash_key += l;
        if(pos != null) hash_key += pos;
        boolean new_content = false;

        if(babelnetCache.get(hash_key) != null && !debug) {
            result = (ArrayList<String>) babelnetCache.get(hash_key);
        } else {
            HashMap<String, String> params = new HashMap<String, String>();

            params.put("lang", l);
            if(pos != null) params.put("pos", pos);
            params.put("word", word);
            params.put("key", KEY);

            URIBuilder b = new URIBuilder(LIST_OF_BABEL_IDS_URL);
            Iterator it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                b.addParameter((String)pair.getKey(), (String)pair.getValue());
                it.remove();
            }
            content = makeHTTPRequest(b.build().toURL(), "");

            new_content = true;

        }

        if(new_content) {

            org.json.JSONArray arr = new JSONArray(content);

            for (int i = 0; i < arr.length(); i++) {
                String id = arr.getJSONObject(i).getString("id");
                result.add(id);
            }

            babelnetCache.put(hash_key, result);

        }

        return result;

    }

    /**
     * Esegue una richiesta HTTP
     */
    private static String makeHTTPRequest(URL url, String urlParameters) {
        HttpURLConnection connection = null;
        try {

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream ());
            wr.writeBytes (urlParameters);
            wr.flush ();
            wr.close ();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();

        } catch (Exception e) {

            e.printStackTrace();
            System.out.print(e);
            return null;

        } finally {

            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Carica una cache con le risposte di babelnet
     */
    private static HashMap<String, Object> loadBabelNetCache() {

        MMConfig config = new MMConfig();

        File f = new File(config.getBABELNET_CACHE_FILE());

        if(f.exists() && !f.isDirectory()) try {
            //use buffering
            InputStream file = new FileInputStream(config.getBABELNET_CACHE_FILE());
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                //deserialize the HashMap
                babelnetCache = (HashMap<String, Object>) input.readObject();
            } finally {
                input.close();
            }
        }
        catch (ClassNotFoundException ex) {}
        catch (IOException ex) {}
        return babelnetCache;
    }

    /**
     * Salva una cache con le risposte di babelnet
     */
    private static void saveBabelNetCache() {

        MMConfig config = new MMConfig();

        File file = new File(config.getBABELNET_CACHE_FILE());
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectOutputStream s = null;
        try {
            s = new ObjectOutputStream(f);
            s.writeObject(babelnetCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(s != null) s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @deprecated
     * Main di testing
     * @param args
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    private static void main(String[] args) throws MalformedURLException, URISyntaxException {

        ArrayList<String> relations_wanted = new ArrayList<String>();
        relations_wanted.add("meronym");
        relations_wanted.add("hyponym");
        relations_wanted.add("hypernym");
        relations_wanted.add("synonyms");

        while(true) {

            MMToolbox toolsbox = new MMToolbox();
            System.out.println("Inserisci una parola:");
            String word = toolsbox.getInputString(false);
            loadBabelNetCache();
            saveBabelNetCache();
            ArrayList<String> synsets = MMBabelNet.getBabelSynset(word, null, null);
            for(String synset_id : synsets) {

                System.out.println("\nBabelNet Synset ID: "+synset_id);
                ArrayList<String> results_defintion_and_examples = MMBabelNet.getBabelSynsetDefinition(synset_id, "IT");
                for (int i = 0; i < results_defintion_and_examples.size(); i++) {
                    if(i == 0) System.out.println("Definizione: "+results_defintion_and_examples.get(i));
                    else System.out.println("Esempio: "+results_defintion_and_examples.get(i));
                }
                if(results_defintion_and_examples.size() == 1) System.out.println("Nessun esempio trovato");

                ArrayList<ArrayList<String>> results_related = MMBabelNet.getRelatedSynset(synset_id, relations_wanted);

                for(ArrayList<String> related : results_related) {

                    System.out.println("\t\tBabelNet Related Synset ID: "+related.get(0));
                    System.out.println("\t\tBabelNet Relation Type: "+related.get(1));
                    ArrayList<String> results_defintion_and_examples_related = MMBabelNet.getBabelSynsetDefinition(related.get(0), "IT");
                    for (int i = 0; i < results_defintion_and_examples_related.size(); i++) {
                        if(i == 0) System.out.println("\t\tDefinizione: "+results_defintion_and_examples_related.get(i));
                        else System.out.println("\t\tEsempio: "+results_defintion_and_examples_related.get(i));
                    }
                    if(results_defintion_and_examples_related.size() == 1) System.out.println("\t\tNessun esempio trovato");

                }

            }
            saveBabelNetCache();

        }

    }


}
