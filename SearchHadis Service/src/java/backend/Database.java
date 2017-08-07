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

    private final MongoCollection<Document> coll_indeks;
    private final MongoCollection<Document> coll_dl;
    
    public final MongoCollection<Document> coll_okapi;
    public final MongoCollection<Document> coll_bim;
    public final MongoCollection<Document> coll_history;
    public final MongoCollection<Document> coll_hadis;
    public final MongoCollection<Document> coll_kitab;
    public final MongoCollection<Document> coll_bab;

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
}
