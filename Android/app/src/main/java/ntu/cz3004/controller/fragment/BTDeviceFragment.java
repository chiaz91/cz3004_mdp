package ntu.cz3004.controller.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Set;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.BTDeviceAdapter;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.listener.OnRecyclerViewInteractedListener;
import ntu.cz3004.controller.util.DialogUtil;
import ntu.cz3004.controller.util.IntentBuilder;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.Utility;

public class BTDeviceFragment extends Fragment implements OnRecyclerViewInteractedListener {
    private static final String TAG = "mdp.frag.bt_devices";
    private static final int DURATION_ONE_SEC = 1000;
    private BluetoothAdapter btAdapter;
    private BTDeviceAdapter adaptorPairedDevices, adapterNearbyDevices;

    // progress count down
    Handler handler = new Handler();
    int curTimerSec;
    Runnable runnableScanCountdown = new Runnable() {
        @Override
        public void run() {
            if (++curTimerSec > Constants.SCAN_DURATION_SEC){
                doEndScanning();
            } else {
                showSnackbar(getString(R.string.discovering, (Constants.SCAN_DURATION_SEC-curTimerSec) ));
                handler.postDelayed(this, DURATION_ONE_SEC);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            DialogUtil.promptBluetoothNotAvailable(getContext());
        } else {
            enableBluetooth();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_select_device, container, false);
        // for paired devices
        RecyclerView rvDevices  = view.findViewById(R.id.rvDevicesPaired);
        rvDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptorPairedDevices = new BTDeviceAdapter();
        adaptorPairedDevices.setOnRecyclerViewInteractListener(this);
        rvDevices.setAdapter(adaptorPairedDevices);

        // for nearby devices
        RecyclerView rvNearByDevices = view.findViewById(R.id.rvDevicesNearby);
        rvNearByDevices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterNearbyDevices = new BTDeviceAdapter();
        adapterNearbyDevices.setOnRecyclerViewInteractListener(this);
        rvNearByDevices.setAdapter(adapterNearbyDevices);

        refreshPairDevices();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.activity_bt_devices));
    }

    @Override
    public void onViewInteracted(View itemView, RecyclerView.ViewHolder holder, int action) {
        BluetoothDevice device = (BluetoothDevice) itemView.getTag();
        MdpLog.d(TAG, String.format("click on %s(%s)", device.getAddress(), device.getName()!=null? device.getName():"unknown"));
        pickDevice(device);
    }

    private void pickDevice(BluetoothDevice device){
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, device.getAddress());

        // Set result and finish this Activity
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        doEndScanning();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bt_refresh_devices: doRefresh();  return true;
            case R.id.action_bt_discoverable: enableDiscoverable(); return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void doRefresh(){
        if (btAdapter == null){
            return;
        }
        if (!btAdapter.isEnabled()){
            enableBluetooth();
            return;
        }
        refreshPairDevices();
        checkPermissionForScanDevice();
    }

    private void enableBluetooth(){
        if (!btAdapter.isEnabled()){
            Intent intent = IntentBuilder.enableBluetooth();
            startActivityForResult(intent, Constants.REQUEST_ENABLE_BT);
        }
    }

    private void disableBluetooth(){
        if (btAdapter.isEnabled()){
            btAdapter.disable();
        }
    }

    private void enableDiscoverable(){
        if (btAdapter== null){
            return;
        }

        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            startActivityForResult(IntentBuilder.enableBtDiscoverable(), Constants.REQUEST_DISCOVER_BT);
        } else {
            showToast("In discoverable mode");
        }
    }

    private void refreshPairDevices() {
        if (btAdapter ==null){
            return;
        }
        Set<BluetoothDevice> set  = btAdapter.getBondedDevices();
        if (!set.isEmpty()) {
            adaptorPairedDevices.setDevices(set);
        }
    }


    private BtDeviceFoundReceiver deviceFoundReceiver;
    private void doEndScanning(){
        if (snackbar!=null){
            snackbar.dismiss();
            snackbar=null;
        }
        handler.removeCallbacks(runnableScanCountdown);
        if (btAdapter!= null && btAdapter.isDiscovering()){
            MdpLog.d(TAG, "End bluetooth scanning");
            btAdapter.cancelDiscovery();

            if (deviceFoundReceiver != null){
                requireActivity().unregisterReceiver(deviceFoundReceiver);
                deviceFoundReceiver = null;
            }
        }
    }
    private void doScanningDevices(){
        // start timer
        handler.removeCallbacks(runnableScanCountdown);
        handler.postDelayed(runnableScanCountdown, DURATION_ONE_SEC);
        curTimerSec = 0;
        showSnackbar(getString(R.string.discovering, Constants.SCAN_DURATION_SEC));


        adapterNearbyDevices.clear();
        if (deviceFoundReceiver == null){
            deviceFoundReceiver = new BtDeviceFoundReceiver();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            requireActivity().registerReceiver(deviceFoundReceiver, filter);
        }
        btAdapter.startDiscovery();
    }

    private void checkPermissionForScanDevice(){
        if (Utility.checkPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)){
            doScanningDevices();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)){
                promptPermissionForScanDevice();
            } else {
                requestPermissions( new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION );
            }
        }
    }

    private void promptPermissionForScanDevice(){
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.permission_required))
                .setMessage(getString(R.string.permission_reason_for_scan))
                .setPositiveButton(getString(R.string.confirm),(dialog, which) -> {
                    requestPermissions( new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION );
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (btAdapter.isEnabled()) {
                    refreshPairDevices();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        ArrayList<String> granted = new ArrayList<>();
//        ArrayList<String> denied  = new ArrayList<>();
//        String permission;
//        for (int i=0; i<permissions.length; i++){
//            permission = permissions[i];
//            if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
//                granted.add(permission);
//            } else {
//                denied.add(permission);
//            }
//        }
//        MdpLog.d(TAG, String.format("request(%d), granted(%d), denied(%d)", requestCode, granted.size(), denied.size()));
//
//        // check permission result for scanning
//        if (requestCode == Constants.REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.length>0 && denied.size() == 0 ) {
//                doScanningDevices();
//            }
//        }

        if (requestCode == Constants.REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                doScanningDevices();
            }
        }
    }

    Toast toast;
    private void showToast(String message){
        if (toast!=null){
            toast.cancel();
        }
        toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    Snackbar snackbar;
    private  void showSnackbar(String message){
        if (snackbar==null){
            snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getString(R.string.cancel), (v)-> doEndScanning());
            snackbar.show();
        } else {
            TextView tvMessage =  snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            tvMessage.setText(message);
        }
    }

    private class BtDeviceFoundReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                adapterNearbyDevices.add(device);
            }
        }
    }
}
