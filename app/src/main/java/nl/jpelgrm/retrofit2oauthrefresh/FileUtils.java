package nl.jpelgrm.retrofit2oauthrefresh;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import nl.jpelgrm.retrofit2oauthrefresh.base64.Base64Support;

public class FileUtils {
    public static void writeFile(byte[] data, String filePath) throws IOException {
        String errorMsg = null;
        FileOutputStream out = null;
        try {
            File parentFolder = new File(filePath).getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            out = new FileOutputStream(filePath);
            out.write(data);
        } catch (IOException e) {
            errorMsg = e.getMessage() + "\n" + "Write file error - please check this filepath: \n" + filePath;
            Log.e("vtt", errorMsg, e);
        } finally {
            closeStreamWithCatchException(out);
        }
        if (errorMsg != null) {
            throw new IOException(errorMsg);
        }
    }

    public static byte[] getByteFromFile(File file) {
        byte[] bytes = null;
        if (file.exists()) {
            int size = (int) file.length();
            InputStream inputStream = null;
            BufferedInputStream buf = null;
            try {
                bytes = new byte[size];
                inputStream = new FileInputStream(file);
                buf = new BufferedInputStream(inputStream);
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (IOException e) {
                Log.e("vtt", e.getMessage(), e);
            } finally {
                closeStreamWithCatchException(buf);
                closeStreamWithCatchException(inputStream);
            }
        }
        return bytes;
    }

    public static void closeStreamWithCatchException(Closeable inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e("vtt", e.getMessage(), e);
            }
        }
    }

    public static List<String> getBase64FromListFilePath(String[] listFilePath) {
        List<String> listBase64Data = new ArrayList<>();
        for (String aListFilePath : listFilePath) {
            if (aListFilePath != null) {
                byte[] imageByteData = FileUtils.getByteFromFile(new File(aListFilePath));
                String imageArray = Base64Support.encodeToString(imageByteData, Base64Support.NO_WRAP);
                listBase64Data.add(imageArray);
            }
        }
        return listBase64Data;
    }

    public static boolean writeBitmapToFile(Bitmap viewBoundariesImage, String filePath) {
        boolean isSuccess = false;
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            viewBoundariesImage.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            isSuccess = true;
            out.close();
        } catch (Exception e) { // NOSONAR
            Log.e("vtt", e.getMessage(), e);
        }
        return isSuccess;
    }

    public static void writeStringToFile(String filePath, String value, boolean deleteExistFile) {
        FileWriter fw = null;
        try {
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (deleteExistFile && file.exists()) {
                file.delete();
            }
            fw = new FileWriter(filePath, true);
            fw.write(value + "\n\n");
        } catch (IOException ioe) { // NOSONAR
            Log.e("vtt", ioe.getMessage(), ioe);
        } finally {
            closeStreamWithCheckException(fw);
        }
    }

    private static void closeStreamWithCheckException(Closeable fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                // Do not use Timber at here because it will cause a loop infinity
                Log.w(FileUtils.class.getSimpleName(), "closeStreamWithCheckException error: " + e.getMessage());
            }
        }
    }

    public static String readStringFromFile(String filePath) {
        StringBuilder text = new StringBuilder();
        FileReader fileReader = null;
        try {
            File file = new File(filePath);
            fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
        } catch (IOException e) {
            Log.e("vtt", e.getMessage(), e);
        } finally {
            closeStreamWithCatchException(fileReader);
        }
        return text.toString();
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void writeInputStreamToFile(@NonNull InputStream input, String fileSaved) throws IOException {
        try (OutputStream fileOs = new FileOutputStream(fileSaved);
             OutputStream stream = new BufferedOutputStream(fileOs)) {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
        }
    }
}
