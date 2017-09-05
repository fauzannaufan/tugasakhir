package hadis;

import backend.Database;
import java.util.ArrayList;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author M. Fauzan Naufan
 */
public class DatabaseHadis extends Database {
    
    public ArrayList<String> getHadis(String id) {
        Hadis H = new Hadis();
        ArrayList<String> arr = new ArrayList<>();
        
        Document doc = coll_hadis.find(new Document("imam", H.getImam(id))
                .append("haditsId", H.getIdHadis(id)))
                .projection(new Document("_id", 0)
                        .append("indo", 1)
                        .append("imam", 1)
                        .append("related", 1)
                        .append("haditsId", 1)).first();

        String imam = "";
        ArrayList<Document> related = new ArrayList<>();

        try {
            imam = doc.get("imam").toString();
            related = (ArrayList<Document>) doc.get("related");
        } catch (NullPointerException e) {
            System.out.println("NULL!");
        }
        
        JSONArray arr_baru = new JSONArray();
        for (int i = 0; i < related.size(); i++) {
            Document doc2 = related.get(i);
            String key = H.setNomorHadis(doc2.get("imam").toString(), doc2.get("haditsId").toString());
            doc2.put("key", key);
            arr_baru.add(doc2);
        }
        JSONObject obj = new JSONObject();
        obj.put("related", arr_baru);

        arr.add(imam);
        arr.add(doc.get("haditsId").toString());
        arr.add(doc.get("indo").toString());
        arr.add(obj.toJSONString());

        return arr;
    }

}
