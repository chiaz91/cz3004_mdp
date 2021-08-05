package ntu.cz3004.controller.view.holder;

import android.view.View;

public abstract class MDPViewHolder {
    public final View view;

    public MDPViewHolder(View view ){
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void setVisible(boolean visible){
        this.view.setVisibility(visible?View.VISIBLE:View.GONE);
    }
}
