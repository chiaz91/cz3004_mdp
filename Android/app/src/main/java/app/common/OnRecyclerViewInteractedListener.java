package app.common;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


public interface OnRecyclerViewInteractedListener<T extends RecyclerView.ViewHolder> {
    void onViewInteracted(View view, T holder, int action);
}
