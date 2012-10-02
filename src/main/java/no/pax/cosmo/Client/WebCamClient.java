package no.pax.cosmo.Client;

import no.pax.cosmo.Util.Util;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class WebCamClient extends AbstractClient {
    private WebCam webCam;

    public WebCamClient()
            throws Exception {
        super(Util.WEB_CAM_CLIENT_NAME);
        webCam = new WebCam();
    }

    public static void main(String... arg) throws Exception {
        new WebCamClient();
    }

    public void send(String message) throws IOException {
        connection.sendMessage(message);
    }

    public void onMessage(String data) {
        final JSONObject jsonObject = Util.convertToJSon(data);
        try {
            final Object from = jsonObject.get("from");

            if (Util.SERVER_NAME.equals(from)) {
                System.out.println("WebCamClient added as client");
            } else {
                final byte[] newImage = webCam.getSnapShot();

                if (newImage != null) {
                    try {
                        final String message = Base64.encodeBase64String(newImage);
                        final String sendStringAsJSon = Util.getSendStringAsJSon(
                                Util.WEB_VIEW_CLIENT_NAME,
                                Util.WEB_CAM_CLIENT_NAME,
                                message);

                        send(sendStringAsJSon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Do nothing");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void disconnect() throws IOException {
        connection.disconnect();
    }
}
