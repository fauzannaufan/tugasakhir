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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final MongoCollection<Document> coll_history;
    private final MongoCollection<Document> coll_hadis;
    private final MongoCollection<Document> coll_kitab;
    private final MongoCollection<Document> coll_bab;

    private MongoClient client;

    public Database() {
        coll_indeks = connect("indeks");
        coll_okapi = connect("okapi");
        coll_dl = connect("doclength");
        coll_bim = connect("bim");
        coll_history = connect("history");
        coll_hadis = connect("hadis");
        coll_kitab = connect("kitab");
        coll_bab = connect("bab");
    }

    public void closeConnection() {
        client.close();
    }

    private MongoCollection<Document> connect(String nama) {

        MongoCollection<Document> coll = null;

        try {
            client = new MongoClient();
            MongoDatabase db = client.getDatabase("carihadis");
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
        long L = coll_indeks.count(new Document("nama", term).append("id", id));
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

    public Document getProbBIM(ArrayList<String> terms, String sid) {
        Document doc = coll_bim.find(new Document("term", terms)
                .append("sid", sid))
                .sort(new Document("date", -1))
                .projection(new Document("_id", 0)
                        .append("pt", 1).append("ut", 1))
                .first();
        
        return doc;
    }

    public void updateBIM(ArrayList<String> terms, ArrayList<Double> pt, ArrayList<Double> ut, String sid) {
        Document doc = new Document("term", terms).append("sid", sid).append("pt", pt).append("ut", ut).append("date", new Date());

        coll_bim.insertOne(doc);
    }

    public void updateOkapi(ArrayList<String> terms, ArrayList<Double> rf, String sid) {
        Document doc = new Document("term", terms).append("sid", sid).append("rf", rf).append("date", new Date());

        coll_okapi.insertOne(doc);
    }

    public ArrayList<Double> getRfOkapi(ArrayList<String> terms, String sid) {
        Document doc = coll_okapi.find(new Document("term", terms)
                .append("sid", sid))
                .sort(new Document("date", -1))
                .projection(new Document("_id", 0)
                        .append("rf", 1))
                .first();
        
        if (doc != null) {
            ArrayList<Double> rf = (ArrayList<Double>) doc.get("rf");
            return rf;
        } else {
            return null;
        }
    }

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

    public void addRelevantDocs(String skema, String sid, ArrayList<String> terms, ArrayList<String> ids, ArrayList<Double> pt, ArrayList<Double> ut) {
        Document doc;
        List<String> ids2 = new ArrayList<>();
        if (ids.size() >= 10) {
            ids2 = ids.subList(0, 10);
        } else {
            ids2.addAll(ids);
        }
        if (pt.isEmpty()) {
            doc = new Document("term", terms).append("SID", sid).append("skema", skema).append("VNR", ids2);
        } else {
            doc = new Document("term", terms).append("SID", sid).append("skema", skema).append("VNR", ids2).append("pt_lama", pt).append("ut_lama", ut);
        }

        long L = coll_history.count(new Document("term", terms).append("skema", skema).append("SID", sid));
        if (L == 0) {
            coll_history.insertOne(doc);
        } else {
            coll_history.replaceOne(new Document("term", terms), doc);
        }

    }

    public void setRelevant(String skema, ArrayList<String> terms, String sid, String id) {

        Document doc1 = new Document("$push", new Document("VR", id));
        Document doc2 = new Document("$pull", new Document("VNR", id));

        coll_history.updateOne(new Document("term", terms).append("SID", sid).append("skema", skema), doc1);
        coll_history.updateOne(new Document("term", terms).append("SID", sid).append("skema", skema), doc2);
    }

    public ArrayList<String> getVR(String skema, String sid, ArrayList<String> terms) {
        ArrayList<Document> arrays = coll_history.find(new Document("term", terms)
                .append("SID", sid)
                .append("skema", skema))
                .projection(new Document("VR", 1)
                        .append("_id", 0))
                .into(new ArrayList<Document>());

        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("VR");
            if (ids != null) {
                return ids;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> getVNR(String skema, String sid, ArrayList<String> terms) {
        ArrayList<Document> arrays = coll_history.find(new Document("term", terms)
                .append("SID", sid)
                .append("skema", skema))
                .projection(new Document("VNR", 1)
                        .append("_id", 0))
                .into(new ArrayList<Document>());

        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("VNR");
            if (ids != null) {
                return ids;
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Double> getPt(ArrayList<String> terms, String sid) {
        ArrayList<Document> arr = coll_history.find(new Document("term", terms)
                .append("SID", sid)
                .append("skema", "bim"))
                .projection(new Document("_id", 0)
                        .append("pt_lama", 1))
                .into(new ArrayList<Document>());

        if (arr.size() > 0) {
            ArrayList<Double> pt = (ArrayList<Double>) arr.get(0).get("pt_lama");
            return pt;
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<Double> getUt(ArrayList<String> terms, String sid) {
        ArrayList<Document> arr = coll_history.find(new Document("term", terms)
                .append("SID", sid)
                .append("skema", "bim"))
                .projection(new Document("_id", 0)
                        .append("ut_lama", 1))
                .into(new ArrayList<Document>());

        if (arr.size() > 0) {
            ArrayList<Double> ut = (ArrayList<Double>) arr.get(0).get("ut_lama");
            return ut;
        } else {
            return new ArrayList<>();
        }
    }
}
