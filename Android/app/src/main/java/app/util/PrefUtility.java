package app.util;

import android.content.Context;
import android.content.SharedPreferences;

import ntu.cz3004.controller.R;
import app.entity.Command;

public class PrefUtility {

    public static SharedPreferences getSharePreferences(Context context){
        String perfName = context.getString(R.string.pref_name);
        return context.getApplicationContext().getSharedPreferences(perfName, Context.MODE_PRIVATE);
    }

    public static boolean getBoolPreference(Context context, int keyId, int defaultValueId){
        SharedPreferences preferences = getSharePreferences(context);
        String key = context.getString(keyId);
        Boolean defaultValue = context.getResources().getBoolean(defaultValueId);
        return preferences.getBoolean(key, defaultValue);
    }

    public static boolean isSecureConnection(Context context){
        return getBoolPreference(context, R.string.state_secure_connection, R.bool.state_secure_connection_default);
    }

    public static boolean isAutoReconnect(Context context){
        return getBoolPreference(context, R.string.state_bluetooth_retry, R.bool.state_bluetooth_retry_default);
    }
    
    public static boolean isAutoUpdate(Context context){
        return getBoolPreference(context, R.string.state_auto_update, R.bool.state_auto_update_default);
    }

    public static boolean isEnableSimulation(Context context){
        return getBoolPreference(context, R.string.state_robot_simulation, R.bool.state_robot_simulation_default);
    }

    public static boolean isEnableAccelerometer(Context context){
        return getBoolPreference(context, R.string.state_use_accelerometer, R.bool.state_use_accelerometer_default);
    }

    public static String getDebugMap(Context context){
        return getSharePreferences(context).getString(context.getString(R.string.key_debug_map), "");
    }


    public static String getLastConnectedBtDevice(Context context){
        String lastConnect = getSharePreferences(context).getString(context.getString(R.string.bt_last_address), null);
        return (lastConnect==null)?null:lastConnect.toUpperCase();
    }

    public static void setLastConnectedBtDevice(Context context, String address){
        SharedPreferences.Editor editor = getSharePreferences(context).edit();
        editor.putString(context.getString(R.string.bt_last_address), address);
        editor.apply();
    }

    public static Command getCommand(Context context){
        Command command = new Command();
        SharedPreferences sharedPref = getSharePreferences(context);
        command.up = sharedPref.getString(context.getString(R.string.cmd_up), context.getString(R.string.cmd_up_default));
        command.left = sharedPref.getString(context.getString(R.string.cmd_left), context.getString(R.string.cmd_left__default));
        command.right = sharedPref.getString(context.getString(R.string.cmd_right), context.getString(R.string.cmd_right_default));
        command.down = sharedPref.getString(context.getString(R.string.cmd_down), context.getString(R.string.cmd_down_default));
        command.explore = sharedPref.getString(context.getString(R.string.cmd_explore), context.getString(R.string.cmd_explore_default));
        command.fastest = sharedPref.getString(context.getString(R.string.cmd_fastest), context.getString(R.string.cmd_fastest_default));
        command.imgRecognition = sharedPref.getString(context.getString(R.string.cmd_img_search), context.getString(R.string.cmd_img_search_default));
        command.f1 = sharedPref.getString(context.getString(R.string.cmd_f1), context.getString(R.string.cmd_f1_default));
        command.f2 = sharedPref.getString(context.getString(R.string.cmd_f2), context.getString(R.string.cmd_f2_default));
        command.f3 = sharedPref.getString(context.getString(R.string.cmd_f3), context.getString(R.string.cmd_f3_default));
        command.stop = sharedPref.getString(context.getString(R.string.cmd_stop), context.getString(R.string.cmd_stop_default));
        command.reqMap = sharedPref.getString(context.getString(R.string.cmd_req_map), context.getString(R.string.cmd_req_map_default));
        return command;
    }

}
