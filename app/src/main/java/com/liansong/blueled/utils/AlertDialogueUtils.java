package com.liansong.blueled.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.liansong.blueled.R;
import com.liansong.blueled.adapters.ScanResultAdapter;
import com.liansong.blueled.bases.BaseApplication;
import com.liansong.blueled.bases.BlueToothActivity;
import com.liansong.blueled.beans.BlueToothBean;
import com.liansong.blueled.data.Constants;

import java.util.LinkedHashSet;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class AlertDialogueUtils {
    private static AlertDialog alertDialog;
    private static LinkedHashSet<BlueToothBean> blueToothDeviceList =new LinkedHashSet<>();
    private static ScanResultAdapter scanResultAdapter;
    private static AlphaAnimation loadingAnimation;
    private static boolean isScanning;
    private static BluetoothAdapter.LeScanCallback leScanCallback;

    public static void showHint(Context context,String hint){
        showHint(context,hint,null);
    }
    public static void showHint(Context context,String hint ,Runnable positive){
        showHint(context,hint,positive,null);
    }
    public static void showHint(Context context, String hint, final Runnable positive, final Runnable negative){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("温馨提示")
                .setMessage(hint)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(positive!=null){
                            positive.run();
                        }
                        alertDialog.dismiss();
                    }
                });
        if(negative!=null){
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    negative.run();
                    alertDialog.dismiss();
                }
            });
        }
        alertDialog=builder.create();
        alertDialog.show();
    }

    public static void showScanDevView(final BlueToothActivity context, final BluetoothAdapter bluetoothAdapter, final BluetoothGattCallback callback){
        //init views
        final View rootView= LayoutInflater.from(context).inflate(R.layout.scan_result_view_layout,null,false);
        RecyclerView recyclerView= (RecyclerView) rootView.findViewById(R.id.scan_result_recycler_view);
        Button btn_exit= (Button) rootView.findViewById(R.id.scan_result_exit);
        Button btn_retry= (Button) rootView.findViewById(R.id.scan_result_retry);
        final TextView tv_scanning= (TextView) rootView.findViewById(R.id.scan_result_scanning);
        Button btn_stop= (Button) rootView.findViewById(R.id.scan_result_stop_scan);
        //init data
        scanResultAdapter = new ScanResultAdapter(blueToothDeviceList,callback,context);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(scanResultAdapter);
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(!blueToothDeviceList.contains(device)){
                    updateScanView(new BlueToothBean(device,rssi));
                    if(device.getAddress().equals(Constants.MAC_ADDRESS)){
                        stopScanDevice(tv_scanning,bluetoothAdapter,leScanCallback);
                        ToastUtil.showToast("扫描到目标设备！");
                    }
                }

            }
        };
        //init listeners
        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanDevice(context,tv_scanning,bluetoothAdapter, leScanCallback);
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanDevice(tv_scanning,bluetoothAdapter,leScanCallback);
            }
        });

        //attach view to dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        alertDialog=builder.setCancelable(false).create();
        alertDialog.show();
        alertDialog.setContentView(rootView);
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);

        //on dialog resume
        startScanDevice(context,tv_scanning,bluetoothAdapter, leScanCallback);
    }

    private static void startAnimation(Context context,TextView tv_scanning) {
        tv_scanning.setVisibility(View.VISIBLE);
        loadingAnimation =new AlphaAnimation(0.3f,1.f);
        loadingAnimation.setRepeatCount(Animation.INFINITE);
        loadingAnimation.setRepeatMode(Animation.REVERSE);
        loadingAnimation.setDuration(800);
        loadingAnimation.setInterpolator(context,android.R.interpolator.linear);
        tv_scanning.startAnimation(loadingAnimation);
    }

    private static void stopAnimation(final TextView tv_scanning) {
        if(loadingAnimation !=null){
            loadingAnimation.cancel();
        }
        tv_scanning.setAnimation(null);
        tv_scanning.setVisibility(View.INVISIBLE);
    }

    private static void startScanDevice(Context context,
                                        final TextView tv_scanning,
                                        final BluetoothAdapter bluetoothAdapter,
                                        final BluetoothAdapter.LeScanCallback leScanCallback) {

        if(!isScanning){
            isScanning =true;
            for(BlueToothBean bean:blueToothDeviceList){
                if(bean.getDevice().getAddress().equals(Constants.MAC_ADDRESS)){
                    ToastUtil.showToast("设备已经在清单上，退出当前扫描。");
                    stopScanDevice(tv_scanning,bluetoothAdapter,leScanCallback);
                    return;
                }
            }
            startAnimation(context,tv_scanning);
            bluetoothAdapter.startLeScan(leScanCallback);
            BaseApplication.postDelay(new Runnable() {
                @Override
                public void run() {
                    stopScanDevice(tv_scanning,bluetoothAdapter,leScanCallback);
                }
            },10000);
        }else {
            ToastUtil.showToast("当前正在扫描蓝牙设备，不需要重复申请扫描...");
        }
    }

    private static void stopScanDevice(TextView tv_scanning,
                                       BluetoothAdapter bluetoothAdapter,
                                       BluetoothAdapter.LeScanCallback leScanCallback) {
        bluetoothAdapter.stopLeScan(leScanCallback);
        stopAnimation(tv_scanning);
        isScanning=false;
    }

    private static void updateScanView(BlueToothBean bean){
        blueToothDeviceList.add(bean);
        if(scanResultAdapter!=null){
            scanResultAdapter.notifyDataSetChanged();
        }
    }

    public static void dismissDialog(){
        if(alertDialog!=null&&alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }



}
