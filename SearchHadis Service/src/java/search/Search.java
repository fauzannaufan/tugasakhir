package search;

import backend.ProsesTeks;
import rf.RelevanceFeedback;
import backend.SearchHadis;
import evaluation.GroundTruth;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import static search.InitDB.*;

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

        //Create Ground Truth
        GroundTruth GT = new GroundTruth();
        GT.createGT(kueri);

        if (sid == null) {
            HttpSession session = request.getSession();
            sid = session.getId();
        }

        //calculate RF
        RelevanceFeedback RF = new RelevanceFeedback();
        ProsesTeks PT = new ProsesTeks();
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);

        ArrayList<String> VR = DBRF.getVR(sid, p_kueri);
        ArrayList<String> VNR = DBRF.getVNR(sid, p_kueri);

        switch (skema) {
            case "bim":
                if (DBRF.checkBIM(p_kueri, sid)) {
                    ArrayList<Double> pt = DBRF.getPt(p_kueri, sid);
                    ArrayList<Double> ut = DBRF.getUt(p_kueri, sid);
                    RF.calculateProbBIM(kueri, VR, VNR, pt, ut, sid);
                }
                break;
            case "okapi":
                if (!VR.isEmpty() || !VNR.isEmpty()) {
                    RF.calculateRfOkapi(kueri, VR, VNR, sid, false);
                }
                break;
            default:
                if (!VR.isEmpty() || !VNR.isEmpty()) {
                    RF.rocchio(kueri, VR, VNR, sid, false);
                }
                break;
        }

        JSONObject hasil;

        //Search
        switch (skema) {
            case "bim":
                hasil = new SearchHadis().searchBIM(kueri, sid, false);
                break;
            case "okapi":
                hasil = new SearchHadis().searchOkapi(kueri, sid, false);
                break;
            default:
                hasil = new SearchHadis().searchVSM(kueri, sid, false);
                break;
        }

        //Kueri ke DB
        ArrayList<String> p_kueri2 = PT.prosesKueri(kueri);
        DBRF.addRelevantDocs(sid, p_kueri2, (ArrayList<Double>) hasil.get("pt"), (ArrayList<Double>) hasil.get("ut"));
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
