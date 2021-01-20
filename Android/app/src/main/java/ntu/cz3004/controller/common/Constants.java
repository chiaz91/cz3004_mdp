package ntu.cz3004.controller.common;

public interface Constants {
    int SCAN_DURATION_SEC = 30;
    int DISCOVERABLE_DURATION_SEC = 60;
    int MESSAGE_INTERVAL_MS = 500;

    // request codes
    int REQUEST_ENABLE_BT = 1001;
    int REQUEST_DISCOVER_BT = 1002;
    int REQUEST_PICK_BT_DEVICE = 1003;
    int REQUEST_SETTING = 1004;
    int REQUEST_LOCATION_PERMISSION = 2001;

    // intent extra
    String EXTRA_FRAGMENT = "fragment";
    String EXTRA_DEVICE_ADDRESS = "device_address";

}
