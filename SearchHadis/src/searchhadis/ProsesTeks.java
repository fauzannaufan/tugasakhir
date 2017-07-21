package searchhadis;

import IndonesianNLP.IndonesianSentenceFormalization;
import IndonesianNLP.IndonesianSentenceTokenizer;
import IndonesianNLP.IndonesianStemmer;
import java.util.ArrayList;


/**
 *
 * @author M. Fauzan Naufan
 */
public class ProsesTeks {
    
    public ArrayList<String> tokenisasi (String text) {
        IndonesianSentenceTokenizer tokenizer = new IndonesianSentenceTokenizer();
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
        
        return tokenizer.tokenizeSentence(text);
    }
    
    public ArrayList<String> deleteStopword(ArrayList<String> tokens) {
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        formalizer.initStopword();
        
        for (int i=0;i<tokens.size();i++) {
            String s = formalizer.deleteStopword(tokens.get(i).toLowerCase());
            if (s.equals("")) {
                tokens.remove(i);
                i--;
            }
        }
        
        return tokens;
    }
    
    public ArrayList<String> normalisasi(ArrayList<String> tokens) {
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        
        for (int i=0;i<tokens.size();i++) {
            String formalized = formalizer.formalizeWord(tokens.get(i).toLowerCase());
            tokens.set(i, formalized);
        }
        
        return tokens;
    }
    
    public ArrayList<String> stemming(ArrayList<String> tokens) {
        IndonesianStemmer stemmer = new IndonesianStemmer();
        
        for (int i=0;i<tokens.size();i++) {
            String stemmed = stemmer.stem(tokens.get(i));
            tokens.set(i, stemmed);
        }
        
        return tokens;
    }
    
    public ArrayList<String> preproses(String text) {
        ArrayList<String> arr = tokenisasi(text);
        arr = deleteStopword(arr);
        arr = normalisasi(arr);
        arr = stemming(arr);
        
        return arr;
    }
    
    //Hanya untuk testing modul
    /*public static void main (String args[]) {
        ProsesTeks PT = new ProsesTeks();
        Hadis H = new Hadis();
        
        for (int i=0;i<1;i++) {
            String text = H.getTeksHadis("bukhari", i);
            System.out.println(text);
            ArrayList<String> arr = PT.preproses(text);
            System.out.println(arr);
        }
        
    }*/
    
}