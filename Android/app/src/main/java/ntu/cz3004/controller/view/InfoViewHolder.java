package ntu.cz3004.controller.view;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.entity.Map;

public class InfoViewHolder extends MDPViewHolder{
    public CheckBox cbMapEdit;
    public TextView tvStatusMain, tvStatusSub, tvRobotPos, tvRobotDir, tvWayPtPos, tvMapP1, tvMapP2, tvMapImages;

    public InfoViewHolder(View view){
        super(view);
        cbMapEdit = view.findViewById(R.id.cb_edit_map);
        tvStatusMain = view.findViewById(R.id.tv_status_title);
        tvStatusSub = view.findViewById(R.id.tv_status_subtitle);
        tvRobotPos = view.findViewById(R.id.tv_status_robot_pos);
        tvRobotDir = view.findViewById(R.id.tv_status_robot_dir);
        tvWayPtPos = view.findViewById(R.id.tv_status_way_pt_pos);
        tvMapP1 = view.findViewById(R.id.tv_status_p1);
        tvMapP2 = view.findViewById(R.id.tv_status_p2);
        tvMapImages = view.findViewById(R.id.tv_status_images);
    }

    public void updateAs(Map map){
        tvRobotPos.setText(String.format("(%d, %d)", map.getRobot().getX(), map.getRobot().getY()));
        tvRobotDir.setText(map.getRobot().getDirection()+"");
        String temp= "N/A";
        if (map.getWayPoint()!=null){
            temp = String.format("(%d, %d)", map.getWayPoint().getX(), map.getWayPoint().getY());
        }
        tvWayPtPos.setText(temp);
        tvMapP1.setText(map.getPartI());
        temp = map.getPartII();
        if (temp.length()==0){
            temp = "(empty)";
        }
        tvMapP2.setText(temp);
        temp = map.getImagesString();
        if (temp.length()==0){
            temp = "(empty)";
        }
        tvMapImages.setText(temp);
    }

}
