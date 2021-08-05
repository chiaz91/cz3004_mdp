package app.entity;

import android.graphics.Point;

import ntu.cz3004.controller.R;
import app.common.Direction;
import app.util.MdpLog;

public class Robot extends MapAnnotation {
    private static final String TAG = "mdp.robot";
    private int direction;

    public Robot() {
        this(1,1, Direction.NORTH);
    }

    public Robot(int x, int y, int direction) {
        super(x,y, R.drawable.ic_robot, "robot");
        this.direction = direction;
    }

    public void set(int x, int y, int direction){
        setPosition(x,y);
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void moveForwardBy(int moves){
        setPosition(getForwardPosition(moves));
    }

    public void turnRight(){
        direction +=90;
        if (direction>=360){
            direction = direction%360;
        }
        MdpLog.v(TAG, String.format("robot.right:: pos(%d, %d), dir(%d)",getX(), getY(), getDirection()));
    }

    public void turnLeft(){
        direction -=90;
        if (direction<0){
            direction+=360;
            direction = direction%360;
        }

        MdpLog.v(TAG, String.format("robot.left:: pos(%d, %d), dir(%d)",getX(), getY(), getDirection()));
    }

    public void turnBack(){
        // OR turn right*2
        direction += 180;
        if (direction>=360){
            direction = direction%360;
        }
        MdpLog.v(TAG, String.format("robot.back:: pos(%d, %d), dir(%d)",getX(), getY(), getDirection()));
    }

    public Point getForwardPosition(){
        return getForwardPosition(1);
    }

    public Point getForwardPosition(int moves){
        switch (direction){
            case Direction.NORTH: return new Point(getX(), Math.min(Map.MAX_ROW-1, getY()+moves));
            case Direction.SOUTH: return new Point(getX(), Math.max(0, getY()-moves));
            case Direction.EAST:  return new Point(Math.min(Map.MAX_COL-1, getX()+moves), getY());
            case Direction.WEST:  return new Point(Math.max(0, getX()-moves) , getY());
            default:
                MdpLog.a(TAG, "DIRECTION INVALID!!! direction="+direction);
                return getPosition();
        }
    }

    public Point getBackwardPosition(){
        turnBack();
        Point position = getForwardPosition();
        turnBack();
        return position;
    }

    public void reset(){
        set(1,1,0);
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d", getX(), getY(), getDirection()/90);
    }
}
