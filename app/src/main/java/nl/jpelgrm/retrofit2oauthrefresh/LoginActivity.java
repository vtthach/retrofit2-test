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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
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
import okhttp3.Cookie;
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
    private Button btnCheckSessionGet;

    private TextView tvResult;
    private TextView tvCookieLog;
    private EditText edTextUserName;
    private EditText edLoginUrl;
    private EditText edTextPassword;
    private EditText edHostUrl;
    private EditText edRedirectUri;

    private EditText edClientId;
    private ScrollView myScroll;

    private Spinner spinnerPropertyPath;

    private final Handler debugHandler = new Handler();

    final SharedPreferences prefs = PocApplication.getInstance().getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

    private CompositeDisposable composite = new CompositeDisposable();

    private String logfile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/log.txt";

    private String[] filePath;

    private Runnable autoScrollDownRunnable = new Runnable() {
        @Override
        public void run() {
            myScroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };
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
        getView();
        setView();
        checkAppPermission();
    }

    private void checkAppPermission() {
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

    private void setView() {
        //        bntLogout.setEnabled(false);

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

        btnCheckSessionGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckSession(false);
            }
        });
        spinnerPropertyPath.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppProperties.getInstance().resetProperties(filePath[position]);
                initProperty();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void startCheckSession(final boolean isPost) {
        composite.clear();
        logOnUi("\n----------Check Session ", DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        getObservableInitService()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<APIClient>() {
                    @Override
                    public void onNext(@NonNull APIClient apiClient) {
                        if (isPost) {
                            apiClient.checkSessionPost(edClientId.getText().toString())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(getCheckSessionDisposal());
                        } else {
                            apiClient.checkSessionGet(edClientId.getText().toString())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(getCheckSessionDisposal());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        updateLogError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void getView() {
        filePath = getResources().getStringArray(R.array.property_paths);
        spinnerPropertyPath = (Spinner) findViewById(R.id.spinnerPropertyPath);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvCookieLog = (TextView) findViewById(R.id.tvCookieLog);
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
        btnCheckSessionGet = (Button) findViewById(R.id.btnCheckSession);
    }

    private void initProperty() {
        edTextUserName.setText(BuildInProperties.getUserName());
        edTextPassword.setText(BuildInProperties.getPassword());
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
                        apiClient.logout()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(getLogoutDisposal());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        updateLogError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        apiClient.login(
                                edLoginUrl.getText().toString(),
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
                        updateLogError(e.getMessage());
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

    private Observer<? super Response<ResponseBody>> getCheckSessionDisposal() {
        DisposableObserver disposableObserver = new DisposableObserver<Response<ResponseBody>>() {

            @Override
            protected void onStart() {
                super.onStart();
            }

            @Override
            public void onNext(@NonNull Response<ResponseBody> responseBody) {
                updateLogWithColor(Color.BLUE, "Check session response code: " + responseBody.code());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                updateLogError(e.getMessage());
                updateUICookieLog();
            }

            @Override
            public void onComplete() {
                updateUICookieLog();
            }
        };
        composite.add(disposableObserver);
        return disposableObserver;
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
                Headers list = responseBody.headers();
                String location = list.get("Location");
                updateLogWithColor(Color.BLUE, "location: " + location);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                updateLogError(e.getMessage());
//                bntLogout.setEnabled(true);
                updateUICookieLog();
            }

            @Override
            public void onComplete() {
                updateUICookieLog();
            }
        };
        composite.add(logout);
        return logout;
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
                    updateLogWithColor(Color.BLUE, "location: " + location);
                }

                // TODO difference key for OP_ID
                Log.i("vtt", "opsId: " + opsId);
                Log.i("vtt", "commAuthId: " + commAuthId);
                Log.i("vtt", "location: " + location);

            }

            @Override
            public void onError(@NonNull Throwable e) {
                updateUICookieLog();
                updateLogError(e.getMessage());
            }

            @Override
            public void onComplete() {
                updateUICookieLog();
            }

            private String getCommAuthId(String commAuthId) {
                try {
                    int firstIndex = commAuthId.indexOf(COMMON_AUTH_ID);
                    int lastIndex = commAuthId.indexOf(';');
                    return commAuthId.substring(firstIndex, lastIndex + 1);
                } catch (Exception e) {
                    updateLogError("commAuthId not found: " + commAuthId);
                }
                return "";
            }

            private String getOpsId(String opbs) {
                try {
                    int firstIndex = opbs.indexOf(OPBS_ID);
                    int lastIndex = opbs.indexOf(';');
                    return opbs.substring(firstIndex, lastIndex + 1);
                } catch (Exception e) {
                    updateLogError("opbs not found: " + opbs);
                }
                return "";
            }
        };
        composite.add(login);
        return login;
    }

    private void updateUICookieLog() {
        tvCookieLog.setText(getSaveCookie());
    }

    private String getSaveCookie() {
        Iterator<Cookie> cookieCache = ServiceGenerator.setCookieCache.iterator();
        String cookieString = "";
        for (Iterator<Cookie> it = cookieCache; it.hasNext(); ) {
            Cookie cookie = it.next();
            cookieString += "\n" + cookie.toString();
        }
        return cookieString;

    }


    public static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }

    private String getLocation() {
        return prefs.getString(KEY_LOCATION, null);
    }

    private String getCookie() {
//        return prefs.getString(KEY_COMMON_AUTH_ID, "")
//                + prefs.getString(KEY_OPS_ID, "") + "requestedURI=../../carbon/admin/index.jsp";
        return prefs.getString(KEY_COMMON_AUTH_ID, "")
                + prefs.getString(KEY_OPS_ID, "");
    }

    private void updateLogError(String message) {
        updateLogWithColor(Color.RED, "Error: " + message);
    }

    private void updateLogWithColor(int color, String message) {
        if (message != null && message.length() > 0) {
            appendColoredText(tvResult, "\n" + message, color);
        }
        autoScrollDown();
    }
}
