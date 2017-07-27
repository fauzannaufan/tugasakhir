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
        
        String teks;
        String indo;
        String no_hadis;
        int jumlah;
        
        MongoCollection<Document> indeks = DB.connect("indeks");
        MongoCollection<Document> doclength = DB.connect("doclength");
        
        jumlah = H.getJumlahHadis(imam);
        for (int i=2330;i<jumlah;i++) {
            System.out.println(i+"/"+jumlah);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.prosesTeks(indo);
            
            //Insert term ke DB
            for (int j=0;j<output.size();j++) {
                if (DB.find(indeks, output.get(j))) {
                    //Update existing
                    if (DB.findId(indeks, output.get(j), no_hadis)) {
                        DB.addId(indeks, no_hadis, output.get(j));
                    } else {
                        DB.update(indeks, no_hadis, output.get(j));
                    }
                } else {
                    //Insert new
                    DB.insert(indeks, no_hadis, output.get(j));
                }
            }
            
            //Insert document length
            DB.insertDocLength(doclength, no_hadis, output.size());
        }
    }
    
    public static void main (String args[]) {
        Indeks I = new Indeks();
        String imam = "darimi";
        I.buatIndeks(imam);
    }
    
}