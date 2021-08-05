package app.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ntu.cz3004.controller.BuildConfig;
import ntu.cz3004.controller.R;

public class IOUtility {
    private static final String TAG = "mdp.util.io";
    private static final int BUFFER_SIZE = 2048;


    public static void copyToClipboard(Context context, String text){
        ClipboardManager clipboard =  ContextCompat.getSystemService(context, ClipboardManager.class);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    //    public static void copyFile(File source, File target)  {
//        try{
//            InputStream in = new FileInputStream(source);
//            OutputStream out = new FileOutputStream(target);
//
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = in.read(buf)) > 0) {
//                out.write(buf, 0, len);
//            }
//            in.close();
//            out.close();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public static void zip(String zipFile, String... files ) throws IOException {
        BufferedInputStream origin;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];
            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }

    public static Uri getZippedApkUri(Context context)  {
        File srcFile = new File(context.getApplicationInfo().publicSourceDir);
        File destFile = new File(context.getCacheDir(), context.getString(R.string.app_name)+".zip");
        MdpLog.d(TAG, "Src Path: "+srcFile.getAbsolutePath());
        MdpLog.d(TAG, "Dst Path: "+destFile.getAbsolutePath());

        try{
            zip(destFile.getAbsolutePath(), srcFile.getAbsolutePath());
            Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".provider", destFile);
            MdpLog.d(TAG, "APK Uri: "+apkUri.getPath());
            return apkUri;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
