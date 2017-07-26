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

/**
 *
 * @author M. Fauzan Naufan
 */
public class Database {
    
    public MongoCollection<Document> connect(String nama) {
        
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
        MongoCollection<Document> coll = connect("indeks");
        long L = coll.count(new Document("nama", term));
        return L != 0;
    }
    
    public boolean findId(String term, String id) {
        MongoCollection<Document> coll = connect("indeks");
        long L = coll.count(Document.parse("{\"nama\" : \""+term+"\", \"id\" : \""+id+"\"}"));
        return L != 0;
    }
    
    public boolean findOkapi(ArrayList<String> terms) {
        MongoCollection<Document> coll = connect("okapi");
        long L = coll.count(new Document("term", terms));
        return L != 0;
    }
    
    public void insertDocLength(String no_hadis, int length) {
        MongoCollection<Document> coll = connect("doclength");
        Document doc = new Document("id", no_hadis)
                .append("length", length);
        
        coll.insertOne(doc);
    }
    
    public void insert(String no_hadis, String term) {
        MongoCollection<Document> coll = connect("indeks");
        Document doc = new Document("nama", term)
                .append("df", 1)
                .append("id", Arrays.asList(no_hadis));
        
        coll.insertOne(doc);
    }
    
    public void update(String no_hadis, String term) {
        MongoCollection<Document> coll = connect("indeks");
        
        //Update document frequency
        Document doc = new Document().append("$inc",
                new Document().append("df", 1))
                .append("$push",
                        new Document().append("id", no_hadis));
        
        coll.updateOne(new Document().append("nama", term), doc);
    }
    
    public void addId(String no_hadis, String term) {
        MongoCollection<Document> coll = connect("indeks");
        
        //Update document frequency
        Document doc = new Document().append("$push",
                new Document().append("id", no_hadis));
        
        coll.updateOne(new Document().append("nama", term), doc);
    }
    
    public int getDf(String term) {
        MongoCollection<Document> coll = connect("indeks");
        Document df = coll.find(new Document("nama", term))
                .projection(new Document("df", 1)
                .append("_id", 0)).first();
        
        return Integer.parseInt(df.get("df").toString());
    }
    
    public ArrayList<String> getIds(String term) {
        MongoCollection<Document> coll = connect("indeks");
        ArrayList<Document> arrays = coll.find(new Document("nama", term))
                .projection(new Document("id", 1)
                .append("_id", 0)).into(new ArrayList<Document>());
        
        ArrayList<String> ids = (ArrayList<String>)arrays.get(0).get("id");
        HashSet<String> hs = new HashSet<>();
        hs.addAll(ids);
        ids.clear();
        ids.addAll(hs);
        
        return ids;
    }
    
    public int getN() {
        MongoCollection<Document> coll = connect("doclength");
        long L = coll.count();
        return Math.toIntExact(L);
    }
    
    public ArrayList<String> getAllIds(String term) {
        MongoCollection<Document> coll = connect("indeks");
        ArrayList<Document> arrays = coll.find(new Document("nama", term))
                .projection(new Document("id", 1)
                .append("_id", 0)).into(new ArrayList<Document>());
        
        ArrayList<String> ids = (ArrayList<String>)arrays.get(0).get("id");
        
        return ids;
    }
    
    public Map<String, Integer> getAllDocLength() {
        MongoCollection<Document> coll = connect("doclength");
        Map<String, Integer> map = new HashMap<>();
        
        FindIterable<Document> doc = coll.find(new Document())
                .projection(new Document("_id", 0));
        
        MongoCursor<Document> cur = doc.iterator();
        while(cur.hasNext()) {
            Document d = cur.next();
            map.put(d.get("id").toString(), Integer.parseInt(d.get("length").toString()));
        }
        
        return map;
    }
    
    public double getDocAvgLength() {
        MongoCollection<Document> coll = connect("doclength");
        
        Document avg = coll.aggregate(Arrays.asList(group("null", avg("avgLength", "$length")))).first();
        
        return Double.parseDouble(avg.get("avgLength").toString());
    }
    
    public Document getProbBIM(ArrayList<String> terms) {
        MongoCollection<Document> coll = connect("bim");
        
        ArrayList<Document> arr = coll.find(new Document("term", terms))
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
        MongoCollection<Document> coll = connect("bim");
        
        Document doc = new Document("$set", new Document("pt", pt).append("ut", ut));
        
        coll.updateOne(new Document("term", terms), doc);
    }
    
    public void updateOkapi(ArrayList<String> terms, ArrayList<Double> rf) {
        MongoCollection<Document> coll = connect("okapi");
        
        Document doc = new Document("$set", new Document("rf", rf));
        
        coll.updateOne(new Document("term", terms), doc);
    }
    
    public ArrayList<Double> getRfOkapi(ArrayList<String> terms) {
        MongoCollection<Document> coll = connect("okapi");
        
        ArrayList<Document> arr = coll.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                .append("rf", 1)).into(new ArrayList<Document>());
        
        if (arr.size() > 0) {
            ArrayList<Double> rf = (ArrayList<Double>)arr.get(0).get("rf");
            return rf;
        } else {
            return null;
        }
    }
    
    public ArrayList<String> getHadis(String id) {
        MongoCollection<Document> coll = connect("coba");
        Hadis H = new Hadis();
        
        Document doc = coll.find(new Document("imam", H.getImam(id))
                .append("haditsId", H.getIdHadis(id)))
                .projection(new Document("_id", 0)
                        .append("indo", 1)
                        .append("imam", 1)
                        .append("kitabId", 1)
                        .append("babId", 1)
                        .append("haditsId",1)).first();
        
        String imam = doc.get("imam").toString();
        String kitabId = doc.get("kitabId").toString();
        String babId = doc.get("babId").toString();
        
        ArrayList<String> arr = new ArrayList<>();
        arr.add(imam);
        arr.add(doc.get("haditsId").toString());
        arr.add(doc.get("indo").toString());
        arr.add(getKitabHadis(imam, kitabId));
        arr.add(getBabHadis(imam, babId));
        
        return arr;
    }
    
    public String getKitabHadis(String imam, String kitabId) {
        MongoCollection<Document> coll = connect("kitab");
        
        Document doc = coll.find(new Document("imam", imam)
                .append("kitabId", kitabId))
                .projection(new Document("_id", 0)
                        .append("judul", 1)).first();
        
        return doc.get("judul").toString();
    }
    
    public String getBabHadis(String imam, String babId) {
        MongoCollection<Document> coll = connect("bab");
        
        Document doc = coll.find(new Document("imam", imam)
                .append("babId", babId))
                .projection(new Document("_id", 0)
                        .append("judul", 1)).first();
        
        return doc.get("judul").toString();
    }
    
    public void addRelevantDocs(String skema, ArrayList<String> terms, ArrayList<String> ids, ArrayList<Double> pt, ArrayList<Double> ut) {
        MongoCollection<Document> coll = connect(skema);
        
        Document doc;
        if (pt.isEmpty()) {
            doc = new Document("term", terms).append("VNR", ids);
        } else {
            doc = new Document("term", terms).append("VNR", ids).append("pt_lama", pt).append("ut_lama", ut);
        }
        
        long L = coll.count(new Document("term", terms));
        if (L == 0) {
            coll.insertOne(doc);
        } else {
            coll.replaceOne(new Document("term", terms), doc);
        }
    }
    
    public void setRelevant(String skema, ArrayList<String> terms, String id) {
        MongoCollection<Document> coll = connect(skema);
        
        Document doc = new Document("$push", new Document("VR", id));
        coll.updateOne(new Document().append("term", terms), doc);
        
        doc = new Document("$pull", new Document("VNR", id));
        coll.updateOne(new Document().append("term", terms), doc);
        
    }
    
    public ArrayList<String> getVR(String skema, ArrayList<String> terms) {
        MongoCollection<Document> coll = connect(skema);
        ArrayList<Document> arrays = coll.find(new Document("term", terms))
                .projection(new Document("VR", 1)
                .append("_id", 0)).into(new ArrayList<Document>());
        
        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>)arrays.get(0).get("VR");
            return ids;
        } else {
            return null;
        }
    }
    
    public ArrayList<String> getVNR(String skema, ArrayList<String> terms) {
        MongoCollection<Document> coll = connect(skema);
        ArrayList<Document> arrays = coll.find(new Document("term", terms))
                .projection(new Document("VNR", 1)
                .append("_id", 0)).into(new ArrayList<Document>());
        
        if (arrays.size() > 0) {
            ArrayList<String> ids = (ArrayList<String>)arrays.get(0).get("VNR");
            return ids;
        } else {
            return new ArrayList<>();
        }
    }
    
    public ArrayList<Double> getPt(ArrayList<String> terms) {
        MongoCollection<Document> coll = connect("bim");
        
        ArrayList<Document> arr = coll.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                .append("pt_lama", 1)).into(new ArrayList<Document>());
        
        if (arr.size() > 0) {
            ArrayList<Double> pt = (ArrayList<Double>)arr.get(0).get("pt_lama");
            return pt;
        } else {
            return new ArrayList<>();
        }
    }
    
    public ArrayList<Double> getUt(ArrayList<String> terms) {
        MongoCollection<Document> coll = connect("bim");
        
        ArrayList<Document> arr = coll.find(new Document("term", terms))
                .projection(new Document("_id", 0)
                .append("ut_lama", 1)).into(new ArrayList<Document>());
        
        if (arr.size() > 0) {
            ArrayList<Double> ut = (ArrayList<Double>)arr.get(0).get("ut_lama");
            return ut;
        } else {
            return new ArrayList<>();
        }
    }
}