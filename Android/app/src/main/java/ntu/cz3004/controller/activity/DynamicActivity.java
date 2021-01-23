package ntu.cz3004.controller.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.fragment.BTDeviceFragment;
import ntu.cz3004.controller.fragment.SettingsFragment;

public class DynamicActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // dynamically load fragment based on intent
        Fragment fragment = null;
        if (getIntent()!=null && getIntent().hasExtra(Constants.EXTRA_FRAGMENT)){
            int  reqCode = getIntent().getIntExtra(Constants.EXTRA_FRAGMENT, -1);
            fragment = getFragmentByRequestCode(reqCode);
        }

        if (fragment != null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container, fragment);
            fragmentTransaction.commit();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.page_not_found))
                    .setMessage(getString(R.string.page_not_found_msg))
                    .setPositiveButton(getString(R.string.confirm),(dialog, which) -> {
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    public Fragment getFragmentByRequestCode(int reqCode){
        Fragment fragment = null;
        switch (reqCode){
            case Constants.REQUEST_PICK_BT_DEVICE:
                fragment = new BTDeviceFragment();
                break;
			case Constants.REQUEST_SETTING:
                    fragment = new SettingsFragment();
                    break;
        }
        return fragment;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
