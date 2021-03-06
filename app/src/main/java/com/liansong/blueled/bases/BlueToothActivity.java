package com.liansong.blueled.bases;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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

import java.util.ArrayList;
import java.util.UUID;

import csh.tiro.cc.aes;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public abstract class BlueToothActivity extends BaseActivity {

    private static final int REQUEST_ENABLE_BT =911;
    private static final int REQUEST_SYSTEM_PERMISSIONS = 912;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean isBlueToothReady;
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
    protected BluetoothGattCallback mBluetoothGattCallback;
    private BluetoothGatt mBluetoothGatt;
    protected ArrayList<BluetoothGattCharacteristic> mNotifyChars=new ArrayList<>();
    protected static final String SERVICE_UUID="0000fee9-0000-1000-8000-00805f9b34fb";
    protected static final String CHARACTER_WRITE_UUID ="d44bc439-abfd-45a2-b575-925416129600";
    protected static final String DESCRIPTOR_UUID ="00002902-0000-1000-8000-00805f9b34fb";
    /**
     * 蓝牙的数据的长度
     */
    protected static final int DATA_LEN=16;
    protected BluetoothGattService mGattService;
    protected BluetoothGattCharacteristic mCharWrite;
    protected BluetoothGattCharacteristic mCharNotify;
    /**
     * 用来存放蓝牙设备发送回来的信息
     */
    protected byte[] mDataReceived = new byte[DATA_LEN];
    /**
     * 用来存放未经加密的命令代码
     */
    protected byte[] mCommandData = {'T','R','1','7','0','3','R','0','2',0,0,0,0,0,0,0};
    /**
     * 用来存放加密的即将发送到蓝牙设备的命令代码
     */
    protected byte[] mDataSend=new byte[DATA_LEN];
    /**
     * 断线后重新链接蓝牙的执行单位。
     */
    protected Runnable mRunnableReconnect=new Runnable() {
        @Override
        public void run() {
            reconnectGatt();
        }
    };
    /**
     * 请求操纵led的执行单位。
     */
    protected Runnable mRunnableSendRequest =new Runnable() {
        @Override
        public void run() {
            sendCommandToLed();
        }
    };


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mBluetoothGattCallback=initBlueToothGattCallBack();
        aes.keyExpansionDefault();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions(){
        boolean isGranted=true;
        for(String pm:PERMISSIONS){
            if(checkSelfPermission(pm) == PackageManager.PERMISSION_DENIED){
                isGranted=false;
                break;
            }
        }
        if(!isGranted){
            requestPermissions(PERMISSIONS, REQUEST_SYSTEM_PERMISSIONS);
        }
    }

    protected void scanLeDevices(){
        if(initBlueTooth()){
            showScanView();
        }
    }

    private void showScanView() {
        closeGatt();
        if(mBluetoothAdapter!=null&&mBluetoothAdapter.isEnabled()&&mBluetoothGattCallback!=null){
            AlertDialogueUtils.showScanDevView(this, mBluetoothAdapter, mBluetoothGattCallback);
        }else {
            showToast("请确保蓝牙已经开启并重试。");
        }
    }


    protected boolean initBlueTooth(){
        isBlueToothReady=false;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BlueTooth Le not supported.", Toast.LENGTH_SHORT).show();
            return false;
        }
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if(mBluetoothManager!=null){
            mBluetoothAdapter=mBluetoothManager.getAdapter();
            if(mBluetoothAdapter!=null){
                if(mBluetoothAdapter.isEnabled()){
                    isBlueToothReady=true;
                }else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }else {
                showToast("mBluetoothAdapter == null");
            }
        }
        return isBlueToothReady;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==RESULT_OK){
                isBlueToothReady=true;
                showScanView();
            }else {
                isBlueToothReady=false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    protected abstract BluetoothGattCallback initBlueToothGattCallBack();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== REQUEST_SYSTEM_PERMISSIONS){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
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
                                    @RequiresApi(api = Build.VERSION_CODES.M)
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
                        break;
                    }else {
                        AlertDialogueUtils.showHint(
                                this,
                                "您已经禁用了本程序所需的系统权限，请先到应用管理中给此程序分配系统权限。",
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

    protected String toTimingFormat(Long millis) {
        int min= (int) (millis/1000/60);
        int sec= (int) (millis - min * 60*1000)/1000;
        int mil= (int) (millis-min*1000*60-sec*1000)/10;
        StringBuffer sb=new StringBuffer();
        if(min<10){
            sb.append('0');
        }
        sb.append(min).append(':');
        if(sec<10){
            sb.append('0');
        }
        sb.append(sec).append('.');
        if(mil<10){
            sb.append('0');
        }
        //01:45.02  1分45秒20毫秒
        sb.append(mil);
        return sb.toString();
    }

    /**
     * 蓝牙连接意外断开时自动重连。
     * @return 是否重连成功。
     */
    private boolean reconnectGatt() {
        showLog("reconnecting......");
        boolean isSuccess=false;
        if(mBluetoothGatt !=null){
            if(Build.BRAND.toLowerCase().equals("samsung")){
                isSuccess= mBluetoothGatt.connect();
                if(!isSuccess){
                    showLog("fail to reconnect, retry in 1s...");
                    BaseApplication.postDelay(mRunnableReconnect,1000);
                }else {
                    showLog("re-connect success.");
                }
            }else {
                BluetoothDevice bluetoothDevice = mBluetoothGatt.getDevice();
                if(bluetoothDevice!=null){
                    BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(this, false, mBluetoothGattCallback);
                    isSuccess=bluetoothGatt!=null;
                    if(!isSuccess){
                        showLog("fail to reconnect, retry in 3s...");
                        BaseApplication.postDelay(mRunnableReconnect,3000);
                    }else {
                        showLog("re-connect success.");
                    }
                }else {
                    showLog("bluetoothDevice == null, abort re-connect.");
                    BaseApplication.removeCallback(mRunnableReconnect);
                }
            }
        }else {
            showLog("gatt==null, the disconnect is not accidental, no need to re-connect.");
            BaseApplication.removeCallback(mRunnableReconnect);
        }
        if(isSuccess){
            BaseApplication.removeCallback(mRunnableReconnect);
        }
        return isSuccess;
    }

    public void closeGatt(){
        onGattClose();
        if(mBluetoothGatt!=null){
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt=null;
            BaseApplication.removeCallback(mRunnableReconnect);
        }
    }

    protected String displayBytesToHexString(byte[] data){
        StringBuffer sb=new StringBuffer();
        for(byte b:data){
            sb.append(String.format("%02X",b));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * 逐个地把所有符合notify属性的char都设置notification.
     * @param gatt
     * @return
     */
    protected boolean setNextNotifyChar(BluetoothGatt gatt){
        boolean isSuccess=false;
        int size = mNotifyChars.size();
        showLog("the quantity of remaining notify chars to set:"+ size);
        if(size>0){
            BluetoothGattCharacteristic notifyChar = mNotifyChars.get(0);
            if(notifyChar!=null){
                mNotifyChars.remove(0);
                if(gatt.setCharacteristicNotification(notifyChar,true)){
                    BluetoothGattDescriptor descriptor = notifyChar.getDescriptor(UUID.fromString(DESCRIPTOR_UUID));
                    if(descriptor!=null){
                        if(descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
                            if(gatt.writeDescriptor(descriptor)){
                                isSuccess=true;
                            }
                        }
                    }
                }
            }
            if(!isSuccess){
                closeGatt();
                showLog("fail to set all the notify char, gatt close.");
            }
        }else {
            isSuccess=true;
        }
        return isSuccess;
    }

    private boolean sendCommandToLed(){
        boolean isSuccess=false;
        int random = (int)(Math.random()*65535) ;
        mCommandData[11] = (byte)((random>>1) & 0xff) ;
        mCommandData[12] = (byte)((random>>2) & 0xff) ;
        mCommandData[13] = (byte)((random>>3) & 0xff) ;
        mCommandData[14] = (byte)((random>>4) & 0xff) ;
        mCommandData[15] = (byte)((random>>5) & 0xff) ;
        aes.cipher(mCommandData, mDataSend);
        if(mCharWrite != null && mCharWrite.setValue(mDataSend)){

            if(mBluetoothGatt.writeCharacteristic(mCharWrite)){
                isSuccess=true;
            }else {
                showLog("send data-----fail");
            }
        }else {
            showLog("mCharWrite.setValue(out)-----fail");
        }
        return isSuccess;
    }

    protected boolean setNotifyChar(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        boolean isSuccess=false;
        if(mCharNotify!=null){
            gatt.setCharacteristicNotification(mCharNotify,false);
            mCharNotify=null;
        }
        if(gatt.setCharacteristicNotification(characteristic,true)){
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(DESCRIPTOR_UUID));
            if(descriptor!=null){
                if(descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)){
                    isSuccess=gatt.writeDescriptor(descriptor);
                }
            }
        }
        if(isSuccess){
            mCharNotify=characteristic;
        }
        return isSuccess;
    }

    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        mBluetoothGatt = bluetoothGatt;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeGatt();
    }

    protected abstract void onGattClose();
}
