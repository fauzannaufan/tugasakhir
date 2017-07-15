package bacahadis;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.Arrays;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class DB {
    
    Block<Document> printBlock = (final Document document) -> {
        System.out.println(document.toJson());
    };
    
    public boolean find(MongoCollection<Document> coll, String term) {
        long L = coll.count(Document.parse("{\"nama\" : \""+term+"\"}"));
        return L != 0;
    }
    
    public MongoCollection<Document> connect(String imam) {
        
        MongoCollection<Document> coll = null;
        
        try {
            MongoClient client = new MongoClient();
            MongoDatabase db = client.getDatabase("test");
            coll = db.getCollection(imam);
            System.out.println("Connected");
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return coll;
    }
    
    public void insert(MongoCollection<Document> coll, String no_hadis, String term) {
        int nohadis = Integer.parseInt(no_hadis);
        Document doc = new Document("nama", term)
                .append("df", 1)
                .append("id", Arrays.asList(nohadis));
        
        coll.insertOne(doc);
    }
    
    public void update(MongoCollection<Document> coll, String no_hadis, String term) {
        int nohadis = Integer.parseInt(no_hadis);
        
        //Update document frequency
        Document doc = new Document().append("$inc",
                new Document().append("df", 1))
                .append("$push",
                        new Document().append("id", nohadis));
        
        coll.updateOne(new Document().append("nama", term), doc);
    }
}
