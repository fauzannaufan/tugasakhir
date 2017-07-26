package rf;

import backend.Database;
import backend.ProsesTeks;
import backend.RelevanceFeedback;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author M. Fauzan Naufan
 */
public class calculateRf extends HttpServlet {

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
        
        String kueri = request.getParameter("kueri");
        String skema = request.getParameter("skema");
        
        RelevanceFeedback RF = new RelevanceFeedback();
        Database DB = new Database();
        ProsesTeks PT = new ProsesTeks();
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        
        ArrayList<String> VR = DB.getVR(skema, p_kueri);
        ArrayList<String> VNR = DB.getVNR(skema, p_kueri);
        
        String a = "";
        if (VNR.size() > 0) {
            if (skema.equals("bim")) {
                ArrayList<Double> pt = DB.getPt(p_kueri);
                ArrayList<Double> ut = DB.getUt(p_kueri);
                if (VR == null) {
                    VR = new ArrayList<>();
                }
                RF.calculateProbBIM(kueri, VR, VNR, pt, ut);
                a = "1";
            } else if (skema.equals("okapi")) {
                if (VR == null) {
                    VR = new ArrayList<>();
                }
                RF.calculateRfOkapi(kueri, VR, VNR);
                a = "2";
            }
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.println(a);
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
