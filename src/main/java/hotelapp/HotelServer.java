package hotelapp;

import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;


/**
 * Server for TravelHelper website
 */
public class HotelServer {
    public static final int PORT = 8010;

    public static void main(String[] args) {
        DBTablesHandler.get().checkTables();

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.addServlet(AuthServlet.class, "/auth");
        handler.addServlet(AuthServlet.class, "/login");
        handler.addServlet(AuthServlet.class, "/register");
        handler.addServlet(LogoutServlet.class, "/logout");

        handler.addServlet(HomeServlet.class, "/");
        handler.addServlet(HomeServlet.class, "/home");
        handler.addServlet(HomeServlet.class, "/hotelsearch");

        handler.addServlet(HotelServlet.class, "/hotel");
        handler.addServlet(MyReviewsServlet.class, "/myReviews");
        handler.addServlet(ProfileServlet.class, "/profile");

        handler.addServlet(ClickServlet.class, "/click");
        handler.addServlet(ReviewDataServlet.class, "/reviewdata");
        handler.addServlet(HotelDataServlet.class, "/hoteldata");
        handler.addServlet(FavHotelsServlet.class, "/favsdata");

        VelocityEngine vel = new VelocityEngine();
        vel.init();
        handler.setAttribute("templateEngine", vel);

        ResourceHandler resource = new ResourceHandler();
        resource.setDirectoriesListed(false);
        resource.setResourceBase("static");

        Server server = new Server(PORT);
        server.setHandler(new HandlerList(resource, handler));
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
