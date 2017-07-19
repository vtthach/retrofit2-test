package nl.jpelgrm.retrofit2oauthrefresh;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import nl.jpelgrm.retrofit2oauthrefresh.api.APIClient;
import nl.jpelgrm.retrofit2oauthrefresh.api.ServiceGenerator;
import nl.jpelgrm.retrofit2oauthrefresh.api.objects.TokenUtils;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    public static final String COMMON_AUTH_ID = "commonAuthId";
    public static final String OPBS_ID = "opbs";

    private static final String KEY_COMMON_AUTH_ID = "KEY_COMMON_AUTH_ID";
    private static final String KEY_OPS_ID = "KEY_OPS_ID";
    private static final String KEY_LOCATION = "KEY_LOCATION";

    private Button btnLogin;
    private Button bntLogout;
    private Button btnClear;

    private TextView tvResult;
    private EditText edTextUserName;
    private EditText edLoginUrl;
    private EditText edTextPassword;
    private EditText edHostUrl;
    private EditText edRedirectUri;

    private EditText edClientId;
    final SharedPreferences prefs = PocApplication.getInstance().getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    private final Handler debugHandler = new Handler();
    private CompositeDisposable composite = new CompositeDisposable();

    private ScrollView myScroll;

    private Runnable autoScrollDownRunnable = new Runnable() {
        @Override
        public void run() {
            myScroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };
    private String logfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log.txt";
    private HttpLoggingInterceptor.Logger logger = new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(final String message) {
            if (!isFinishing()) {
                debugHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("vtt", "Log message: " + message);
                        FileUtils.writeStringToFile(logfile, message, false);
                        logOnUi(null, message);
                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tvResult = (TextView) findViewById(R.id.tvResult);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        bntLogout = (Button) findViewById(R.id.bntLogout);
        btnClear = (Button) findViewById(R.id.btnClear);
        edTextUserName = (EditText) findViewById(R.id.edUserName);
        edTextPassword = (EditText) findViewById(R.id.edPassword);
        edLoginUrl = (EditText) findViewById(R.id.edLoginUrl);
        edHostUrl = (EditText) findViewById(R.id.edHostUrl);
        edRedirectUri = (EditText) findViewById(R.id.edRedirectUri);
        edClientId = (EditText) findViewById(R.id.edClientId);
        myScroll = (ScrollView) findViewById(R.id.myScroll);

        bntLogout.setEnabled(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogin(edTextUserName.getText().toString(), edTextPassword.getText().toString());
            }
        });

        bntLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogout();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvResult.setText("");
            }
        });

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();


    }

    private void initProperty() {
        edHostUrl.setText(BuildInProperties.getHostUrl());
        edClientId.setText(BuildInProperties.getClientId());
        edRedirectUri.setText(BuildInProperties.getRedirectUri());
        edLoginUrl.setText(BuildInProperties.getLoginPostUrl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        composite.dispose();
        debugHandler.removeCallbacksAndMessages(null);
    }

    private void startLogout() {
        composite.clear();
        logOnUi("\n----------LOGOUT ", DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        getObservableInitService()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<APIClient>() {
                    @Override
                    public void onNext(@NonNull APIClient apiClient) {
                        apiClient.logout(getCookie(), getLocation()).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(getLogoutDisposal());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        updateUIError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppProperties.getInstance().resetProperties();
        initProperty();
        PocApplication.getInstance().setLogCallback(logger);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PocApplication.getInstance().setLogCallback(null);
    }

    private Observable<APIClient> getObservableInitService() {
        return io.reactivex.Observable.defer(new Callable<ObservableSource<APIClient>>() {
            @Override
            public ObservableSource<APIClient> call() throws Exception {
                return Observable.just(ServiceGenerator.createService(APIClient.class, logger, edHostUrl.getText().toString()));
            }
        });
    }

    protected void startLogin(final String userName, final String password) {
        composite.clear();
        logOnUi("\n----------LOGIN ", DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        getObservableInitService()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<APIClient>() {
                    @Override
                    public void onNext(@NonNull APIClient apiClient) {
                        Log.i("vtt", "Start login");
                        String token = TokenUtils.getBase64FromUserNameAndPassword(userName, password);
                        apiClient.login(edLoginUrl.getText().toString(),
                                "Basic " + token,
                                TokenUtils.RESPONSE_TYPE,
                                edClientId.getText().toString(),
                                edRedirectUri.getText().toString(),
                                TokenUtils.SCOPE)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(getLoginDisposal());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        updateUIError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    private void logOnUi(String formatMsg, String msg) {
        if (formatMsg != null && formatMsg.length() > 0) {
            appendColoredText(tvResult, "\n" + formatMsg, Color.BLUE);
        }
        if (msg != null && msg.length() > 0) {
            appendColoredText(tvResult, "\n" + msg, Color.DKGRAY);
        }
        autoScrollDown();
    }

    private void autoScrollDown() {
        debugHandler.removeCallbacks(autoScrollDownRunnable);
        debugHandler.postDelayed(autoScrollDownRunnable, 200);
    }

    private Observer<? super Response<ResponseBody>> getLoginDisposal() {
        DisposableObserver login = new DisposableObserver<Response<ResponseBody>>() {
            @Override
            protected void onStart() {
                super.onStart();
            }

            @Override
            public void onNext(@NonNull Response<ResponseBody> responseBody) {
                Log.i("vtt", "Success response: " + responseBody.toString());
                Headers list = responseBody.headers();
                String opsId = "";
                String commAuthId = "";
                String location = "";
                Map<String, List<String>> map = list.toMultimap();
                List<String> cookieMaps = map.get("Set-Cookie");
                if (cookieMaps != null) {
                    for (String value : cookieMaps) {
                        if (value.contains(OPBS_ID)) {
                            opsId = getOpsId(value);
                        } else if (value.contains(COMMON_AUTH_ID)) {
                            commAuthId = getCommAuthId(value);
                        }
                    }
                    location = list.get("Location");
                    prefs.edit().putString(KEY_OPS_ID, opsId).apply();
                    prefs.edit().putString(KEY_COMMON_AUTH_ID, commAuthId).apply();
                    prefs.edit().putString(KEY_LOCATION, location).apply();
                    updateUIError(Color.BLUE, "opsId: " + opsId);
                    updateUIError(Color.BLUE, "commAuthId: " + commAuthId);
                    updateUIError(Color.BLUE, "location: " + location);
                }

                // TODO difference key for OP_ID
                Log.i("vtt", "opsId: " + opsId);
                Log.i("vtt", "commAuthId: " + commAuthId);
                Log.i("vtt", "location: " + location);
                bntLogout.setEnabled(true);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                updateUIError(e.getMessage());
            }

            @Override
            public void onComplete() {

            }

            private String getCommAuthId(String commAuthId) {
                try {
                    int firstIndex = commAuthId.indexOf(COMMON_AUTH_ID);
                    int lastIndex = commAuthId.indexOf(';');
                    return commAuthId.substring(firstIndex, lastIndex + 1);
                } catch (Exception e) {
                    updateUIError("commAuthId not found: " + commAuthId);
                }
                return "";
            }

            private String getOpsId(String opbs) {
                try {
                    int firstIndex = opbs.indexOf(OPBS_ID);
                    int lastIndex = opbs.indexOf(';');
                    return opbs.substring(firstIndex, lastIndex + 1);
                } catch (Exception e) {
                    updateUIError("opbs not found: " + opbs);
                }
                return "";
            }
        };
        composite.add(login);
        return login;
    }


    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }


    private Observer<? super Response<ResponseBody>> getLogoutDisposal() {
        DisposableObserver logout = new DisposableObserver<Response<ResponseBody>>() {
            @Override
            protected void onStart() {
                super.onStart();
            }

            @Override
            public void onNext(@NonNull Response<ResponseBody> responseBody) {
                Log.i("vtt", "Success response: " + responseBody.toString());
                Log.i("vtt", "Success header: " + responseBody.headers());
                bntLogout.setEnabled(false);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                updateUIError(e.getMessage());
                bntLogout.setEnabled(true);
            }

            @Override
            public void onComplete() {

            }
        };
        composite.add(logout);
        return logout;
    }

    private String getLocation() {
        return prefs.getString(KEY_LOCATION, null);
    }

    private String getCookie() {
        return prefs.getString(KEY_COMMON_AUTH_ID, null)
                + prefs.getString(KEY_OPS_ID, null) + "requestedURI=../../carbon/admin/index.jsp";
    }

    private void updateUIError(String message) {
        updateUIError(Color.RED, "Error: " + message);
    }

    private void updateUIError(int color, String message) {
        if (message != null && message.length() > 0) {
            appendColoredText(tvResult, "\n" + message, color);
        }
        autoScrollDown();
    }
}
