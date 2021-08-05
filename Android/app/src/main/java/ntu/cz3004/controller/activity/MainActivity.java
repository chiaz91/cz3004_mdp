package ntu.cz3004.controller.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.common.BluetoothStatusListener;
import app.common.Constants;
import app.control.BTRobotController;
import app.control.MapEditor;
import app.control.MdpParser;
import app.entity.Command;
import app.entity.MDPMessage;
import app.entity.Map;
import app.entity.Robot;
import app.service.BluetoothChatService;
import app.util.DialogUtil;
import app.util.IntentBuilder;
import app.util.MdpLog;
import app.util.PrefUtility;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.view.MapView;
import ntu.cz3004.controller.view.adapter.MDPMessageAdapter;
import ntu.cz3004.controller.view.adapter.MDPPagerAdapter;
import ntu.cz3004.controller.view.holder.ControlsViewHolder;
import ntu.cz3004.controller.view.holder.InfoViewHolder;
import ntu.cz3004.controller.view.holder.MapEditViewHolder;
import ntu.cz3004.controller.view.holder.MessagesViewHolder;

public class MainActivity extends AppCompatActivity implements BluetoothStatusListener, Map.OnMapChangedListener, View.OnClickListener, MdpParser.OnParseResultListener {
    private static final String TAG = "mdp.act.main";
    private BTRobotController controller;
    private MdpParser mdpParser;
    // map
    private MapView mv;
    private Map map;
    private MapEditor mapEditor;
    // BT chat
    private MDPMessageAdapter msgAdapter;
    // views & holder
    private ViewPager pager;
    private View viewMain, viewBtChat;
    private ControlsViewHolder vhControls;
    private MapEditViewHolder vhMapEdit;
    private InfoViewHolder vhInfo;
    private MessagesViewHolder vhMessages;
    // multi-threading
    final Handler handler = new Handler();
    private Runnable taskUpdateMap, taskReconnectBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // init ui
        initPager();
        initMap();
        initRobotControls();
        initMapEdit();
        initBTChat();
        initInfoSheet();
        loadInfoMessages();

        // set up controller
        Command cmd = PrefUtility.getCommand(this);
        boolean enableSimulation = PrefUtility.isEnableSimulation(this);
        controller = new BTRobotController(this, mapEditor, cmd);
        controller.registerListener(this);
        controller.setEnableSimulation(enableSimulation);
        controller.setMessageIntervalMs(Constants.MESSAGE_INTERVAL_MS);
        if (!controller.isSupported()){
            DialogUtil.promptBluetoothNotAvailable(this);
        } else {
            onStateChanges(controller.getState());
            reconnectLastDevice();
        }
        mdpParser = new MdpParser(this);
    }

    private void initPager(){
        pager = findViewById(R.id.pager);
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);
        viewMain = LayoutInflater.from(this).inflate(R.layout.layout_map, pager, false);
        viewBtChat = LayoutInflater.from(this).inflate(R.layout.layout_chat, pager, false);
        View[] views = new View[]{viewMain, viewBtChat};
        MDPPagerAdapter adapter = new MDPPagerAdapter(isTablet, views);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                invalidateOptionsMenu();
            }
        });
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
            }
        });
    }

    private void initBTChat(){
        ArrayList<MDPMessage> messages = new ArrayList();
        msgAdapter = new MDPMessageAdapter(messages);
        vhMessages = new MessagesViewHolder(viewBtChat, msgAdapter);
        vhMessages.setOnSendClickListener((v)->{
            String msg = vhMessages.etMessage.getText().toString();
            if (msg!=null && msg.length()>0){
                if (msg.startsWith("?")){
                    MdpLog.d(TAG,"received simulation message!");
                    MDPMessage btMsg = new MDPMessage(MDPMessage.Type.INCOMING, "SYS", msg.substring(1));
                    onCommunicate(btMsg);
                    return;
                }
                // send message
                if (controller.isConnected()){
                    controller.sendMessage(msg);
                }else {
                    showToast(getString(R.string.not_connected));
                }
            }
        });
        vhMessages.setOnLongClickSendClickListener((v -> {
            DialogUtil.promptDialogTestMessages(this, vhMessages.etMessage);
            return true;
        }));
    }

    private void loadInfoMessages(){
        msgAdapter.add(new MDPMessage(MDPMessage.Type.SYSTEM, "sys", "AY20/21S2 Group1"));
        String[] notes = getResources().getStringArray(R.array.msg_protocol);
        for(String note: notes){
            String[] parts = note.split(":");
            msgAdapter.add(new MDPMessage(MDPMessage.Type.INFO,parts[0], parts[1]));
        }
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

        // use accelerometer
        boolean enableAccelerometer = PrefUtility.isEnableAccelerometer(this);
        controller.setEnableAccelerometer(enableAccelerometer);
        controller.startSensor();

        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        stopBTReconnectTask();
        stopAutoUpdateTask();
        controller.endSensor();

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
        controller.setPauseSensor(false);
        vhControls.setVisible(true);
        vhMapEdit.setVisible(false);
        mapEditor.setMode(MapEditor.Mode.NONE);
        mv.setDraggable(false);
        vhInfo.tvStatusMain.setText(R.string.controller_cap);
        vhInfo.tvStatusSub.setVisibility(View.VISIBLE);
    }

    private void switchToMapEdit(){
        controller.setPauseSensor(true);
        vhControls.setVisible(false);
        vhMapEdit.setVisible(true);
        vhMapEdit.returnToStart();
        mapEditor.setMode(MapEditor.Mode.ROBOT);
        mv.setDraggable(true);
        vhInfo.tvStatusMain.setText(getString(R.string.map_edition_cap));
        vhInfo.tvStatusSub.setVisibility(View.GONE);
    }

    private void requestSpeech(){
        try {
            startActivityForResult(IntentBuilder.speechInput( getString(R.string.speech_cmd_hint)), Constants.REQUEST_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            showToast(getString(R.string.speech_not_supported));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // robot control
            case R.id.btn_ctrl_up: controller.up(); break;
            case R.id.btn_ctrl_left: controller.left(); break;
            case R.id.btn_ctrl_right: controller.right(); break;
            case R.id.btn_ctrl_down: controller.down(); break;
            case R.id.btn_ctrl_f1: controller.f1(); break;
            case R.id.btn_ctrl_f2: controller.f2(); break;
            case R.id.btn_ctrl_f3: controller.f3(); break;
            case R.id.btn_ctrl_explore: controller.explore(); break;
            case R.id.btn_ctrl_fastest: controller.fastest(); break;
            case R.id.btn_ctrl_img_search: controller.imgRecognition(); break;
            case R.id.btn_ctrl_get_map: controller.requestMap(); break;
            case R.id.btn_ctrl_send_map: controller.sendConfig(); break;
            case R.id.btn_ctrl_speech: requestSpeech(); break;
            default: showSnackbar(getString(R.string.work_in_progress)); break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (pager.getCurrentItem() == 1){
            inflater.inflate(R.menu.bt_chat, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // main menu
        boolean showReconnect = true;
        if (!controller.isSupported() || controller.isConnected() || PrefUtility.getLastConnectedBtDevice(this) == null){
            showReconnect = false;
        }
        menu.findItem(R.id.action_bt_reconnect).setVisible(showReconnect);
        menu.findItem(R.id.action_bt_disconnect).setVisible(controller.isConnected());
        // bt chat menu
        if (pager.getCurrentItem() == 1){
             menu.findItem(R.id.action_show_received).setChecked(msgAdapter.isShowReceived());
             menu.findItem(R.id.action_show_sent).setChecked(msgAdapter.isShowSent());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // main menu
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
                return true;
            // bt chat menu
            case R.id.action_show_sent:
                item.setChecked(!item.isChecked());
                msgAdapter.setShowSent(item.isChecked());
                return true;
            case R.id.action_show_received:
                item.setChecked(!item.isChecked());
                msgAdapter.setShowReceived(item.isChecked());
                return true;
            case R.id.action_clear_history:
                msgAdapter.clear();
                return true;
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
                Command cmd = PrefUtility.getCommand(this);
                boolean enableSimulation = PrefUtility.isEnableSimulation(this);
                controller.setCommand(cmd);
                controller.setEnableSimulation(enableSimulation);
                break;

            case Constants.REQUEST_SPEECH_INPUT:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    ArrayList<String> messages = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    MdpLog.d(TAG, messages.toString());
                    mdpParser.parseSpeechCommands( messages.toArray(new String[messages.size()]));
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private Toast toast;
    public void showToast(String message){
        if(toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private Snackbar snackbar;
    public  void showSnackbar(String message){
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
        invalidateOptionsMenu();
        switch (state){
            case BluetoothChatService.STATE_CONNECTED:
                getSupportActionBar().setSubtitle( getString(R.string.connected_to__device_, controller.getConnectedDeviceName()) );
                break;
            case BluetoothChatService.STATE_CONNECTING:
                getSupportActionBar().setSubtitle( getString(R.string.connecting_)) ;
                break;
            case BluetoothChatService.STATE_LISTEN:
                getSupportActionBar().setSubtitle(getString(R.string.await_for_connection));
                break;
            case BluetoothChatService.STATE_NONE:
                getSupportActionBar().setSubtitle(getString(R.string.not_connected));
                break;
        }
    }

    @Override
    public void onCommunicate(MDPMessage message) {
        msgAdapter.add(message);
        vhMessages.scrollToEnd();

        if (message.getType() == MDPMessage.Type.INCOMING){
            mdpParser.parseMessage(message.getContent());
        }
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
            String lastConnect = PrefUtility.getLastConnectedBtDevice(this);
            if (lastConnect!=null && controller.shouldReconnect()){
                boolean isSecure = PrefUtility.isSecureConnection(this);
                controller.connectDevice(lastConnect, isSecure);
            }
        }
    }

    // parsing messages
    @Override
    public void onReceivedStatus(String status) {
        vhInfo.tvStatusSub.setText(status);
    }

    @Override
    public void onReceivedMapRequest() {
        String response = String.format("MAP|%s|%s|%s",map.getRobot().toString(), map.getPartI(), map.getPartII());
        controller.sendMessage(response);
    }

    @Override
    public void onReceivedMapUpdate(int x, int y, int direction, String p1, String p2) {
        map.getRobot().set(x,y,direction);
        map.updateMapAs(p1,p2);
        map.notifyChanges();
        mapEditor.highlightRobot();
    }

    @Override
    public void onReceivedMove(MdpParser.MoveType type, int numMove) {
        Robot bot = map.getRobot();
        switch (type){
            case FORWARD:bot.moveForwardBy(numMove); break;
            case TURN_BACK:bot.turnBack(); break;
            case TURN_LEFT:bot.turnLeft(); break;
            case TURN_RIGHT:bot.turnRight(); break;
        }
        map.notifyChanges();
        mapEditor.highlightRobot();
    }

    @Override
    public void onReceivedNavigation(MdpParser.NavigationType type) {
        switch (type){
            case EXPLORATION: controller.explore(); break;
            case FASTEST_PATH: controller.fastest(); break;
            case IMAGE_RECOGNITION: controller.imgRecognition(); break;
        }
    }

    @Override
    public void onReceivedNewImage(String strImage) {
        map.addImage(strImage);
    }

    @Override
    public void onReceivedNewImageSet(String strImage) {
        map.updateImageAs(strImage);
    }

    // multi-threading tasks
    public void startAutoUpdateTask(){
        if (taskUpdateMap == null){
            taskUpdateMap = new Runnable() {
                @Override
                public void run() {
                    MdpLog.d("mdp.threads", "req map");
                    controller.requestMap();
                    handler.postDelayed(this, Constants.MAP_UPDATE_INTERVAL_MS);
                }
            };
        }
        handler.postDelayed(taskUpdateMap, Constants.MAP_UPDATE_INTERVAL_MS);
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
                        showToast(getString(R.string.bluetooth_not_enable));
                    } else {
                        reconnectLastDevice();
                    }
                    handler.postDelayed(this, Constants.BT_RECONNECT_INTERVAL_MS);
                }
            };
        }
        handler.postDelayed(taskReconnectBT, Constants.BT_RECONNECT_INTERVAL_MS);
    }

    public void stopBTReconnectTask(){
        if (taskReconnectBT != null){
            handler.removeCallbacks(taskReconnectBT);
            taskReconnectBT = null;
        }
    }
}