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
        MongoCollection<Document> coll2;
        
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        jumlah = H.getJumlahHadis(imam);
        coll = DB.connect("indeks");
        coll2 = DB.connect("doclength");
        for (int i=0;i<jumlah;i++) {
            System.out.println(i);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.prosesTeks(indo);
            
            //Insert term ke DB
            for (int j=0;j<output.size();j++) {
                if (DB.find(coll, output.get(j))) {
                    //Update existing
                    if (DB.findId(coll, output.get(j), no_hadis)) {
                        DB.addId(coll, no_hadis, output.get(j));
                    } else {
                        DB.update(coll, no_hadis, output.get(j));
                    }
                } else {
                    //Insert new
                    DB.insert(coll, no_hadis, output.get(j));
                }
            }
            
            //Insert document length
            DB.insertDocLength(coll2, no_hadis, output.size());
        }
    }
    
    public static void main (String args[]) {
        Indeks I = new Indeks();
        String imam = "bukhari";
        I.buatIndeks(imam);
    }
    
}