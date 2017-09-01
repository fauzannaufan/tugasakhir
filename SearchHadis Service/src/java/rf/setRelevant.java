package rf;

import backend.ProsesTeks;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static search.InitDB.*;

/**
 *
 * @author M. Fauzan Naufan
 */
public class setRelevant extends HttpServlet {

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
        
        String kueri = request.getParameter("kueri");
        String sid = request.getParameter("sid");
        String id = request.getParameter("id");
        String status = request.getParameter("status");
        ArrayList<String> terms = PT.prosesKueri(kueri);
        
        System.out.println(status);
        switch (status) {
            case "sR":
                DBRF.setRelevant(terms, sid, id);
                break;
            case "sNR":
                DBRF.setNonRelevant(terms, sid, id);
                break;
            case "uR":
                DBRF.unRelevant(terms, sid, id);
                break;
            default:
                DBRF.unNonRelevant(terms, sid, id);
                break;
        }
        try (PrintWriter out = response.getWriter()) {
            out.println("Sukses");
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
