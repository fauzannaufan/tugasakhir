package searchhadis;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class SearchHadis {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        Hadis H = new Hadis();
        
        //Inisialisasi variabel
        MongoCollection<Document> coll;
        int[] dfs;
        int term_no;
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        
        //Input kueri
        String kueri = "siksa kubur orang kencing";
        
        //Kueri ke DB
        coll = DB.connect("indeks");
        ArrayList<String> p_kueri = PT.prosesTeks(kueri);
        term_no = p_kueri.size();
        dfs = new int[term_no];
        for (int i=0;i<p_kueri.size();i++) {
            dfs[i] = DB.getDf(coll, p_kueri.get(i));
            ids.add(DB.getIds(coll, p_kueri.get(i)));
        }
        
        //Menghitung pt
        double[] pt = new double[term_no];
        int N = DB.getN();
        for (int i=0;i<term_no;i++) {
            //pt[i] = (double)dfs[i]/N*2/3 + ((double)1/3);
            pt[i] = 0.5;
        }
        
        //Menghitung nilai dokumen
        for (int i=0;i<ids.size();i++) {
            for (int j=0;j<ids.get(i).size();j++) {
                String id = ids.get(i).get(j);
                double a = Math.log10(pt[i]/(1.0-pt[i]));
                double b = Math.log10(N/(double)dfs[i]);
                map.put(id, map.getOrDefault(id, 0.0)+a+b);
            }
        }
        
        //Urutkan Map berdasarkan skor
        List<Map.Entry<String,Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<String,Double> o1, Map.Entry<String,Double> o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<String,Double> result = new LinkedHashMap<>();
        list.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });
        
        //Cetak hasil pencarian
        List<Map.Entry<String,Double>> list2 = new ArrayList<>(result.entrySet());
        list2 = list2.subList(0, 9);
        for (Map.Entry entry : list2) {
            System.out.print(entry.getKey()+" : ");
            System.out.println(entry.getValue());
        }
    }
    
}
