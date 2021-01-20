package ntu.cz3004.controller.listener;

import ntu.cz3004.controller.entity.BTMessage;

public interface BluetoothStatusListener {
    void onStateChanges(int state);
    void onCommunicate(BTMessage message);
    void onToastMessage(String message);

}
