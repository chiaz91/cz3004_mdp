package ntu.cz3004.controller.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.io.File;

import ntu.cz3004.controller.BuildConfig;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.util.IntentBuilder;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.Utility;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private static final String TAG = "mdp.frag.setting";
    private BluetoothAdapter btAdapter;
    private SwitchPreference prefBtEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        requireActivity().registerReceiver(btStateChangedReceiver, filter);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(getString(R.string.pref_name));
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // show debug version info
        findPreference("info_version").setSummary(String.format("%s (%d)",BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        findPreference("info_build").setSummary(String.format("%s (%s)",BuildConfig.BUILD_TYPE, BuildConfig.TIMESTAMP));

        // share apk
        Preference shareApkPref = findPreference("share_apk");
        shareApkPref.setVisible(BuildConfig.BUILD_TYPE.equalsIgnoreCase("release"));
        shareApkPref.setOnPreferenceClickListener(preference -> {
            shareApkAsZip();
            return true;
        });

        // bluetooth functions
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        prefBtEnabled = findPreference("bt_enabled");
        prefBtEnabled.setChecked(btAdapter.isEnabled());
        prefBtEnabled.setOnPreferenceChangeListener((preference, newValue) -> false);
        prefBtEnabled.setOnPreferenceClickListener(this);
        findPreference("bt_discoverable").setOnPreferenceClickListener(this);
        findPreference("bt_last_address").setOnPreferenceChangeListener((preference, newValue) -> {
            if (Utility.validate(Utility.BT_ADDRESS, (String) newValue)){
                return true;
            } else {
                showToast("Invalid Bluetooth address");
                return false;
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.activity_setting));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();

    }

    @Override
    public void onDestroy() {
        requireActivity().unregisterReceiver(btStateChangedReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey()){
            case "bt_enabled":
                if(btAdapter.isEnabled()){
                    btAdapter.disable();
                } else {
                    btAdapter.enable();
                }
                break;
            case "bt_discoverable":
                if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    startActivityForResult(IntentBuilder.enableBtDiscoverable(), Constants.REQUEST_DISCOVER_BT);
                } else {
                    showToast("in discoverable mode");
                }
        }
        return true;
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getActivity().setResult(Activity.RESULT_OK);
    }


    private Toast toast;
    private void showToast(String message){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }



    private final BroadcastReceiver btStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                    case BluetoothAdapter.STATE_ON:
                        prefBtEnabled.setChecked(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_OFF:
                        prefBtEnabled.setChecked(false);
                        break;
                }
            }
        }
    };

    private void shareApkAsZip()  {
        File srcFile = new File(getContext().getApplicationInfo().publicSourceDir);
        File destFile = new File(getContext().getCacheDir(), getContext().getString(R.string.app_name)+".zip");

        MdpLog.d(TAG, "Path: "+destFile.getAbsolutePath());

        try{
            Utility.zip(destFile.getAbsolutePath(), srcFile.getAbsolutePath());
            Uri apkUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID+".provider", destFile);
            MdpLog.d(TAG, "Path: "+apkUri.getPath());

            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("application/zip");
            share.putExtra(Intent.EXTRA_STREAM, apkUri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, getString(R.string.share_app_title)));

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
