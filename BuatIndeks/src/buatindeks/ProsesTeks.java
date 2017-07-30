package buatindeks;

import IndonesianNLP.IndonesianSentenceFormalization;
import IndonesianNLP.IndonesianSentenceTokenizer;
import IndonesianNLP.IndonesianStemmer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


/**
 *
 * @author M. Fauzan Naufan
 */
public class ProsesTeks {
    
    public String preproses (String text) {
        IndonesianStemmer stemmer = new IndonesianStemmer();
        
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
        
        return text;
    }
    
    public String deleteStopword(String text) {
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        formalizer.initStopword();
        
        return formalizer.deleteStopword(text.toLowerCase());
    }
    
    public ArrayList<String> stemming(ArrayList<String> tokens) {
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        IndonesianStemmer stemmer = new IndonesianStemmer();

        for (int i=0;i<tokens.size();i++) {
            String s = formalizer.formalizeWord(tokens.get(i).toLowerCase());
            s = stemmer.stem(s);
            s = formalizer.formalizeWord(s);
            tokens.set(i, s);
        }
        
        return tokens;
    }
    
    public ArrayList<String> tokenisasi(String text) {
        IndonesianSentenceTokenizer tokenizer = new IndonesianSentenceTokenizer();
        return tokenizer.tokenizeSentence(text);
    }
    
    public ArrayList<String> prosesTeks(String text) {
        text = preproses(text);
        text = deleteStopword(text);
        
        ArrayList<String> arr = tokenisasi(text);
        arr = stemming(arr);
        
        return arr;
    }
    
    public ArrayList<String> prosesKueri(String text) {
        ArrayList<String> arr = prosesTeks(text);
        Database DB = new Database();
        
        HashSet<String> hs = new HashSet<>();
        hs.addAll(arr);
        arr.clear();
        arr.addAll(hs);
        
        for (int i=0;i<arr.size();i++) {
            String s = arr.get(i);
            if(!DB.find(s)) {
                arr.remove(s);
            }
        }
        
        Collections.sort(arr);
        DB.closeConnection();
        return arr;
    }
    
    //Hanya untuk testing modul
    /*public static void main (String args[]) {
        ProsesTeks PT = new ProsesTeks();
        Hadis H = new Hadis();
        
        for (int i=0;i<10;i++) {
            String text = H.getTeksHadis("bukhari", i);
            text = text.substring(text.indexOf("<")+1);
            System.out.println(text);
            ArrayList<String> arr = PT.prosesTeks(text);
            System.out.println(arr);
        }
        
    }*/
    
}