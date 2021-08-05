package ntu.cz3004.controller.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import app.common.Constants;
import app.util.IOUtility;
import app.util.IntentBuilder;
import app.util.Utility;
import ntu.cz3004.controller.BuildConfig;
import ntu.cz3004.controller.R;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private static final String TAG = "mdp.frag.setting";
    private BluetoothAdapter btAdapter;


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
        shareApkPref.setOnPreferenceClickListener(this);

        // bluetooth functions
        btAdapter = BluetoothAdapter.getDefaultAdapter();
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
    public boolean onPreferenceClick(Preference preference) {

        switch (preference.getKey()){
            case "share_apk":
                shareApkAsZip();
                break;
            case "bt_discoverable":
                if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    startActivityForResult(IntentBuilder.enableBtDiscoverable(), Constants.REQUEST_DISCOVER_BT);
                } else {
                    showToast("in discoverable mode");
                }
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        requireActivity().setResult(Activity.RESULT_OK);
    }


    private Toast toast;
    private void showToast(String message){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }


    private void shareApkAsZip(){
        Uri apkUri = IOUtility.getZippedApkUri(getContext());
        if (apkUri != null){
            Intent share = IntentBuilder.shareApk(apkUri);
            startActivity(Intent.createChooser(share, getString(R.string.share_app_title)));
        }
    }
}
