package ntu.cz3004.controller.view.holder;

import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import ntu.cz3004.controller.R;

public class MapEditViewHolder extends MDPViewHolder{
    public HorizontalScrollView hsv;
    public RadioGroup rgEditMode;
    public RadioButton rbRobot, rbWayPoint, rbCellUnknown, rbCellExplored, rbCellObstacle;
    public Button btnMap, btnSave,  btnReset;

    public MapEditViewHolder(View view) {
        super(view);
        hsv = (HorizontalScrollView) view;
        rgEditMode = view.findViewById(R.id.rg_map_edit_mode);
        rbRobot = view.findViewById(R.id.rb_set_robot);
        rbWayPoint = view.findViewById(R.id.rb_set_way_point);
        rbCellUnknown = view.findViewById(R.id.rb_set_unknown);
        rbCellExplored = view.findViewById(R.id.rb_set_explored);
        rbCellObstacle = view.findViewById(R.id.rb_set_obstacle);
        btnMap = view.findViewById(R.id.btn_set_map);
        btnSave = view.findViewById(R.id.btn_map_save);
        btnReset = view.findViewById(R.id.btn_map_reset);
    }

    public void setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener listener){
        rgEditMode.setOnCheckedChangeListener(listener);
    }

    public void setOnClickListener(View.OnClickListener listener){
        btnMap.setOnClickListener(listener);
        btnSave.setOnClickListener(listener);
        btnReset.setOnClickListener(listener);
    }

    public void setOnLongClickListener(View.OnLongClickListener listener){
        btnMap.setOnLongClickListener(listener);
        btnSave.setOnLongClickListener(listener);
        rbRobot.setOnLongClickListener(listener);
        rbWayPoint.setOnLongClickListener(listener);
        rbCellUnknown.setOnLongClickListener(listener);
        rbCellExplored.setOnLongClickListener(listener);
    }

    public void returnToStart(){
        hsv.scrollTo(0,0);
        rbRobot.setChecked(true);
    }
}
