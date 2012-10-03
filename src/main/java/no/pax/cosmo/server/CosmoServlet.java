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
import java.sql.ClientInfoStatus;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CosmoServlet extends HttpServlet {
    private WebSocketFactory _wsFactory;
    private final Map<String, CosmoWebSocket> _members = new ConcurrentHashMap<String, CosmoWebSocket>();

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
                if ("cosmo".equals(protocol)) {
                    return new CosmoWebSocket();

                }
                return null;
            }
        });

        int maxTextMessageSize = 32 * 1024; // default are 16 * 1024, need more space in order to send images.
        _wsFactory.setMaxTextMessageSize(maxTextMessageSize);
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
        volatile String connectionId;

        /**
         * Callback for when a WebSocket connection is opened.
         * <p>Remember the passed {@link Connection} object for later sending and
         * add this WebSocket to the members set.
         */
        public void onOpen(Connection connection) {
            _connection = connection;
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

            final JSONObject jsonObject = Util.convertToJSon(data);

            try {
                final String to = String.valueOf(jsonObject.get("to"));

                if (Util.SERVER_NAME.equals(to)) {
                    final String regestrationObject = String.valueOf(jsonObject.get("from"));
                    connectionId = regestrationObject;

                    if (!_members.containsKey(regestrationObject)) { // todo handle update AKA refresh
                        System.out.println("new client registered " + connectionId);
                        _members.put(regestrationObject, this);
                        final String sendStringAsJSon = Util.getSendStringAsJSon(Util.WEB_VIEW_CLIENT_NAME, Util.SERVER_NAME, "OK");
                        this.sendDataToClients(this._connection,sendStringAsJSon);
                    } else {
                        _members.remove(regestrationObject);
                        _members.put(regestrationObject, this);
                        final String sendStringAsJSon = Util.getSendStringAsJSon(Util.WEB_VIEW_CLIENT_NAME, Util.SERVER_NAME, "OK");
                        this.sendDataToClients(this._connection,sendStringAsJSon);
                    }
                } else {
                    final CosmoWebSocket cosmoWebSocket = _members.get(to);

                    if (cosmoWebSocket != null) {
                        sendDataToClients(cosmoWebSocket._connection, data);
                    } else {
                        System.out.println("Client with name: " + to + " not found...");
                    }
                }


            } catch (JSONException e) {
                System.out.println("Error during parsing: " + e.getMessage());
            }
        }

        private void sendDataToClients(Connection connection, String messageTosend) {

            try {
                connection.sendMessage(messageTosend);
            } catch (IOException e) {
                System.out.println("Error during sending: " + e.getMessage());
            }
        }
    }
}
