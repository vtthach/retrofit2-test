package nl.jpelgrm.retrofit2oauthrefresh.api;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import nl.jpelgrm.retrofit2oauthrefresh.PocApplication;
import nl.jpelgrm.retrofit2oauthrefresh.api.objects.FakeX509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ServiceGenerator {

    public static final int TIME_OUT = 5*60; // Second
    public static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static final CookiePersistor sharedPrefsCookiePersistor;

    public static final CookieCache setCookieCache;

    static {
        // Ignore ssl
        httpClient.sslSocketFactory(FakeX509TrustManager.getAllSslSocketFactory());
        httpClient.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        // Timeout
        httpClient.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.writeTimeout(TIME_OUT, TimeUnit.SECONDS);
        httpClient.readTimeout(TIME_OUT, TimeUnit.SECONDS);

        // Disable follow redirect
        httpClient.followRedirects(false);
        httpClient.followSslRedirects(false);

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(PocApplication.getInstance().getLogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(httpLoggingInterceptor);

        sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(getAppContext());

        setCookieCache = new SetCookieCache();

        // Automatic manage cookie
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(setCookieCache, sharedPrefsCookiePersistor);
        httpClient.cookieJar(cookieJar);
    }

    private static Context getAppContext() {
        return PocApplication.getInstance().getApplicationContext();
    }

    public static <S> S createService(Class<S> serviceClass, HttpLoggingInterceptor.Logger logger, String hostUrl) {
        // host url
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(hostUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient client = httpClient.build();
        Retrofit retrofit = retrofitBuilder.client(client).build();
        return retrofit.create(serviceClass);
    }
}

