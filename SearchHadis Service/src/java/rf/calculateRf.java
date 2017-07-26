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
        
        RelevanceFeedback RF = new RelevanceFeedback();
        Database DB = new Database();
        ProsesTeks PT = new ProsesTeks();
        ArrayList<String> p_kueri = PT.prosesKueri(kueri);
        
        ArrayList<String> VR = DB.getVR(p_kueri);
        ArrayList<String> VNR = DB.getVNR(p_kueri);
        ArrayList<Double> pt = DB.getpt(p_kueri);
        
        String a = "";
        if (pt != null && VR != null) {
            RF.calculateProbBIM(kueri, VR, pt);
            a = "1";
        }
        if (VR != null && VNR != null) {
            RF.calculateRfOkapi(kueri, VR, VNR);
            a = "2";
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
