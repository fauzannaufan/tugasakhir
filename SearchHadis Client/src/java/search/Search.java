package search;

import com.sun.xml.ws.util.StringUtils;
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

    private void hitungRF(String kueri) {
        //Formulasi Relevance Feedback
        Form form = new Form();
        Client client = ClientBuilder.newClient();
        String url = "http://localhost:8080/SearchHadis_Service/calculateRf";

        form.param("kueri", kueri);
        client.target(url).request(MediaType.TEXT_HTML)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);
    }

    private JSONObject searchHadis(String kueri, String skema) {
        Form form = new Form();
        Client client = ClientBuilder.newClient();
        String url = "";
        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();

        form.param("kueri", kueri);
        if (skema.equals("bim")) {
            url = "http://localhost:8080/SearchHadis_Service/SearchBIM";
        } else if (skema.equals("okapi")) {
            url = "http://localhost:8080/SearchHadis_Service/SearchOkapi";
        }

        String result = client.target(url).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);

        try {
            obj = (JSONObject) parser.parse(result);
        } catch (ParseException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }

        return obj;
    }
    
    private void postHasiltoDB(String kueri, ArrayList<String> hasil, Object pt, Object ut) {
        JSONObject obj = new JSONObject();
        obj.put("kueri", kueri);
        obj.put("ids", hasil);
        obj.put("pt", pt);
        obj.put("ut", ut);

        Form form = new Form();
        form.param("param", obj.toJSONString());

        String url = "http://localhost:8080/SearchHadis_Service/addRelevantDocs";
        Client client = ClientBuilder.newClient();
        client.target(url).request(MediaType.TEXT_HTML)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);
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
        
        ArrayList<String> result_key = new ArrayList<>();

        String skema = request.getParameter("skema");
        String kueri = request.getParameter("kueri");

        //hitungRF(kueri);
        JSONObject obj = searchHadis(kueri, skema);
        JSONArray arr = (JSONArray) obj.get("hasil");

        try (PrintWriter out = response.getWriter()) {
            //Bagian head, dan title
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
                    + "        <div class=\"main\">\n"
                    + "            <div><form action=\"\" method=\"post\">\n");

            //Bagian search bar
            out.println("                <input id=\"searchbar2\" class=\"text\" name=\"kueri\" type=\"text\" value=\"" + kueri + "\">\n"
                    + "                <input id=\"searchbutton2\" type=\"submit\" value=\"Cari\">\n");

            //Bagian radio button
            out.println("               Skema pembobotan :");
            if (skema.equals("bim")) {
                out.println("                <input type=\"radio\" name=\"skema\" value=\"bim\" checked>BIM\n"
                        + "                <input type=\"radio\" name=\"skema\" value=\"okapi\">Okapi\n");
            } else {
                out.println("                <input type=\"radio\" name=\"skema\" value=\"bim\">BIM\n"
                        + "                <input type=\"radio\" name=\"skema\" value=\"okapi\" checked>Okapi\n");
            }
            out.println("           </form></div><br>\n");

            //Bagian hasil pencarian
            if (arr.isEmpty()) {
                out.println("Maaf, hadis tidak ditemukan.");
            } else {
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject obj2 = (JSONObject) arr.get(i);
                    result_key.add(obj2.get("key").toString());
                    String imam = StringUtils.capitalize(obj2.get("imam").toString());
                    out.println("<h3 class=\"topic\"><a href=\"hadis.jsp?id="
                            + obj2.get("key").toString() + "\">" 
                            + "HR. " + imam + " No. " + obj2.get("haditsId").toString() 
                            + "</a></h3>");
                }
            }
            out.println("</div>\n"
                    + "    </body>\n"
                    + "</html>");
        }
        
        //postHasiltoDB(kueri, result_key, obj.get("pt"), obj.get("ut"));
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
