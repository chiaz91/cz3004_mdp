package ntu.cz3004.controller.util;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.util.Locale;

import ntu.cz3004.controller.activity.DynamicActivity;
import ntu.cz3004.controller.common.Constants;

public class IntentBuilder {

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
}