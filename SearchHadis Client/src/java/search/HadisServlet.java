package search;

import com.sun.xml.ws.util.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
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
public class HadisServlet extends HttpServlet {

    private void setRelevant(String kueri, String skema, String id) {
        JSONObject obj = new JSONObject();
        obj.put("skema", skema);
        obj.put("kueri", kueri);
        obj.put("id", id);

        Form form = new Form();
        form.param("param", obj.toJSONString());

        String url = "http://localhost:8080/SearchHadis_Service/setRelevant";
        Client client = ClientBuilder.newClient();
        client.target(url).request(MediaType.TEXT_HTML)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);
    }

    private String getDataHadis(String id) {
        Form form = new Form();
        form.param("id", id);

        String url = "http://localhost:8080/SearchHadis_Service/getDataHadis";
        Client client = ClientBuilder.newClient();
        String hasil = client.target(url).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);

        return hasil;
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
        String id = request.getParameter("id");
        String kueri = request.getParameter("kueri");
        String skema = request.getParameter("skema");

        setRelevant(kueri, skema, id);
        String s = getDataHadis(id);

        JSONObject obj = new JSONObject();
        JSONParser parser = new JSONParser();

        try {
            obj = (JSONObject) parser.parse(s);
        } catch (ParseException ex) {
            Logger.getLogger(HadisServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONArray arr = (JSONArray) obj.get("hadis");
        JSONObject obj2 = (JSONObject) arr.get(0);
        String imam = StringUtils.capitalize(obj2.get("imam").toString());
        String indo = obj2.get("indo").toString().replace("[", "").replace("]", "");
        
        JSONObject obj3 = new JSONObject();
        try {
            obj3 = (JSONObject) parser.parse(obj2.get("related").toString());
        } catch (ParseException ex) {
            Logger.getLogger(HadisServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JSONArray related = (JSONArray)obj3.get("related");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<div class=\"main\">\n");
            out.println("<h3 class=\"topic\">HR. " + imam + " No. " + obj2.get("haditsId").toString() + "</h3>\n");
            out.println("<h4>Kitab : " + obj2.get("kitab").toString() + "</h4>");
            out.println("<h4>Bab : " + obj2.get("bab").toString() + "</h4>");
            out.println("<p class=\"indo2\">" + obj2.get("arab").toString() + "</p>\n");
            out.println("<p class=\"indo2\">" + indo + "</p><br>\n");
            out.println("<h3 class=\"topic\">Hadis-hadis terkait</h3>\n");
            for (int i = 0; i < related.size(); i++) {
                JSONObject obj4 = (JSONObject) related.get(i);
                String imam2 = StringUtils.capitalize(obj4.get("imam").toString());
                out.println("<a href=\"hadis.jsp?id="+obj4.get("key").toString()+"&kueri="+kueri+"&skema="+skema+"\">");
                out.println("<b>HR. "+imam2+" No. "+obj4.get("haditsId").toString()+"</b>\n");
                out.println("</a><br>");
            }
            if (related.isEmpty()) {
                out.println("<h4>Tidak ada hadis terkait.</h4>");
            }
            out.println("</div><br><br>\n");
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
