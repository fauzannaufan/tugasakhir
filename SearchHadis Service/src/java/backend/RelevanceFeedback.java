package backend;

import java.util.ArrayList;

/**
 *
 * @author M. Fauzan Naufan
 */
public class RelevanceFeedback {
    
    public void calculateProbBIM(String kueri, ArrayList<String> VR, ArrayList<String> VNR, ArrayList<Double> pt, ArrayList<Double> ut, String sid) {
        ProsesTeks PT = new ProsesTeks();
        Database DB = new Database();
        
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        int VRt[] = new int[p_kueri.size()];
        int VNRt[] = new int[p_kueri.size()];
        ArrayList<Double> newpt = new ArrayList<>();
        ArrayList<Double> newut = new ArrayList<>();
        
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
            int kappa = 5;
            newpt.add((VRt[i]+kappa*pt.get(i))/(VR.size()+kappa));
            newut.add((VNRt[i]+kappa*ut.get(i))/(VNR.size()+kappa));
        }
        
        DB.updateBIM(p_kueri, newpt, newut, sid);
        DB.closeConnection();
    }
    
    public void calculateRfOkapi(String kueri, ArrayList<String> VR, ArrayList<String> VNR, String sid) {
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
        
        DB.updateOkapi(p_kueri, rf, sid);
        DB.closeConnection();
    }

}
