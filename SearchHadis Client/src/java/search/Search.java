package search;

import com.sun.xml.ws.util.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
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

    private String searchHadis(String kueri, String skema, String sid) {
        Form form = new Form();
        Client client = ClientBuilder.newClient();

        form.param("kueri", kueri);
        form.param("skema", skema);
        form.param("sid", sid);
        String url = "http://localhost:8080/SearchHadis_Service/Search";
        //String url = "http://167.205.35.177:8080/SearchHadis_Service/Search";

        String result = client.target(url).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED), String.class);

        return result;
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

        String skema = request.getParameter("skema");
        String kueri = request.getParameter("kueri");
        Cookie[] cookies = request.getCookies();
        boolean notset = false;
        String sid = null;

        //Check SID
        if (cookies != null) {
            int x = 0;
            while (x < cookies.length && !cookies[x].getName().equals("sid")) {
                x++;
            }
            if (x < cookies.length) {
                sid = cookies[x].getValue();
            } else {
                notset = true;
            }
        }

        //Cari hadis
        String result = searchHadis(kueri, skema, sid);
        JSONParser parser = new JSONParser();
        JSONObject obj = new JSONObject();

        int idx = result.indexOf("|");

        if (notset) {
            String sid2 = result.substring(0, idx);
            Cookie c_sid = new Cookie("sid", sid2);
            response.addCookie(c_sid);
        }
        response.addCookie(new Cookie("kueri", kueri));
        response.addCookie(new Cookie("skema", skema));

        try {
            obj = (JSONObject) parser.parse(result.substring(idx + 1));
        } catch (ParseException ex) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONArray arr = (JSONArray) obj.get("hasil");
        Double tp = Math.ceil((double) arr.size() / 10);
        int total_page = tp.intValue();

        try (PrintWriter out = response.getWriter()) {
            //Bagian head, dan title
            out.println("<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
                    + "        <link rel=\"stylesheet\" href=\"main.css\">\n"
                    + "        <script type=\"text/javascript\" src=\"javascript.js\"></script>\n"
                    + "        <script type=\"text/javascript\">\n"
                    + "    function open_page(page_no) {\n"
                    + "        document.getElementById(\"page-\"+page_no).style.display = 'block';\n"
                    + "        document.getElementById(\"pagination-\"+page_no).classList.add('active');\n"
                    + "        for (var i=1;i<11;i++) {\n"
                    + "            if (i != page_no) {\n"
                    + "            document.getElementById(\"page-\"+i).style.display = 'none';\n"
                    + "            document.getElementById(\"pagination-\"+i).classList.remove('active');\n"
                    + "            }\n"
                    + "        }\n"
                    + "    }\n"
                    + "         </script>\n"
                    + "        <title>" + kueri + " - Cari Hadis!" + "</title>\n"
                    + "    </head>\n"
                    + "    <body onload=\"init()\">\n"
                    + "        <a href=\"index.jsp\"><h1>Cari Hadis!</h1></a>\n"
                    + "\n"
                    + "        <div class=\"main\">\n"
                    + "            <div class=\"search\"><form action=\"\" method=\"post\">\n");

            //Bagian search bar
            out.println("                <input id=\"searchbar2\" class=\"text\" name=\"kueri\" type=\"text\" value=\"" + kueri + "\" required>\n"
                    + "                <input id=\"searchbutton2\" type=\"submit\" value=\"Cari\"><br>\n");

            //Bagian radio button
            out.println("               Skema pembobotan :");
            switch (skema) {
                case "bim":
                    out.println("                <input type=\"radio\" name=\"skema\" value=\"vsm\">VSM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"bim\" checked>BIM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"okapi\">Okapi\n");
                    break;
                case "okapi":
                    out.println("                <input type=\"radio\" name=\"skema\" value=\"vsm\">VSM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"bim\">BIM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"okapi\" checked>Okapi\n");
                    break;
                default:
                    //VSM
                    out.println("                <input type=\"radio\" name=\"skema\" value=\"vsm\" checked>VSM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"bim\">BIM\n"
                            + "                <input type=\"radio\" name=\"skema\" value=\"okapi\">Okapi\n");
                    break;
            }
            out.println("           </form></div><br>\n");
            
            //Bagian evaluasi
            DecimalFormat df = new DecimalFormat("#0.000");
            df.setRoundingMode(RoundingMode.HALF_UP);
            out.println("<table align=\"center\" class=\"eval\">");
            out.println("<tr>");
            out.println("<th>Precision</th>");
            out.println("<th>Recall</th>");
            out.println("<th>Av. Precision</th>");
            out.println("</tr><tr>");
            out.println("<td>");
            out.println(df.format(obj.get("precision")));
            out.println("</td><td>");
            out.println(df.format(obj.get("recall")));
            out.println("</td><td>");
            out.println(df.format(obj.get("ap")));
            out.println("</td></tr></table><br>");

            //Bagian hasil pencarian
            if (arr.isEmpty()) {
                out.println("Maaf, hadis tidak ditemukan.");
            } else {
                for (int i = 0; i < total_page; i++) {
                    if (i != 0) {
                        out.println("<div id=\"page-" + (i + 1) + "\" style=\"display:none\">\n");
                    } else {
                        out.println("<div id=\"page-" + (i + 1) + "\" >\n");
                    }
                    int j = 0;
                    out.println("<table>");
                    while (j < 10 && i * 10 + j < arr.size()) {
                        JSONObject obj2 = (JSONObject) arr.get(i * 10 + j);
                        out.println("<tr>");
                        out.println("<td width=\"7%\"><a onclick=\"vote_down('"+obj2.get("key").toString()+"')\"><img id=\"down-"+obj2.get("key").toString()+"\" src=\"down0.png\"></a></td>");
                        out.println("<td width=\"7%\"><a onclick=\"vote_up('"+obj2.get("key").toString()+"')\"><img id=\"up-"+obj2.get("key").toString()+"\" src=\"up0.png\"></a></td>");
                        out.println("<td>");
                        String imam = StringUtils.capitalize(obj2.get("imam").toString());
                        out.println("<h3 class=\"topic\"><a href=\"http://hadits.in/?"+imam+"/"+obj2.get("haditsId").toString()+"\">"
                                + "HR. " + imam + " No. " + obj2.get("haditsId").toString()
                                + "</a></h3>");
                        out.println("<p class=\"indo\">" + obj2.get("snippet").toString() + "</p>");
                        out.println("</td>");
                        j++;
                        out.println("</tr>");
                    }
                    out.println("</table>");
                    out.println("</div>\n");
                }

                //Bagian nomor halaman
                out.println("<div class=\"pagination\">\n"
                        + "  <a>&laquo;</a>\n");
                out.println("  <a id=\"pagination-1\" class=\"active\" onclick=\"open_page(1)\">1</a>\n");
                for (int i = 1; i < total_page; i++) {
                    out.println("  <a id=\"pagination-" + (i + 1) + "\" onclick=\"open_page(" + (i + 1) + ")\">" + (i + 1) + "</a>\n");
                }
                out.println("  <a>&raquo;</a>\n"
                        + "</div>\n");
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
