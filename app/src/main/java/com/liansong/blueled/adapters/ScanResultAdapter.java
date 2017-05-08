package com.liansong.blueled.adapters;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseApplication;
import com.liansong.blueled.bases.BlueToothActivity;
import com.liansong.blueled.beans.BlueToothBean;
import com.liansong.blueled.utils.LogUtil;
import com.liansong.blueled.viewHolders.ScanResultViewHolder;

import java.util.LinkedHashSet;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {

    private LinkedHashSet<BlueToothBean> list;
    private BlueToothActivity mContext;
    private BluetoothGattCallback mBluetoothGattCallback;

    public ScanResultAdapter(LinkedHashSet<BlueToothBean> list, BluetoothGattCallback bluetoothGattCallback,BlueToothActivity context) {
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
    public void onBindViewHolder(final ScanResultViewHolder holder, int position) {
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
                mContext.closeGatt();
                BluetoothGatt bluetoothGatt = bean.getDevice().connectGatt(mContext, false, mBluetoothGattCallback);
                if(bluetoothGatt!=null){
                    holder.getTv_devStatus().setText("Device Status: Connecting...");
                    final AlphaAnimation alphaAnimation=new AlphaAnimation(0.3f,1.f);
                    alphaAnimation.setDuration(800);
                    alphaAnimation.setRepeatMode(Animation.REVERSE);
                    alphaAnimation.setRepeatCount(Animation.INFINITE);
                    holder.getTv_devStatus().startAnimation(alphaAnimation);
                    BaseApplication.postDelay(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                alphaAnimation.cancel();
                                holder.getTv_devStatus().setAnimation(null);
                                holder.getTv_devStatus().setText("Device Status: Connect failed, please retry again.");
                            }catch (Exception e){
                                showLog(e.toString());
                            }
                        }
                    },10000);
                }else {
                    Toast.makeText(mContext,"BluetoothGatt Open fails.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showLog(String msg){
        LogUtil.showLog(ScanResultAdapter.class.getName(),msg);
    }


}
