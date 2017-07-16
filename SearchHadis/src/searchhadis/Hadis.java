package searchhadis;

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
        
        String teks = null;
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/"+imam+".json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            
            JSONObject obj2 = (JSONObject)arr.get(idx);
            String indo = obj2.get("indo").toString();
            String no_hadis = obj2.get("haditsId").toString();
            teks = no_hadis+"<"+indo;
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(Hadis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return teks;
    }
    
}
