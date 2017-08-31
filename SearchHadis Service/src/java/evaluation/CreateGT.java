package evaluation;

import backend.ProsesTeks;
import backend.SearchHadis;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static search.InitDB.*;

/**
 *
 * @author novan
 */
public class CreateGT extends HttpServlet {

    HashSet<String> hs;
    ArrayList<String> ids;

    public void prosesArray(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JSONObject obj = (JSONObject) arr.get(i);

            JSONParser parser = new JSONParser();
            JSONObject related = new JSONObject();
            try {
                related = (JSONObject) parser.parse(obj.get("related").toString());
            } catch (ParseException ex) {
                
            }
            JSONArray rel = (JSONArray) related.get("related");
            for (int j = 0; j < rel.size(); j++) {
                JSONObject obj2 = (JSONObject) rel.get(j);
                if (!obj2.get("imam").equals("ahmad") && !obj2.get("imam").equals("bukhari")) {
                    hs.add(obj2.get("key").toString());
                }
            }
        }
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ProsesTeks PT = new ProsesTeks();
        SearchHadis SH = new SearchHadis();

        String kueri = request.getParameter("kueri");
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);

        //Cek apakah sudah ada atau belum
        if (DE.checkGT(p_kueri)) {
            JSONObject vsm = SH.searchVSM(kueri, "groundTruth", true);
            JSONObject bim = SH.searchBIM(kueri, "groundTruth", true);
            JSONObject okapi = SH.searchOkapi(kueri, "groundTruth", true);

            JSONArray arr1 = (JSONArray) bim.get("hasil");
            JSONArray arr2 = (JSONArray) okapi.get("hasil");
            JSONArray arr3 = (JSONArray) vsm.get("hasil");
            
            prosesArray(arr1);
            prosesArray(arr2);
            prosesArray(arr3);

            ids.addAll(hs);
            DE.insertGT(p_kueri, ids);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
