package ntu.cz3004.controller.util;

import android.content.Context;
import android.content.SharedPreferences;

import ntu.cz3004.controller.R;

public class PrefUtility {

    public static SharedPreferences getSharePreferences(Context context){
        String perfName = context.getString(R.string.pref_name);
        return context.getApplicationContext().getSharedPreferences(perfName, Context.MODE_PRIVATE);
    }

    public static String getLastConnectedBtDevice(Context context){
        return getSharePreferences(context).getString(context.getString(R.string.bt_last_address), null);
    }

    public static void setLastConnectedBtDevice(Context context, String address){
        SharedPreferences.Editor editor = getSharePreferences(context).edit();
        editor.putString(context.getString(R.string.bt_last_address), address);
        editor.apply();
    }


}
