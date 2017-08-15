package hadis;

import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Hadis {
    
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
    
    public String getIdHadis(String id) {
        int i = 0;
        while (i < id.length() && !Character.isDigit(id.charAt(i))) i++;
        return id.substring(i);
    }
    
    public String getImam(String id) {
        int i = 0;
        while (i < id.length() && !Character.isDigit(id.charAt(i))) i++;
        
        String s = id.substring(0,i);
        String imam = "";
        switch(s) {
            case "AB" :
                imam = "abudaud"; break;
            case "AH" :
                imam = "ahmad"; break;
            case "B" :
                imam = "bukhari"; break;
            case "D" :
                imam = "darimi"; break;
            case "I" :
                imam = "ibnumajah"; break;
            case "MA" :
                imam = "malik"; break;
            case "MU" :
                imam = "muslim"; break;
            case "N" :
                imam = "nasai"; break;
            case "T" :
                imam = "tirmidzi"; break;
        }
        return imam;
    }
    
    public int getJumlahHadis(String imam) {
        int size = -1;
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            size = arr.size();
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Hadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return size;
    }
    
    public String getTeksHadis(String imam, int idx) {
        
        String teks = "";
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            
            JSONObject obj2 = (JSONObject)arr.get(idx);
            String indo = obj2.get("indo").toString();
            String no_hadis = setNomorHadis(imam,obj2.get("haditsId").toString());
            teks = no_hadis+"<"+indo;
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Hadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return teks;
    }
    
}