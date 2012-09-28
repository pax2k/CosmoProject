package no.pax.cosmo.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Created: rak
 * Date: 27.09.12
 */
public class CosmoServer {
    Server server;

    public CosmoServer() throws Exception {
        // Get the server port and create a server
        int port = 8080;
        server = new Server(port);

        // Create the servlet handler and define the Chat servlet
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(CosmoServlet.class,"/cosmo/*");

        // Create a resource handler for static content (eg index.html, chat.js, chat.css)
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource("no/pax/docs/"));

        // Create the default handler for all other requests
        DefaultHandler defaultHandler = new DefaultHandler();

        // Set the handlers on the server as a handler list
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] {servletHandler,resourceHandler,defaultHandler});
        server.setHandler(handlers);

        // Start the server and join it to avoid exit.
        server.start();
    }

    public void stop() throws Exception {
         server.stop();
    }
}
