package backend;

import java.util.ArrayList;

/**
 *
 * @author M. Fauzan Naufan
 */
public class RelevanceFeedback {
    
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
    
    public void calculateRfOkapi(String kueri, ArrayList<String> VR, ArrayList<String> VNR) {
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        int VRt[] = new int[p_kueri.size()];
        int VNRt[] = new int[p_kueri.size()];
        ArrayList<Double> rf = new ArrayList<>();
        
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
        
        for (int i=0;i<VNR.size();i++) {
            String id = VNR.get(i);
            for (int j=0;j<ids.size();j++) {
                if (ids.get(j).contains(id)) {
                    VNRt[j] += 1;
                }
            }
        }
        
        for (int i=0;i<p_kueri.size();i++) {
            rf.add( ( ( (double)VRt[i] + 0.5 ) / ( (double)VNRt[i] + 0.5 ) ) / 
                    ( ( (double)DB.getDf(p_kueri.get(i)) - VRt[i] + 0.5)/
                            ( (double)DB.getN() - DB.getDf(p_kueri.get(i)) - VR.size() + VRt[i] + 0.5 ) ) );
        }
        
        if (DB.findOkapi(p_kueri)) {
            DB.updateOkapi(p_kueri, rf);
        } else {
            DB.insertOkapi(p_kueri, rf);
        }
        
    }

}
