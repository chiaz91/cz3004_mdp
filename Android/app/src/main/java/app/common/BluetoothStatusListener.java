package app.common;

import app.entity.BTMessage;

public interface BluetoothStatusListener {
    void onStateChanges(int state);
    void onCommunicate(BTMessage message);
    void onToastMessage(String message);

}
