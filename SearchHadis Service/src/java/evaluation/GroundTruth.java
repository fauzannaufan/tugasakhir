package evaluation;

import backend.ProsesTeks;
import backend.SearchHadis;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static search.InitDB.DE;

/**
 *
 * @author novan
 */
public class GroundTruth {
    
    HashSet<String> hs;
    ArrayList<String> ids;

    private void prosesArray(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);

            JSONParser parser = new JSONParser();
            JSONObject related = new JSONObject();
            try {
                related = (JSONObject) parser.parse(obj.get("related").toString());
            } catch (ParseException ex) {
                
            }
            JSONArray rel = (JSONArray) related.get("related");
            for (int j = 0; j < rel.size(); j++) {
                JSONObject obj2 = (JSONObject) rel.get(j);
                if (!obj2.get("imam").equals("ahmad") && !obj2.get("imam").equals("bukhari")) {
                    hs.add(obj2.get("key").toString());
                }
            }
        }
    }
    
    public void createGT(String kueri) {
        ProsesTeks PT = new ProsesTeks();
        SearchHadis SH = new SearchHadis();
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        hs = new HashSet<>();
        ids = new ArrayList<>();

        //Cek apakah sudah ada atau belum
        boolean a = DE.checkGT(p_kueri);
        if (!DE.checkGT(p_kueri)) {
            JSONObject vsm = SH.searchVSM(kueri, "groundTruth", true);
            JSONObject bim = SH.searchBIM(kueri, "groundTruth", true);
            JSONObject okapi = SH.searchOkapi(kueri, "groundTruth", true);

            JSONArray arr1 = (JSONArray) bim.get("hasil");
            JSONArray arr2 = (JSONArray) okapi.get("hasil");
            JSONArray arr3 = (JSONArray) vsm.get("hasil");
            
            prosesArray(arr1);
            prosesArray(arr2);
            prosesArray(arr3);

            ids.addAll(hs);
            DE.insertGT(p_kueri, ids);
        }
    }
}
