package no.pax.cosmo.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created: rak
 * Date: 29.09.12
 */
public class Util {
    public static final int DEFAULT_IDLE_TIME = 1000 * 60 * 60 * 10;

    public static final String BARK_CLIENT_NAME = "BARK_CLIENT";
    public static final String WEB_CAM_CLIENT_NAME = "WEB_CAM_CLIENT";
    public static final String WEB_VIEW_CLIENT_NAME = "WEB_VIEW_CLIENT";

    public static JSONObject convertToJSon(String data) {
        try {
           // String data = "{register : raymond}";
            return new JSONObject(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getValueFromJSon(JSONObject object, String wantedValueKey){
        String returnString = null;
        try {
            returnString = object.get(wantedValueKey).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    public static String getSendStringAsJSon(String to, String from, String value) {
        String returnString = null;
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("to", to);
            jsonObject.put("from", from);
            jsonObject.put("value", value);

            returnString = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return returnString;
    }
}
