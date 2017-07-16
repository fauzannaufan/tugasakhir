package searchhadis;

import java.util.ArrayList;

/**
 *
 * @author M. Fauzan Naufan
 */
public class SearchHadis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Inisialisasi variabel
        ProsesTeks PT = new ProsesTeks();
        Hadis H;
        
        String kueri = "shalat subuh berjamaah";
        ArrayList<String> p_kueri = PT.preproses(kueri);
    }
    
}
