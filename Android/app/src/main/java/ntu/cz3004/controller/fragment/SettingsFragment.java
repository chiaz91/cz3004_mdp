package ntu.cz3004.controller.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.File;

import ntu.cz3004.controller.BuildConfig;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.Utility;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "mdp.frag.setting";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(getString(R.string.pref_name));
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // show debug version info
        findPreference("info_version").setSummary(String.format("%s (%d)",BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        findPreference("info_build").setSummary(String.format("%s (%s)",BuildConfig.BUILD_TYPE, BuildConfig.TIMESTAMP));
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        getActivity().setResult(Activity.RESULT_OK);
    }
}
