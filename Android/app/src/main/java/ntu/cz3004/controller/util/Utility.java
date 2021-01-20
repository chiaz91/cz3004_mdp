package ntu.cz3004.controller.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utility {
    public static final String PATTERN_PART_I  = "^(\\s*|[0-9A-F]{76})$";
    public static final String PATTERN_PART_II = "^(\\s*|[0-9A-F]{0,75})$";
    private static final int BUFFER_SIZE = 2048;

    public static boolean validate(String reqEx, String str){
        Pattern pattern = Pattern.compile(reqEx, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(str).matches();
    }

    /**
     * String conversion from hex value to binary value
     * @param hex hex value (no prefix)
     * @return binary value (no prefix)
     */
    public static String hexToBinary(String hex){
        return new BigInteger(hex, 16).toString(2);
    }

    /**
     * String conversion from binary value to hex value
     * @param binary binary value (no prefix)
     * @return hex value (no prefix)
     */
    public static String binaryToHex(String binary){
        /* Assume binary is 100001, it will break down as 10-0001 and convert as 0x21
           However, when parsing the map, 0x21 will be converted back as 0010-0001
           sign extending will cause error in parsing p2
         */
//        return new BigInteger(binary, 2).toString(16);

        String nibble;
        StringBuilder result = new StringBuilder();
        int length = binary.length();
        for (int i = 0; i < length ; i+=4) {
            nibble = binary.substring(i, Math.min(length, i+4));
            if (nibble.length()<4){
                nibble = String.format("%-4s", nibble).replaceAll(" ", "0");
            }
            String hexLetter = new BigInteger( nibble, 2).toString(16);
            result.append(hexLetter);
        }
        return result.toString();
    }

    /**
     * Apply AND operation to check if flags contains mask
     * @param flags
     * @param mask
     * @return true if flags contains mask
     */
    public static boolean bitwiseCompare(int flags, int mask){
        return  (flags & mask) == mask;
    }


    /**
     * Check if required permission is grant for application
     * @param context
     * @param permission
     * @return true if permission is grant
     * @see android.Manifest.permission
     */
    public static boolean checkPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

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
        BufferedInputStream origin = null;
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

}