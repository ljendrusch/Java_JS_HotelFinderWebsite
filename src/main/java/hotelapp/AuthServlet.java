package hotelapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.StringWriter;


/**
 * Servlet handling logging in, registering, and initiating sessions / auth cookies
 */
public class AuthServlet extends HttpServlet {
    /**
     * GET HTTP request shows a blank login / register form
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("auth get @ " + uri);

        // if user already logged in
        if (req.getSession(false) != null)
            res.sendRedirect("/home");

        // homogenize login / register uri
        else if (req.getRequestURI().compareTo("/auth") != 0 || req.getQueryString() != null)
            res.sendRedirect("/auth");

        else {
            res.setContentType("text/html");
            res.setStatus(HttpServletResponse.SC_OK);

            VelocityEngine vel = (VelocityEngine) req.getServletContext().getAttribute("templateEngine");
            Template tpl = vel.getTemplate("static/Auth.html");
            StringWriter writer = new StringWriter();
            tpl.merge(new VelocityContext(), writer);
            res.getWriter().println(writer);
        }
    }

    /**
     * POST request handles logging in / registering and initiating session / auth cookie
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("auth post @ " + req.getRequestURI());

        res.setContentType("text/html");

        String username = StringEscapeUtils.escapeHtml4(req.getParameter("username"));
        String password = StringEscapeUtils.escapeHtml4(req.getParameter("password"));

        VelocityContext vc = new VelocityContext();
        boolean serviceable = true;
        if (username == null || username.isBlank()) {
            serviceable = false;
            vc.put("uerr", "Please enter a username");
        } else if (!username.matches("^[a-zA-Z](_(?!([._]))|\\.(?!([_.]))|[a-zA-Z0-9]){4,18}[a-zA-Z0-9]$")) {
            serviceable = false;
            vc.put("uerr", "Username not allowed (see below)");
        }

        if (password == null || password.isBlank()) {
            serviceable = false;
            vc.put("perr", "Please enter a password");
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[_.@$!%*?&])[A-Za-z\\d_.@$!%*?&]{8,20}$")) {
            serviceable = false;
            vc.put("perr", "Password not allowed (see below)");
        }

        DBQueriesHandler dbHandler = DBQueriesHandler.get();
        if (serviceable) {
            // if user clicked 'register'
            if (StringEscapeUtils.escapeHtml4(req.getParameter("register")) != null) {
                // if credentials are valid, unique, and insert to db
                if (dbHandler.registerUser(username, password)) {
                    vc.put("register", username);
                } else {
                    serviceable = false;
                    vc.put("perr", "Username already taken");
                }
            // if user clicked 'login'
            } else if (StringEscapeUtils.escapeHtml4(req.getParameter("login")) != null) {
                // if credentials matched a db entry
                if (dbHandler.loginUser(username, password)) {
                    vc.put("login", username);
                } else {
                    serviceable = false;
                    vc.put("perr", "Username and/or password incorrect");
                }
            }
        }

        if (serviceable) {
            HttpSession session = req.getSession();
            session.setAttribute("username", username);
            session.setAttribute("last_login", dbHandler.cycleLastLogin(username));
            res.sendRedirect("/home");
        } else {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/Auth.html");
            StringWriter writer = new StringWriter();
            tpl.merge(vc, writer);
            res.getWriter().println(writer);
        }
    }
}
