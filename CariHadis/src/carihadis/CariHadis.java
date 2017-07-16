package carihadis;

import java.util.ArrayList;

/**
 *
 * @author M. Fauzan Naufan
 */
public class CariHadis {

    public void cariTerm(String term) {
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProsesKueri PK = new ProsesKueri();
        ArrayList<String> p_query = PK.Proses();
        for (int i=0;i<p_query.size();i++) {
            new CariHadis().cariTerm(p_query.get(i));
        }
    }
    
}
