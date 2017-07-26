package backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.bson.Document;

/**
 *
 * @author M. Fauzan Naufan
 */
public class SearchHadis {

    public int getTftd(ArrayList<String> allIds, String id) {
        int tftd = 0;
        tftd = allIds.stream().filter((s) -> (s.equals(id))).map((_item) -> 1).reduce(tftd, Integer::sum);
        return tftd;
    }

    public double countOkapi(double Lave, int tftd, int Ld) {
        double k1 = 1.5;
        double b = 0.75;

        double hasil = ((double) (k1 + 1) * tftd) / ((double) k1 * ((1 - b) + b * (Ld / Lave)) + tftd);

        return hasil;
    }

    public JSONObject sortResulttoJSON(Map map, double[] pt, double[] ut) {
        Map<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list;
        List<Map.Entry<String, Double>> list2;
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        Database DB = new Database();

        list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) -> (o2.getValue()).compareTo(o1.getValue()));
        list.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });

        //Cetak hasil pencarian
        list2 = new ArrayList<>(result.entrySet());
        if (list2.size() >= 10) {
            list2 = list2.subList(0, 10);
        }

        for (Map.Entry entry : list2) {
            JSONObject obj2 = new JSONObject();
            ArrayList<String> arr2 = DB.getHadis(entry.getKey().toString());
            obj2.put("key", entry.getKey());
            obj2.put("imam", arr2.get(0));
            obj2.put("haditsId", arr2.get(1));
            obj2.put("indo", arr2.get(2));
            obj2.put("kitab", arr2.get(3));
            obj2.put("bab", arr2.get(4));
            arr.add(obj2);
        }

        obj.put("hasil", arr);
        if (pt != null && ut != null) {
            JSONArray arr3 = new JSONArray();
            JSONArray arr4 = new JSONArray();
            for (double d : pt) {
                arr3.add(d);
            }
            for (double d : ut) {
                arr4.add(d);
            }
            obj.put("pt", arr3);
            obj.put("ut", arr4);
        } else {
            obj.put("pt", new ArrayList<>());
            obj.put("ut", new ArrayList<>());
        }
        return obj;
    }

    public JSONObject searchBIM(String kueri) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        Hadis H = new Hadis();

        //Inisialisasi variabel
        int[] dfs;
        int term_no;
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        Document RF;

        //Kueri ke DB
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        term_no = p_kueri.size();
        RF = DB.getProbBIM(p_kueri);
        dfs = new int[term_no];
        for (int i = 0; i < p_kueri.size(); i++) {
            dfs[i] = DB.getDf(p_kueri.get(i));
            ids.add(DB.getIds(p_kueri.get(i)));
        }

        //Menghitung pt dan ut
        double[] pt = new double[term_no];
        double[] ut = new double[term_no];
        int N = DB.getN();
        for (int i = 0; i < term_no; i++) {
            if (RF == null) {
                pt[i] = ((double) dfs[i] / N * 2 / 3) + ((double) 1 / 3);
                ut[i] = (double) dfs[i] / N;
            } else {
                ArrayList<Double> arr_pt = (ArrayList<Double>) RF.get("pt");
                ArrayList<Double> arr_ut = (ArrayList<Double>) RF.get("ut");
                pt[i] = (double) arr_pt.get(i);
                ut[i] = (double) arr_ut.get(i);
            }
        }

        //Menghitung nilai dokumen
        for (int i = 0; i < ids.size(); i++) {
            for (int j = 0; j < ids.get(i).size(); j++) {
                String id = ids.get(i).get(j);
                double a = Math.log10(pt[i] / (1.0 - pt[i]));
                double b = Math.log10((1.0 - ut[i]) / ut[i]);
                map.put(id, map.getOrDefault(id, 0.0) + a + b);
            }
        }

        return sortResulttoJSON(map, pt, ut);
    }

    public JSONObject searchOkapi(String kueri) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        Hadis H = new Hadis();

        //Inisialisasi variabel
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        int N = DB.getN();
        double Lave = DB.getDocAvgLength();
        ArrayList<String> allIds;
        Map<String, Integer> allDocLength = DB.getAllDocLength();
        ArrayList<Double> RF;

        //Kueri ke DB
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        System.out.println(p_kueri);
        RF = DB.getRfOkapi(p_kueri);
        for (int i = 0; i < p_kueri.size(); i++) {
            ids.add(DB.getIds(p_kueri.get(i)));
        }

        //Menghitung nilai dokumen
        for (int i = 0; i < ids.size(); i++) {
            int df = DB.getDf(p_kueri.get(i));
            allIds = DB.getAllIds(p_kueri.get(i));
            for (int j = 0; j < ids.get(i).size(); j++) {
                String id = ids.get(i).get(j);
                int tftd = getTftd(allIds, id);
                int Ld = allDocLength.get(id);
                double a;
                if (RF == null) {
                    a = Math.log10((double) N / df * countOkapi(Lave, tftd, Ld));
                } else {
                    a = Math.log10(RF.get(i) * countOkapi(Lave, tftd, Ld));
                }
                map.put(id, map.getOrDefault(id, 0.0) + a);
            }
        }

        return sortResulttoJSON(map, null, null);
    }

}
