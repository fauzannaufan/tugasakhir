package searchhadis;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Database {
    
    public boolean find(MongoCollection<Document> coll, String term) {
        long L = coll.count(Document.parse("{\"nama\" : \""+term+"\"}"));
        return L != 0;
    }
    
    public boolean findId(MongoCollection<Document> coll, String term, String id) {
        long L = coll.count(Document.parse("{\"nama\" : \""+term+"\", \"id\" : \""+id+"\"}"));
        return L != 0;
    }
    
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
    
    public void insertDocLength(MongoCollection<Document> coll, String no_hadis, int length) {
        Document doc = new Document("id", no_hadis)
                .append("length", length);
        
        coll.insertOne(doc);
    }
    
    public void insert(MongoCollection<Document> coll, String no_hadis, String term) {
        Document doc = new Document("nama", term)
                .append("df", 1)
                .append("id", Arrays.asList(no_hadis));
        
        coll.insertOne(doc);
    }
    
    public void update(MongoCollection<Document> coll, String no_hadis, String term) {
        
        //Update document frequency
        Document doc = new Document().append("$inc",
                new Document().append("df", 1))
                .append("$push",
                        new Document().append("id", no_hadis));
        
        coll.updateOne(new Document().append("nama", term), doc);
    }
    
    public void addId(MongoCollection<Document> coll, String no_hadis, String term) {
        
        //Update document frequency
        Document doc = new Document().append("$push",
                new Document().append("id", no_hadis));
        
        coll.updateOne(new Document().append("nama", term), doc);
    }
    
    public int getDf(MongoCollection<Document> coll, String term) {
        Document df = coll.find(new Document("nama", term))
                .projection(new Document("df", 1)
                .append("_id", 0)).first();
        
        return Integer.parseInt(df.get("df").toString());
    }
    
    public ArrayList<String> getIds(MongoCollection<Document> coll, String term) {
        ArrayList<Document> arrays = coll.find(new Document("nama", term))
                .projection(new Document("id", 1)
                .append("_id", 0)).into(new ArrayList<Document>());
        ArrayList<String> ids = (ArrayList<String>)arrays.get(0).get("id");
        Collections.sort(ids);
        return ids;
    }
    
    public int getN() {
        MongoCollection<Document> coll = connect("doclength");
        long L = coll.count();
        return Math.toIntExact(L);
    }
}
