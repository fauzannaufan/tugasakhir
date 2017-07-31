package buatindeks;

import java.util.ArrayList;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Hadis {
    
    private ArrayList<Document> data_hadis;
    
    public Hadis() {}
    
    public Hadis(String imam) {
        Database DB = new Database();
        data_hadis = DB.getDataHadis(imam);
        DB.closeConnection();
    }
    
    public String setNomorHadis(String imam, String nomor) {
        String no_hadis = "";
        switch(imam) {
            case "abudaud" :
                no_hadis = "AB"; break;
            case "ahmad" :
                no_hadis = "AH"; break;
            case "bukhari" :
                no_hadis = "B"; break;
            case "darimi" :
                no_hadis = "D"; break;
            case "ibnumajah" :
                no_hadis = "I"; break;
            case "malik" :
                no_hadis = "MA"; break;
            case "muslim" :
                no_hadis = "MU"; break;
            case "nasai" :
                no_hadis = "N"; break;
            case "tirmidzi" :
                no_hadis = "T"; break;
        }
        return no_hadis+nomor;
    }
    
    public int getJumlahHadis(String imam) {
        return data_hadis.size();
    }
    
    public String getTeksHadis(String imam, int idx) {
        
        String teks;
        
        String indo = data_hadis.get(idx).get("indo").toString();
        String no_hadis = setNomorHadis(imam, data_hadis.get(idx).get("haditsId").toString());
        teks = no_hadis+"<"+indo;
        
        return teks;
    }
    
    //Hanya untuk testing modul
    /*public static void main (String args[]) {
        int idx_error = 2330;
        //for (int i=idx_error;i<idx;i++) {
            String s = new Hadis().getTeksHadis("darimi", idx_error);
        
            //String s = new Hadis().setNomorHadis("bukhari", "501");
            System.out.println(s);
        //}
    }*/
    
}