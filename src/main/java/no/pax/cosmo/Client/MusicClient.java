package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created: rak
 * Date: 02.10.12
 */
public class MusicClient extends AbstractClient {
    private static final String SONG_ONE = "src/main/resources/sounds/Song1.mp3";
    private static final String SONG_TWO = "src/main/resources/sounds/Song2.mp3";
    private MP3Player player;
    private Thread playThread = null;

    public MusicClient()
            throws Exception {
        super(Util.WEB_MUSIC_CLIENT_NAME);
    }

    public static void main(String... arg) throws Exception {
        new MusicClient();
    }

    public void onMessage(String data) {
        JSONObject object = Util.convertToJSon(data);

        try {
            final String from = String.valueOf(object.get("from"));
            if (from.equals("SERVER")) {
                System.out.println("MusicClient registration done");
            } else if (from.equals(Util.WEB_VIEW_CLIENT_NAME)) {
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
        } catch (JSONException e) {
            e.printStackTrace();
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
}

