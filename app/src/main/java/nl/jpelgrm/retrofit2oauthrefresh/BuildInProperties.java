package nl.jpelgrm.retrofit2oauthrefresh;

public class BuildInProperties {

    //    public String response_type = "code"; // Static
//    public String scope = "openid"; // Static
//
//    // My machine
//    public static final String BASE_URL = "https://10.214.1.52:9443";
//    public static String redirect_uri = "https://localhost/poc"; // Platform must provide
//    public String client_id = "wV2LMVd3MUvM4U756nZ0NKDCfv4a"; // My Platform must provide

    private static final String DEFAULT_ENDPOINT = "https://10.214.1.52:9443";
    private static final String DEFAULT_REDIRECT_URI = "https://localhost/poc";
    private static final String DEFAULT_CLIENT_ID = "wV2LMVd3MUvM4U756nZ0NKDCfv4a";

    //    public String base64UAndP = "cG9jdXNlcjpwb2N1c2Vy"; // My Android client input user name &password -> encode : Base64.encode(userName:password);)
    private static final String KEY_PROPERTY_ENDPOINT = "host_url";
    private static final String KEY_PROPERTY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_CLIENT_ID = "client_id";

    public static String getHostUrl() {
        return AppProperties.getInstance().getProperty(KEY_PROPERTY_ENDPOINT, DEFAULT_ENDPOINT);
    }

    public static String getRedirectUri() {
        return AppProperties.getInstance().getProperty(KEY_PROPERTY_REDIRECT_URI, DEFAULT_REDIRECT_URI);
    }

    public static String getClientId() {
        return AppProperties.getInstance().getProperty(KEY_CLIENT_ID, DEFAULT_CLIENT_ID);
    }

    public static final String getLoginPostUrl() {
        return AppProperties.getInstance().getProperty("login_url", "/oauth2/authorize");
    }
}
