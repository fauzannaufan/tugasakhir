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
        ArrayList<ArrayList<Integer>> ids = new ArrayList<>();
        TreeSet<Integer> sorted_id = new TreeSet<>();
        Map<Integer, Double> map = new HashMap<>();
        
        //Input kueri
        String imam = "bukhari";
        String kueri = "siksa kubur orang kencing";
        
        //Kueri ke DB
        coll = DB.connect(imam);
        ArrayList<String> p_kueri = PT.prosesTeks(kueri);
        term_no = p_kueri.size();
        dfs = new int[term_no];
        for (int i=0;i<p_kueri.size();i++) {
            dfs[i] = DB.getDf(coll, p_kueri.get(i));
            ids.add(DB.getIds(coll, p_kueri.get(i)));
        }
        for (int i=0;i<ids.size();i++) {
            for (int j=0;j<ids.get(i).size();j++) {
                sorted_id.add(ids.get(i).get(j));
            }
        }
        
        //Menghitung pt dan ut
        double[] pt = new double[term_no];
        double[] ut = new double[term_no];
        int N = H.getJumlahHadis(imam);
        for (int i=0;i<term_no;i++) {
            pt[i] = 0.5;
            ut[i] = (double)dfs[i]/N;
        }
        
        //Menghitung nilai dokumen
        double[] score = new double[N];
        for (int i=0;i<N;i++) {
            score[i] = 0.0;
        }
        for (int i=0;i<ids.size();i++) {
            for (int j=0;j<ids.get(i).size();j++) {
                int id = ids.get(i).get(j);
                double a = Math.log10(pt[i]/(1.0-pt[i]));
                double b = Math.log10((1.0-ut[i])/ut[i]);
                score[id-1] += a+b;
            }
        }
        
        //Masukkan id dan skor ke dalam Map
        Iterator<Integer> iter = sorted_id.iterator();
        while (iter.hasNext()) {
            int id = iter.next();
            if (score[id-1] > 0) {
                map.put(id, score[id-1]);
            }
        }
        
        //Urutkan Map berdasarkan skor
        List<Map.Entry<Integer,Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<Integer,Double> result = new LinkedHashMap<>();
        list.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });
        
        //Cetak hasil pencarian
        List<Map.Entry<Integer,Double>> list2 = new ArrayList<>(result.entrySet());
        list2 = list2.subList(0, 9);
        for (Map.Entry entry : list2) {
            System.out.print(entry.getKey()+" : ");
            System.out.println(entry.getValue());
        }
    }
    
}
