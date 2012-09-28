package no.pax.cosmo;

import no.pax.cosmo.server.CosmoServer;

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
            @Override
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
