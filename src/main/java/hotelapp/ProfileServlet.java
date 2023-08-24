package hotelapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;


/**
 * Servlet handling the user profile endpoint and html page
 */
public class ProfileServlet extends HttpServlet {
    /**
     * GET HTTP request shows the logged-in user's profile info
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("profile get @ " + uri);

        if (req.getSession(false) == null)
            res.sendRedirect("/auth");

        else if (query != null)
            res.sendRedirect("/profile");

        else {
            res.setContentType("text/html");
            res.setStatus(HttpServletResponse.SC_OK);

            String username = (String) req.getSession(false).getAttribute("username");
            String last_login = (String) req.getSession(false).getAttribute("last_login");

            VelocityContext vc = new VelocityContext();
            vc.put("username", username);
            vc.put("last_login", last_login);

            FavHotels fh = FavHotels.of(username);
            if (!fh.isEmpty())
                vc.put("fav_hotels", fh.toJsonArray());

            LinkHistory lh = LinkHistory.of(username);
            if (!lh.isEmpty())
                vc.put("link_history", lh.toJsonObject());

            Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/Profile.html");
            StringWriter writer = new StringWriter();
            tpl.merge(vc, writer);
            res.getWriter().println(writer);
        }
    }
}
