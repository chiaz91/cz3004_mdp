package ntu.cz3004.controller.control;

import ntu.cz3004.controller.entity.Command;

public class BTRobotController {
    private static final String TAG = "mdp.ctrl.bt_robot";
    private BluetoothController bluetoothController;
    private MapEditor mapEditor;
    private Command command;
    private boolean enableSimulation = false;

    public BTRobotController(BluetoothController bluetoothController, MapEditor mapEditor, Command command) {
        this.bluetoothController = bluetoothController;
        this.mapEditor = mapEditor;
        this.command = command;
    }

    public BluetoothController getBluetoothController() {
        return bluetoothController;
    }


    public void setBluetoothController(BluetoothController bluetoothController) {
        this.bluetoothController = bluetoothController;
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
        if (!bluetoothController.isConnected()){
            return false;
        }
        if (enableSimulation){
            boolean success = mapEditor.robotMoveFront();
            if (success ){
                bluetoothController.sendMessage(command.up);
            }
            return success;
        } else {
            bluetoothController.sendMessage(command.up);
            return true;
        }
    }

    public boolean down(){
        if (!bluetoothController.isConnected()){
            return false;
        }
        if (enableSimulation){
            boolean success = mapEditor.robotMoveBack();
            if (success ){
                bluetoothController.sendMessage(command.down);
            }
            return success;
        } else {
            bluetoothController.sendMessage(command.down);
            return true;
        }
    }

    public boolean left(){
        if (!bluetoothController.isConnected()){
            return false;
        }
        if (enableSimulation){
            mapEditor.robotTurnLeft();
        }
        bluetoothController.sendMessage(command.left);
        return true;
    }
    public boolean right(){
        if (!bluetoothController.isConnected()){
            return false;
        }
        if (enableSimulation){
            mapEditor.robotTurnRight();
        }
        bluetoothController.sendMessage(command.right);
        return true;
    }

    public void f1(){
        bluetoothController.sendMessage(command.f1);
    }

    public void f2(){
        bluetoothController.sendMessage(command.f2);
    }
    public void explore(){
        bluetoothController.sendMessage(command.explore);
    }

    public void fastest(){
        bluetoothController.sendMessage(command.fastest);
    }

    public void imgRecognition(){
        bluetoothController.sendMessage(command.imgRecognition);
    }

    public void requestMap(){
        bluetoothController.sendMessage(command.reqMap);
    }
}
