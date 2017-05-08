package com.liansong.blueled.activities;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BlueToothActivity;
import com.liansong.blueled.customized.LedStatusViewer;
import com.liansong.blueled.customized.LedView;
import com.liansong.blueled.utils.AlertDialogueUtils;
import com.liansong.blueled.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import csh.tiro.cc.aes;

public class MainActivity extends BlueToothActivity {
    private ListView lstv_record;
    private TextView tv_timing;
    private LedStatusViewer mLedStatusViewer;
    private ArrayAdapter<String> mAdapter;
    private LedView mLed1;
    private LedView mLed2;
    private ArrayList<String> arr_records =new ArrayList<>();

    private static final int DATA_LEN=16;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharWrite;
    private BluetoothGattCharacteristic mCharNotify;
    private byte[] mDataSend = {'T','R','1','7','0','3','R','0','2',0,0,0,0,0,0,0}; //用来存放发送到蓝牙设备的信息
    private byte[] mDataReceived = new byte[DATA_LEN];//用来存放蓝牙设备发送回来的信息
    private boolean isTiming;
    private Random mRandom=new Random();
    private boolean isCharWriteFound;
    private boolean isCharNotifyFound;


    public static void startActivity(Activity context){
        context.startActivity(new Intent(context,MainActivity.class));
    }


    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        lstv_record= (ListView) findViewById(R.id.main_lstv_control_record);
        tv_timing= (TextView) findViewById(R.id.main_tv_timing);
        mLedStatusViewer= (LedStatusViewer) findViewById(R.id.main_cstm_led_viewer);
        mLed1=mLedStatusViewer.getLed1();
        mLed2=mLedStatusViewer.getLed2();
    }

    @Override
    protected void initData() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            try {
                actionBar.setSubtitle("Ver "+getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        mAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arr_records){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view= (TextView) super.getView(position, convertView, parent);
                view.setGravity(Gravity.CENTER);
                return view;
            }
        };
        lstv_record.setAdapter(mAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.main_menu_scan_dev:
                scanLeDevices();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void initListener() {
        mLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLedStatusViewer.isConnected()){
                    litLed(0,!mLed1.isLit());
                }else {
                    ToastUtil.showToast("感应灯未链接，请先尝试链接感应灯。");
                }
            }
        });

        mLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLedStatusViewer.isConnected()){
                    litLed(1,!mLed2.isLit());
                }else {
                    ToastUtil.showToast("感应灯未链接，请先尝试链接感应灯。");
                }
            }
        });

    }

    private void litLed(int ledIndex, boolean isToLit) {
        int index = 15;
        while (index != 10) {
            mDataSend[index] = getRandomDigit();
            index--;
        }
        int value=isToLit?1:0;
        switch (ledIndex) {
            case 0:
                mDataSend[9]= (byte) (value&0xff);
                break;
            case 1:
                mDataSend[10]= (byte) (value&0xff);
                break;
            default:
                break;
        }
        byte[] out=new byte[DATA_LEN];
        aes.cipher(mDataSend,out);
       if(mCharWrite != null && mCharWrite.setValue(out)){
           showLog("mCharWrite.setValue(out)-----success");
           if(getBluetoothGatt().writeCharacteristic(mCharWrite)){
               showLog("getBluetoothGatt().writeCharacteristic-----success");
           }else {
               showLog("getBluetoothGatt().writeCharacteristic-----fail");
           }
       }else {
           showLog("mCharWrite.setValue(out)-----fail");
       }

    }


    @NonNull
    @Override
    protected BluetoothGattCallback initBlueToothGattCallBack() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(newState== BluetoothProfile.STATE_CONNECTED){
                    AlertDialogueUtils.dismissDialog();
                    showLog("Gatt connected.");
                    showLog("begin to discover gatt service...");
                    if(gatt.discoverServices()){
                        showLog("discovering......");
                    }else {
                        showLog("discovering service fail to init.");
                    }
                }else {
                    showLog("Gatt disconnected.");
                    closeGatt();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                boolean isSuccess=false;
                if(status==BluetoothGatt.GATT_SUCCESS){
                    showLog("onServicesDiscovered:");
                    mGattService = gatt.getService(UUID.fromString(SERVICE_UUID));
                    if(mGattService!=null){
                        showLog("target service found");
                        List<BluetoothGattCharacteristic> characteristics = mGattService.getCharacteristics();
                        showLog("iterating characteristics:");
                        mNotifyChars.clear();
                        for(BluetoothGattCharacteristic characteristic:characteristics){
                            showLog("char uuid ="+characteristic.getUuid().toString());
                            if(characteristic.getUuid().toString().equals(CHARACTER_WRITE_UUID)){
                                mCharWrite=characteristic;
                                isCharWriteFound=true;
                            }else if(characteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                                mNotifyChars.add(characteristic);
                            }
                        }
                        if(isCharWriteFound&&mNotifyChars.size()>0){
                            if(setNextNotifyChar(gatt)){
                                showLog("write char is found and the first notify char is set.");
                                isSuccess=true;
                            }
                        }
                    }else {
                        showLog("target service not found");
                    }
                }
                if(!isSuccess){
                    showLog("Fail to connect sensor led. Close gatt.");
                    closeGatt();
                }
            }


            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                showLog("onDescriptorWrite");
                super.onDescriptorWrite(gatt, descriptor, status);
                setNextNotifyChar(gatt);
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                showLog("onCharacteristicChanged:");
                showLog("UUID="+characteristic.getUuid().toString());
                byte[] data = characteristic.getValue();
                if(data==null){
                    showLog("data=null");
                }else {
                    int len=data.length;
                    showLog("data length="+len);
                    StringBuffer sb=new StringBuffer();
                    for(byte b:data){
                        String hexString = Integer.toHexString(b&0xff);
                        sb.append("0x");
                        if(hexString.length()<2){
                            sb.append('0');
                        }
                        sb.append(hexString).append(" ");
                    }
                    showLog(sb.toString().trim());
                    if(len==DATA_LEN){
                        showLog("data len="+DATA_LEN+",invCipher data...");
                        aes.invCipher(data, mDataReceived);
                        showLog("result:");
                        sb=new StringBuffer();
                        for(byte b:mDataReceived){
                            String hexString = Integer.toHexString(b&0xff);
                            sb.append("0x");
                            if(hexString.length()<2){
                                sb.append('0');
                            }
                            sb.append(hexString).append(" ");
                        }
                        showLog(sb.toString().trim());
                        updateLedViews();
                    }
                }

            }
        };
    }

    protected boolean setNextNotifyChar(BluetoothGatt gatt){
        boolean isSuccess=false;
        int size = mNotifyChars.size();
        if(size ==0){
            isSuccess=true;
            mLedStatusViewer.setConnected(true);
            showLog("All the notify char have been set.");
        }else {
            showLog("the quantity of remaining notify chars to set:"+ size);
            BluetoothGattCharacteristic notifyChar = mNotifyChars.get(0);
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
        return isSuccess;
    }

    private void updateLedViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mDataReceived[0] == 'T' && mDataReceived[1] == 'R' && mDataReceived[2] == '1' && mDataReceived[3] == '7' &&
                   mDataReceived[4] == '0' && mDataReceived[5] == '3' && mDataReceived[6] == 'R' && mDataReceived[7] == '0' &&
                   mDataReceived[8] == '2'){
                    showLog("data format fits protocol.");
                    boolean isToLitLed1=false;
                    boolean isToLitLed2=false;
                    switch (mDataReceived[9]){
                        case 0:
                            isToLitLed1=false;
                            showLog("Led 1 is about to off.");
                            break;
                        case 1:
                            isToLitLed1=true;
                            showLog("Led 1 is about to lit.");
                            if(!mLed1.isLit()&&!mLed2.isLit()){
                                showLog("目前led 1 和 led 2 都还没亮，此时收到了打开led 1的命令，符合开始计时条件，开始计时");
                                startTiming();
                            }
                            break;
                        default:
                            break;
                    }
                    switch (mDataReceived[10]){
                        case 0:
                            isToLitLed2=false;
                            showLog("Led 2 is about to off.");
                            break;
                        case 1:
                            isToLitLed2=true;
                            showLog("Led 2 is about to lit.");
                            if(mLed1.isLit()&&!mLed2.isLit()){
                                showLog("目前led 1已经亮了，但led 2还没亮，此时收到了打开led 2的命令，停止计时。");
                                stopTiming();
                            }
                            break;
                        default:
                            break;
                    }
                    mLedStatusViewer.toggleLeds(isToLitLed1,isToLitLed2);
                }else {
                    showLog("data format do not fit protocol.");
                }
            }
        });

    }

    private synchronized void stopTiming() {
        isTiming=false;
    }

    private synchronized void startTiming() {
        if(!isTiming){
            updateConsole("符合计时条件，开始计时...");
            new AsyncTask<Void,Long,Void>(){
                @Override
                protected void onPreExecute() {
                    isTiming=true;
                    tv_timing.setText("00:00.00");
                }

                @Override
                protected Void doInBackground(Void... params) {
                    long timeMilli=0;
                    while (isTiming){
                        SystemClock.sleep(10);
                        timeMilli+=10;
                        publishProgress(timeMilli);
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Long... values) {
                    tv_timing.setText(convertLongMillisToSeconds(values[0]));
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    updateConsole("计时结束，间隔时间："+tv_timing.getText().toString());
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void updateConsole(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                arr_records.add(msg);
                mAdapter.notifyDataSetChanged();
                lstv_record.smoothScrollToPosition(arr_records.size()-1);
            }
        });
    }

    @Override
    protected void showLog(String msg) {
        super.showLog(msg);
        updateConsole(msg);
    }

    @Override
    protected void showToast(String msg) {
        super.showToast(msg);
        updateConsole(msg);
    }

    public byte getRandomDigit() {
        return (byte) mRandom.nextInt(128);
    }

    @Override
    public void closeGatt() {
        super.closeGatt();
        mLedStatusViewer.setConnected(false);
        isTiming=false;
        isCharNotifyFound=false;
        isCharWriteFound=false;
    }
}
