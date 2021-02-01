package ntu.cz3004.controller.common;

public interface Constants {
    boolean SECURE_BLUETOOTH_CONNECTION = false;
    int SCAN_DURATION_SEC = 30;
    int DISCOVERABLE_DURATION_SEC = 60;
    int MESSAGE_INTERVAL_MS = 500;
    int MAP_UPDATE_INTERVAL_MS = 1000;
    int BT_RECONNECT_INTERVAL_MS = 3000;

    // request codes
    int REQUEST_ENABLE_BT = 1001;
    int REQUEST_DISCOVER_BT = 1002;
    int REQUEST_PICK_BT_DEVICE = 1003;
    int REQUEST_SETTING = 1004;
    int REQUEST_LOCATION_PERMISSION = 2001;

    // intent extra
    String EXTRA_FRAGMENT = "fragment";
    String EXTRA_DEVICE_ADDRESS = "device_address";


    // Message types sent from the BluetoothChatService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";
}
