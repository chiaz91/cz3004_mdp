package ntu.cz3004.controller.view;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ntu.cz3004.controller.R;
import ntu.cz3004.controller.adapter.BTMessageAdapter;

public class BTChatViewHolder extends MDPViewHolder{
    public RecyclerView rvChatHistory;
    public EditText etMessage;
    public ImageButton btnSend;

    public BTChatViewHolder(View view, BTMessageAdapter adapter){
        super(view);
        rvChatHistory = view.findViewById(R.id.rv_messages);
        etMessage = view.findViewById(R.id.et_msg);
        btnSend = view.findViewById(R.id.btn_send);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        rvChatHistory.setLayoutManager(linearLayoutManager);
        rvChatHistory.setAdapter(adapter);
    }

    public void scrollToEnd(){
        RecyclerView.Adapter adapter = rvChatHistory.getAdapter();
        rvChatHistory.scrollToPosition(adapter.getItemCount() -1);
    }

    public void setOnSendClickListener(View.OnClickListener listener){
        btnSend.setOnClickListener(listener);
    }
}
