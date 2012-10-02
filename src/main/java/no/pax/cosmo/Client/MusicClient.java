package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

/**
 * Created: rak
 * Date: 02.10.12
 */
public class MusicClient implements WebSocket.OnTextMessage {
    private final Connection connection;
    private static final String SONG_ONE = "src/main/resources/sounds/Song1.mp3";
    private static final String SONG_TWO = "src/main/resources/sounds/Song2.mp3";
    private MP3Player player;
    private Thread playThread = null;

    public MusicClient()
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
        final String to = Util.getValueFromJSon(object, "to");

        if (to.equals(Util.WEB_MUSIC_CLIENT_NAME)) {
            final String value = Util.getValueFromJSon(object, "value");

            System.out.println("PLAY MUSIC: " + value);

            String musicPath = null;
            if (value.equals("1")) {
                musicPath = SONG_ONE;
            } else if (value.equals("2")) {
                musicPath = SONG_TWO;
            }

            playMusic(musicPath);
        }
    }

    private void playMusic(final String value) {
        if (player == null) {
            player = new MP3Player(value);
        } else {
            player.stopPlayer();
            player = new MP3Player(value);

            if (playThread != null) {
                playThread.interrupt();
            }
        }

        Runnable runnable = new Runnable() {
            public void run() {
                player.play();
            }
        };

        playThread = new Thread(runnable);
        playThread.start();
    }

    public void disconnect() throws IOException {
        connection.disconnect();
    }

    public static void main(String... arg) throws Exception {
        new MusicClient();
    }
}

