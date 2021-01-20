package ntu.cz3004.controller.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.listener.OnRecyclerViewInteractedListener;

public class BTDeviceAdapter extends RecyclerView.Adapter {
    private static int VIEW_TYPE_EMPTY = 0;
    private static int VIEW_TYPE_DEVICE = 1;
    private ArrayList<BluetoothDevice> devices;
    private OnRecyclerViewInteractedListener listener;

    public BTDeviceAdapter(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
    }
    public void setDevices(ArrayList<BluetoothDevice> devices){
        this.devices = devices;
        notifyDataSetChanged();
    }
    public void setOnRecyclerViewInteractListener(OnRecyclerViewInteractedListener listener){
        this.listener = listener;
    }

    public void add(BluetoothDevice device){
        if (!devices.contains(device)){
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if (devices.size() == 0){
            return 1;
        }
        return devices.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (devices.size() == 0){
            return VIEW_TYPE_EMPTY;
        } else {
            return VIEW_TYPE_DEVICE;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_EMPTY){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_no_device, parent, false);
            return new EmptyViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bt_device, parent, false);
            return new DeviceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_DEVICE){
            ((DeviceViewHolder)holder).updateAs(devices.get(position));
        }

    }


    public class EmptyViewHolder extends RecyclerView.ViewHolder{
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView tvTextMain;
        TextView tvTextSub;
        DeviceViewHolder(View view) {
            super(view);
            this.view = view;
            tvTextMain = view.findViewById(R.id.tv_text_main);
            tvTextSub = view.findViewById(R.id.tv_text_sub);
            view.setOnClickListener(this);
        }

        void updateAs(BluetoothDevice device){
            view.setTag(device);
            tvTextMain.setText(device.getAddress());
            tvTextSub.setText(device.getName());
            tvTextSub.setVisibility(device.getName()!=null ? View.VISIBLE:View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (listener != null){
                listener.onViewInteracted(view, this, 1);
            }
        }
    }
}
