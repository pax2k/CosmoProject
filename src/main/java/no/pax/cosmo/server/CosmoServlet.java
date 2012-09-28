package no.pax.cosmo.server;


import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/* ------------------------------------------------------------ */

/**
 * Example WebSocket Chat Servlet.
 * <p>This servlet demonstrates the Jetty server side WebSocket APIs by
 * implementing a (very) simple chat room.   All connections received with the "chat"
 * sub protocol name are added to the _members set. All other connections are rejected.
 * Any message received on any connection is assumed to a chat message and
 * is echoed verbatim to all connections in the _member set.
 */
public class CosmoServlet extends HttpServlet {
    private WebSocketFactory _wsFactory;
    private final Set<CosmoWebSocket> _members = new CopyOnWriteArraySet<CosmoWebSocket>();

    /* ------------------------------------------------------------ */

    /**
     * Initialise the servlet by creating the WebSocketFactory.
     */
    @Override
    public void init() throws ServletException {
        // Create and configure WS factory
        _wsFactory = new WebSocketFactory(new WebSocketFactory.Acceptor() {
            public boolean checkOrigin(HttpServletRequest request, String origin) {
                // Allow all origins
                return true;
            }

            public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                // Return new ChatWebSocket for chat protocol connections
                if ("cosmo".equals(protocol))
                    return new CosmoWebSocket();
                return null;
            }
        });
        _wsFactory.setMaxIdleTime(600000);

    }

    /* ------------------------------------------------------------ */

    /**
     * Handle the handshake GET request.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // If the WebSocket factory accepts the connection, then return
        if (_wsFactory.acceptWebSocket(request, response))
            return;
        // Otherwise send an HTTP error.
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
    }


    /* ------------------------------------------------------------ */

    /**
     * Chat WebSocket Example.
     * <p>This class implements the {@link OnTextMessage} interface so that
     * it can handle the call backs when websocket messages are received on
     * a connection.
     * </p>
     */
    private class CosmoWebSocket implements WebSocket.OnTextMessage {
        volatile Connection _connection;

        /* ------------------------------------------------------------ */

        /**
         * Callback for when a WebSocket connection is opened.
         * <p>Remember the passed {@link Connection} object for later sending and
         * add this WebSocket to the members set.
         */
        public void onOpen(Connection connection) {
            _connection = connection;
            _members.add(this);
        }

        /* ------------------------------------------------------------ */

        /**
         * Callback for when a WebSocket connection is closed.
         * <p>Remove this WebSocket from the members set.
         */
        public void onClose(int closeCode, String message) {
            _members.remove(this);
        }

        /* ------------------------------------------------------------ */

        /**
         * Callback for when a WebSocket message is received.
         * <p>Send the message to all connections in the members set.
         */
        public void onMessage(String data) {
            System.out.println("Got data: " + data);
            String messageTosend = "";

            if (data.equals("getBark")) {
                messageTosend = data;
            } else if (data.startsWith("BARK")) {
                messageTosend = data;
            }

            sendDataToClients(messageTosend);
        }

        private void sendDataToClients(String messageTosend) {
            for (CosmoWebSocket member : _members) {
                try {

                    member._connection.sendMessage(messageTosend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
