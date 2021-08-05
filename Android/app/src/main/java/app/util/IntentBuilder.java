package app.util;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;

import java.util.Locale;

import app.common.Constants;
import ntu.cz3004.controller.activity.DynamicActivity;

public class IntentBuilder {

    public static Intent toSetting(Context context){
        return new Intent(context, DynamicActivity.class)
                .putExtra(Constants.EXTRA_FRAGMENT, Constants.REQUEST_SETTING);
    }

    public static Intent toPickBtDevice(Activity activity){
        return new Intent(activity, DynamicActivity.class)
                .putExtra(Constants.EXTRA_FRAGMENT, Constants.REQUEST_PICK_BT_DEVICE);
    }

    /**
     * Create an intent to turn on bluetooth
     * <p>use {@link Activity#startActivityForResult(Intent, int)} for request</p>
     * <p>state changes can be checked in onActivityResult</p>
     * @return created intent
     */
    public static Intent enableBluetooth(){
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    /**
     * Create intent that request system to enable bluetooth discovering mode
     * <p>To check with discoverable mode changes, use following filter to register broadcast receiver</p>
     * {@code
     *      IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
     *      activity.registerReceiver(deviceFoundReceiver, filter);
     * }
     * <p>scan mode can be retrieved like below</p>
     * {@code
     *     int scanMode =  intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE);
     *     if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
     *         // start of discoverable
     *     }else {
     *         // end of discoverable
     *     }
     * }
     * @return created intent
     * @see BluetoothAdapter#ACTION_SCAN_MODE_CHANGED
     */
    public static Intent enableBtDiscoverable(){
        return new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.DISCOVERABLE_DURATION_SEC);
    }

    /**
     * Create intent that request system to convert audio message to string
     * <p>use {@link Activity#startActivityForResult(Intent, int)} for request</p>
     * <p>string message will be return in onActivityResult with {@link RecognizerIntent#EXTRA_RESULTS}</p>
     * @param hint
     * @return
     */
    public static Intent speechInput(String hint){
        return new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                .putExtra(RecognizerIntent.EXTRA_PROMPT, hint);
    }


    public static Intent shareApk(Uri apkUri){
        return new Intent(Intent.ACTION_SEND)
                .setType("application/zip")
                .putExtra(Intent.EXTRA_STREAM, apkUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
}
