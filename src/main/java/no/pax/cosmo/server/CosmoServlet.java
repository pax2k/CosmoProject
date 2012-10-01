package no.pax.cosmo.server;


import no.pax.cosmo.Util.Util;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class CosmoServlet extends HttpServlet {
    private WebSocketFactory _wsFactory;
    private final Set<CosmoWebSocket> _members = new CopyOnWriteArraySet<CosmoWebSocket>();

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
                // Return new WebSocket for connections
                if ("cosmo".equals(protocol))
                    return new CosmoWebSocket();
                return null;
            }
        });
        _wsFactory.setMaxIdleTime(Util.DEFAULT_IDLE_TIME);
    }

    /**
     * Handle the handshake GET request.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // If the WebSocket factory accepts the connection, then return
        if (_wsFactory.acceptWebSocket(request, response)) {
            return;
        }
        // Otherwise send an HTTP error.
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Websocket only");
    }

    /**
     * Chat WebSocket Example.
     * <p>This class implements the {@link OnTextMessage} interface so that
     * it can handle the call backs when websocket messages are received on
     * a connection.
     * </p>
     */
    private class CosmoWebSocket implements WebSocket.OnTextMessage {
        volatile Connection _connection;

        /**
         * Callback for when a WebSocket connection is opened.
         * <p>Remember the passed {@link Connection} object for later sending and
         * add this WebSocket to the members set.
         */
        public void onOpen(Connection connection) {
            _connection = connection;
            _members.add(this);
        }

        /**
         * Callback for when a WebSocket connection is closed.
         * <p>Remove this WebSocket from the members set.
         */
        public void onClose(int closeCode, String message) {
            _members.remove(this);
        }

        /**
         * Callback for when a WebSocket message is received.
         * <p>Send the message to all connections in the members set.
         */
        public void onMessage(String data) {
            System.out.println("Got data: " + data);
            sendDataToClients(data);  // todo handle different clients
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
