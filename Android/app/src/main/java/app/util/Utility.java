package app.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.math.BigInteger;
import java.util.regex.Pattern;

public class Utility {
    public static final String PATTERN_PART_I  = "^(\\s*|[0-9A-F]{76})$";
    public static final String PATTERN_PART_II = "^(\\s*|[0-9A-F]{0,76})$";
    public static final String BT_ADDRESS = "^[0-9A-F]{2}(:[0-9A-F]{2}){5}$";

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
        return new BigInteger(binary, 2).toString(16);
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

}
