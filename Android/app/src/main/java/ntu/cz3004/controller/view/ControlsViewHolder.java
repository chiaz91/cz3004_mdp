package ntu.cz3004.controller.view;

import android.view.View;
import android.widget.ImageButton;

import ntu.cz3004.controller.R;

public class ControlsViewHolder extends MDPViewHolder {
    public ImageButton btnUp, btnDown, btnLeft, btnRight, btnFastest, btnExplore, btnImgRecognition, btnF1, btnF2,  btnF3, btnGetMap, btnSendMap;

    public ControlsViewHolder(View view){
        super(view);
        btnUp = view.findViewById(R.id.btn_ctrl_up);
        btnDown = view.findViewById(R.id.btn_ctrl_down);
        btnLeft = view.findViewById(R.id.btn_ctrl_left);
        btnRight = view.findViewById(R.id.btn_ctrl_right);
        btnFastest = view.findViewById(R.id.btn_ctrl_fastest);
        btnExplore = view.findViewById(R.id.btn_ctrl_explore);
        btnImgRecognition = view.findViewById(R.id.btn_ctrl_img_search);
        btnF1 = view.findViewById(R.id.btn_ctrl_f1);
        btnF2 = view.findViewById(R.id.btn_ctrl_f2);
        btnF3 = view.findViewById(R.id.btn_ctrl_f3);
        btnGetMap = view.findViewById(R.id.btn_ctrl_get_map);
        btnSendMap = view.findViewById(R.id.btn_ctrl_send_map);
    }

    public void setOnClickListener(View.OnClickListener listener){
        btnUp.setOnClickListener(listener);
        btnDown.setOnClickListener(listener);
        btnLeft.setOnClickListener(listener);
        btnRight.setOnClickListener(listener);
        btnFastest.setOnClickListener(listener);
        btnExplore.setOnClickListener(listener);
        btnImgRecognition.setOnClickListener(listener);
        btnF1.setOnClickListener(listener);
        btnF2.setOnClickListener(listener);
        btnF3.setOnClickListener(listener);
        btnGetMap.setOnClickListener(listener);
        btnSendMap.setOnClickListener(listener);
    }
}
