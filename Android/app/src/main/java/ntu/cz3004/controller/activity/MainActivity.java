package ntu.cz3004.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.util.IntentBuilder;
import ntu.cz3004.controller.util.MdpLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "mdp.act.main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
    }
    @Override
    protected void onResume() {
        super.onResume();
        MdpLog.logVersion();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // adjust menu with bt status
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_to_bt_device:
                startActivityForResult(IntentBuilder.toPickBtDevice(this), Constants.REQUEST_PICK_BT_DEVICE);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_PICK_BT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(Constants.EXTRA_DEVICE_ADDRESS);
                    showToast("BT device ("+address+") selected");
                    // connect to device
                    // save as last connected
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private Toast toast;
    private void showToast(String message){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}