package hotelapp;

import com.google.gson.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.PrintWriter;


/**
 * Servlet handling the transmission and manipulation of favorited hotel status
 */
public class FavHotelsServlet extends HttpServlet {
    /**
     * GET HTTP request manipulates a user's fav hotels data
     * or returns a JsonObject with a user's favorite hotels
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("favsdata get @ " + uri);
        // ?act=click/clear/get&id=hotelid (id only on click)

        if (req.getSession(false) == null)
            return;

        res.setContentType("text/html");

        JsonObject jo = new JsonObject();
        PrintWriter out = res.getWriter();
        String username = (String) req.getSession().getAttribute("username");
        String action, idString = null;
        if ((action = StringEscapeUtils.escapeHtml4(req.getParameter("act"))) == null || action.isBlank()
                || (action.compareTo("click") != 0 && action.compareTo("clear") != 0 && action.compareTo("get") != 0)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", String.format("Incorrect request action '%s'", (action == null || action.isBlank()) ? "[missing]" : "'" + action + "'"));
        } else if (action.compareTo("click") == 0 &&
                ((idString = StringEscapeUtils.escapeHtml4(req.getParameter("id"))) == null || idString.isBlank() || !DBQueriesHandler.get().checkHotelId(idString))) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jo.addProperty("Error", String.format("No hotel found with id %s", (idString == null || idString.isBlank()) ? "[missing]" : "'" + idString + "'"));
        } else {
            res.setStatus(HttpServletResponse.SC_OK);

            FavHotels fh = FavHotels.of(username);
            if (action.compareTo("click") == 0) {
                boolean isFav = fh.toggleFavStatus(idString);
                jo.addProperty("clicked", idString);
                jo.addProperty("fav", isFav);
            } else {
                if (action.compareTo("clear") == 0)
                    fh.clear();
                jo.add("fav_hotels", fh.toJsonArray());
            }
        }

        out.println(jo);
        out.flush();
    }
}
