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
        Database db = new Database();
        ProsesTeks PT = new ProsesTeks();
        MongoCollection<Document> coll;
        
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        jumlah = H.getJumlahHadis(imam);
        coll = db.connect(imam);
        for (int i=0;i<jumlah;i++) {
            System.out.println(i);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.preproses(indo);
            
            //Insert ke DB
            for (int j=0;j<output.size();j++) {
                if (db.find(coll, output.get(j))) {
                    //Update existing
                    db.update(coll, no_hadis, output.get(j));
                } else {
                    //Insert new
                    db.insert(coll, no_hadis, output.get(j));
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