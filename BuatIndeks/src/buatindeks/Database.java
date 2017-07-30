package buatindeks;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Arrays;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Database {

    private final MongoCollection<Document> coll_indeks;
    private final MongoCollection<Document> coll_dl;
    private MongoClient client;

    public Database() {
        coll_indeks = connect("indeks");
        coll_dl = connect("doclength");
    }
    
    public void closeConnection() {
        client.close();
    }

    private MongoCollection<Document> connect(String nama) {

        MongoCollection<Document> coll = null;

        try {
            client = new MongoClient();
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

}