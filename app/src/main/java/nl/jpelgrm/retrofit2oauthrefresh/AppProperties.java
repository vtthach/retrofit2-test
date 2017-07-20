package nl.jpelgrm.retrofit2oauthrefresh;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
    static AppProperties appProperties = new AppProperties();
    Properties prop = new Properties();

    private AppProperties() {
    }

    public static AppProperties getInstance() {
        return appProperties;
    }

    public void resetProperties(String fileName) {
        Context context = getApplicationContext();
        if (context != null) {
            loadPropertiesFromFile(context, fileName);
        }
    }

    public static void loadFromAssetFile(@NonNull Context context, Properties prop) {
        try {
            InputStream is = context.getAssets().open("oauth_config.properties");
            prop.load(is);
            is.close();
        } catch (IOException e) {
        }
    }

    protected static Context getApplicationContext() {
        return PocApplication.getInstance() != null ? PocApplication.getInstance().getApplicationContext() : null;
    }

    public void loadPropertiesFromFile(@NonNull Context context, String fileName) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName);
            if (file.exists()) {
                InputStream fileStream = new FileInputStream(file);
                prop.load(fileStream);
                fileStream.close();
            }
        } catch (IOException e) {
        }
    }

    public String getProperty(String key, String defValue) {
        return prop.getProperty(key, defValue);
    }
}
