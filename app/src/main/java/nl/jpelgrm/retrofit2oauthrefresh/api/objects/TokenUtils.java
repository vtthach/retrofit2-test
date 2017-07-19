package nl.jpelgrm.retrofit2oauthrefresh.api.objects;

import android.util.Log;

import nl.jpelgrm.retrofit2oauthrefresh.base64.Base64Util;

public class TokenUtils {
    public static String RESPONSE_TYPE = "code"; // Static
    public static String SCOPE = "openid"; // Static

    // My machine
    public final String BASE_URL = "https://10.214.1.52:9443";
    public String redirect_uri = "https://localhost/poc"; // Platform must provide
    public String client_id = "wV2LMVd3MUvM4U756nZ0NKDCfv4a"; // My Platform must provide
    public String base64UAndP = "cG9jdXNlcjpwb2N1c2Vy"; // My Android client input user name &password -> encode : Base64.encode(userName:password);)

    public static String getBase64FromUserNameAndPassword(String userName, String password) {
        String strToEncode = userName + ":" + password;
        String rs = Base64Util.getBase64FromByte(strToEncode.getBytes());
        String log = String.format("Base64 encode :\n-Username: %s\n-Password: %s\n-Base64Encode: %s", userName, password, rs);
        Log.i("vtt", log);
        return rs;
    }

//    public static final String BASE_URL = "https://10.214.3.78:8244/";
//    public static String redirect_uri = "http://localhost:8080/playground2/oauth2client"; // TEST2 Platform must provide
//    public String client_id = "_JT5NF8hoNr16D2s07WUCVnPUKwa"; // Platform must provide
//    public String base64UAndP = "c2FuZXRoOjExMTExMQ=="; // Android client input user name &password -> encode : Base64.encode(userName:password);)
}
