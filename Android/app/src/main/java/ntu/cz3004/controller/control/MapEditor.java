package ntu.cz3004.controller.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.entity.Map;
import ntu.cz3004.controller.entity.Robot;
import ntu.cz3004.controller.util.DialogUtil;
import ntu.cz3004.controller.util.MdpLog;
import ntu.cz3004.controller.util.PrefUtility;
import ntu.cz3004.controller.util.Utility;
import ntu.cz3004.controller.view.MapEditViewHolder;
import ntu.cz3004.controller.view.MapView;

/** Assist in map editing
 * <ul>
 *     <li>save edit mode, perform respective edition when a position given</li>
 *     <li>update map view when map/robot is modified</li>
 * </ul>
 */
public class MapEditor implements View.OnClickListener, View.OnLongClickListener, RadioGroup.OnCheckedChangeListener {
    public enum Mode {
        NONE, ROBOT, WAY_POINT, CELL_UNKNOWN, CELL_EXPLORED, CELL_OBSTACLE;
    }
    private Context context;
    private Map map;
    private MapView mv;
    private Mode mode;

    public MapEditor(MapView mapView, MapEditViewHolder viewHolder){
        this.context = mapView.getContext();
        this.map = mapView.getMap();
        this.mv = mapView;
        this.mode = Mode.NONE;

        // init
        viewHolder.setOnCheckedChangeListener(this);
        viewHolder.setOnClickListener(this);
        viewHolder.setOnLongClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        mv.setSelection(null);
        switch (checkedId){
            case R.id.rb_set_robot: setMode(Mode.ROBOT); break;
            case R.id.rb_set_way_point: setMode(Mode.WAY_POINT); break;
            case R.id.rb_set_unknown: setMode(Mode.CELL_UNKNOWN); break;
            case R.id.rb_set_explored: setMode(Mode.CELL_EXPLORED); break;
            case R.id.rb_set_obstacle: setMode(Mode.CELL_OBSTACLE); break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_set_map: DialogUtil.promptEditMapDescriptor(context, map); break;
            case R.id.btn_map_save: saveMap(); break;
            case R.id.btn_map_reset: resetMap(); break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.rb_set_robot: DialogUtil.promptEditRobot(context, map); return true;
            case R.id.rb_set_way_point: DialogUtil.promptClearWayPoint(context, map); return true;
            case R.id.rb_set_unknown: DialogUtil.promptSetAllCellState(context, map, Map.STATE_UNEXPLORED); return true;
            case R.id.rb_set_explored: DialogUtil.promptSetAllCellState(context, map, Map.STATE_EXPLORED); return true;
            case R.id.btn_set_map: copyMapDescriptor(); return true;
        }
        return false;
    }

    public Map getMap(){
        return map;
    }

    public void highlightRobot(){
        Robot robot = map.getRobot();
        mv.setSelection(robot.getPosition());
    }

    public boolean robotMoveFront(){
        Robot robot = map.getRobot();
        Point nextPosition = robot.getForwardPosition();
        boolean success = moveRobotTo(nextPosition, false);
        if (success){
            highlightRobot();
        }
        return success;
    }

    public boolean robotMoveBack(){
        Robot robot = map.getRobot();
        Point nextPosition = robot.getBackwardPosition();
        boolean success = moveRobotTo(nextPosition, false);
        if (success){
            highlightRobot();
        }
        return success;
    }

    public void robotTurnLeft(){
        map.getRobot().turnLeft();
        map.notifyChanges();
        highlightRobot();
    }

    public void robotTurnRight(){
        map.getRobot().turnRight();
        map.notifyChanges();
        highlightRobot();
    }

    public void robotTurnBack(){
        map.getRobot().turnBack();
        map.notifyChanges();
        highlightRobot();
    }


    public void setMode(Mode mode){
        this.mode = mode;
    }

    public boolean editOn(Point position){
        mv.setSelection(position);
        switch (mode){
            case ROBOT: return moveRobotTo(position, true);
            case WAY_POINT: return moveWayPointTo(position);
            case CELL_UNKNOWN: return editCellAs(position, Map.STATE_UNEXPLORED);
            case CELL_EXPLORED: return editCellAs(position, Map.STATE_EXPLORED);
            case CELL_OBSTACLE: return editCellAs(position, Map.STATE_OBSTACLE);
            default: return false;
        }
    }




    public boolean moveRobotTo(Point position, boolean showWarning){
        Robot robot = map.getRobot();
        if (map.isSafeMove(position.y, position.x, true)){
            robot.setPosition(position);
            map.notifyChanges();
            return true;
        } else {
            if (showWarning){
                mv.setSelection(position, MapView.FLAG_COLOR_WARN|MapView.FLAG_HIGHLIGHT_SURROUNDING);
                mv.invalidate();
            }
            return false;
        }
    }

    public boolean moveWayPointTo(Point position){
        boolean success = map.createWayPointAt(position.y, position.x);
        if (!success){
            mv.setSelection(position, MapView.FLAG_COLOR_WARN|MapView.FLAG_HIGHLIGHT_SURROUNDING);
        }
        return success;
    }

    public boolean editCellAs(Point position, int toState){
        map.setCellAs(position.y, position.x, toState);
        map.notifyChanges();
        return true;
    }

    public void resetMap(){
        mv.clear();
        map.reset();
    }


    private void copyMapDescriptor(){
        String strMap = String.format("P1: %s\nP2: %s\nImg: %s", map.getPartI(), map.getPartII(), map.getImagesString());
        Utility.copyToClipboard(context, strMap);
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void saveMap(){
        String saving = String.format("%s|%s|%s", map.getPartI(), map.getPartII(), map.getImages());
        SharedPreferences pref = PrefUtility.getSharePreferences(mv.getContext());
        String key = mv.getContext().getString(R.string.key_debug_map);
        pref.edit().putString(key, saving).apply();

        MdpLog.d("mdp.save", "saving "+saving);
    }




}
