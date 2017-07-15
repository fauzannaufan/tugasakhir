package bacahadis;

import IndonesianNLP.IndonesianSentenceFormalization;
import IndonesianNLP.IndonesianSentenceTokenizer;
import IndonesianNLP.IndonesianStemmer;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author M. Fauzan Naufan
 */
public class BacaHadis {

    public void getHadisById(JSONArray arr, int id) {
        boolean found = false;
        int i = 0;
        JSONObject obj2 = (JSONObject)arr.get(i);
        while (!found && i < arr.size()) {
            obj2 = (JSONObject)arr.get(i);
            if (obj2.get("haditsId").equals(id+"")) {
                found = true;
            } else {
                i++;
            }
        }
        System.out.println(i);
        System.out.println(obj2);
    }
    
    public void searchHadis(JSONArray arr, String keyword) {
        boolean found = false;
        JSONObject obj2;
        for (int i=0;i<arr.size();i++) {
            obj2 = (JSONObject)arr.get(i);
            if (obj2.get("indo").toString().contains(keyword)) {
                System.out.println("> "+obj2.get("haditsId"));
                System.out.println(obj2.get("indo"));
            }
        }
    }
    
    public void searchRelatedHadis(JSONArray arr, int id) {
        boolean found2;
        JSONObject obj2;
        for (int i=0;i<arr.size();i++) {
            found2 = false;
            obj2 = (JSONObject)arr.get(i);
            JSONArray arr2 = (JSONArray) obj2.get("related");
            for (int j=0;j<arr2.size();j++) {
                JSONObject obj3 = (JSONObject) arr2.get(j);
                if (obj3.get("imam").equals("bukhari")) {
                    if (obj3.get("haditsId").equals(id+"")) {
                        found2 = true;
                    }
                }
            }
            if (found2) {
                System.out.println("> "+obj2.get("haditsId"));
                //System.out.println(obj2.get("related"));
            }
        }
    }
    
    public ArrayList<String> preprocess(String text, boolean unique) {
        IndonesianStemmer stemmer = new IndonesianStemmer();
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        IndonesianSentenceTokenizer tokenizer = new IndonesianSentenceTokenizer();
        
        //Membuang tanda-tanda baca
        text = text.replaceAll("['`,()\";.:?!@#$%^&*]", "");
        text = text.replaceAll("[/]", " ");
        
        //Membuang nama-nama periwayat hadis
        while (text.contains("[") || text.contains("]")) {
            int idx = text.indexOf("[");
            String sub;
            if (text.indexOf("]",idx)+1 == text.length()) {
                sub = text.substring(idx);
            } else {
                sub = text.substring(idx,text.indexOf("]",idx)+2);
            }
            text = text.replace(sub, "");
        }
        
        // Memproses dash
        while (text.contains("-")) {
            int indexDash = text.indexOf("-");
            int indexLastSpace = text.lastIndexOf(" ", indexDash);
            if (indexLastSpace < 0) {
                indexLastSpace = 0;
            } else {
                indexLastSpace += 1;
            }
            int indexSpace = text.indexOf(" ", indexDash);
            if (indexSpace < 0) {
                indexSpace = text.length();
            }
            String sub1 = text.substring(indexLastSpace, indexDash).toLowerCase();
            String sub2 = text.substring(indexDash+1, indexSpace).toLowerCase();
            if (stemmer.stem(sub1).equals(stemmer.stem(sub2))) {
                //Hukum-hukum -> hukum
                text = text.substring(0,indexDash)+text.substring(indexSpace);
            } else {
                //Jual-beli -> jual beli
                text = text.replace("-", " ");
            }
        }
        
        //Menghilangkan stopwords
        formalizer.initStopword();
        text = formalizer.deleteStopword(text.toLowerCase());
        System.out.println(text);
        //Tokenisasi
        ArrayList<String> tokens = new ArrayList<>();
        tokens.addAll(tokenizer.tokenizeSentence(text));
        
        //Formalisasi dan Stemming
        for (int i=0;i<tokens.size();i++) {
            String formalized = formalizer.formalizeWord(tokens.get(i).toLowerCase());
            String stemmed = stemmer.stem(formalized);
            tokens.set(i, stemmed);
        }
        
        //Unique
        if (unique) {
            Set<String> hs = new HashSet<>();
            hs.addAll(tokens);
            tokens.clear();
            tokens.addAll(hs);
        }
        
        //System.out.println(text);
        
        return tokens;
    }
    
    public int cekJumlahHadis(String imam) {
        int size = -1;
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            size = arr.size();
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(BacaHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return size;
    }
    
    public String bacaHadis(String imam, int idx) {
        
        String teks = null;
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            
            JSONObject obj2 = (JSONObject)arr.get(idx);
            String indo = obj2.get("indo").toString();
            String no_hadis = obj2.get("haditsId").toString();
            teks = no_hadis+"<"+indo;
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(BacaHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return teks;
    }
    
    public static void main (String args[]) {
        try {
            JSONParser parser = new JSONParser();
            String imam = "bukhari";
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            
            String teks = new BacaHadis().bacaHadis(imam, 268);
            String indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> a = new BacaHadis().preprocess(indo, true);
            
            new BacaHadis().getHadisById(arr,1917);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(BacaHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
