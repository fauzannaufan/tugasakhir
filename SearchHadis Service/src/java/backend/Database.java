package backend;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Database {

    private final MongoCollection<Document> coll_indeks;
    private final MongoCollection<Document> coll_okapi;
    private final MongoCollection<Document> coll_dl;
    private final MongoCollection<Document> coll_bim;
    private final MongoCollection<Document> coll_coba;
    private final MongoCollection<Document> coll_kitab;
    private final MongoCollection<Document> coll_bab;

    public Database() {
        coll_indeks = connect("indeks");
        coll_okapi = connect("okapi");
        coll_dl = connect("doclength");
        coll_bim = connect("bim");
        coll_coba = connect("coba");
        coll_kitab = connect("kitab");
        coll_bab = connect("bab");
    }

    private MongoCollection<Document> connect(String nama) {

        MongoCollection<Document> coll = null;

        try {
            MongoClient client = new MongoClient();
            MongoDatabase db = client.getDatabase("test");
            coll = db.getCollection(nama);
            System.out.println("Connected");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return coll;
    }

    public boolean find(String term) {
        long L = coll_indeks.count(new Document("nama", term));
        return L != 0;
    }

    public boolean findId(String term, String id) {
        long L = coll_indeks.count(Document.parse("{\"nama\" : \"" + term + "\", \"id\" : \"" + id + "\"}"));
        return L != 0;
    }

    public boolean findOkapi(ArrayList<String> terms) {
        long L = coll_okapi.count(new Document("term", terms));
        return L != 0;
    }

    public void insertDocLength(String no_hadis, int length) {
        Document doc = new Document("id", no_hadis)
                .append("length", length);

        coll_dl.insertOne(doc);
    }

    public void insert(String no_hadis, String term) {
        Document doc = new Document("nama", term)
                .append("df", 1)
                .append("id", Arrays.asList(no_hadis));

        coll_indeks.insertOne(doc);
    }

    public void update(String no_hadis, String term) {
        Document doc = new Document().append("$inc",
                new Document().append("df", 1))
                .append("$push",
                        new Document().append("id", no_hadis));

        coll_indeks.updateOne(new Document().append("nama", term), doc);
    }

    public void addId(String no_hadis, String term) {
        Document doc = new Document().append("$push",
                new Document().append("id", no_hadis));

        coll_indeks.updateOne(new Document().append("nama", term), doc);
    }

    public int getDf(String term) {
        Document df = coll_indeks.find(new Document("nama", term))
                .projection(new Document("df", 1)
                        .append("_id", 0)).first();

        return Integer.parseInt(df.get("df").toString());
    }

    public ArrayList<String> getIds(String term) {
        ArrayList<Document> arrays = coll_indeks.find(new Document("nama", term))
                .projection(new Document("id", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());

        ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("id");
        HashSet<String> hs = new HashSet<>();
        hs.addAll(ids);
        ids.clear();
        ids.addAll(hs);

        return ids;
    }

    public int getN() {
        long L = coll_dl.count();
        return Math.toIntExact(L);
    }

    public ArrayList<String> getAllIds(String term) {
        ArrayList<Document> arrays = coll_indeks.find(new Document("nama", term))
                .projection(new Document("id", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());

        ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("id");

        return ids;
    }

    public Map<String, Integer> getAllDocLength() {
        Map<String, Integer> map = new HashMap<>();

        FindIterable<Document> doc = coll_dl.find(new Document())
                .projection(new Document("_id", 0));

        MongoCursor<Document> cur = doc.iterator();
        while (cur.hasNext()) {
            Document d = cur.next();
            map.put(d.get("id").toString(), Integer.parseInt(d.get("length").toString()));
        }

        return map;
    }

    public double getDocAvgLength() {
        Document avg = coll_dl.aggregate(Arrays.asList(group("null", avg("avgLength", "$length")))).first();

        return Double.parseDouble(avg.get("avgLength").toString());
    }

    public Document getProbBIM(ArrayList<String> terms) {
        ArrayList<Document> arr = coll_bim.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                        .append("pt", 1).append("ut", 1)).into(new ArrayList<Document>());

        if (arr.size() > 0) {
            Document doc = arr.get(0);
            return doc;
        } else {
            return null;
        }
    }

    public void updateBIM(ArrayList<String> terms, ArrayList<Double> pt, ArrayList<Double> ut) {
        Document doc = new Document("$set", new Document("pt", pt).append("ut", ut));

        coll_bim.updateOne(new Document("term", terms), doc);
    }

    public void updateOkapi(ArrayList<String> terms, ArrayList<Double> rf) {
        Document doc = new Document("$set", new Document("rf", rf));

        coll_okapi.updateOne(new Document("term", terms), doc);
    }

    public ArrayList<Double> getRfOkapi(ArrayList<String> terms) {
        ArrayList<Document> arr = coll_okapi.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                        .append("rf", 1)).into(new ArrayList<Document>());

        if (arr.size() > 0) {
            ArrayList<Double> rf = (ArrayList<Double>) arr.get(0).get("rf");
            return rf;
        } else {
            return null;
        }
    }

    public ArrayList<String> getHadis(String id) {
        Hadis H = new Hadis();

        Document doc = coll_coba.find(new Document("imam", H.getImam(id))
                .append("haditsId", H.getIdHadis(id)))
                .projection(new Document("_id", 0)
                        .append("indo", 1)
                        .append("imam", 1)
                        .append("arab", 1)
                        .append("kitabId", 1)
                        .append("babId", 1)
                        .append("related", 1)
                        .append("haditsId", 1)).first();

        String imam = doc.get("imam").toString();
        String kitabId = doc.get("kitabId").toString();
        String babId = doc.get("babId").toString();
        ArrayList<Document> related = (ArrayList<Document>) doc.get("related");
        JSONArray arr_baru = new JSONArray();
        for (int i = 0; i < related.size(); i++) {
            Document doc2 = related.get(i);
            String key = H.setNomorHadis(doc2.get("imam").toString(), doc2.get("haditsId").toString());
            doc2.put("key", key);
            arr_baru.add(doc2);
        }
        JSONObject obj = new JSONObject();
        obj.put("related", arr_baru);

        ArrayList<String> arr = new ArrayList<>();
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

    public void addRelevantDocs(String skema, ArrayList<String> terms, ArrayList<String> ids, ArrayList<Double> pt, ArrayList<Double> ut) {
        Document doc;
        if (pt.isEmpty()) {
            doc = new Document("term", terms).append("VNR", ids);
        } else {
            doc = new Document("term", terms).append("VNR", ids).append("pt_lama", pt).append("ut_lama", ut);
        }

        if (skema.equals("bim")) {
            long L = coll_bim.count(new Document("term", terms));
            if (L == 0) {
                coll_bim.insertOne(doc);
            } else {
                coll_bim.replaceOne(new Document("term", terms), doc);
            }
        } else if (skema.equals("okapi")) {
            long L = coll_okapi.count(new Document("term", terms));
            if (L == 0) {
                coll_okapi.insertOne(doc);
            } else {
                coll_okapi.replaceOne(new Document("term", terms), doc);
            }
        }

    }

    public void setRelevant(String skema, ArrayList<String> terms, String id) {

        Document doc1 = new Document("$push", new Document("VR", id));
        Document doc2 = new Document("$pull", new Document("VNR", id));

        if (skema.equals("bim")) {
            coll_bim.updateOne(new Document().append("term", terms), doc1);
            coll_bim.updateOne(new Document().append("term", terms), doc2);
        } else if (skema.equals("okapi")) {
            coll_okapi.updateOne(new Document().append("term", terms), doc1);
            coll_okapi.updateOne(new Document().append("term", terms), doc2);
        }
    }

    public ArrayList<String> getVR(String skema, ArrayList<String> terms) {
        ArrayList<Document> arrays = new ArrayList<>();
        if (skema.equals("bim")) {
            arrays = coll_bim.find(new Document("term", terms))
                .projection(new Document("VR", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());
        } else if (skema.equals("okapi")) {
            arrays = coll_okapi.find(new Document("term", terms))
                .projection(new Document("VR", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());
        }

        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("VR");
            return ids;
        } else {
            return null;
        }
    }

    public ArrayList<String> getVNR(String skema, ArrayList<String> terms) {
        ArrayList<Document> arrays = new ArrayList<>();
        if (skema.equals("bim")) {
            arrays = coll_bim.find(new Document("term", terms))
                .projection(new Document("VNR", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());
        } else if (skema.equals("okapi")) {
            arrays = coll_okapi.find(new Document("term", terms))
                .projection(new Document("VNR", 1)
                        .append("_id", 0)).into(new ArrayList<Document>());
        }

        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("VNR");
            return ids;
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Double> getPt(ArrayList<String> terms) {
        ArrayList<Document> arr = coll_bim.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                        .append("pt_lama", 1)).into(new ArrayList<Document>());

        if (arr.size() > 0) {
            ArrayList<Double> pt = (ArrayList<Double>) arr.get(0).get("pt_lama");
            return pt;
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Double> getUt(ArrayList<String> terms) {
        ArrayList<Document> arr = coll_bim.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                        .append("ut_lama", 1)).into(new ArrayList<Document>());

        if (arr.size() > 0) {
            ArrayList<Double> ut = (ArrayList<Double>) arr.get(0).get("ut_lama");
            return ut;
        } else {
            return new ArrayList<>();
        }
    }
}
