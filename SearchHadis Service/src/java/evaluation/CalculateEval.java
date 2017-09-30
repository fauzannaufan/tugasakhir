package evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static search.InitDB.DE;

/**
 *
 * @author M. Fauzan Naufan
 */
public class CalculateEval {

    public ArrayList<Double> Calculate(ArrayList<String> p_kueri, List<Map.Entry<String, Double>> list2) {
        ArrayList<Double> nilai = new ArrayList<>();
        int R = 0;
        double total_precision = 0;
        int jumlah_relevan = DE.countRelevantDocs(p_kueri);
        int i = 0;

        for (Map.Entry entry : list2) {
            String key = entry.getKey().toString();
            //Cek ground truth
            if (DE.isRelevant(p_kueri, key)) {
                R++;
                total_precision += (double) R / (i + 1);
            }
            i++;
        }

        double precision = (double) R / list2.size();
        double recall = (double) R / jumlah_relevan;
        double ap = (double) total_precision / jumlah_relevan;
        
        nilai.add(precision);
        nilai.add(recall);
        nilai.add(ap);
        
        return nilai;
    }

}
