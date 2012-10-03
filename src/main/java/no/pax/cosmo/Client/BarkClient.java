package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.json.JSONObject;

import java.io.IOException;


public class BarkClient extends AbstractClient implements BarkListener {
    private BarkDetection detection;
    private MongoDBConnector connector;

    public BarkClient()
            throws Exception {
        super(Util.BARK_CLIENT_NAME);
        connector = new MongoDBConnector();
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
        final String from = Util.getValueFromJSon(jsonObject, "from");

        if (Util.SERVER_NAME.equals(from)) {
            System.out.println("BarkClient registration done");
        } else if (Util.WEB_VIEW_CLIENT_NAME.equals(from)) {
            newNumberOfBarks(false);
        }
    }

    public void newNumberOfBarks(boolean updateValue) {
        final Integer numberOfBarksToday;

        if (updateValue) {
            numberOfBarksToday = connector.updateBarkCounter();
        } else {
            numberOfBarksToday = connector.getBarkCounter();
        }

        try {
            final String sendStringAsJSon = Util.getSendStringAsJSon(
                    Util.WEB_VIEW_CLIENT_NAME,
                    Util.BARK_CLIENT_NAME,
                    String.valueOf(numberOfBarksToday));
            send(sendStringAsJSon);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
