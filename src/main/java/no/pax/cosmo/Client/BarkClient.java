package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class BarkClient extends AbstractClient implements BarkListener {
    private BarkDetection detection;

    public BarkClient()
            throws Exception {
        super(Util.BARK_CLIENT_NAME);
        detection = new BarkDetection(this);
    }

    public static void main(String... arg) throws Exception {
        new BarkClient();
    }

    public void send(String message) throws IOException {
        connection.sendMessage(message);
    }

    public void onMessage(String data) {
        final JSONObject jsonObject = Util.convertToJSon(data);
        try {
            final String from = String.valueOf(jsonObject.get("from"));
            if (from.equals("SERVER")) {
                System.out.println("BarkClient registration done");
            } else if (from.equals(Util.WEB_VIEW_CLIENT_NAME)) {
                newNumberOfBarks();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void newNumberOfBarks() {
        try {
            final String sendStringAsJSon = Util.getSendStringAsJSon(
                    Util.WEB_VIEW_CLIENT_NAME,
                    Util.BARK_CLIENT_NAME,
                    String.valueOf(detection.getBarkCounter()));
            send(sendStringAsJSon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
