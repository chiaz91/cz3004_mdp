package ntu.cz3004.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.BTMessageAdapter;
import ntu.cz3004.controller.adapter.MDPPagerAdapter;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.control.BluetoothController;
import ntu.cz3004.controller.control.MapEditor;
import ntu.cz3004.controller.entity.BTMessage;
import ntu.cz3004.controller.entity.Command;
import ntu.cz3004.controller.entity.Map;
import ntu.cz3004.controller.listener.BluetoothStatusListener;
import ntu.cz3004.controller.service.BluetoothChatService;
import ntu.cz3004.controller.util.DialogUtil;
import ntu.cz3004.controller.util.IntentBuilder;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.PrefUtility;
import ntu.cz3004.controller.view.BTChatViewHolder;
import ntu.cz3004.controller.view.ControlsViewHolder;
import ntu.cz3004.controller.view.InfoViewHolder;
import ntu.cz3004.controller.view.MapEditViewHolder;
import ntu.cz3004.controller.view.MapView;

public class MainActivity extends AppCompatActivity implements BluetoothStatusListener, Map.OnMapChangedListener, View.OnClickListener {
    private static final String TAG = "mdp.act.main";
    private BluetoothController controller;
    private Command cmd;
    // map
    private MapView mv;
    private Map map;
    private MapEditor mapEditor;
    // BT chat
    private BTMessageAdapter btMessageAdapter;
    // views & holder
    private ViewPager pager;
    private View viewMain, viewBtChat;
    private ControlsViewHolder vhControls;
    private MapEditViewHolder vhMapEdit;
    private InfoViewHolder vhInfo;
    private BTChatViewHolder vhBTChat;
    // multi-threading
    final Handler handler = new Handler();
    private Runnable taskUpdateMap, taskReconnectBT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(getString(R.string.not_connected));
        controller = new BluetoothController(this);
        controller.registerListener(this);

        initPager();
        initMap();
        initRobotControls();
        initMapEdit();
        initBTChat();
        initInfoSheet();
        loadTestData();
        cmd = PrefUtility.getCommand(this);

        if (!controller.isSupported()){
            DialogUtil.promptBluetoothNotAvailable(this);
        } else {
            reconnectLastDevice();
        }
    }

    private void initPager(){
        pager = findViewById(R.id.pager);
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        viewMain = LayoutInflater.from(this).inflate(R.layout.layout_map, pager, false);
        viewBtChat = LayoutInflater.from(this).inflate(R.layout.layout_chat, pager, false);
        View[] views = new View[]{viewMain, viewBtChat};
        MDPPagerAdapter adapter = new MDPPagerAdapter(isTablet, views);
        pager.setAdapter(adapter);
    }

    private void initMap(){
        map = new Map();
        map.setListener(this);

        mv = viewMain.findViewById(R.id.map);
        mv.setMap(map);
    }

    private void initRobotControls(){
        View vControls = viewMain.findViewById(R.id.container_controls);
        vhControls = new ControlsViewHolder(vControls);
        vhControls.setOnClickListener(this);
    };

    private void initMapEdit(){
        View vMapEditMode =  viewMain.findViewById(R.id.container_map_edit);
        vhMapEdit = new MapEditViewHolder(vMapEditMode);
        mapEditor = new MapEditor(mv, vhMapEdit);
        mv.setOnClickListener(v -> {
            // click action on cell
            Point position = (Point) v.getTag();
            mapEditor.editOn(position);
        });
    }

    private void initInfoSheet(){
        View vStatus = viewMain.findViewById(R.id.bottom_sheet);
        vhInfo = new InfoViewHolder(vStatus);
        vhInfo.updateAs(map);
        vhInfo.cbMapEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                switchToMapEdit();
            } else {
                switchToControl();
                // TODO: Edit is completed, send update to RPi
            }
        });
    }

    private void initBTChat(){
        ArrayList<BTMessage> messages = new ArrayList();
        btMessageAdapter = new BTMessageAdapter(messages);
        vhBTChat = new BTChatViewHolder(viewBtChat, btMessageAdapter);
        vhBTChat.setOnSendClickListener((v)->{
            String msg = vhBTChat.etMessage.getText().toString();
            if (msg!=null && msg.length()>0){
                // send message
                if (controller.isConnected()){
                    controller.sendMessage(msg);
                }else {
                    MdpLog.d(TAG, "device not connect");
                }
            }
        });
    }

    private void loadTestData(){
        // TODO remove debug messages
        btMessageAdapter.add(new BTMessage(BTMessage.Type.SYSTEM, "sys", "AY20/21S2 Group1"));
        String[] notes = getResources().getStringArray(R.array.release_notes);
        for(String note: notes){
            String[] parts = note.split(":");
            btMessageAdapter.add(new BTMessage(BTMessage.Type.OUTGOING, parts[0], parts[1]));
        }

        String debugMap = PrefUtility.getDebugMap(this);
        MdpLog.d(TAG, "debug map: "+debugMap);
        String[] mapData = debugMap.split("\\|");
        try{ // attempt load map data
            map.mapFromString(mapData[0], mapData[1]);
        } catch (Exception e){ }

        try{ // attempt load image data
            map.imagesFromString(mapData[2]);
        } catch (Exception e){ }
    }
    @Override
    protected void onResume() {
        super.onResume();
        MdpLog.logVersion();

        // bt reconnect
        boolean btReconnect = PrefUtility.isAutoReconnect(this);
        if (btReconnect){
            startBTReconnectTask();
        }

        // auto map update
        boolean autoUpdate = PrefUtility.isAutoUpdate(this);
        vhControls.btnGetMap.setEnabled(!autoUpdate);
        if (autoUpdate){
            startAutoUpdateTask();
        }
    }

    @Override
    protected void onPause() {
        stopBTReconnectTask();
        stopAutoUpdateTask();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() != 0){
            pager.setCurrentItem(0, true);
        } else {
            if (controller.isConnected()){
                DialogUtil.promptExitAppWarming(this);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        controller.stopService();
        super.onDestroy();
    }

    private void switchToControl(){
        vhControls.setVisible(true);
        vhMapEdit.setVisible(false);
        mapEditor.setMode(MapEditor.Mode.NONE);

        // TODO: finish edit, send update to RPi
    }

    private void switchToMapEdit(){
        vhControls.setVisible(false);
        vhMapEdit.setVisible(true);
        vhMapEdit.returnToStart();
        mapEditor.setMode(MapEditor.Mode.ROBOT);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // robot control
            case R.id.btn_ctrl_up: controller.sendMessage(cmd.up); break;
            case R.id.btn_ctrl_left: controller.sendMessage(cmd.left); break;
            case R.id.btn_ctrl_right: controller.sendMessage(cmd.right); break;
            case R.id.btn_ctrl_down: controller.sendMessage(cmd.down); break;
            case R.id.btn_ctrl_f1: controller.sendMessage(cmd.f1); break;
            case R.id.btn_ctrl_f2: controller.sendMessage(cmd.f2); break;
            case R.id.btn_ctrl_explore: controller.sendMessage(cmd.explore); break;
            case R.id.btn_ctrl_fastest: controller.sendMessage(cmd.fastest); break;
            case R.id.btn_ctrl_img_search: controller.sendMessage(cmd.imgRecognition); break;
            case R.id.btn_ctrl_stop: controller.sendMessage(cmd.stop); break;
            case R.id.btn_ctrl_get_map: controller.sendMessage(cmd.reqMap); break;

            default: showSnackbar("work in progress"); break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showReconnect = true;
        if (!controller.isSupported() || controller.isConnected() || PrefUtility.getLastConnectedBtDevice(this) == null){
            showReconnect = false;
        }
        menu.findItem(R.id.action_bt_reconnect).setVisible(showReconnect);
        menu.findItem(R.id.action_bt_disconnect).setVisible(controller.isConnected());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_to_bt_device:
                startActivityForResult(IntentBuilder.toPickBtDevice(this), Constants.REQUEST_PICK_BT_DEVICE);
                return true;
            case R.id.action_to_setting:
                startActivityForResult(IntentBuilder.toSetting(this), Constants.REQUEST_SETTING);
                return true;
            case R.id.action_bt_disconnect:
                controller.stopService();
                return true;
            case R.id.action_bt_reconnect:
                reconnectLastDevice();
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    reconnectLastDevice();
                }
                break;
            case Constants.REQUEST_PICK_BT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // connect to device
                    String address = data.getExtras().getString(Constants.EXTRA_DEVICE_ADDRESS);
                    boolean isSecure = PrefUtility.isSecureConnection(this);
                    controller.connectDevice(address, isSecure);
                    // save as last connected
                    PrefUtility.setLastConnectedBtDevice(this, address);
                }
                break;

            case Constants.REQUEST_SETTING:
                cmd = PrefUtility.getCommand(this);

                // TODO: check states, update multithreading events
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

    Snackbar snackbar;
    private  void showSnackbar(String message){
        if (snackbar==null){
            View rootView = findViewById(android.R.id.content);
            snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        } else {
            TextView tvMessage = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
            tvMessage.setText(message);
        }
        snackbar.show();
    }


    // map
    @Override
    public void onMapChanged() {
        mv.invalidate();
        vhInfo.updateAs(map);
    }

    // BT communication related
    @Override
    public void onStateChanges(int state) {
        switch (state){
            case BluetoothChatService.STATE_CONNECTED:
                getSupportActionBar().setSubtitle( getString(R.string.connected_to__device_, controller.getConnectedDeviceName()) );
                break;
            case BluetoothChatService.STATE_CONNECTING:
                getSupportActionBar().setSubtitle( getString(R.string.connecting_)) ;
                break;
            case BluetoothChatService.STATE_LISTEN:
            case BluetoothChatService.STATE_NONE:
                getSupportActionBar().setSubtitle(getString(R.string.not_connected));
                break;
        }
    }

    @Override
    public void onCommunicate(BTMessage message) {
        btMessageAdapter.add(message);
        vhBTChat.scrollToEnd();

        // TODO: decode on receiving message
    }

    @Override
    public void onToastMessage(String message) {
        showToast(message);
    }


    public void reconnectLastDevice(){
        if (!controller.isEnabled()){
            Intent intent = IntentBuilder.enableBluetooth();
            startActivityForResult(intent, Constants.REQUEST_ENABLE_BT);
        } else {
            String lastConnect = PrefUtility.getLastConnectedBtDevice(MainActivity.this);
            if (lastConnect!=null && controller.shouldReconnect()){
                boolean isSecure = PrefUtility.isSecureConnection(MainActivity.this);
                controller.connectDevice(lastConnect, isSecure);
            }
        }
    }

    // multi-threading tasks
    public void startAutoUpdateTask(){
        if (taskUpdateMap == null){
            taskUpdateMap = new Runnable() {
                @Override
                public void run() {
                    MdpLog.d("mdp.threads", "req map");
                    controller.sendMessage(cmd.reqMap);
                    handler.postDelayed(this, Constants.MAP_UPDATE_INTERVAL_MS);
                }
            };
        }
        handler.post(taskUpdateMap);
    }
    public void stopAutoUpdateTask(){
        if (taskUpdateMap != null ){
            handler.removeCallbacks(taskUpdateMap);
            taskUpdateMap = null;
        }
    }

    public void startBTReconnectTask(){
        if (taskReconnectBT == null){
            taskReconnectBT = new Runnable() {
                @Override
                public void run() {
                    MdpLog.d("mdp.threads", "bt reconnect");
                    if (!controller.isEnabled()){
                        showToast("bluetooth is not enabled.");
                    } else {
                        reconnectLastDevice();
                    }
                    handler.postDelayed(this, Constants.BT_RECONNECT_INTERVAL_MS);
                }
            };
        }
        handler.post(taskReconnectBT);
    }
    public void stopBTReconnectTask(){
        if (taskReconnectBT != null){
            handler.removeCallbacks(taskReconnectBT);
            taskReconnectBT = null;
        }
    }
}