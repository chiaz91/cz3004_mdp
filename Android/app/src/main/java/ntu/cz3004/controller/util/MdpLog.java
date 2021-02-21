package ntu.cz3004.controller.util;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;

import ntu.cz3004.controller.BuildConfig;

public class MdpLog {
    private static String TAG = "mdp.log";
    public static boolean testing = true;
    public static int level = 6;
    public static final int LEVEL_ASSERT  = 1;
    public static final int LEVEL_ERROR   = 2;
    public static final int LEVEL_WARN    = 3;
    public static final int LEVEL_INFO    = 4;
    public static final int LEVEL_DEBUG   = 5;
    public static final int LEVEL_VERBOSE = 6;

    public static void a(String tag, String message){
        if (testing && level >= LEVEL_ASSERT){
            Log.wtf(tag, message);
        }
    }

    public static void e(String tag, String message){
        if (testing && level >= LEVEL_ERROR){
            Log.e(tag, message);
        }
    }
    public static void w(String tag, String message){
        if (testing && level >= LEVEL_WARN){
            Log.w(tag, message);
        }
    }

    public static void i(String tag, String message){
        if (testing && level >= LEVEL_INFO){
            Log.i(tag, message);
        }
    }
    public static void d(String tag, String message){
        if (testing && level >= LEVEL_DEBUG){
            Log.d(tag, message);
        }
    }

    public static void v(String tag, String message){
        if (testing && level >= LEVEL_VERBOSE){
            Log.v(tag, message);
        }
    }

    public static void logBTDevice(BluetoothDevice btDevice){
        String msg = String.format("[BT device] addr: %s, name: %s",btDevice.getAddress(), btDevice.getName());
//        String msg = "Bluetooth device info\n";
//        msg+= String.format("%s: %s\n", "name", btDevice.getName());
//        msg+= String.format("%s: %s\n", "addr", btDevice.getAddress());
        /** Other information:
         * Type     : classic / low energy / dual(classic+low energy)
         * BondState: none / bonding / bonded
         * Class    : getMajorDeviceClass then compare, can determine device is PC, mobile, wearable or etc
         *            seems only valid if when device is bonded
         * UUIDs    : get list of UUIDs
         */
        v(TAG, msg);
    }


    /**
     * Useful to read intent.getExtras()
     */
    public static void logBundle(Bundle bundle) {
        String msg = "Bundle info\n";

        for (String key : bundle.keySet()) {
            msg += String.format("[%s]:%s\n", key, bundle.get(key));
        }
        MdpLog.d(TAG, msg);
    }

    public static void logVersion(){
        String msg = "app version\n";
        msg += String.format("version.code: %d\n", BuildConfig.VERSION_CODE);
        msg += String.format("version.name: %s\n", BuildConfig.VERSION_NAME);
        msg += String.format("build.type: %s\n", BuildConfig.BUILD_TYPE);
        msg += String.format("build.time: %s\n", BuildConfig.TIMESTAMP);
        i(TAG, msg);
    }

}
