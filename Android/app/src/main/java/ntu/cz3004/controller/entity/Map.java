package ntu.cz3004.controller.entity;

import android.graphics.Point;

import java.util.HashMap;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.Utility;

/**
 * Entity to represent the map's status.
 * <ul>
 *     <li>size of the map is 15*20</li>
 *     <li>each cell can either be unexplored, explored or obstacle state</li>
 *     <li>contains many annotations like robot, way-point or detected images</li>
 * </ul>
 * @see MapAnnotation
 */
public class Map {
    private final static String TAG = "mdp.map";
    public final static int MAX_ROW = 20;
    public final static int MAX_COL = 15;
    public static final int STATE_UNEXPLORED = 0;
    public static final int STATE_EXPLORED = 1;
    public static final int STATE_OBSTACLE = 2;
    private static int[][] cellStates;
    private Robot robot;
    private MapAnnotation wayPoint;
    private HashMap<String, MapAnnotation> images;
    private OnMapChangedListener listener;

    private String p1 = null;
    private String p2 = null;
    private String strImages = null;


    public interface OnMapChangedListener{
        void onMapChanged();
    }


    public Map() {
        cellStates = new int[MAX_ROW][MAX_COL];
        robot = new Robot();
        images = new HashMap<>();
    }

    public OnMapChangedListener getListener() {
        return listener;
    }

    public void setListener(OnMapChangedListener listener) {
        this.listener = listener;
    }

    public void notifyChanges(){
        if (this.listener != null){
            this.listener.onMapChanged();
        }
    }

    public Robot getRobot() {
        return robot;
    }

    public MapAnnotation getWayPoint() {
        return wayPoint;
    }

    /**
     * Creating a way point annotation on given position
     * @param row row of center position of way point
     * @param col column of center position of way point
     * @return true is way point if way point is created
     * @see #isSafeMove(int, int, boolean)
     */
    public boolean createWayPointAt(int row, int col){
        if (!isSafeMove(row, col, true)){
            return false;
        }
        wayPoint = new MapAnnotation(col, row, R.drawable.ic_way_point, "way point");
        notifyChanges();
        return true;
    }

    /**
     * remove way point from the map
     */
    public void clearWayPoint(){
        this.wayPoint = null;
        notifyChanges();
    }


    public HashMap<String, MapAnnotation> getImages() {
        return images;
    }

    public void setImages(HashMap<String, MapAnnotation> images) {
        this.images = images;
    }

    /**
     * update map status based on map descriptor string
     * @param explorations part 1 of descriptor that represent exploration status
     * @param obstacles part 2 of descriptor that represent obstacle status of explored region
     */
    public void mapFromString(String explorations, String obstacles) {
        MdpLog.d(TAG, "update map from string");
        MdpLog.d(TAG, "Explore   : "+explorations);
        MdpLog.d(TAG, "Obstacles : "+obstacles);
        String tempP1 = Utility.hexToBinary(explorations);
        tempP1 = tempP1.substring(2, tempP1.length() - 2);//remove header and tail

        String tempP2 ="F" + obstacles;//append "F" to prevent 0s being removed during conversion.
        tempP2 = Utility.hexToBinary(tempP2);
        tempP2 = tempP2.substring(4);//remove "F" in binary

        // read and update exploration states
        int[][] map = new int[MAX_ROW][MAX_COL];
        int row, col;
        try {
            for (row = 0; row < MAX_ROW; row++) {
                for (col = 0; col < MAX_COL; col++) {
                    map[row][col] = Character.getNumericValue(tempP1.charAt(row * MAX_COL + col));
                }
            }
        } catch (Exception e) {
            MdpLog.w(TAG, "parseExploration: Error parsing");
        }


        // read and update obstacles states (only update explored region)
        int i = 0;
        for (row = 0; row < MAX_ROW; row++) {
            for (col = 0; col < MAX_COL; col++) {
                if (map[row][col] == STATE_EXPLORED) {
                    try {
                        if (Character.getNumericValue(tempP2.charAt(i)) == 1) {
                            map[row][col] = STATE_OBSTACLE;
                        }
                        i++;
                    } catch (Exception e) {
                        MdpLog.w(TAG, "parseObstacle: Error parsing");
                        break;
                    }
                }
            }
        }
        cellStates = map;
        p1 = explorations;
        p2 = obstacles;
        notifyChanges();
    }



    /**
     * get cell state at given position
     * @param row rol of cell
     * @param col column of cell
     * @return state of cell on given position
     */
    public int getStateAt(int row, int col) {
        return cellStates[row][col];
    }

    /**
     * change a cell's state to another state
     * @param row row of cell to be changed
     * @param col column of cell to be changed
     * @param state new state of the cell
     */
    public void setCellAs(int row, int col, int state){
        if (state<STATE_UNEXPLORED || state>STATE_OBSTACLE){
            return;
        }
        cellStates[row][col] = state;
        p1 = null;
        p2 = null;
    }

    /**
     * change entire map to a given state
     * @param state new state
     */
    public void setAllCellAs(int state){
        if (state<STATE_UNEXPLORED || state>STATE_OBSTACLE){
            return;
        }
        for (int r=0; r<MAX_ROW; r++){
            for (int c=0; c<MAX_COL; c++){
                cellStates[r][c] = state;
            }
        }
        p1 = null;
        p2 = null;
        notifyChanges();
    }


    // not used
    public static int[][] getCellStates() {
        return cellStates;
    }

    /**
     * to check if the cell is able to place an annotation
     * @param row row of center position
     * @param col column of center position
     * @param checkSurrounding true is the annotation is more than a cell
     * @return true if cell is able to place annotation, false the otherwise
     */
    public boolean isSafeMove(int row, int col, boolean checkSurrounding) {
        if (!checkSurrounding) {
            return getStateAt(row, col) != STATE_OBSTACLE;
        }
        // check edge
        if (row + 1 >= Map.MAX_ROW || row - 1 < 0 || col + 1 >= Map.MAX_COL || col - 1 < 0) {
            MdpLog.v(TAG,String.format("isSafeMove: pos(%d, %d) might hit wall", row,col));
            return false;
        }
        // check obstacle
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                if (getStateAt(r, c) == Map.STATE_OBSTACLE) {
                    MdpLog.v(TAG,String.format("isSafeMove: pos(%d, %d) might hit obstacle", r,c));
                    return false;
                }
            }
        }

        MdpLog.v(TAG,String.format("isSafeMove: pos(%d, %d) is safe", row,col));
        return true;
    }

    /**
     * convert current map status into part 1 of map descriptor
     * @return string that represent exploration status
     * @see #mapFromString(String, String)
     */
    public String getPartI(){
        if (p1 != null){
            return p1;
        }
        // flatten cell status
        String part1 = "";
        for ( int r=0; r<MAX_ROW; r++ ) {
            for ( int c=0; c<MAX_COL; c++ ) {
                part1+= cellStates[r][c];
            }
        }
        part1 = part1.replace(""+STATE_OBSTACLE, ""+STATE_EXPLORED);
        part1 = "11"+part1+"11";
        p1 = Utility.binaryToHex(part1).toUpperCase();
        return p1;
    }

    /**
     * convert current map status into part 2 of map descriptor
     * @return string that represent obstacle status
     * @see #mapFromString(String, String)
     */
    public String getPartII(){
        if (p2 != null){
            return p2;
        }
        String part2 = "1000";
        int state = 0;
        for ( int r=0; r<MAX_ROW; r++ ) {
            for ( int c=0; c<MAX_COL; c++ ) {
                state = cellStates[r][c];
                if (state!=STATE_UNEXPLORED){
                    switch (state){
                        case STATE_EXPLORED: part2+=0; break;
                        case STATE_OBSTACLE: part2+=1; break;
                    }
                }
            }
        }

        String result = Utility.binaryToHex(part2).toUpperCase();
        if (result.length()>0){
            p2 = result.substring(1);
        } else {
            p2 = "";
        }
        return p2;
    }

    /**
     * reset the map to initialised state
     */
    public void reset(){
        setAllCellAs(STATE_UNEXPLORED);
        robot.setPosition(new Point(1,1));
        robot.setDirection(0);
        wayPoint=null;
        images.clear();
        p1 = null;
        p2 = null;
        strImages = null;
        notifyChanges();
    }

}
