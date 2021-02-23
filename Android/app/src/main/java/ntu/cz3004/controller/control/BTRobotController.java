package ntu.cz3004.controller.control;

import android.content.Context;

import ntu.cz3004.controller.entity.Command;

public class BTRobotController extends BluetoothController{
    private static final String TAG = "mdp.ctrl.bt_robot";
    private MapEditor mapEditor;
    private Command command;
    private boolean enableSimulation = false;

    public BTRobotController(Context context, MapEditor mapEditor, Command command) {
        super(context);
        this.mapEditor = mapEditor;
        this.command = command;
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
            boolean success = mapEditor.robotMoveBack();
            if (success ){
                sendMessage(command.down);
            }
            return success;
        } else {
            sendMessage(command.down);
            return true;
        }
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
}
