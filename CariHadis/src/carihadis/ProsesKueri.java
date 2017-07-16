package carihadis;

import IndonesianNLP.IndonesianSentenceFormalization;
import IndonesianNLP.IndonesianSentenceTokenizer;
import IndonesianNLP.IndonesianStemmer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Kelas untuk memproses kueri dari user
 * @author M. Fauzan Naufan
 */
public class ProsesKueri {
    
    /**
     * Fungsi untuk memproses kueri secara keseluruhan
     * Fungsi ini akan dipanggil oleh main program
     * Fungsi mengembalikan token-token dari kueri
     * @return
     */
    public ArrayList<String> Proses() {
        String kueri = Baca();
        return Preproses(kueri);
    }
    
    /**
     * Fungsi untuk membaca kueri dari user
     * @return 
     */
    private String Baca() {
        String kueri = "shalat subuh berjamaah";
        return kueri;
    }
    
    /** 
     * Fungsi untuk melakukan preproses terhadap kueri
     * @param kueri
     * @return 
     */
    private ArrayList<String> Preproses(String text) {
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
        Set<String> hs = new HashSet<>();
        hs.addAll(tokens);
        tokens.clear();
        tokens.addAll(hs);
        
        //System.out.println(text);
        //System.out.println(tokens);
        
        return tokens;
    }
}
