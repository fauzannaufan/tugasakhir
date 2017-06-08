package bacahadis;

import IndonesianNLP.IndonesianSentenceFormalization;
import IndonesianNLP.IndonesianSentenceTokenizer;
import IndonesianNLP.IndonesianStemmer;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
public class KitabHadis {
    
    public void insertKata(String imam, int id, String kata) {
        String url = "jdbc:mysql://localhost:3306/hadis";
        String user = "root";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, "")) {
                String query = "INSERT INTO coba VALUES (0, ?, ?, ?)";
                try (PreparedStatement st = con.prepareStatement(query)) {
                    st.setString(1, imam);
                    st.setInt(2, id);
                    st.setString(3, kata);
                    st.executeUpdate();
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(KitabHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public double idf(int N, int df) {
        return Math.log10(N/df);
    }
    
    public double getIdf(String term) {
        String url = "jdbc:mysql://localhost:3306/hadis";
        String user = "root";
        double idf = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, "")) {
                Statement stmt = con.createStatement();
                String sql = "SELECT idf FROM idf_kitab WHERE term = '"+term+"'";
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    idf = rs.getDouble("idf");
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(KitabHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idf;
    }
    
    public void countIdf() {
        String url = "jdbc:mysql://localhost:3306/hadis";
        String user = "root";
        int N = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(url, user, "")) {
                Statement stmt = con.createStatement();
                
                //Menghitung jumlah dokumen
                String sql1 = "SELECT COUNT(DISTINCT imam, kitabId) AS jumlah FROM `coba` ";
                ResultSet rs1 = stmt.executeQuery(sql1);
                while (rs1.next()) {
                    N = rs1.getInt("jumlah");
                }
                System.out.println(N);
                
                //Menghitung document frequency setiap kata
                String sql2 = "SELECT kata, count(*) as jumlah FROM coba GROUP BY kata";
                ResultSet rs2 = stmt.executeQuery(sql2);
                while (rs2.next()) {
                    String kata = rs2.getString("kata");
                    int jumlah = rs2.getInt("jumlah");
                    
                    //Memasukkan idf ke DB
                    String query = "INSERT INTO idf_kitab VALUES (0, ?, ?)";
                    try (PreparedStatement st = con.prepareStatement(query)) {
                        st.setString(1, kata);
                        st.setDouble(2, idf(N,jumlah));
                        st.executeUpdate();
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(KitabHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<String> preprocess(String text, boolean unique) {
        IndonesianStemmer stemmer = new IndonesianStemmer();
        IndonesianSentenceFormalization formalizer = new IndonesianSentenceFormalization();
        IndonesianSentenceTokenizer tokenizer = new IndonesianSentenceTokenizer();
        
        text = text.replaceAll("['`,()\";]", "");
        text = text.replaceAll("[/]", " ");
        
        // Memproses dash
        if (text.contains("-")) {
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
        if (unique) {
            Set<String> hs = new HashSet<>();
            hs.addAll(tokens);
            tokens.clear();
            tokens.addAll(hs);
        }
        
        return tokens;
    }
    
    public static void main(String args[]) {
        KitabHadis KH = new KitabHadis();
        try {
            //Baca data JSON
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/metadata/kitab.json";
            
            //Ambil array pada data
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            
            //Ambil imam dan judul kitab
            JSONObject obj2;
            /*for (int i=0;i<arr.size();i++) {
                obj2 = (JSONObject)arr.get(i);
                String judul = obj2.get("judul").toString();
                System.out.print(judul+"   ");
                ArrayList<String> output = KH.preprocess(judul, true);
                System.out.println(output);
                for (int j=0;j<output.size();j++) {
                    //KH.insertKata(obj2.get("imam")+"", Integer.parseInt(obj2.get("kitabId")+""),output.get(j));
                }
            }*/
            //KH.countIdf();
            System.out.println(KH.getIdf("salat"));
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(BacaHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
