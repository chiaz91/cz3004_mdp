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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.BTMessageAdapter;
import ntu.cz3004.controller.adapter.MDPPagerAdapter;
import ntu.cz3004.controller.common.Constants;
import ntu.cz3004.controller.control.BluetoothController;
import ntu.cz3004.controller.entity.BTMessage;
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
            controller.connectDevice(lastConnect, true);
        } else {
            getSupportActionBar().setSubtitle(getString(R.string.not_connected));
        }

        initPager();
        initMap();
        initRobotControls();
        initBTChat();
        loadTestData();
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
        ViewGroup vgControls = viewMain.findViewById(R.id.container_controls);

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
        btMessageAdapter.add(new BTMessage(BTMessage.Type.SYSTEM, "sys", "TEST START\n(FORMAT TO BE CONFIRMED)"));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.OUTGOING, "Me", "update"));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.INCOMING, "RPI", String.format("{'map':{'p1':'%s','p2':'%s','images':'%s'},'robot':{'pos':'%s'.'dir':%s} }",getString(R.string.part_one_default), getString(R.string.test_pii_2), getString(R.string.test_images_1),"(1,1)","N")));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.OUTGOING, "Me", String.format("{'robot':'(1,1)','dir':'N','way-pt':'(6,14)'}")));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.OUTGOING, "Me", "update"));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.INCOMING, "RPI", String.format("{'map':{'p1':'%s','p2':'%s','images':'%s'},'robot':{'pos':'%s'.'dir':%s} }",getString(R.string.part_one_default), getString(R.string.test_pii_2), getString(R.string.test_images_2),"(1,1)","N")));
        btMessageAdapter.add(new BTMessage(BTMessage.Type.SYSTEM, "sys", "TEST END"));

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // robot control
            case R.id.btn_ctrl_up: controller.sendMessage("w"); break;
            case R.id.btn_ctrl_left: controller.sendMessage("a"); break;
            case R.id.btn_ctrl_right: controller.sendMessage("d"); break;
            case R.id.btn_ctrl_down: controller.sendMessage("s"); break;
            case R.id.btn_ctrl_f1: controller.sendMessage("f1"); break;
            case R.id.btn_ctrl_f2: controller.sendMessage("f2"); break;
            case R.id.btn_ctrl_explore: controller.sendMessage("explore"); break;
            case R.id.btn_ctrl_fastest: controller.sendMessage("fastest"); break;
            case R.id.btn_ctrl_stop: controller.sendMessage("stop"); break;
            case R.id.btn_ctrl_get_map: controller.sendMessage("requestMap"); break;

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

            case R.id.action_bt_disconnect:
                controller.stopService();
                return true;
            case R.id.action_bt_reconnect:
                String lastConnect = PrefUtility.getLastConnectedBtDevice(this);
                controller.connectDevice(lastConnect, true);
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
                    controller.connectDevice(address, true);
                    PrefUtility.setLastConnectedBtDevice(this, address);
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