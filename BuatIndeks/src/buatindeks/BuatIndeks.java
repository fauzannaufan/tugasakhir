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
        long[] time = new long[10];
        
        jumlah = H.getJumlahHadis(imam);
        int x = 0;
        int y = 0;
        
        for (int i=3150;i<3200;i++) {
            System.out.println(i+"/"+jumlah);
            
            //Ambil term-term dari hadis
            teks = H.getTeksHadis(imam, i);
            no_hadis = teks.substring(0,teks.indexOf("<"));
            indo = teks.substring(teks.indexOf("<")+1);
            ArrayList<String> output = PT.prosesTeks(indo);
            
            //Insert term ke DB
            for (int j=0;j<output.size();j++) {
                long start = System.currentTimeMillis();
                boolean a = DB.find(output.get(j));
                long int1 = System.currentTimeMillis();
                time[0] += (int1-start);
                if (a) {
                    x++;
                    //Update existing
                    boolean b = DB.findId(output.get(j), no_hadis);
                    long int2 = System.currentTimeMillis();
                    time[1] += (int2-int1);
                    
                    if (b) {
                        DB.addId(no_hadis, output.get(j));
                        long int3 = System.currentTimeMillis();
                        time[3] += (int3-int2);
                    } else {
                        DB.update(no_hadis, output.get(j));
                        long int4 = System.currentTimeMillis();
                        time[4] += (int4-int2);
                    }
                } else {
                    y++;
                    //Insert new
                    DB.insert(no_hadis, output.get(j));
                    long int3 = System.currentTimeMillis();
                    time[2] += (int3-int1);
                }
            }
            
            //Insert document length
            DB.insertDocLength(no_hadis, output.size());
        }
        DB.closeConnection();
        
        System.out.println("Find term : "+time[0]);
        System.out.println("Find id in term : "+time[1]);
        System.out.println("Insert new : "+time[2]);
        System.out.println("Add Id : "+time[3]);
        System.out.println("Update df & Add id : "+time[4]);
        
        System.out.println("x : "+x);
        System.out.println("y : "+y);
    }
    
    public static void main (String args[]) {
        BuatIndeks I = new BuatIndeks();
        String imam = "tirmidzi";
        I.buatIndeks(imam);
    }
    
}