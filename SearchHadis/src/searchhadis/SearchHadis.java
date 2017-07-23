package searchhadis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    
    public void calculateProbBIM(String kueri, ArrayList<String> VR, ArrayList<Double> pt) {
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        int VRt[] = new int[p_kueri.size()];
        ArrayList<Double> newpt = new ArrayList<>();
        
        p_kueri.stream().forEach((s) -> {
            ids.add(DB.getIds(s));
        });
        
        for (int i=0;i<VR.size();i++) {
            String id = VR.get(i);
            for (int j=0;j<ids.size();j++) {
                if (ids.get(j).contains(id)) {
                    VRt[j] += 1;
                }
            }
        }
        
        for (int i=0;i<p_kueri.size();i++) {
            int kappa = 5;
            newpt.add((VRt[i]+kappa*pt.get(i))/(VR.size()+kappa));
        }
        
        if (DB.findBIM(p_kueri)) {
            DB.updateBIM(p_kueri, newpt);
        } else {
            DB.insertBIM(p_kueri, newpt);
        }
    }
    
    public double countOkapi(String id, String term, int N, double Lave, int df, int tftd, int Ld) {
        double k1 = 1.5;
        double b = 0.75;
        
        double hasil = (double)N/df * ((double)(k1+1)*tftd) / ((double)k1*((1-b)+b*(Ld/Lave))+tftd);
        
        return hasil;
    }
    
    public void sortResult(Map map) {
        Map<String,Double> result = new LinkedHashMap<>();
        List<Map.Entry<String,Double>> list;
        List<Map.Entry<String,Double>> list2;
        
        list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (Map.Entry<String,Double> o1, Map.Entry<String,Double> o2) -> (o2.getValue()).compareTo(o1.getValue()));
        list.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });
        
        //Cetak hasil pencarian
        list2 = new ArrayList<>(result.entrySet());
        if (list2.size()>10) {
            list2 = list2.subList(0, 9);
        }
        for (Map.Entry entry : list2) {
            System.out.print(entry.getKey()+" : ");
            System.out.println(entry.getValue());
        }
    }
    
    public void searchBIM(String kueri) {
        //Inisialisasi kelas
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        Hadis H = new Hadis();
        
        //Inisialisasi variabel
        int[] dfs;
        int term_no;
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        Map<String, Double> map = new HashMap<>();
        ArrayList<Double> RF;
        
        //Kueri ke DB
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        term_no = p_kueri.size();
        RF = DB.getProbBIM(p_kueri);
        dfs = new int[term_no];
        for (int i=0;i<p_kueri.size();i++) {
            dfs[i] = DB.getDf(p_kueri.get(i));
            ids.add(DB.getIds(p_kueri.get(i)));
        }
        
        //Menghitung pt dan ut
        double[] pt = new double[term_no];
        int N = DB.getN();
        for (int i=0;i<term_no;i++) {
            //pt[i] = 0.5;
            if (RF == null) {
                pt[i] = ((double)dfs[i]/N*2/3) + ((double)1/3);
            } else {
                pt[i] = (double)RF.get(i);
            }
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
        
        sortResult(map);
    }
    
    public void searchOkapi(String kueri) {
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
        
        //Kueri ke DB
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        for (int i=0;i<p_kueri.size();i++) {
            ids.add(DB.getIds(p_kueri.get(i)));
        }
        
        //Menghitung nilai dokumen
        for (int i=0;i<ids.size();i++) {
            int df = DB.getDf(p_kueri.get(i));
            allIds = DB.getAllIds(p_kueri.get(i));
            for (int j=0;j<ids.get(i).size();j++) {
                String id = ids.get(i).get(j);
                int tftd = getTftd(allIds,id);
                int Ld = allDocLength.get(id);
                double a = Math.log10(countOkapi(id,p_kueri.get(i),N,Lave,df,tftd,Ld));
                map.put(id, map.getOrDefault(id, 0.0)+a);
            }
        }
        
        sortResult(map);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SearchHadis SH = new SearchHadis();
        String kueri = "shalat wajib berjamaah";
        
        SH.searchBIM(kueri);
        //SH.searchOkapi(kueri);
        
        /*ArrayList<String> ar = new ArrayList<>();
        ar.add("kencing");
        ar.add("kubur");
        ar.add("orang");
        ar.add("siksa");*/
        
        ArrayList<String> ar2 = new ArrayList<>();
        ar2.add("B872");
        ar2.add("B5648");
        ar2.add("B3424");
        //ar2.add("B1873");
        ar2.add("B1137");
        
        ArrayList<Double> pt = new ArrayList<>();
        pt.add(0.86111111111112);
        pt.add(0.86111111111112);
        pt.add(0.69444444444444);
        
        SH.calculateProbBIM(kueri, ar2, pt);
    }
    
}
