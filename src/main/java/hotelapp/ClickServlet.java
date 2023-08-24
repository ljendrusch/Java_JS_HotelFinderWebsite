package hotelapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import java.io.IOException;


/**
 * Servlet handling link click tracking / clearing
 */
public class ClickServlet extends HttpServlet {
    /**
     * HTTP GET Request on link click, adds a click to DB and redirects to link
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("click get @ " + uri);

        if (req.getSession(false) == null)
            return;

        String link;
        if ((link = StringEscapeUtils.escapeHtml4(req.getParameter("link"))) == null || link.isBlank() || !DBQueriesHandler.get().checkLink(link)) {
            res.sendRedirect("/home");
            return;
        }

        String username = (String) req.getSession(false).getAttribute("username");
        LinkHistory lh = LinkHistory.of(username);
        lh.addClick(link);

        res.sendRedirect("https://www." + link);
    }

    /**
     * HTTP Post request, clears the user's link history
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("click post @ " + req.getRequestURI());

        if (req.getSession(false) == null)
            return;

        String username = (String) req.getSession(false).getAttribute("username");
        LinkHistory.clear(username);

        res.sendRedirect("/profile");
    }
}
