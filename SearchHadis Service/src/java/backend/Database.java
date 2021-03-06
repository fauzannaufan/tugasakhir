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

    private final MongoCollection<Document> coll_indeksgt;
    private final MongoCollection<Document> coll_indekstest;
    private final MongoCollection<Document> coll_dl;

    public final MongoCollection<Document> coll_okapi;
    public final MongoCollection<Document> coll_bim;
    public final MongoCollection<Document> coll_rocchio;
    public final MongoCollection<Document> coll_history;
    public final MongoCollection<Document> coll_hadis;
    public final MongoCollection<Document> coll_gt;

    private MongoClient client;

    public Database() {
        coll_indeksgt = connect("indeksgt");
        coll_indekstest = connect("indekstest");
        coll_okapi = connect("okapi");
        coll_dl = connect("doclength");
        coll_bim = connect("bim");
        coll_history = connect("history");
        coll_hadis = connect("hadis");
        coll_gt = connect("gt");
        coll_rocchio = connect("rocchio");
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
        long L = coll_indekstest.count(new Document("nama", term));
        return L != 0;
    }

    public int getDf(String term) {
        Document df = coll_indekstest.find(new Document("nama", term))
                .projection(new Document("df", 1)
                        .append("_id", 0)).first();

        return Integer.parseInt(df.get("df").toString());
    }
    
    public ArrayList<String> getIdsGt(String term) {
        ArrayList<Document> arrays = coll_indeksgt.find(new Document("nama", term))
                .projection(new Document("id", 1)
                        .append("_id", 0)).into(new ArrayList<>());

        ArrayList<String> ids = (ArrayList<String>) arrays.get(0).get("id");
        HashSet<String> hs = new HashSet<>();
        hs.addAll(ids);
        ids.clear();
        ids.addAll(hs);

        return ids;
    }
    
    public ArrayList<String> getIdsTest(String term) {
        ArrayList<Document> arrays = coll_indekstest.find(new Document("nama", term))
                .projection(new Document("id", 1)
                        .append("_id", 0)).into(new ArrayList<>());

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
        ArrayList<Document> arrays = coll_indekstest.find(new Document("nama", term))
                .projection(new Document("id", 1)
                        .append("_id", 0)).into(new ArrayList<>());

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
