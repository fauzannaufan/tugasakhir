package searchhadis;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Indeks {
    
    public void buatIndeks(String imam) {
        //Inisialisasi variabel
        Hadis H = new Hadis();
        Database DB = new Database();
        ProsesTeks PT = new ProsesTeks();
        MongoCollection<Document> coll;
        
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        jumlah = H.getJumlahHadis(imam);
        coll = DB.connect(imam);
        for (int i=0;i<jumlah;i++) {
            System.out.println(i);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.preproses(indo);
            
            //Insert ke DB
            for (int j=0;j<output.size();j++) {
                if (DB.find(coll, output.get(j))) {
                    //Update existing
                    DB.update(coll, no_hadis, output.get(j));
                } else {
                    //Insert new
                    DB.insert(coll, no_hadis, output.get(j));
                }
            }
        }
    }
    
    public static void main (String args[]) {
        Indeks I = new Indeks();
        String imam = "bukhari";
        I.buatIndeks(imam);
    }
    
}