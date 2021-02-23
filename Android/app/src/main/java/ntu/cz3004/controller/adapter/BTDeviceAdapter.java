package ntu.cz3004.controller.adapter;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import ntu.cz3004.controller.R;
import ntu.cz3004.controller.listener.OnRecyclerViewInteractedListener;

public class BTDeviceAdapter extends RecyclerView.Adapter {
    private static int VIEW_TYPE_INFO = 0;
    private static int VIEW_TYPE_DEVICE = 1;
    private ArrayList<BluetoothDevice> devicesPaired;
    private ArrayList<BluetoothDevice> devicesNearBy;
    private OnRecyclerViewInteractedListener listener;

    public BTDeviceAdapter() {
        this.devicesPaired = new ArrayList<>();
        this.devicesNearBy = new ArrayList<>();
    }
    public void setDevicesPaired(Collection<BluetoothDevice> devicesPaired){
        this.devicesPaired.clear();
        this.devicesPaired.addAll(devicesPaired);
        notifyDataSetChanged();
    }

    public int getPairedCount(){
        return this.devicesPaired.size();
    }

    public int getNearbyCount(){
        return this.devicesNearBy.size();
    }

    public void setOnRecyclerViewInteractListener(OnRecyclerViewInteractedListener listener){
        this.listener = listener;
    }

    public void addPaired(BluetoothDevice device){
        if (!devicesPaired.contains(device)){
            devicesPaired.add(device);
            notifyDataSetChanged();
        }
    }

    public void addNearby(BluetoothDevice device){
        if (!devicesNearBy.contains(device)){
            devicesNearBy.add(device);
            notifyDataSetChanged();
        }
    }

    public void clearPaired(){
        devicesPaired.clear();
        notifyDataSetChanged();
    }

    public void clearNearby(){
        devicesNearBy.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return devicesPaired.size()+devicesNearBy.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return VIEW_TYPE_INFO;
        } else {
            return VIEW_TYPE_DEVICE;
        }
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_INFO){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_info, parent, false);
            return new InfoViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_bt_device, parent, false);
            return new DeviceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_DEVICE){
            if (position>devicesPaired.size()){
                ((DeviceViewHolder)holder).updateAs(devicesNearBy.get(position-devicesPaired.size()-1));
            } else {
                ((DeviceViewHolder)holder).updateAs(devicesPaired.get(position-1));
            }
        }else {
            ((InfoViewHolder)holder).update();
        }
    }


    public class InfoViewHolder extends RecyclerView.ViewHolder{
        TextView tvTextMain;
        public InfoViewHolder(@NonNull View view) {
            super(view);
            tvTextMain = view.findViewById(R.id.tv_text_main);
        }

        public void update(){
            int paired = getPairedCount();
            int nearby = getNearbyCount();
            if (paired + nearby==0){
                tvTextMain.setText("No device found!");
            } else {
                String msg = "";
                if (paired>0){
                    msg+= "Paired ("+paired+")";
                }
                if (nearby>0){
                    if (msg.length()>0){
                        msg+=", ";
                    }
                    msg+= "Nearby ("+nearby+")";
                }
                tvTextMain.setText(msg);
            }
        }
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View view;
        TextView tvPos;
        TextView tvTextMain;
        TextView tvTextSub;
        DeviceViewHolder(View view) {
            super(view);
            this.view = view;
            tvPos = view.findViewById(R.id.tv_device_pos);
            tvTextMain = view.findViewById(R.id.tv_text_main);
            tvTextSub = view.findViewById(R.id.tv_text_sub);
            view.setOnClickListener(this);
        }

        void updateAs(BluetoothDevice device){
            view.setTag(device);
            tvPos.setText(""+(getAdapterPosition()));
            tvTextMain.setText(device.getAddress()+String.format(" %s", devicesPaired.contains(device)?"(paired)":""));
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
