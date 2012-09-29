package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.IOException;
import java.net.URI;


public class BarkClient implements WebSocket.OnTextMessage, BarkListener {
    private final Connection connection;
    private BarkDetection detection;

    public BarkClient()
            throws Exception {

        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.setBufferSize(4096);
        factory.start();

        WebSocketClient client = factory.newWebSocketClient();
        client.setMaxIdleTime(Util.DEFAULT_IDLE_TIME);
        client.setProtocol("cosmo");

        final String host = "localhost";
        final int port = 8080;
        final String connectionPath = "ws://" + host + ":" + port + "/cosmo";
        connection = client.open(new URI(connectionPath), this).get();

        detection = new BarkDetection(this);
    }

    public void send(String message) throws IOException {
        connection.sendMessage(message);
    }

    public void onOpen(Connection connection) {

    }

    public void onClose(int closeCode, String message) {

    }

    public void onMessage(String data) {
        if (data.equals("getBark")) {
            newNumberOfBarks();
        } else {
            System.out.println("Do nothing");
        }
    }

    public void disconnect() throws IOException {
        connection.disconnect();
    }

    public static void main(String... arg) throws Exception {
        BarkClient client = new BarkClient();
    }

    public void newNumberOfBarks() {
        try {
            send(String.valueOf("BARK" + detection.getBarkCounter()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
