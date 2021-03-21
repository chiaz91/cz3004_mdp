package ntu.cz3004.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ntu.cz3004.controller.R;
import app.entity.BTMessage;


public class BTMessageAdapter extends RecyclerView.Adapter<BTMessageAdapter.MessageViewHolder> {
    private static final String TAG = "mdp.adapter.bt_msg";
    private ArrayList<BTMessage> messages, messagesFiltered;
//    private OnRecyclerViewInteractedListener listener;
    private final int VIEW_TYPE_IN  = 1;
    private final int VIEW_TYPE_OUT = 2;
    private final int VIEW_TYPE_SYS = 3;
    private boolean showReceived = true;
    private boolean showSent = true;
    private boolean showSystem = true;

    public BTMessageAdapter(ArrayList<BTMessage> messages) {
        this.messagesFiltered = new ArrayList<>();
        this.messages = messages;
        doFiltering();
    }

//    public void setOnRecyclerViewInteractListener(OnRecyclerViewInteractedListener listener){
//        this.listener = listener;
//    }
    public boolean isShowSystem() {
        return showSystem;
    }

    public void setShowSystem(boolean showSystem) {
        this.showSystem = showSystem;
    }

    public boolean isShowReceived() {
        return showReceived;
    }

    public void setShowReceived(boolean showReceived) {
        this.showReceived = showReceived;
        doFiltering();
    }

    public boolean isShowSent() {
        return showSent;
    }

    public void setShowSent(boolean showSent) {
        this.showSent = showSent;
        doFiltering();
    }

    @Override
    public int getItemCount() {
        return messagesFiltered.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (messagesFiltered.get(position).getType()){
            case INCOMING:
                return VIEW_TYPE_IN;
            case OUTGOING:
                return VIEW_TYPE_OUT;
            default:
                return VIEW_TYPE_SYS;
        }
    }

    @Override
    @NonNull
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SYS){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_system, parent, false);
            return new MessageViewHolder(view);
        } else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_simple, parent, false);
            return new UserMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.updateAs(messagesFiltered.get(position));
    }

    public void add(BTMessage message){
        if (messages.size()>0){
            BTMessage lastMsg = messages.get(messages.size()-1);
            if (message.equals(lastMsg)){
                lastMsg.increaseCount();
                notifyDataSetChanged();
                return;
            }
        }

        messages.add(message);
        addToFiltered(message);
        notifyDataSetChanged();
    }

    private void addToFiltered(BTMessage message){
        switch (message.getType()){
            case SYSTEM:
                if (showSystem){ messagesFiltered.add(message); }
                break;
            case INCOMING:
                if (showReceived){ messagesFiltered.add(message); }
                break;
            case OUTGOING:
                if (showSent){ messagesFiltered.add(message); }
                break;
        }
    }

    public void clear(){
        this.messages.clear();
        this.messagesFiltered.clear();
        notifyDataSetChanged();
    }

    public void doFiltering(){
        this.messagesFiltered.clear();
        for (BTMessage msg:messages) {
            addToFiltered(msg);
        }
        notifyDataSetChanged();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvContent;
        MessageViewHolder(View view) {
            super(view);
            this.view = view;
            tvContent = view.findViewById(R.id.tv_message);
        }

        void updateAs(BTMessage message){
            tvContent.setText(message.getContent());
        }
    }



    public class UserMessageViewHolder extends MessageViewHolder implements View.OnClickListener {
        TextView tvSender, tvCount;
        UserMessageViewHolder(View view) {
            super(view);
            tvSender = view.findViewById(R.id.tv_sender);
            tvCount = view.findViewById(R.id.tv_count);
//            view.setOnClickListener(this);
        }

        void updateAs(BTMessage message){
            super.updateAs(message);
            tvSender.setText(message.getSender());
            tvCount.setText(String.format("x %d", message.getCount()));
            tvCount.setVisibility(message.getCount()>1?View.VISIBLE:View.INVISIBLE);

            // TEST LAYOUT
            if (message.getType()== BTMessage.Type.INCOMING){
                tvSender.setTextColor(ContextCompat.getColor(tvSender.getContext(), R.color.secondaryColor));
            } else {
                tvSender.setTextColor(ContextCompat.getColor(tvSender.getContext(), R.color.primaryColor));
            }
        }

        @Override
        public void onClick(View v) {
            // TODO allow click or long click to update map as selected message(PENDING)
//            if (listener != null){
//                listener.onViewInteracted(view, this, 1);
//            }

        }
    }

}
