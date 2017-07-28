package buatindeks;

import java.util.ArrayList;

/**
 *
 * @author M. Fauzan Naufan
 */
public class BuatIndeks {
    
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
        for (int i=2330;i<jumlah;i++) {
            System.out.println(i+"/"+jumlah);
            
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
        BuatIndeks I = new BuatIndeks();
        String imam = "darimi";
        I.buatIndeks(imam);
    }
    
}