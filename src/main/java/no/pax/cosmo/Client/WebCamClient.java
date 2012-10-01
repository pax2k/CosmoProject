package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;


public class WebCamClient implements WebSocket.OnTextMessage {
    private final Connection connection;
    private WebCam webCam;

    public WebCamClient()
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

        webCam = new WebCam();
    }

    public static void main(String... arg) throws Exception {
        new WebCamClient();
    }

    public void send(String message) throws IOException {
        connection.sendMessage(message);
    }

    public void onOpen(Connection connection) {

    }

    public void onClose(int closeCode, String message) {

    }

    public void onMessage(String data) {
        JSONObject object = Util.convertToJSon(data);
        final String value = Util.getValueFromJSon(object, "to");

        if (value.equals(Util.WEB_CAM_CLIENT_NAME)) {
            final byte[] newImage = webCam.getSnapShot();

            if (newImage != null) {
                try {
                    final String message = Base64.encodeBase64String(newImage);
                    final String sendStringAsJSon = Util.getSendStringAsJSon(
                            Util.WEB_VIEW_CLIENT_NAME,
                            Util.WEB_CAM_CLIENT_NAME, message);

                    send(sendStringAsJSon);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Do nothing");
            }
        }
    }

    public void disconnect() throws IOException {
        connection.disconnect();
    }
}
