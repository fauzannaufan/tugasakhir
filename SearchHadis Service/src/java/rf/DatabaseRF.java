package rf;

import backend.Database;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class DatabaseRF extends Database {
    
    public boolean findOkapi(ArrayList<String> terms) {
        long L = coll_okapi.count(new Document("term", terms));
        return L != 0;
    }
    
    public boolean findVR(ArrayList<String> terms, String sid, String id) {
        long L = coll_history.count(new Document("term", terms).append("SID", sid).append("VR", id));
        return L != 0;
    }
    
    public boolean findVNR(ArrayList<String> terms, String sid, String id) {
        long L = coll_history.count(new Document("term", terms).append("SID", sid).append("VNR", id));
        return L != 0;
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

    public void addRelevantDocs(String sid, ArrayList<String> terms, ArrayList<Double> pt, ArrayList<Double> ut) {
        Document doc;
        List<String> ids2 = new ArrayList<>();
        if (pt.isEmpty()) {
            doc = new Document("term", terms).append("SID", sid).append("VNR", ids2);
        } else {
            doc = new Document("term", terms).append("SID", sid).append("VNR", ids2).append("pt_lama", pt).append("ut_lama", ut);
        }

        long L = coll_history.count(new Document("term", terms).append("SID", sid));
        if (L == 0) {
            coll_history.insertOne(doc);
        }

    }

    public void setRelevant(ArrayList<String> terms, String sid, String id) {

        Document doc1 = new Document("$push", new Document("VR", id));
        Document doc2 = new Document("$pull", new Document("VNR", id));
        
        if (!findVR(terms, sid, id)) {
            coll_history.updateOne(new Document("term", terms).append("SID", sid), doc1);
        }
        coll_history.updateOne(new Document("term", terms).append("SID", sid), doc2);
    }
    
    public void setNonRelevant(ArrayList<String> terms, String sid, String id) {

        Document doc1 = new Document("$pull", new Document("VR", id));
        Document doc2 = new Document("$push", new Document("VNR", id));

        if (!findVNR(terms, sid, id)) {
            coll_history.updateOne(new Document("term", terms).append("SID", sid), doc2);
        }
        coll_history.updateOne(new Document("term", terms).append("SID", sid), doc1);
    }
    
    public void unRelevant(ArrayList<String> terms, String sid, String id) {

        Document doc1 = new Document("$pull", new Document("VR", id));
        coll_history.updateOne(new Document("term", terms).append("SID", sid), doc1);
    }
    
    public void unNonRelevant(ArrayList<String> terms, String sid, String id) {

        Document doc2 = new Document("$pull", new Document("VNR", id));
        coll_history.updateOne(new Document("term", terms).append("SID", sid), doc2);
    }

    public ArrayList<String> getVR(String skema, String sid, ArrayList<String> terms) {
        ArrayList<Document> arrays = coll_history.find(new Document("term", terms)
                .append("SID", sid)
                .append("skema", skema))
                .projection(new Document("VR", 1)
                        .append("_id", 0))
                .into(new ArrayList<>());

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
                .into(new ArrayList<>());

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
