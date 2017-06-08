package bacahadis;

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
public class BacaHadis {

    public void getHadisById(JSONArray arr, int id) {
        boolean found = false;
        int i = 0;
        JSONObject obj2 = (JSONObject)arr.get(i);
        while (!found && i < arr.size()) {
            obj2 = (JSONObject)arr.get(i);
            if (obj2.get("haditsId").equals(id+"")) {
                found = true;
            } else {
                i++;
            }
        }
        System.out.println(obj2);
    }
    
    public void searchHadis(JSONArray arr, String keyword) {
        boolean found = false;
        JSONObject obj2;
        for (int i=0;i<arr.size();i++) {
            obj2 = (JSONObject)arr.get(i);
            if (obj2.get("indo").toString().contains(keyword)) {
                System.out.println("> "+obj2.get("haditsId"));
                System.out.println(obj2.get("indo"));
            }
        }
    }
    
    public void searchRelatedHadis(JSONArray arr, int id) {
        boolean found2;
        JSONObject obj2;
        for (int i=0;i<arr.size();i++) {
            found2 = false;
            obj2 = (JSONObject)arr.get(i);
            JSONArray arr2 = (JSONArray) obj2.get("related");
            for (int j=0;j<arr2.size();j++) {
                JSONObject obj3 = (JSONObject) arr2.get(j);
                if (obj3.get("imam").equals("bukhari")) {
                    if (obj3.get("haditsId").equals(id+"")) {
                        found2 = true;
                    }
                }
            }
            if (found2) {
                System.out.println("> "+obj2.get("haditsId"));
                //System.out.println(obj2.get("related"));
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int related = 0;
        int unrelated = 0;
        
        try {
            JSONParser parser = new JSONParser();
            String filename = "E:/Semester 8/TA/TA 1/hadits-data/data/bukhari.json";
            
            Object obj = parser.parse(new FileReader(filename));
            JSONArray arr = (JSONArray) obj;
            new BacaHadis().searchHadis(arr, "wahyu");
            //new BacaHadis().getHadisById(arr, 14);
            //new BacaHadis().searchRelatedHadis(arr, 4613);
            /*
            JSONObject obj2 = (JSONObject)arr.get(0);
            JSONArray arr2 = (JSONArray)obj2.get("related");
            System.out.println(arr2);*/
            
            /*System.out.println("Panjang array : "+arr.size());
            for (int i=0;i<arr.size();i++) {
                JSONObject obj2 = (JSONObject)arr.get(i);
                JSONArray arr2 = (JSONArray)obj2.get("related");
                int size = arr2.size();
                if (size > 0) {
                    related += 1;
                } else {
                    System.out.println(i);
                    unrelated += 1;
                }
            }
            
            System.out.println("Related = "+related);
            System.out.println("Unrelated = "+unrelated);*/
            
        } catch (IOException | ParseException ex) {
            Logger.getLogger(BacaHadis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
