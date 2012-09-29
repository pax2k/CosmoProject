package no.pax.cosmo;

import no.pax.cosmo.server.CosmoServer;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Main {
    CosmoServer server;

    public Main() {
        try {
            startEmbeddedServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... arg) throws Exception {
        new Main();
    }

    private void startEmbeddedServer() throws Exception {
        Runnable serverRunnable = new Runnable() {

            public void run() {
                try {
                    server = new CosmoServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Scanner scanner = new Scanner(System.in);
                final String next = scanner.next();

                if (next.equals("x")) {
                    try {
                        server.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread serverThread = new Thread(serverRunnable);
        serverThread.start();
    }
}
