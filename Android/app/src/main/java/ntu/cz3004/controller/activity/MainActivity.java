package ntu.cz3004.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
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
import ntu.cz3004.controller.util.IntentBuilder;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.PrefUtility;
import ntu.cz3004.controller.view.MapView;

public class MainActivity extends AppCompatActivity implements BluetoothStatusListener, Map.OnMapChangedListener, View.OnClickListener {
    private static final String TAG = "mdp.act.main";
    private BluetoothController controller;
    private Command cmd;
    // pager
    private View viewMain, viewBtChat;
    // map
    private MapView mv;
    private Map map;
    private ImageButton btnGetUpdate;
    // BT chat
    private BTMessageAdapter btMessageAdapter;
    private EditText etMessage;
    private ImageButton btnSend;
    private RecyclerView rvChatHistory;

    // BT edit map
    private CheckBox cbMapEdit;
    private MapEditor mapEditor;

    ViewGroup vgControls, vgMapEditMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        controller = new BluetoothController(this);
        controller.registerListener(this);
        String lastConnect = PrefUtility.getLastConnectedBtDevice(this);
        if (lastConnect!=null){
            controller.connectDevice(lastConnect, Constants.SECURE_BLUETOOTH_CONNECTION);
        } else {
            getSupportActionBar().setSubtitle(getString(R.string.not_connected));
        }

        initPager();
        initMap();
        initRobotControls();
        initMapEdit();
        initBTChat();
        loadTestData();
        cmd = PrefUtility.getCommand(this);
    }
    private void initPager(){
        ViewPager pager = findViewById(R.id.pager);
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
        vgControls = viewMain.findViewById(R.id.container_controls);

        // controls
        vgControls.findViewById(R.id.btn_ctrl_up).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_down).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_left).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_right).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_f1).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_f2).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_explore).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_fastest).setOnClickListener(this);
        vgControls.findViewById(R.id.btn_ctrl_stop).setOnClickListener(this);
        btnGetUpdate = vgControls.findViewById(R.id.btn_ctrl_get_map);
        btnGetUpdate.setOnClickListener(this);

    };

    private void initMapEdit(){
        cbMapEdit = viewMain.findViewById(R.id.cb_edit_map);
        cbMapEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                switchToMapEdit();
            } else {
                switchToControl();
                // TODO: Edit is completed, send update to RPi
            }
        });
        vgMapEditMode =  viewMain.findViewById(R.id.container_map_edit);
        mapEditor = new MapEditor(mv, vgMapEditMode);
        mv.setOnClickListener(v -> {
            // click action on cell
            Point position = (Point) v.getTag();
            mapEditor.editOn(position);
        });
    }

    private void initBTChat(){
        rvChatHistory = viewBtChat.findViewById(R.id.rv_messages);
        etMessage = viewBtChat.findViewById(R.id.et_msg);
        btnSend = viewBtChat.findViewById(R.id.btn_send);

        ArrayList<BTMessage> messages = new ArrayList();
        btMessageAdapter = new BTMessageAdapter(messages);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvChatHistory.setLayoutManager(linearLayoutManager);
        rvChatHistory.setAdapter(btMessageAdapter);

        btnSend.setOnClickListener((v)->{
            String msg = etMessage.getText().toString();
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
        // TODO: remove testing code
        map.mapFromString(getString(R.string.part_one_default), getString(R.string.test_pii_2));
        map.imagesFromString(getString(R.string.test_images_1));
    }
    @Override
    protected void onResume() {
        super.onResume();
        MdpLog.logVersion();
    }

    @Override
    protected void onDestroy() {
        controller.stopService();
        super.onDestroy();
    }

    private void switchToControl(){
        vgControls.setVisibility(View.VISIBLE);
        vgMapEditMode.setVisibility(View.GONE);
        mapEditor.setMode(MapEditor.Mode.NONE);
    }

    private void switchToMapEdit(){
        vgControls.setVisibility(View.GONE);
        vgMapEditMode.setVisibility(View.VISIBLE);
        mapEditor.setMode(MapEditor.Mode.ROBOT);
        ((RadioButton) vgMapEditMode.findViewById(R.id.rb_set_robot)).setChecked(true);
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
                String lastConnect = PrefUtility.getLastConnectedBtDevice(this);
                controller.connectDevice(lastConnect, Constants.SECURE_BLUETOOTH_CONNECTION);
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
                    controller.connectDevice(address, Constants.SECURE_BLUETOOTH_CONNECTION);
                    PrefUtility.setLastConnectedBtDevice(this, address);
                    // connect to device
                    // save as last connected
                }
                break;

            case Constants.REQUEST_SETTING:
                cmd = PrefUtility.getCommand(this);
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
        rvChatHistory.scrollToPosition(btMessageAdapter.getItemCount() -1);


        // TODO: decode on receiving message
    }

    @Override
    public void onToastMessage(String message) {
        showToast(message);
    }



}