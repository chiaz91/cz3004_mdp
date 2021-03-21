package ntu.cz3004.controller.control;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import ntu.cz3004.controller.entity.Command;
import ntu.cz3004.controller.util.MdpLog;

public class BTRobotController extends BluetoothController implements SensorEventListener {
    private static final String TAG = "mdp.ctrl.bt_robot";
    private static final double THRESHOLD = 2.5;
    private MapEditor mapEditor;
    private Command command;
    private boolean enableSimulation = false;
    private boolean enableAccelerometer = false;
    // sensor
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int messageIntervalMs = 0;
    private long nextMessageTime = 0;
    private boolean pauseSensor = false;

    public BTRobotController(Context context, MapEditor mapEditor, Command command) {
        super(context);
        this.mapEditor = mapEditor;
        this.command = command;
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public MapEditor getMapEditor() {
        return mapEditor;
    }

    public void setMapEditor(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public boolean isEnableSimulation() {
        return enableSimulation;
    }

    public void setEnableSimulation(boolean enableSimulation) {
        this.enableSimulation = enableSimulation;
    }

    public boolean isEnableAccelerometer() {
        return enableAccelerometer;
    }

    public void setEnableAccelerometer(boolean enableAccelerometer) {
        this.enableAccelerometer = enableAccelerometer;
    }

    public boolean isPauseSensor() {
        return pauseSensor;
    }

    public void setPauseSensor(boolean pauseSensor) {
        this.pauseSensor = pauseSensor;
    }

    public int getMessageIntervalMs() {
        return messageIntervalMs;
    }

    public void setMessageIntervalMs(int messageIntervalMs) {
        this.messageIntervalMs = messageIntervalMs;
    }

    public boolean up(){
        if (!isConnected()){
            return false;
        }
        if (enableSimulation){
            boolean success = mapEditor.robotMoveFront();
            if (success ){
                sendMessage(command.up);
            }
            return success;
        } else {
            sendMessage(command.up);
            return true;
        }
    }

    public boolean down(){
        if (!isConnected()){
            return false;
        }
        if (enableSimulation){
            mapEditor.robotTurnBack();
        }
        sendMessage(command.down);
        return true;
    }

    public boolean left(){
        if (!isConnected()){
            return false;
        }
        if (enableSimulation){
            mapEditor.robotTurnLeft();
        }
        sendMessage(command.left);
        return true;
    }
    public boolean right(){
        if (!isConnected()){
            return false;
        }
        if (enableSimulation){
            mapEditor.robotTurnRight();
        }
        sendMessage(command.right);
        return true;
    }

    public void f1(){
        sendMessage(command.f1);
    }

    public void f2(){
        sendMessage(command.f2);
    }
    public void f3(){
        sendMessage(command.f3);
    }
    public void explore(){
        sendMessage(command.explore);
    }

    public void fastest(){
        sendMessage(command.fastest);
    }

    public void imgRecognition(){
        sendMessage(command.imgRecognition);
    }

    public void requestMap(){
        sendMessage(command.reqMap);
    }


    // Accelerometer related
    public void startSensor(){
        if(enableAccelerometer){
            mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void endSensor(){
        if(enableAccelerometer) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (pauseSensor){
            return;
        }
        if (System.currentTimeMillis() < nextMessageTime){
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
            float xAcc = event.values[0];
            float yAcc = -event.values[1];

            if (Math.abs(xAcc)>=Math.abs(yAcc)){
                if (xAcc > THRESHOLD) {
                    left();
                    nextMessageTime = System.currentTimeMillis() + messageIntervalMs;
                    MdpLog.v(TAG, String.format("xAcc: %.2f, yAcc: %.2f, action: left",xAcc,yAcc));
                } else if (xAcc < -THRESHOLD) {
                    right();
                    nextMessageTime = System.currentTimeMillis() + messageIntervalMs;
                    MdpLog.v(TAG, String.format("xAcc: %.2f, yAcc: %.2f, action: right",xAcc,yAcc));
                }
            } else {
                if (yAcc > THRESHOLD) {
                    up();
                    nextMessageTime = System.currentTimeMillis() + messageIntervalMs;
                    MdpLog.v(TAG, String.format("xAcc: %.2f, yAcc: %.2f, action: up",xAcc,yAcc));
                } else if (yAcc < -THRESHOLD) {
                    down();
                    nextMessageTime = System.currentTimeMillis() + messageIntervalMs;
                    MdpLog.v(TAG, String.format("xAcc: %.2f, yAcc: %.2f, action: down",xAcc,yAcc));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
