package backend;

import evaluation.CalculateEval;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static search.InitDB.*;

/**
 *
 * @author M. Fauzan Naufan
 */
public class SearchHadis {

    private int getTftd(ArrayList<String> allIds, String id) {
        int tftd = 0;
        tftd = allIds.stream().filter((s) -> (s.equals(id))).map((_item) -> 1).reduce(tftd, Integer::sum);
        return tftd;
    }

    private double countOkapi(double Lave, int tftd, int Ld) {
        double k1 = 1.5;
        double b = 0.75;

        double hasil = ((double) (k1 + 1) * tftd) / ((double) k1 * ((1 - b) + b * (Ld / Lave)) + tftd);

        return hasil;
    }

    public ArrayList<Double> bobotDokumen(ArrayList<String> p_kueri, String id) {
        ArrayList<String> allIds;
        ArrayList<Double> hasil = new ArrayList<>();
        double doc_weight = 0.0;

        //Menghitung weight total dokumen
        for (int i = 0; i < p_kueri.size(); i++) {
            allIds = DB.getAllIds(p_kueri.get(i));
            int tftd = getTftd(allIds, id);

            double weight;
            if (tftd == 0) {
                weight = 0.0;
            } else {
                weight = 1.0 + Math.log10((double) tftd);
            }

            doc_weight += weight * weight;
        }

        //Menghitung nilai dokumen
        for (int i = 0; i < p_kueri.size(); i++) {
            allIds = DB.getAllIds(p_kueri.get(i));

            int tftd = getTftd(allIds, id);
            double weight;
            if (tftd == 0) {
                weight = 0;
            } else {
                weight = 1.0 + Math.log10((double) tftd);
            }

            double n_weight;
            if (doc_weight == 0.0) {
                n_weight = 0.0;
            } else {
                n_weight = (double) weight / Math.sqrt(doc_weight);
            }

            hasil.add(n_weight);
        }

        return hasil;
    }

    public ArrayList<Double> bobotKueri(ArrayList<String> p_kueri) {
        ArrayList<Double> arr = new ArrayList<>();
        int[] tf = new int[p_kueri.size()];
        double[] idf = new double[p_kueri.size()];
        double[] tf_idf = new double[p_kueri.size()];
        int a = -1;
        String s, t = "";
        int N = DB.getN();

        //Menangani term frequency dan idf
        for (int i = 0; i < p_kueri.size(); i++) {
            s = p_kueri.get(i);
            if (s.equals(t)) {
                tf[a] += 1;
            } else {
                a++;
                tf[a] = 1;
                int df = DB.getDf(s);
                idf[a] = Math.log10((double) N / df);
            }
            t = s;
        }

        double total_weight = 0;
        for (int i = 0; i < a + 1; i++) {
            tf_idf[i] = (double) (1 + Math.log10(tf[i])) * idf[i];
            total_weight += tf_idf[i] * tf_idf[i];
        }

        for (int i = 0; i < a + 1; i++) {
            double b = (double) tf_idf[i] / Math.sqrt(total_weight);
            arr.add(b);
        }

        return arr;
    }

    private String createSnippet(String indo) {
        int i1;
        int i2;

        if (indo.toLowerCase().contains("nabi")) {
            i1 = indo.toLowerCase().indexOf("nabi");
        } else {
            i1 = indo.length();
        }

        if (indo.toLowerCase().contains("rasulullah")) {
            i2 = indo.toLowerCase().indexOf("rasulullah");
        } else {
            i2 = indo.length();
        }

        int i;
        if (i1 == 0 && i2 == 0) {
            return "";
        }
        if (i1 < i2) {
            i = i1;
        } else {
            i = i2;
        }

        if (i + 320 > indo.length()) {
            return indo.substring(i);
        } else {
            if (indo.charAt(i + 299) == Character.MIN_VALUE || indo.charAt(i + 300) == Character.MIN_VALUE) {
                return indo.substring(i, i + 300);
            } else {
                return indo.substring(i, indo.indexOf(" ", i + 300));
            }
        }
    }

    private JSONObject sortResulttoJSON(Map map, double[] pt, double[] ut, boolean gt, ArrayList<String> p_kueri) {
        long t0 = System.currentTimeMillis();
        Map<String, Double> result = new LinkedHashMap<>();
        List<Map.Entry<String, Double>> list;
        List<Map.Entry<String, Double>> list2;
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();

        long t1 = System.currentTimeMillis();
        list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) -> (o2.getValue()).compareTo(o1.getValue()));
        list.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });

        long t2 = System.currentTimeMillis();
        //Cetak hasil pencarian
        list2 = new ArrayList<>(result.entrySet());

        if (gt) {
            if (list2.size() >= 20) {
                list2 = list2.subList(0, 20);
            }

            for (Map.Entry entry : list2) {
                JSONObject obj2 = new JSONObject();
                ArrayList<String> arr2 = DBH.getHadis(entry.getKey().toString());
                obj2.put("key", entry.getKey());
                obj2.put("related", arr2.get(3));
                arr.add(obj2);
            }
            obj.put("hasil", arr);

        } else {
            int size = DE.countRelevantDocs(p_kueri);
            if (list2.size() >= size) {
                list2 = list2.subList(0, size);
            }

            long t3 = System.currentTimeMillis();
            for (Map.Entry entry : list2) {
                JSONObject obj2 = new JSONObject();
                ArrayList<String> arr2 = DBH.getHadis(entry.getKey().toString());
                obj2.put("key", entry.getKey());
                obj2.put("imam", arr2.get(0));
                obj2.put("haditsId", arr2.get(1));
                obj2.put("indo", arr2.get(2));
                obj2.put("snippet", createSnippet(arr2.get(2)));
                arr.add(obj2);
            }

            long t4 = System.currentTimeMillis();
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
            
            long t5 = System.currentTimeMillis();

            ArrayList<Double> eval = new CalculateEval().Calculate(p_kueri, list2);

            obj.put("precision", eval.get(0));
            obj.put("recall", eval.get(1));
            if (Double.isNaN(eval.get(2))) {
                obj.put("ap", 0.0);
            } else {
                obj.put("ap", eval.get(2));
            }

            System.out.println("Init vars : " + (t1 - t0));
            System.out.println("Sort by score : " + (t2 - t1));
            System.out.println("Sub list : " + (t3 - t2));
            System.out.println("Get data hadis : " + (t4 - t3));
            System.out.println("Pt ut : " + (t5 - t4));
        }
        return obj;
    }

    public JSONObject searchBIM(String kueri, String sid, boolean gt) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();

        //Inisialisasi variabel
        int[] dfs;
        int term_no;
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        Map<String, Double> resultmap = new HashMap<>();
        int N = DB.getN();

        //Proses Kueri
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        term_no = p_kueri.size();

        //Get DF dan Kemunculan Term
        dfs = new int[term_no];
        for (int i = 0; i < p_kueri.size(); i++) {
            dfs[i] = DB.getDf(p_kueri.get(i));
            if (gt) {
                ids.add(DB.getIdsGt(p_kueri.get(i)));
            } else {
                ids.add(DB.getIdsTest(p_kueri.get(i)));
            }
        }

        //Menghitung pt dan ut
        double[] pt = new double[term_no];
        double[] ut = new double[term_no];
        Document RF = DBRF.getProbBIM(p_kueri, sid);

        if (RF == null || gt) {
            for (int i = 0; i < term_no; i++) {
                pt[i] = ((double) dfs[i] / N * 2 / 3) + ((double) 1 / 3);
                ut[i] = (double) dfs[i] / N;
            }
        } else {
            ArrayList<Double> arr_pt = (ArrayList<Double>) RF.get("pt");
            ArrayList<Double> arr_ut = (ArrayList<Double>) RF.get("ut");
            for (int i = 0; i < term_no; i++) {
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

        //Menghilangkan dokumen dengan skor <= 0
        map.entrySet().stream().filter((entry) -> (entry.getValue() >= 0)).forEach((entry) -> {
            resultmap.put(entry.getKey(), entry.getValue());
        });

        long t0 = System.currentTimeMillis();
        JSONObject a = sortResulttoJSON(resultmap, pt, ut, gt, p_kueri);
        long t1 = System.currentTimeMillis();
        System.out.println("Sort BIM : " + (t1 - t0));
        return a;
    }

    public JSONObject searchOkapi(String kueri, String sid, boolean gt) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();

        //Inisialisasi variabel
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        ArrayList<String> allIds;
        ArrayList<Double> RF;

        //Get Doc Length, Doc Avg Length, N
        Map<String, Integer> allDocLength = DB.getAllDocLength();
        double Lave = DB.getDocAvgLength();
        int N = DB.getN();

        //Proses Kueri
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);

        //Kueri ke DB
        RF = DBRF.getRfOkapi(p_kueri, sid);
        for (int i = 0; i < p_kueri.size(); i++) {
            if (gt) {
                ids.add(DB.getIdsGt(p_kueri.get(i)));
            } else {
                ids.add(DB.getIdsTest(p_kueri.get(i)));
            }
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
                if (RF == null || gt) {
                    a = Math.log10((double) N / df * countOkapi(Lave, tftd, Ld));
                } else {
                    a = Math.log10(RF.get(i) * countOkapi(Lave, tftd, Ld));
                }
                map.put(id, map.getOrDefault(id, 0.0) + a);
            }
        }

        long t0 = System.currentTimeMillis();
        JSONObject a = sortResulttoJSON(map, null, null, gt, p_kueri);
        long t1 = System.currentTimeMillis();
        System.out.println("Sort Okapi : " + (t1 - t0));
        return a;
    }

    public JSONObject searchVSM(String kueri, String sid, boolean gt) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();

        //Inisialisasi variabel
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        ArrayList<String> allIds;
        ArrayList<Double> RF;
        Map<String, Double> doc_weight = new HashMap<>();

        //Proses Kueri
        ArrayList<String> p_kueri = PT.prosesKueriVSM(kueri);
        ArrayList<String> p_kueri2 = PT.prosesKueri(kueri);

        //Hitung weight kueri
        ArrayList<Double> w_kueri = bobotKueri(p_kueri);

        //Kueri ke DB
        RF = DBRF.getRocchio(p_kueri2, sid);
        for (int i = 0; i < p_kueri2.size(); i++) {
            if (gt) {
                ids.add(DB.getIdsGt(p_kueri2.get(i)));
            } else {
                ids.add(DB.getIdsTest(p_kueri2.get(i)));
            }
        }

        //Menghitung weight total dokumen
        for (int i = 0; i < ids.size(); i++) {
            allIds = DB.getAllIds(p_kueri2.get(i));
            for (int j = 0; j < ids.get(i).size(); j++) {
                String id = ids.get(i).get(j);

                int tftd = getTftd(allIds, id);

                double weight = 1.0 + Math.log10((double) tftd);
                doc_weight.put(id, doc_weight.getOrDefault(id, 0.0) + (weight * weight));
            }
        }

        //Menghitung nilai dokumen
        for (int i = 0; i < ids.size(); i++) {
            allIds = DB.getAllIds(p_kueri2.get(i));
            for (int j = 0; j < ids.get(i).size(); j++) {
                String id = ids.get(i).get(j);

                int tftd = getTftd(allIds, id);
                double weight = 1.0 + Math.log10((double) tftd);
                double n_weight = (double) weight / Math.sqrt(doc_weight.get(id));
                
                double product;
                if (RF == null || gt) {
                    product = w_kueri.get(i) * n_weight;
                } else {
                    product = RF.get(i) * n_weight;
                }

                map.put(id, map.getOrDefault(id, 0.0) + product);
            }
        }

        long t0 = System.currentTimeMillis();
        JSONObject a = sortResulttoJSON(map, null, null, gt, p_kueri2);
        long t1 = System.currentTimeMillis();
        System.out.println("Sort VSM : " + (t1 - t0));
        return a;
    }

}
