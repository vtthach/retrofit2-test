package nl.jpelgrm.retrofit2oauthrefresh;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by nguyenminhhai on 23/9/15.
 */
public class CommonUtils {
    public static final String SA_PHONE_NUMBER_PREFIX = "0027";

    public static boolean isHighResolution(Camera.Size sz) {
        return (sz.width >= 720 && sz.height >= 720);
    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String trimPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.trim();
        phoneNumber = phoneNumber.replace(" ", "");
        return phoneNumber;
    }

    public static String getPhoneNumberSAFormat(String rawPhoneNumber) {
        if (!TextUtils.isEmpty(rawPhoneNumber)) {
            rawPhoneNumber = makePhoneNumberFromFormat(rawPhoneNumber);
            StringBuffer sb = new StringBuffer(rawPhoneNumber);
            if (rawPhoneNumber.length() > 3) {
                sb.insert(3, " ");
            }
            if (rawPhoneNumber.length() > 7) {
                sb.insert(7, " ");
            }
            return sb.toString();
        }
        return rawPhoneNumber;
    }

    public static String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(c.getTime());
    }

    public static boolean isListValid(List<?> list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isMainThread(Looper looper) {
        return looper == Looper.getMainLooper();
    }

    public static InputStream getInputStreamFromAssets(String filename, Context context)
            throws IOException {
        AssetManager manager = context.getAssets();
        return manager != null ? manager.open(filename) : null;
    }

    public static String formatMoney(double money) {
        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(Locale.getDefault());
        unusualSymbols.setDecimalSeparator('.');
        unusualSymbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat("###,##0.00", unusualSymbols);
        return "R" + formatter.format(money);
    }

    public static int getZoomFromRatio(List<Integer> zooms, float ratio) {
        ratio *= 100;
        int index = zooms.size() - 1;
        for (int indexZoom = 0; indexZoom < zooms.size(); indexZoom++) {
            //            Timber.d("Zoom: " + indexZoom + " : " + zooms.get(indexZoom));
            if ((int) ratio == zooms.get(indexZoom)) {
                index = indexZoom;
                break;
            } else if (ratio < zooms.get(indexZoom)) {
                index = (indexZoom - 1) >= 0 ? indexZoom - 1 : 0;
                break;
            }
        }
        return index;
    }

    public static String getImeiId(Context context) {
        try {
            TelephonyManager telephonyMgr =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyMgr != null ? telephonyMgr.getDeviceId() : "";
        } catch (SecurityException se) {
            // Ignore
            Log.i("vtt", "SecurityException : " + se.getMessage());
            return "";
        }
    }

    public static String makePhoneNumberFormat(String number) {
        return SA_PHONE_NUMBER_PREFIX + number.substring(1);
    }

    public static String makePhoneNumberFromFormat(String number) {
        if (!TextUtils.isEmpty(number) && number.contains(SA_PHONE_NUMBER_PREFIX)) {
            number = "0" + number.substring(SA_PHONE_NUMBER_PREFIX.length());
        }
        return number;
    }

    public static String getSmartShopperCardFormat(String card) {
        if (!TextUtils.isEmpty(card)) {
            StringBuilder sb = new StringBuilder(card);
            Integer[] indexs = new Integer[]{4, 9, 14};
            for (Integer i : indexs) {
                if (card.length() > i) {
                    sb.insert(i, " ");
                }
            }
            return sb.toString();
        }
        return card;
    }

    public static int getPropertyWithDefaultValue(String key, int defValue) {
        int rs = defValue;
        String value = AppProperties.getInstance().getProperty(key, defValue + "");
        try {
            rs = Integer.valueOf(value);
        } catch (NumberFormatException ignore) {
            // Ignore
        }
        return rs;
    }

    public static float getPropertyWithDefaultValue(String key, float defValue) {
        float rs = defValue;
        String value = AppProperties.getInstance().getProperty(key, defValue + "");
        try {
            rs = Float.valueOf(value);
        } catch (NumberFormatException e) {
            // Ignore
        }
        return rs;
    }

    public static boolean getPropertyWithDefaultValue(String key, boolean defValue) {
        boolean rs = defValue;
        String value = AppProperties.getInstance().getProperty(key, defValue + "");
        try {
            rs = Boolean.valueOf(value);
        } catch (NumberFormatException ignore) {
            // Ignore
        }
        return rs;
    }

    public static long getPropertyWithDefaultValue(String key, long defValue) {
        long rs = defValue;
        String value = AppProperties.getInstance().getProperty(key, defValue + "");
        try {
            rs = Long.valueOf(value);
        } catch (NumberFormatException ignore) {
            // Ignore
        }
        return rs;
    }
}
