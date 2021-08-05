package app.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;

import app.common.Direction;
import app.entity.Map;
import app.entity.Robot;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.view.adapter.MDPMapPagerAdapter;

public class DialogUtil {

    public static void promptExitAppWarming(Context context){
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
            .setTitle("Exiting")
            .setMessage("Map data and bluetooth connection will be lost")
            .setPositiveButton(R.string.confirm, (dialog, which) -> {
                ((Activity)context).finish();
            })
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)
            .create();
        alert.show();
    }

    public static void promptPageNotFound(Context context){
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.page_not_found))
            .setMessage(context.getString(R.string.page_not_found_msg))
            .setPositiveButton(context.getString(R.string.confirm),(dialog, which) -> {
                ((Activity)context).finish();
            })
            .setCancelable(false)
            .create();
        alert.show();
    }

    public static void promptBluetoothNotAvailable(Context context){
        AlertDialog alert =  new MaterialAlertDialogBuilder(context)
                .setTitle("Bluetooth not available")
                .setMessage("This device does not support bluetooth service.")
                .setPositiveButton(context.getString(R.string.confirm), (dialog, which) -> ((Activity)context).finish())
                .setCancelable(false)
                .create();
        alert.show();
    }

    public static void promptPermissionForScanDevice(Context context, DialogInterface.OnClickListener clickListener){
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.permission_required))
                .setMessage(context.getString(R.string.permission_reason_for_scan))
                .setPositiveButton(context.getString(R.string.confirm), clickListener)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .create();
        alert.show();
    }



    // map edit
    public static void promptEditMapDescriptor(Context context, Map map){
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
            map.updateMapAs(part1, part2);
            map.updateImageAs(strImages);
            alert.dismiss();
        });
    }

    // dialogs
    public static void promptEditRobot(Context context, Map map){
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_input_robot, null);
        final TextInputLayout inputX = view.findViewById(R.id.input_x);
        final TextInputLayout inputY = view.findViewById(R.id.input_y);
        final RadioGroup rgDir = view.findViewById(R.id.rg_robot_dir);

        // init
        Robot robot = map.getRobot();
        inputX.getEditText().setText(String.format("%d", robot.getX()));
        inputY.getEditText().setText(String.format("%d", robot.getY()));
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
                .setNeutralButton("RESET", (dialog, which) -> {
                    robot.reset();
                    map.notifyChanges();
                })
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
            int dir = Direction.NORTH;
            switch (rgDir.getCheckedRadioButtonId()){
                case R.id.rb_dir_north: dir=Direction.NORTH; break;
                case R.id.rb_dir_south: dir=Direction.SOUTH; break;
                case R.id.rb_dir_west: dir=Direction.WEST; break;
                case R.id.rb_dir_east: dir=Direction.EAST; break;
            }
            robot.set(x,y,dir);
            map.notifyChanges();
            alert.dismiss();
        });
    }
    public static void promptClearWayPoint(Context context, Map map){
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

    public static  void promptSetAllCellState(Context context, Map map, int state){
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


    public static void promptLoadMap(Context context, Map map){
        // data
        String saved = PrefUtility.getDebugMap(context);
        String[] maps = context.getResources().getStringArray(R.array.map_values);
        ArrayList<String> listMap = new ArrayList<>();
        listMap.add(saved);
        listMap.addAll(Arrays.asList(maps));
        MDPMapPagerAdapter adapter = new MDPMapPagerAdapter(listMap);

        // custom view
        View view = LayoutInflater.from(context).inflate(R.layout.layout_map_load, null);
        ViewPager pager = view.findViewById(R.id.pager);
        pager.setAdapter(adapter);
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
//                .setTitle("Load Map")
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Choose", (dialog, which) -> {
                    int pos = pager.getCurrentItem();
                    String mdf = listMap.get(pos);
                    map.reset();
                    map.updateAs(mdf);
                })
                .setCancelable(true)
                .create();
        alert.show();
    }



    public static void promptDialogTestMessages(Context context, EditText etMessage){
        String[] msgs = context.getResources().getStringArray(R.array.msg_test);
        AlertDialog alert = new MaterialAlertDialogBuilder(context)
                .setTitle("Test messages")
                .setItems(msgs, (dialog, which) -> {
                    String msg = msgs[which];
                    try{
                        etMessage.setText(msg);
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create();
        alert.show();
    }
}
