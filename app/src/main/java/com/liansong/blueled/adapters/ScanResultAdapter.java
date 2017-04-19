package com.liansong.blueled.adapters;

import android.app.Activity;
import android.bluetooth.BluetoothGattCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liansong.blueled.R;
import com.liansong.blueled.beans.BlueToothBean;
import com.liansong.blueled.viewHolders.ScanResultViewHolder;

import java.util.LinkedHashSet;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {

    private LinkedHashSet<BlueToothBean> list;
    private Activity mContext;
    private BluetoothGattCallback mBluetoothGattCallback;

    public ScanResultAdapter(LinkedHashSet<BlueToothBean> list, BluetoothGattCallback bluetoothGattCallback,Activity context) {
        this.list = list;
        mContext = context;
        mBluetoothGattCallback=bluetoothGattCallback;
    }

    @Override
    public ScanResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView=LayoutInflater.from(mContext).inflate(R.layout.item_device,parent,false);
        return new ScanResultViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(ScanResultViewHolder holder, int position) {
        final BlueToothBean bean = (BlueToothBean) list.toArray()[position];
        holder.getTv_devName().setText("Device Name:  "+bean.getDevice().getName());
        holder.getTv_devAddress().setText("Device Address:  "+bean.getDevice().getAddress());
        holder.getTv_devRSSI().setText("Device RSSI:  "+bean.getRSSI());
        String temp;
        switch (bean.getDevice().getType()){
            case 0:
                temp="DEVICE_TYPE_UNKNOWN";
                break;
            case 1:
                temp="DEVICE_TYPE_CLASSIC";
                break;
            case 2:
                temp="DEVICE_TYPE_LE";
                break;
            case 3:
                temp="DEVICE_TYPE_DUAL";
                break;
            default:
                temp="Error";
                break;
        }
        holder.getTv_devType().setText("Device Type:  "+temp);
        holder.getTv_devStatus().setText(bean.isConnect()?"Device Status:  Connected.":"Device Status:  Disconnected");
        holder.getBtn_connect().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bean.getDevice().connectGatt(mContext,true,mBluetoothGattCallback);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
