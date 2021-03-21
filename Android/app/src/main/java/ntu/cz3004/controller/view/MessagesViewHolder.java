package ntu.cz3004.controller.view;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.MDPMessageAdapter;

public class MessagesViewHolder extends MDPViewHolder{
    public RecyclerView rvMessages;
    public EditText etMessage;
    public ImageButton btnSend;

    public MessagesViewHolder(View view, MDPMessageAdapter adapter){
        super(view);
        rvMessages = view.findViewById(R.id.rv_messages);
        etMessage = view.findViewById(R.id.et_msg);
        btnSend = view.findViewById(R.id.btn_send);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rvMessages.setLayoutManager(linearLayoutManager);
        rvMessages.setAdapter(adapter);
    }

    public void scrollToEnd(){
        RecyclerView.Adapter adapter = rvMessages.getAdapter();
        rvMessages.scrollToPosition(adapter.getItemCount() -1);
    }

    public void setOnSendClickListener(View.OnClickListener listener){
        btnSend.setOnClickListener(listener);
    }

    public void setOnLongClickSendClickListener(View.OnLongClickListener listener){
        btnSend.setOnLongClickListener(listener);
    }
}
