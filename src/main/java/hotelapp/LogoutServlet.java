package hotelapp;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;


/**
 * Servlet handling ending the session / auth cookie and logging out
 */
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("logout get @ " + req.getRequestURI());

        HttpSession session = req.getSession(false);
        if (session != null)
            session.invalidate();

        res.sendRedirect("/auth");
    }
}
