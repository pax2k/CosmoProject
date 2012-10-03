package no.pax.cosmo.Client;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created: rak
 * Date: 01.10.12
 */
public class MP3Player {
    private Player player;

    public MP3Player(String pathToFile) {
        player = null;
        try {
            player = new Player(new FileInputStream(new File(pathToFile)));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void play() {
        try {
            player.play();
        } catch (JavaLayerException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stopPlayer() {
        if (player == null) {
            return;
        }

        player.close();
    }
}
