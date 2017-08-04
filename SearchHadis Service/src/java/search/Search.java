package search;

import backend.Database;
import backend.ProsesTeks;
import backend.RelevanceFeedback;
import backend.SearchHadis;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author M. Fauzan Naufan
 */
public class Search extends HttpServlet {

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

        String kueri = request.getParameter("kueri");
        String skema = request.getParameter("skema");
        String sid = request.getParameter("sid");

        if (sid == null) {
            HttpSession session = request.getSession();
            sid = session.getId();
        }

        //calculate RF
        RelevanceFeedback RF = new RelevanceFeedback();
        Database DB = new Database();
        ProsesTeks PT = new ProsesTeks();
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);

        ArrayList<String> VR = DB.getVR(skema, sid, p_kueri);
        ArrayList<String> VNR = DB.getVNR(skema, sid, p_kueri);

        if (VNR.size() > 0 && VR.size() > 0) {
            if (skema.equals("bim")) {
                ArrayList<Double> pt = DB.getPt(p_kueri, sid);
                ArrayList<Double> ut = DB.getUt(p_kueri, sid);
                RF.calculateProbBIM(kueri, VR, VNR, pt, ut, sid);
            } else if (skema.equals("okapi")) {
                RF.calculateRfOkapi(kueri, VR, VNR, sid);
            }
        }

        JSONObject hasil = new JSONObject();

        if (skema.equals("bim")) {
            hasil = new SearchHadis().searchBIM(kueri, sid);
        } else if (skema.contains("okapi")) {
            hasil = new SearchHadis().searchOkapi(kueri, sid);
        }

        //Submit hasil pencarian ke DB
        ArrayList<String> ids = new ArrayList<>();
        ArrayList<String> p_kueri2 = PT.prosesKueri(kueri);
        JSONArray arr = (JSONArray) hasil.get("hasil");
        for (int i = 0; i < arr.size(); i++) {
            JSONObject arr_elem = (JSONObject) arr.get(i);
            ids.add(arr_elem.get("key").toString());
        }
        DB.addRelevantDocs(skema, sid, p_kueri2, ids, (ArrayList<Double>)hasil.get("pt"), (ArrayList<Double>)hasil.get("ut"));
        DB.closeConnection();

        try (PrintWriter out = response.getWriter()) {
            out.println(sid + "|");
            out.println(hasil);
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
