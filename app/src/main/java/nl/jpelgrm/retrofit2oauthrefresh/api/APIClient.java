package nl.jpelgrm.retrofit2oauthrefresh.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface APIClient {
    // .header("Content-type", "application/x-www-form-urlencoded;charset=UTF-8")
//                        .header("Authorization", "Basic " +TokenUtils.getBase64FromUserNameAndPassword(userName, password))
    @FormUrlEncoded
    @POST()
    // My machine
//    @POST("/authorize") // Test Saneth
    @Headers("Content-type: application/x-www-form-urlencoded;charset=UTF-8")
    Observable<Response<ResponseBody>> login(
            @Url String url,
            @Header("Authorization") String token,
            @Field("response_type") String responseType,
            @Field("client_id") String clientId,
            @Field("redirect_uri") String redirectUri,
            @Field("scope") String scope);

    @Headers("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    @POST("/oidc/logout")
    Observable<Response<ResponseBody>> logout();

    @GET("/oidc/checksession")
    Observable<Response<ResponseBody>> checkSessionGet(@Query("client_id") String clientId);

    @FormUrlEncoded
    @POST("/oidc/checksession")
    Observable<Response<ResponseBody>> checkSessionPost(@Field("client_id") String clientId);
}
