package evaluation;

import backend.ProsesTeks;
import backend.SearchHadis;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import search.InitDB;
import static search.InitDB.*;

/**
 *
 * @author M. Fauzan Naufan
 */
public class GroundTruth {
    
    HashSet<String> hs;
    ArrayList<String> ids;
    
    public void prosesArray(JSONArray arr) {
        for (int i=0;i<arr.size();i++) {
            JSONObject obj = (JSONObject) arr.get(i);
            hs.add(obj.get("key").toString());
            
            JSONParser parser = new JSONParser();
            JSONObject related = new JSONObject();
            try {
                related = (JSONObject)parser.parse(obj.get("related").toString());
            } catch (ParseException ex) {
                Logger.getLogger(GroundTruth.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONArray rel = (JSONArray) related.get("related");
            for (int j=0;j<rel.size();j++) {
                JSONObject obj2 = (JSONObject)rel.get(j);
                if (!obj2.get("imam").equals("ahmad")) {
                    hs.add(obj2.get("key").toString());
                }
            }
        }
    }
    
    public void createGT(String kueri) {
        SearchHadis SH = new SearchHadis();
        ProsesTeks PT = new ProsesTeks();
        hs = new HashSet<>();
        ids = new ArrayList<>();
        
        JSONObject bim = SH.searchBIM(kueri, "groundTruth", true);
        JSONObject okapi = SH.searchOkapi(kueri, "groundTruth", true);
        
        JSONArray arr1 = (JSONArray) bim.get("hasil");
        JSONArray arr2 = (JSONArray) okapi.get("hasil");
        prosesArray(arr1);
        prosesArray(arr2);
        
        ids.addAll(hs);
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        DE.insertGT(p_kueri, ids);
    }
    
    public static void main (String args[]) {
        GroundTruth GT = new GroundTruth();
        new InitDB().InitDB();
        String kueri = "pintu surga dan neraka";
        GT.createGT(kueri);
    }
}
