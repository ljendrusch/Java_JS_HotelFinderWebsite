package hotelapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;


/**
 * Servlet handling the display of a hotel and all of its reviews
 */
public class HotelServlet extends HttpServlet {
    /**
     * GET HTTP request shows a hotel and all of its reviews
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("hotel get @ " + uri);

        String idString;
        if (req.getSession(false) == null) {
            res.sendRedirect("/auth");
            return;
        }

        else if ((idString = StringEscapeUtils.escapeHtml4(req.getParameter("id"))) == null || idString.isBlank() || !idString.matches("^\\d+$")) {
            res.sendRedirect("/home");
            return;
        }

        res.setContentType("text/html");
        res.setStatus(HttpServletResponse.SC_OK);

        VelocityContext vc = new VelocityContext();
        vc.put("username", req.getSession(false).getAttribute("username"));
        vc.put("last_login", req.getSession(false).getAttribute("last_login"));
        vc.put("hotelid", idString);

        Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/Hotel.html");
        StringWriter writer = new StringWriter();
        tpl.merge(vc, writer);
        res.getWriter().println(writer);
    }
}
