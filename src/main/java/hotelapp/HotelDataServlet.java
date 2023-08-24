package hotelapp;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet handling transmission of hotel data
 */
public class HotelDataServlet extends HttpServlet {
    /**
     * GET HTTP request returns a JsonObject with hotel(s)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("hoteldata get @ " + uri);

        if (req.getSession(false) == null)
            return;

        String username = (String) req.getSession().getAttribute("username");
        res.setContentType("text/html");

        JsonObject jo = new JsonObject();
        PrintWriter out = res.getWriter();
        String by, queryString = null;
        if ((by = StringEscapeUtils.escapeHtml4(req.getParameter("by"))) == null || by.isBlank() || (by.compareTo("name") != 0 && by.compareTo("id") != 0)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", "Insufficient request information");
        } else if (by.compareTo("name") == 0 &&
                ((queryString = StringEscapeUtils.escapeHtml4(req.getParameter("query"))) == null || queryString.isBlank() || !DBQueriesHandler.get().checkHotelName(queryString))) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", String.format("No hotel found with name fragment %s", (queryString == null || queryString.isBlank()) ? "[missing]" : "'" + queryString + "'"));
        } else if (by.compareTo("id") == 0 &&
                ((queryString = StringEscapeUtils.escapeHtml4(req.getParameter("query"))) == null || queryString.isBlank() || !DBQueriesHandler.get().checkHotelId(queryString))) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", String.format("No hotel found with id %s", (queryString == null || queryString.isBlank()) ? "[missing]" : "'" + queryString + "'"));
        } else if (by.compareTo("name") == 0) {
            // serviceable request, return hotels selected by name fragment
            jo.add("hotels", DBQueriesHandler.get().searchHotels(queryString, username));
        } else {
            // serviceable request, return hotel selected by id
            JsonArray ja = new JsonArray();
            ja.add(DBQueriesHandler.get().searchHotel(Long.parseLong(queryString), username));
            jo.add("hotels", ja);
            System.out.println(jo);
        }

        out.println(jo);
        out.flush();
    }
}
