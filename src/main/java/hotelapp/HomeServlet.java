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
 * Servlet for home page; has a search bar that fetches hotel data via dynamic JavaScript
 */
public class HomeServlet extends HttpServlet {
    /**
     * GET HTTP request to construct home page html file
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("home get @ " + uri);

        if (req.getSession(false) == null) {
            res.sendRedirect("/auth");
            return;

        } else if (req.getRequestURI().compareTo("/home") != 0 || req.getQueryString() != null) {
            res.sendRedirect("/home");
            return;
        }

        res.setContentType("text/html");
        res.setStatus(HttpServletResponse.SC_OK);

        VelocityContext vc = new VelocityContext();
        vc.put("username", req.getSession(false).getAttribute("username"));
        vc.put("last_login", req.getSession(false).getAttribute("last_login"));

        Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/Home.html");
        StringWriter writer = new StringWriter();
        tpl.merge(vc, writer);
        res.getWriter().println(writer);
    }
}
