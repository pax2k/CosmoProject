package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        if (data.equals("getImage")) {
            final boolean savedImage = webCam.saveSnapShot();

            if (savedImage) {
                File f = new File("ray.jpg");
                final byte[] bytesFromFile;

                try {
                    bytesFromFile = getBytesFromFile(f);
                    final String message = Base64.encodeBase64String(bytesFromFile);
                    send(message);
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

    // Returns the contents of the file in a byte array.
    public byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
