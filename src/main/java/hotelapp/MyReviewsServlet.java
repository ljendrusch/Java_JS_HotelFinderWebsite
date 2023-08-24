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
import java.util.List;
import java.util.Random;


/**
 * Servlet handling adding, editing, and deleting user's reviews
 */
public class MyReviewsServlet extends HttpServlet {
    /**
     * GET HTTP request to display all of the user's reviews,
     * delete buttons for each, links to edit pages for each,
     * and a form for making a new review
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String uri = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null && !query.isBlank()) uri = uri + "?" + query;
        System.out.println("myReviews get @ " + uri);

        String reviewId;
        if (req.getSession(false) == null)
            res.sendRedirect("/auth");

        else if ((reviewId = StringEscapeUtils.escapeHtml4(req.getParameter("id"))) != null && !reviewId.isBlank()) {
            res.setContentType("text/html");

            VelocityContext vc = new VelocityContext();
            vc.put("username", req.getSession(false).getAttribute("username"));
            vc.put("last_login", req.getSession(false).getAttribute("last_login"));

            List<String> review = DBQueriesHandler.get().findMyReview((String) req.getSession(false).getAttribute("username"), reviewId);
            if (review.size() > 0) {
                String type;
                if ((type = StringEscapeUtils.escapeHtml4(req.getParameter("type"))) != null && type.compareTo("delete") == 0) {
                    DBQueriesHandler.get().deleteReview((String) req.getSession(false).getAttribute("username"), reviewId);
                    res.sendRedirect("/myReviews");
                    return;
                }

                res.setStatus(HttpServletResponse.SC_OK);
                vc.put("review", review);
                vc.put("reviewId", reviewId);
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                vc.put("rerr", reviewId);
            }

            Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/MyReviews.html");
            StringWriter writer = new StringWriter();
            tpl.merge(vc, writer);
            res.getWriter().println(writer);
        }

        else if (req.getQueryString() != null)
            res.sendRedirect("/myReviews");

        else {
            res.setContentType("text/html");
            res.setStatus(HttpServletResponse.SC_OK);

            VelocityContext vc = new VelocityContext();
            vc.put("username", req.getSession(false).getAttribute("username"));
            vc.put("last_login", req.getSession(false).getAttribute("last_login"));

            List<List<String>> myReviews = DBQueriesHandler.get().getMyReviews((String) req.getSession(false).getAttribute("username"));
            if (myReviews.size() > 0) {
                vc.put("myReviews", myReviews);
            } else {
                vc.put("noReviews", "You have no reviews! Make one above");
            }

            Template tpl = ((VelocityEngine) req.getServletContext().getAttribute("templateEngine")).getTemplate("static/MyReviews.html");
            StringWriter writer = new StringWriter();
            tpl.merge(vc, writer);
            res.getWriter().println(writer);
        }
    }

    /**
     * POST HTTP request to add or edit a review
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        System.out.println("myReviews post @ " + req.getRequestURI());

        String type;
        if ((type = StringEscapeUtils.escapeHtml4(req.getParameter("type"))) != null) {
            if (type.compareTo("insert") == 0) {
                String hotelIdString;
                if ((hotelIdString = StringEscapeUtils.escapeHtml4(req.getParameter("id"))) != null
                        && DBQueriesHandler.get().checkHotelId(hotelIdString)) {
                    Random r = new Random();
                    String reviewId = String.format("%08x%08x%08x", r.nextInt(), r.nextInt(), r.nextInt());
                    long hotelId = Long.parseLong(hotelIdString);
                    String username = (String) req.getSession(false).getAttribute("username");
                    String title = StringEscapeUtils.escapeHtml4(req.getParameter("title"));
                    String text = StringEscapeUtils.escapeHtml4(req.getParameter("text"));
                    DBQueriesHandler.get().insertReview(reviewId, hotelId, username, title, text);
                }
            } else if (type.compareTo("update") == 0) {
                String title = StringEscapeUtils.escapeHtml4(req.getParameter("title"));
                String text = StringEscapeUtils.escapeHtml4(req.getParameter("text"));
                String username = (String) req.getSession(false).getAttribute("username");
                String reviewId = StringEscapeUtils.escapeHtml4(req.getParameter("id"));
                DBQueriesHandler.get().updateReview(title, text, username, reviewId);
            }
        }

        res.sendRedirect("/myReviews");
    }
}
