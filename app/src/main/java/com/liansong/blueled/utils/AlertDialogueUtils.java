package com.liansong.blueled.utils;

import android.app.Activity;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.liansong.blueled.R;
import com.liansong.blueled.adapters.ScanResultAdapter;
import com.liansong.blueled.bases.BaseApplication;
import com.liansong.blueled.beans.BlueToothBean;

import java.util.LinkedHashSet;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class AlertDialogueUtils {
    private static AlertDialog alertDialog;
    private static LinkedHashSet<BlueToothBean> blueToothList =new LinkedHashSet<>();
    private static ScanResultAdapter scanResultAdapter;
    private static Animation loadAnimation;

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

    public static void showScanDevView(final Activity context, final BluetoothAdapter bluetoothAdapter, final BluetoothGattCallback callback){
        final View rootView= LayoutInflater.from(context).inflate(R.layout.scan_result_view_layout,null,false);
        RecyclerView recyclerView= (RecyclerView) rootView.findViewById(R.id.scan_result_recycler_view);
        Button btn_exit= (Button) rootView.findViewById(R.id.scan_result_exit);
        Button btn_retry= (Button) rootView.findViewById(R.id.scan_result_retry);
        final TextView tv_scanning= (TextView) rootView.findViewById(R.id.scan_result_scanning);
        blueToothList.clear();
        scanResultAdapter = new ScanResultAdapter(blueToothList,callback,context);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(scanResultAdapter);
        final BluetoothAdapter.LeScanCallback leScanCallback=new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                updateScanView(new BlueToothBean(device,rssi));
            }
        };
        startScanDevice(context,tv_scanning,bluetoothAdapter,leScanCallback);
        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanDevice(context,tv_scanning,bluetoothAdapter,leScanCallback);
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        alertDialog=builder.setCancelable(false).create();
        alertDialog.show();
        alertDialog.setContentView(rootView);
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
    }

    private static void startAnimation(Context context,TextView tv_scanning) {
        tv_scanning.setVisibility(View.VISIBLE);
        loadAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_twinkling_text);
        loadAnimation.setRepeatCount(Animation.INFINITE);
        loadAnimation.setRepeatMode(Animation.RESTART);
        loadAnimation.setDuration(800);
        loadAnimation.setInterpolator(context,android.R.interpolator.linear);
        tv_scanning.startAnimation(loadAnimation);
    }

    private static void stopAnimation(TextView tv_scanning) {
        tv_scanning.setVisibility(View.INVISIBLE);
        if(loadAnimation!=null){
            loadAnimation.cancel();
        }
    }

    private static void startScanDevice(Context context, final TextView tv_scanning, final BluetoothAdapter bluetoothAdapter, final BluetoothAdapter.LeScanCallback leScanCallback) {
        startAnimation(context,tv_scanning);
        bluetoothAdapter.startLeScan(leScanCallback);
        BaseApplication.postDelay(new Runnable() {
            @Override
            public void run() {
                bluetoothAdapter.stopLeScan(leScanCallback);
                stopAnimation(tv_scanning);
            }
        },10000);
    }

    private static void updateScanView(BlueToothBean bean){
        blueToothList.add(bean);
        if(scanResultAdapter!=null){
            scanResultAdapter.notifyDataSetChanged();
        }
    }


}
