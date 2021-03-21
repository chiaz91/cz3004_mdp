package app.common;

import app.entity.MDPMessage;

public interface BluetoothStatusListener {
    void onStateChanges(int state);
    void onCommunicate(MDPMessage message);
    void onToastMessage(String message);

}
