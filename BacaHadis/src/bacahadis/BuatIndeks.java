package bacahadis;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class BuatIndeks {
    
    public static void main (String args[]) {
        //Inisialisasi variabel
        BacaHadis B = new BacaHadis();
        DB db = new DB();
        MongoCollection<Document> coll;
        
        String imam = "bukhari";
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        jumlah = B.cekJumlahHadis(imam);
        coll = db.connect(imam);
        for (int i=0;i<jumlah;i++) {
            System.out.println(i);
            
            //Ambil term-term dari hadis
            teks = B.bacaHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            //System.out.println(indo);
            ArrayList<String> output = B.preprocess(indo, true);
            
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
}
