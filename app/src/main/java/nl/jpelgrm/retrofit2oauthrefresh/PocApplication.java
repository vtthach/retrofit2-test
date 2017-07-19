package nl.jpelgrm.retrofit2oauthrefresh;


import android.app.Application;

import okhttp3.logging.HttpLoggingInterceptor;

public class PocApplication extends Application {
    private static PocApplication instance;

    private HttpLoggingInterceptor.Logger logCallback;

    public static PocApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public void setLogCallback(HttpLoggingInterceptor.Logger callback) {
        this.logCallback = callback;
    }

    public HttpLoggingInterceptor.Logger getLogger() {
        return new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (logCallback != null) {
                    logCallback.log(message);
                }
            }
        };
    }
}
