package searchhadis;

import java.util.ArrayList;

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
        
        jumlah = H.getJumlahHadis(imam);
        for (int i=0;i<jumlah;i++) {
            System.out.println(i);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.prosesTeks(indo);
            
            //Insert term ke DB
            for (int j=0;j<output.size();j++) {
                if (DB.find(output.get(j))) {
                    //Update existing
                    if (DB.findId(output.get(j), no_hadis)) {
                        DB.addId(no_hadis, output.get(j));
                    } else {
                        DB.update(no_hadis, output.get(j));
                    }
                } else {
                    //Insert new
                    DB.insert(no_hadis, output.get(j));
                }
            }
            
            //Insert document length
            DB.insertDocLength(no_hadis, output.size());
        }
    }
    
    public static void main (String args[]) {
        Indeks I = new Indeks();
        String imam = "bukhari";
        I.buatIndeks(imam);
    }
    
}