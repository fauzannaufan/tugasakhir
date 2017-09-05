package rf;

import backend.ProsesTeks;
import backend.SearchHadis;
import java.util.ArrayList;
import static search.InitDB.*;

/**
 *
 * @author M. Fauzan Naufan
 */
public class RelevanceFeedback {

    public void pseudoBIM(String kueri, ArrayList<String> V, String sid) {
        ProsesTeks PT = new ProsesTeks();

        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        ArrayList<Double> newpt = new ArrayList<>();
        ArrayList<Double> newut = new ArrayList<>();

        int N = DB.getN();

        p_kueri.stream().forEach((s) -> {
            ids.add(DB.getIdsTest(s));
        });

        if (V.size() > 0) {
            int Vt[] = new int[p_kueri.size()];

            for (int i = 0; i < V.size(); i++) {
                String id = V.get(i);
                for (int j = 0; j < ids.size(); j++) {
                    if (ids.get(j).contains(id)) {
                        Vt[j] += 1;
                    }
                }
            }
            for (int i = 0; i < p_kueri.size(); i++) {
                int df = DB.getDf(p_kueri.get(i));
                newpt.add((double) (Vt[i] + 0.5) / (V.size() + 1));
                newut.add((double) (df - Vt[i] + 0.5) / (N - V.size() + 1));
            }
        }

        DBRF.updateBIM(p_kueri, newpt, newut, sid, true);
    }

    public void calculateProbBIM(String kueri, ArrayList<String> VR, ArrayList<String> VNR, ArrayList<Double> pt, ArrayList<Double> ut, String sid) {
        ProsesTeks PT = new ProsesTeks();

        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        ArrayList<Double> newpt = new ArrayList<>();
        ArrayList<Double> newut = new ArrayList<>();

        p_kueri.stream().forEach((s) -> {
            ids.add(DB.getIdsTest(s));
        });

        if (VR.size() > 0) {
            int VRt[] = new int[p_kueri.size()];

            for (int i = 0; i < VR.size(); i++) {
                String id = VR.get(i);
                for (int j = 0; j < ids.size(); j++) {
                    if (ids.get(j).contains(id)) {
                        VRt[j] += 1;
                    }
                }
            }
            for (int i = 0; i < p_kueri.size(); i++) {
                int kappa = 5;
                newpt.add((VRt[i] + kappa * pt.get(i)) / (VR.size() + kappa));
            }
        } else {
            newpt = pt;
        }

        if (VNR.size() > 0) {
            int VNRt[] = new int[p_kueri.size()];

            for (int i = 0; i < VNR.size(); i++) {
                String id = VNR.get(i);
                for (int j = 0; j < ids.size(); j++) {
                    if (ids.get(j).contains(id)) {
                        VNRt[j] += 1;
                    }
                }
            }
            for (int i = 0; i < p_kueri.size(); i++) {
                int kappa = 5;
                newut.add((VNRt[i] + kappa * ut.get(i)) / (VNR.size() + kappa));
            }
        } else {
            newut = ut;
        }

        DBRF.updateBIM(p_kueri, newpt, newut, sid, false);
    }

    public void calculateRfOkapi(String kueri, ArrayList<String> VR, ArrayList<String> VNR, String sid, boolean pseudo) {
        ProsesTeks PT = new ProsesTeks();

        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<ArrayList<String>> ids = new ArrayList<>();
        int VRt[] = new int[p_kueri.size()];
        int VNRt[] = new int[p_kueri.size()];
        ArrayList<Double> rf = new ArrayList<>();

        p_kueri.stream().forEach((s) -> {
            ids.add(DB.getIdsTest(s));
        });

        for (int i = 0; i < VR.size(); i++) {
            String id = VR.get(i);
            for (int j = 0; j < ids.size(); j++) {
                if (ids.get(j).contains(id)) {
                    VRt[j] += 1;
                }
            }
        }

        for (int i = 0; i < VNR.size(); i++) {
            String id = VNR.get(i);
            for (int j = 0; j < ids.size(); j++) {
                if (ids.get(j).contains(id)) {
                    VNRt[j] += 1;
                }
            }
        }

        for (int i = 0; i < p_kueri.size(); i++) {
            rf.add((((double) VRt[i] + 0.5) / ((double) VNRt[i] + 0.5))
                    / (((double) DB.getDf(p_kueri.get(i)) - VRt[i] + 0.5)
                    / ((double) DB.getN() - DB.getDf(p_kueri.get(i)) - VR.size() + VRt[i] + 0.5)));
        }

        DBRF.updateOkapi(p_kueri, rf, sid, pseudo);
    }

    public void rocchio(String kueri, ArrayList<String> VR, ArrayList<String> VNR, String sid, boolean pseudo) {

        ProsesTeks PT = new ProsesTeks();
        SearchHadis SH = new SearchHadis();

        double alpha = 1.0;
        double beta = 0.75;
        double gamma = 0.15;

        //Bobot kueri awal
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        ArrayList<Double> q0 = SH.bobotKueri(p_kueri);

        //Vektor Dokumen Relevan
        double[] Dr = new double[p_kueri.size()];
        for (int i = 0; i < VR.size(); i++) {
            ArrayList<Double> bobot = SH.bobotDokumen(p_kueri, VR.get(i));
            for (int j = 0; j < bobot.size(); j++) {
                Dr[j] += bobot.get(j);
            }
        }
        for (int i = 0; i < Dr.length; i++) {
            Dr[i] = (double) Dr[i] / VR.size();
        }

        //Vektor Dokumen Non Relevan
        double[] Dnr = new double[p_kueri.size()];
        for (int i = 0; i < VNR.size(); i++) {
            ArrayList<Double> bobot = SH.bobotDokumen(p_kueri, VNR.get(i));
            for (int j = 0; j < bobot.size(); j++) {
                Dnr[j] += bobot.get(j);
            }
        }
        for (int i = 0; i < Dnr.length; i++) {
            if (VNR.isEmpty()) {
                Dnr[i] = 0;
            } else {
                Dnr[i] = (double) Dnr[i] / VNR.size();
            }
        }

        ArrayList<Double> qm = new ArrayList<>();
        for (int i = 0; i < q0.size(); i++) {
            qm.add(alpha * q0.get(i) + beta * Dr[i] - gamma * Dnr[i]);
        }

        DBRF.updateRocchio(p_kueri, qm, sid, pseudo);

    }

}
