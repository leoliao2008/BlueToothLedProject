package com.liansong.blueled.bases;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.liansong.blueled.utils.AlertDialogueUtils;

import java.util.UUID;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public abstract class BlueToothActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT =911;
    private static final int REQUEST_PERMISSIONS = 912;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean isBlueToothReady;
    private UUID[] mUUIDs=new UUID[]{UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")};
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        requestPermissions();
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isGranted=true;
            for(String pm:PERMISSIONS){
                if(checkSelfPermission(pm)==PackageManager.PERMISSION_DENIED){
                    isGranted=false;
                    break;
                }
            }
            if(!isGranted){
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            }
        }
    }

    protected void scanLeDevices(){
        if(initBlueTooth()){
            startScanLeDevices();
        }
    }

    private void startScanLeDevices() {
        AlertDialogueUtils.showScanDevView(this,mBluetoothAdapter,initBlueToothGattCallBack());
    }


    protected boolean initBlueTooth(){
        isBlueToothReady=false;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BlueTooth Le not supported.", Toast.LENGTH_SHORT).show();
            return false;
        }

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        if(mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            isBlueToothReady=true;
        }
        return isBlueToothReady;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                isBlueToothReady=true;
                startScanLeDevices();
            }else {
                isBlueToothReady=false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract BluetoothGattCallback initBlueToothGattCallBack();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== REQUEST_PERMISSIONS){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        alertDialogRequestPermission();
                        break;
                    }else {
                        AlertDialogueUtils.showHint(
                                this,
                                "您已经禁用了本程序所需的系统权限，请到应用管理中设置相关系统权限。",
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }
                        );
                    }
                }
            }
        }
    }

    private void alertDialogRequestPermission() {
        StringBuffer msg=new StringBuffer();
        msg.append("为了保证本程序的正常运行，需要批准获得以下权限：\r\n");
        for(String pm:PERMISSIONS){
            msg.append(pm).append("\r\n");
        }
        msg.append("请按确认重新申请，按取消退出程序。");

        AlertDialogueUtils.showHint(
                BlueToothActivity.this,
                msg.toString(),
                new Runnable() {
                    @Override
                    public void run() {
                        requestPermissions();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });

    }
}
