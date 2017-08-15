package evaluation;

import backend.Database;
import java.util.ArrayList;
import java.util.Arrays;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class DatabaseEval extends Database {

    public void insertGT(ArrayList<String> terms, ArrayList<String> ids) {
        Document doc = new Document("terms", terms).append("id", ids);
        coll_gt.insertOne(doc);
    }

    public boolean isRelevant(ArrayList<String> terms, String id) {
        long L = coll_gt.count(new Document("terms", terms).append("id", id));
        return L != 0;
    }

    public int countRelevantDocs(ArrayList<String> terms) {
        Document doc = coll_gt.aggregate(Arrays.asList(
                new Document("$match", new Document("terms", terms)),
                new Document("$project", new Document("_id", 0)
                        .append("count", new Document("$size", "$id")))))
                .first();

        return (int) doc.get("count");
    }
}
