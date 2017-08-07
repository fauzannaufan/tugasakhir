package hadis;

import backend.Database;
import backend.Hadis;
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
                        .append("arab", 1)
                        .append("kitabId", 1)
                        .append("babId", 1)
                        .append("related", 1)
                        .append("haditsId", 1)).first();

        String imam = "";
        String kitabId = "";
        String babId = "";
        ArrayList<Document> related = new ArrayList<>();

        try {
            imam = doc.get("imam").toString();
            kitabId = doc.get("kitabId").toString();
            babId = doc.get("babId").toString();
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
        arr.add(getKitabHadis(imam, kitabId));
        arr.add(getBabHadis(imam, babId));
        arr.add(doc.get("arab").toString());
        arr.add(obj.toJSONString());

        return arr;
    }

    public String getKitabHadis(String imam, String kitabId) {
        Document doc = coll_kitab.find(new Document("imam", imam)
                .append("kitabId", kitabId))
                .projection(new Document("_id", 0)
                        .append("judul", 1)).first();

        return doc.get("judul").toString();
    }

    public String getBabHadis(String imam, String babId) {
        Document doc = coll_bab.find(new Document("imam", imam)
                .append("babId", babId))
                .projection(new Document("_id", 0)
                        .append("judul", 1)).first();

        return doc.get("judul").toString();
    }

}
