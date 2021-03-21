package ntu.cz3004.controller.activity;

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
import app.entity.BTMessage;
import app.entity.Command;
import app.entity.Map;
import app.entity.MapAnnotation;
import app.entity.Robot;
import app.service.BluetoothChatService;
import app.util.DialogUtil;
import app.util.IntentBuilder;
import app.util.MdpLog;
import app.util.PrefUtility;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.BTMessageAdapter;
import ntu.cz3004.controller.adapter.MDPPagerAdapter;
import ntu.cz3004.controller.view.BTChatViewHolder;
import ntu.cz3004.controller.view.ControlsViewHolder;
import ntu.cz3004.controller.view.InfoViewHolder;
import ntu.cz3004.controller.view.MapEditViewHolder;
import ntu.cz3004.controller.view.MapView;

public class MainActivity extends AppCompatActivity implements BluetoothStatusListener, Map.OnMapChangedListener, View.OnClickListener {
    private static final String TAG = "mdp.act.main";
    private BTRobotController controller;
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
                    showToast("device not connect");
                }
            }
        });
        vhBTChat.setOnLongClickSendClickListener((v -> {
            DialogUtil.promptDialogTestMessages(this, controller);
            return true;
        }));
    }

    private void loadInfoMessages(){
        btMessageAdapter.add(new BTMessage(BTMessage.Type.SYSTEM, "sys", "AY20/21S2 Group1"));
        String[] notes = getResources().getStringArray(R.array.release_notes);
        for(String note: notes){
            String[] parts = note.split(":");
            btMessageAdapter.add(new BTMessage(BTMessage.Type.OUTGOING, parts[0], parts[1]));
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
        vhInfo.tvStatusMain.setText("Controller");
        vhInfo.tvStatusSub.setVisibility(View.VISIBLE);
    }

    private void switchToMapEdit(){
        controller.setPauseSensor(true);
        vhControls.setVisible(false);
        vhMapEdit.setVisible(true);
        vhMapEdit.returnToStart();
        mapEditor.setMode(MapEditor.Mode.ROBOT);
        vhInfo.tvStatusMain.setText("Map Editing");
        vhInfo.tvStatusSub.setVisibility(View.GONE);
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
            case R.id.btn_ctrl_send_map: sendConfigMessage(); break;
            default: showSnackbar("work in progress"); break;
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
             menu.findItem(R.id.action_show_received).setChecked(btMessageAdapter.isShowReceived());
             menu.findItem(R.id.action_show_sent).setChecked(btMessageAdapter.isShowSent());
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
            case R.id.action_import_map:
                DialogUtil.promptImportMap(this, map);
                return true;
            // bt chat menu
            case R.id.action_show_sent:
                item.setChecked(!item.isChecked());
                btMessageAdapter.setShowSent(item.isChecked());
                return true;
            case R.id.action_show_received:
                item.setChecked(!item.isChecked());
                btMessageAdapter.setShowReceived(item.isChecked());
                return true;
            case R.id.action_clear_history:
                btMessageAdapter.clear();
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
        invalidateOptionsMenu();
        switch (state){
            case BluetoothChatService.STATE_CONNECTED:
                getSupportActionBar().setSubtitle( getString(R.string.connected_to__device_, controller.getConnectedDeviceName()) );
                break;
            case BluetoothChatService.STATE_CONNECTING:
                getSupportActionBar().setSubtitle( getString(R.string.connecting_)) ;
                break;
            case BluetoothChatService.STATE_LISTEN:
                getSupportActionBar().setSubtitle("Await for connection");
                break;
            case BluetoothChatService.STATE_NONE:
                getSupportActionBar().setSubtitle(getString(R.string.not_connected));
                break;
        }
    }

    @Override
    public void onCommunicate(BTMessage message) {
        btMessageAdapter.add(message);
        vhBTChat.scrollToEnd();

        if (message.getType() == BTMessage.Type.INCOMING){
            parseMessage(message.getContent());
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
            String lastConnect = PrefUtility.getLastConnectedBtDevice(MainActivity.this);
            if (lastConnect!=null && controller.shouldReconnect()){
                boolean isSecure = PrefUtility.isSecureConnection(MainActivity.this);
                controller.connectDevice(lastConnect, isSecure);
            }
        }
    }

    // parse messages
    private void sendConfigMessage(){
        Robot bot = map.getRobot();
        MapAnnotation wp = map.getWayPoint();
        // goal position will be sent if way-point not set
        String wpCoord = String.format("%d,%d", 18, 13);;
        if (wp != null){
            wpCoord = String.format("%d,%d",  wp.getY(), wp.getX());;
        }
        String config = String.format("CONFIG|%s|%s|%s|%s", bot.toString(), wpCoord, map.getPartI(), map.getPartII());
        controller.sendMessage(config);
    }

    private void parseMessage(String received){
        // split message to prevent concatenation of message
        String[] cmds = received.split("\n");
        for(String command : cmds){
            MdpLog.d(TAG, "parsing ["+command+"]");
            String[] parts = command.split("\\|");
            try{
                switch (parts[0]){
                    case "MAP": parseMap(parts); break;
                    case "MOV": parseMove(parts); break;
                    case "IMG": parseImage(parts); break;
                    case "IMGS": parseImages(parts); break;
                    case "STATUS": vhInfo.tvStatusSub.setText(parts[1]); break;
                    default:
                        MdpLog.d(TAG, "Unknown Message: "+received);
                }
            } catch (Exception e){
                MdpLog.d(TAG, "Error Parsing: "+received);
                e.printStackTrace();
            }
        }
    }
    private void parseMap(String... params){
        if (params.length==1){
            String response = String.format("MAP|%s|%s|%s",map.getRobot().toString(), map.getPartI(), map.getPartII());
            controller.sendMessage(response);
        } else {
            // row,col,dir
            String[] botCoord = params[1].split(",");
            int x = Integer.parseInt(botCoord[1]);
            int y = Integer.parseInt(botCoord[0]);
            int dir = Integer.parseInt(botCoord[2]);
            String p1 = params[2];
            String p2 = "";
            if (params.length>=4){
                p2 = params[3];
            }
            map.getRobot().set(x,y,dir*90);
            map.mapFromString(p1,p2);
            map.notifyChanges();
            mapEditor.highlightRobot();
        }
    }

    private void parseMove(String... params){
        Robot bot = map.getRobot();
        switch (params[1]){
            case "A": map.getRobot().turnLeft(); break;
            case "D": map.getRobot().turnRight(); break;
            case "Q": map.getRobot().turnBack(); break;
            default:
                try{
                    // move by n+1
                    int moves = Integer.parseInt(params[1]);
                    bot.moveForwardBy(moves+1);
                } catch (Exception e){
                    MdpLog.e(TAG, "Unknown command for move "+params[1] );
                    e.printStackTrace();
                }
        }
        map.notifyChanges();
        mapEditor.highlightRobot();
    }

    private void parseImage(String... param){
        MapAnnotation img = MapAnnotation.createImageFromString(param[1]);
        map.getImages().put(img.getName(), img);
        map.notifyChanges();
    }

    private void parseImages(String... params){
        String strImages = params[1].substring(1, params[1].length()-1);
        map.imagesFromString(strImages);
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
                        showToast("bluetooth is not enabled.");
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