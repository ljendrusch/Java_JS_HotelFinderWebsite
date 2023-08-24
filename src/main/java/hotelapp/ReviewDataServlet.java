package hotelapp;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet handling the transmission and manipulation of review data
 */
public class ReviewDataServlet extends HttpServlet {
    private static final int LIMIT = 10;

    /**
     * GET HTTP request returns a JsonObject with review(s)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("reviewdata get @ " + uri);

        if (req.getSession(false) == null)
            return;

        res.setContentType("text/html");

        JsonObject jo = new JsonObject();
        PrintWriter out = res.getWriter();
        String idString, offsetString, nextVprevString;
        if ((idString = StringEscapeUtils.escapeHtml4(req.getParameter("id"))) == null || idString.isBlank() || !DBQueriesHandler.get().checkHotelId(idString)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", String.format("No hotel found with id %s", (idString == null || idString.isBlank()) ? "[missing]" : "'" + idString + "'"));
        } else if ((offsetString = StringEscapeUtils.escapeHtml4(req.getParameter("offset"))) == null || offsetString.isBlank() || !offsetString.matches("^\\d+$")
                || (nextVprevString = StringEscapeUtils.escapeHtml4(req.getParameter("dir"))) == null || nextVprevString.isBlank()
                || (nextVprevString.compareTo("next") != 0 && nextVprevString.compareTo("prev") != 0)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", "Insufficient request information");
        } else {
            long id = Long.parseLong(idString);
            int offset = Integer.parseInt(offsetString);
            boolean nextVprev = nextVprevString.compareTo("next") == 0;
            if (!nextVprev) offset -= 20;

            jo = DBQueriesHandler.get().getReviewsSlice(id, LIMIT, offset);
        }

        out.println(jo);
        out.flush();
    }
}
