package search;

import com.sun.xml.ws.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        response.setContentType("text/html;charset=UTF-8");

        String method = request.getParameter("method");
        String kueri = request.getParameter("kueri");
        
        String url = "";

        Form form = new Form();
        form.param("kueri", kueri);

        Client client = ClientBuilder.newClient();

        if (method.equals("bim")) {
            url = "http://localhost:8080/SearchHadis_Service/SearchBIM";
        } else if (method.equals("okapi")) {
            url = "http://localhost:8080/SearchHadis_Service/SearchOkapi";
        }

        String result = client.target(url).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);

        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();
        ArrayList<String> p_kueri = new ProsesTeks().prosesKueri(kueri);
        try {
            obj = (JSONObject) parser.parse(result);
        } catch (ParseException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JSONArray arr = (JSONArray) obj.get("hasil");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                    + "        <link rel=\"stylesheet\" href=\"main.css\">\n"
                    + "        <title>JSP Page</title>\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <a href=\"index.jsp\"><h1>Cari Hadis!</h1></a>\n"
                    + "\n"
                    + "        <div class=\"main\">");
            out.println("<form action=\"Search\" method=\"post\">\n");
            if (method.equals("okapi")) {
                out.println("                <input type=\"radio\" name=\"method\" value=\"okapi\" checked>Okapi\n"
                        + "                <input type=\"radio\" name=\"method\" value=\"bim\">BIM\n");
            } else {
                out.println("                <input type=\"radio\" name=\"method\" value=\"okapi\">Okapi\n"
                        + "                <input type=\"radio\" name=\"method\" value=\"bim\" checked>BIM\n");
            }
            out.println("                <input id=\"searchbar2\" class=\"text\" name=\"kueri\" type=\"text\" value=\"" + kueri + "\">\n"
                    + "                <input id=\"searchbutton2\" type=\"submit\" value=\"Cari\">\n"
                    + "            </form><br>");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj3 = (JSONObject) arr.get(i);
                String imam = StringUtils.capitalize(obj3.get("imam").toString());
                String indo = obj3.get("indo").toString().replace("[", "").replace("]", "");
                for (String s : p_kueri) {
                    indo = indo.replaceAll("(?i)("+s+")", "<b>$1</b>");
                }
                out.println("<p class=\"topic\">" + "HR. " + imam + " No. " + obj3.get("haditsId").toString() + "</p>");
                out.println("<p class=\"indo\">" + indo + "</p>");
            }
            if (arr.isEmpty()) {
                out.println("Maaf, hadis tidak ditemukan.");
            }
            out.println("</div>\n"
                    + "    </body>\n"
                    + "</html>");
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
