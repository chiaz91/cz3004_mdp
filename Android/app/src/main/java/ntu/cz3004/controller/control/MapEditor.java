package ntu.cz3004.controller.control;

import android.content.Context;
import android.graphics.Point;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.common.Direction;
import ntu.cz3004.controller.entity.Map;
import ntu.cz3004.controller.entity.Robot;
import ntu.cz3004.controller.util.Utility;
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

    public MapEditor( MapView mapView, View viewEditModes){
        this.context = mapView.getContext();
        this.map = mapView.getMap();
        this.mv = mapView;
        this.mode = Mode.NONE;
        // init
        RadioGroup rgEditMode = viewEditModes.findViewById(R.id.rg_map_edit_mode);
        rgEditMode.setOnCheckedChangeListener(this);
        viewEditModes.findViewById(R.id.btn_set_map).setOnClickListener(this);
        viewEditModes.findViewById(R.id.btn_set_map).setOnLongClickListener(this);
        viewEditModes.findViewById(R.id.btn_map_reset).setOnClickListener(this);
        viewEditModes.findViewById(R.id.rb_set_robot).setOnLongClickListener(this);
        viewEditModes.findViewById(R.id.rb_set_way_point).setOnLongClickListener(this);
        viewEditModes.findViewById(R.id.rb_set_unknown).setOnLongClickListener(this);
        viewEditModes.findViewById(R.id.rb_set_explored).setOnLongClickListener(this);
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
            case R.id.btn_set_map: promptEditMapDescriptor(); break;
            case R.id.btn_map_reset: resetMap(); break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()){
            case R.id.rb_set_robot: promptEditRobot(); return true;
            case R.id.rb_set_way_point: promptClearWayPoint(); return true;
            case R.id.rb_set_unknown: promptSetAllCellState(Map.STATE_UNEXPLORED); return true;
            case R.id.rb_set_explored: promptSetAllCellState(Map.STATE_EXPLORED); return true;
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
            default: return true;
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
//        showSnackbar("Copied to clipboard");
    }


    // dialogs
    public void promptEditRobot(){
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_input_robot, null);
        final TextInputLayout inputX = view.findViewById(R.id.input_x);
        final TextInputLayout inputY = view.findViewById(R.id.input_y);
        final RadioGroup rgDir = view.findViewById(R.id.rg_robot_dir);

        // init
        Robot robot = map.getRobot();
        inputX.getEditText().setText( ""+robot.getX());
        inputY.getEditText().setText( ""+robot.getY());
        switch (robot.getDirection()){
            case Direction.NORTH: rgDir.check(R.id.rb_dir_north); break;
            case Direction.SOUTH: rgDir.check(R.id.rb_dir_south); break;
            case Direction.WEST: rgDir.check(R.id.rb_dir_west); break;
            case Direction.EAST: rgDir.check(R.id.rb_dir_east); break;
        }

        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.robot)
                .setView(view)
                .setPositiveButton(context.getString(R.string.confirm),null)
                .setNegativeButton(context.getString(R.string.cancel),null)
                .create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // validate x and y coordinate
            int x = robot.getX();
            int y = robot.getY();
            inputX.setError(null);
            inputY.setError(null);

            try{
                x = Integer.parseInt(inputX.getEditText().getText().toString());
                if (x<0 || x >= Map.MAX_COL){
                    inputX.setError(context.getString(R.string.invalid));
                }
            } catch (Exception e){
                inputX.setError(context.getString(R.string.error_parsing));
            }

            try{
                y = Integer.parseInt(inputY.getEditText().getText().toString());
                if (y<0 || y >= Map.MAX_ROW){
                    inputY.setError(context.getString(R.string.invalid));
                }
            } catch (Exception e){
                inputY.setError(context.getString(R.string.error_parsing));
            }

            if (inputX.getError()!=null || inputY.getError()!=null){
                return;
            }
            if (!map.isSafeMove(y, x, true)){
                Toast.makeText(context, R.string.msg_invalid_robot_position, Toast.LENGTH_SHORT).show();
                return;
            }

            // update robot
            robot.setPosition(new Point(x,y));
            int dir = Direction.NORTH;
            switch (rgDir.getCheckedRadioButtonId()){
                case R.id.rb_dir_north: dir=Direction.NORTH; break;
                case R.id.rb_dir_south: dir=Direction.SOUTH; break;
                case R.id.rb_dir_west: dir=Direction.WEST; break;
                case R.id.rb_dir_east: dir=Direction.EAST; break;
            }
            robot.setDirection(dir);
            map.notifyChanges();
            alert.dismiss();
        });
    }
    public void promptClearWayPoint(){
        if (map.getWayPoint() == null){
            Toast.makeText(context, R.string.msg_way_point_not_set,Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.clear_way_point_title)
                .setMessage(R.string.clear_way_point_msg)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    map.clearWayPoint();
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create();
        alert.show();
    }

    public void promptSetAllCellState(int state){
        String strState = "Unknown";
        switch (state){
            case Map.STATE_EXPLORED: strState = "Explored"; break;
            case Map.STATE_OBSTACLE: strState = "Obstacle"; break;
        }
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_all_cells_as_title)
                .setMessage(context.getString(R.string.set_all_cells_as_msg, strState))
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    map.setAllCellAs(state);
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create();
        alert.show();
    }

    public void promptEditMapDescriptor(){
        InputFilter[] filers = new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            if (source != null) {
                String s = source.toString();
                if (s.contains("\n")) {
                    return s.replaceAll("\n", "");
                }
            }
            return null;
        }};

        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_input_map, null);
        final TextInputLayout inputP1 = view.findViewById(R.id.input_map_p1);
        final TextInputLayout inputP2 = view.findViewById(R.id.input_map_p2);
        final TextInputLayout inputImages = view.findViewById(R.id.input_map_images);
        inputP1.getEditText().setText(map.getPartI());
        inputP1.getEditText().setFilters(filers);
        inputP2.getEditText().setText(map.getPartII());
        inputP2.getEditText().setFilters(filers);
        inputImages.getEditText().setText(map.getImagesString());
        inputImages.getEditText().setFilters(filers);

        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.set_map_descriptor_title)
                .setView(view)
                .setPositiveButton(context.getString(R.string.confirm),null)
                .setNegativeButton(context.getString(R.string.cancel),null)
                .create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            inputP1.setError(null);
            inputP2.setError(null);
            String part1 = inputP1.getEditText().getText().toString().trim();
            if (!Utility.validate(Utility.PATTERN_PART_I, part1)){
                inputP1.setError(context.getString(R.string.invalid));
            }
            String part2 = inputP2.getEditText().getText().toString().trim();
            if (!Utility.validate(Utility.PATTERN_PART_II, part2)){
                inputP2.setError(context.getString(R.string.invalid));
            }

            String strImages = inputImages.getEditText().getText().toString().trim();
            if (inputP1.getError()!=null || inputP2.getError()!=null){
                return;
            }
            if (part1.length()==0){
                part1 = context.getString(R.string.part_one_default);
            }
            map.mapFromString(part1, part2);
            map.imagesFromString(strImages);
            alert.dismiss();
        });
    }

}
