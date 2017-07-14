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
        MongoCollection<Document> coll = db.connect();
        
        String imam = "bukhari";
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        jumlah = B.cekJumlahHadis(imam);
        for (int i=2;i<10;i++) {
            //Ambil term-term dari hadis
            teks = B.bacaHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            //System.out.println(indo);
            ArrayList<String> output = B.preprocess(indo, true);
            
            //Insert ke DB
            for (int j=0;j<output.size();j++) {
                System.out.print(no_hadis+"  ");
                System.out.println(output.get(j));
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
