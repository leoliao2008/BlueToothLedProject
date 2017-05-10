package com.liansong.blueled.activities;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseApplication;
import com.liansong.blueled.bases.BlueToothActivity;
import com.liansong.blueled.customized.LedStatusViewer;
import com.liansong.blueled.customized.LedView;
import com.liansong.blueled.utils.AlertDialogueUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import csh.tiro.cc.aes;

public class MainActivity extends BlueToothActivity {
    private ListView lstv_record;
    private TextView tv_timing;
    private LedStatusViewer mLedStatusViewer;
    private ArrayAdapter<String> mAdapter;
    private LedView mLed1;
    private LedView mLed2;
    private Button btn_clearLog;
    private ArrayList<String> arr_records =new ArrayList<>();
    private boolean isTiming;
    private boolean isCharWriteFound;
    private boolean isCharNotifyFound;
    /**
     * 触发计时开始的led
     */
    private char ledTrigger;
    /**
     * 触发计时终止的led
     */
    private char ledTerminate;
    private boolean isResponseToNotification;


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
        btn_clearLog= (Button) findViewById(R.id.main_btn_clear_log);
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
    protected void initListener() {
        mLed1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLedStatusViewer.isConnected()){
                    litLed(0,!mLed1.isLit());
                }else {
                    showLog("感应灯未链接，请先尝试链接感应灯。");
                }
            }
        });

        mLed2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLedStatusViewer.isConnected()){
                    litLed(1,!mLed2.isLit());
                }else {
                    showLog("感应灯未链接，请先尝试链接感应灯。");
                }
            }
        });

        btn_clearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arr_records.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

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


    /**
     * 点亮或关闭某盏led
     * @param ledIndex led编号，0为led A，1为led B。
     * @param isToLit true为打开，false为关闭
     */
    private void litLed(int ledIndex, boolean isToLit) {
        int value=isToLit?1:0;
        switch (ledIndex) {
            case 0:
                mCommandData[9]= (byte) (value&0xff);
                break;
            case 1:
                mCommandData[10]= (byte) (value&0xff);
                break;
            default:
                break;
        }
        BaseApplication.post(mRunnableSendRequest);
    }




    @NonNull
    @Override
    protected BluetoothGattCallback initBlueToothGattCallBack() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
                if(newState== BluetoothProfile.STATE_CONNECTED){
                    AlertDialogueUtils.dismissDialog();
                    showLog("Gatt connected.");
                    setBluetoothGatt(gatt);
                    showLog("begin to discover gatt service...");
                    if(gatt.discoverServices()){
                        showLog("discovering......");
                    }else {
                        showLog("discovering service fail to init.");
                    }
                }else {
                    showLog("Gatt disconnected.");
                    if(getBluetoothGatt()!=null){
                        showLog("try to re-connect gatt in 1s...");
                        BaseApplication.postDelay(mRunnableReconnect,1000);
                    }else {
                        closeGatt();
                    }
                }
            }


            //此处有坑：onServicesDiscovered 回调里不能直接执行 write /readDataFromCharacteristic() 或者 enableNotificationOfCharacteristic之类的，而要放到主线程里执行，如 handler.post( … );
            @Override
            public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                boolean isSuccess=false;
                if(status==BluetoothGatt.GATT_SUCCESS){
                    showLog("onServicesDiscovered:");
                    mGattService = gatt.getService(UUID.fromString(SERVICE_UUID));
                    if(mGattService!=null){
                        List<BluetoothGattCharacteristic> characteristics = mGattService.getCharacteristics();
                        mNotifyChars.clear();
                        for(BluetoothGattCharacteristic characteristic:characteristics){
                            if(characteristic.getUuid().toString().equals(CHARACTER_WRITE_UUID)){
                                mCharWrite=characteristic;
                                isCharWriteFound=true;
                            }else if(characteristic.getProperties()==BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                                mNotifyChars.add(characteristic);
                            }
                        }
                        if(isCharWriteFound&&mNotifyChars.size()>0){
                            mCharWrite.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                            isSuccess=true;
                            //有坑，必须在主线程中设置notify char.
                            BaseApplication.post(new Runnable() {
                                @Override
                                public void run() {
//                                    setNotifyChar(gatt,mNotifyChars.get(0)); //只设置一个notify char会无法收到回复，原因未知。
                                    setNextNotifyChar(gatt);
                                }
                            });
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
                if(mNotifyChars.size()>0){
                    setNextNotifyChar(gatt);
                }else {
                    mLedStatusViewer.setConnected(true);
                    showLog("All the notify char have been set.");
                }
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
               //不知道为什么每一次请求会引起两次回复，这里只使用一次回复，忽略另外一次回复。
                isResponseToNotification=!isResponseToNotification;
                if(isResponseToNotification){
                    byte[] data = characteristic.getValue();
                    if(data!=null){
                        if(data.length==DATA_LEN){
                            aes.invCipher(data, mDataReceived);
                            updateLedViews();
                        }
                    }
                }

                //接收一次数据后线程休息50毫秒，以提高数据接收稳定性。
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
    }


    /**
     * 在主线程中更新led的状态。
     */
    private synchronized void updateLedViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mDataReceived[0] == 'T' && mDataReceived[1] == 'R' && mDataReceived[2] == '1' && mDataReceived[3] == '7' &&
                   mDataReceived[4] == '0' && mDataReceived[5] == '3' && mDataReceived[6] == 'R' && mDataReceived[7] == '0' &&
                   mDataReceived[8] == '2'){
                    boolean isToLitLed1=false;
                    boolean isToLitLed2=false;
                    switch (mDataReceived[9]){
                        case 0:
                            isToLitLed1=false;
                            mCommandData[9]=0;//如果不这么操作，led有时会无法返回数据，原因未明。
                            break;
                        case 1:
                            isToLitLed1=true;
                            break;
                        default:
                            break;
                    }
                    switch (mDataReceived[10]){
                        case 0:
                            isToLitLed2=false;
                            mCommandData[10]=0;//如果不这么操作，led有时会无法返回数据，原因未明。
                            break;
                        case 1:
                            isToLitLed2=true;
                            break;
                        default:
                            break;
                    }
                    shouldUpdateTimingView(isToLitLed1,isToLitLed2);
                    mLedStatusViewer.toggleLeds(isToLitLed1,isToLitLed2);
                }
            }
        });
    }

    /**
     * 判断是否启动或关闭计时。
     * @param isToLitLed1
     * @param isToLitLed2
     */
    private void shouldUpdateTimingView(boolean isToLitLed1,boolean isToLitLed2){
        boolean isLedOneLit=mLed1.isLit();
        boolean isLedTwoLit=mLed2.isLit();
        if(!isTiming){
            //在计时未开始且两盏灯都没亮的情况下，找出将被点亮的led，开始计时。
            if(!isLedOneLit&&!isLedTwoLit){
                //只打开led1或led2其中一盏，才符合计时开始的条件。
                if(isToLitLed1&&!isToLitLed2){
                    ledTrigger='A';
                    startTiming();
                }else if(isToLitLed2&&!isToLitLed1){
                    ledTrigger='B';
                    startTiming();
                }
            }

        }else {
            //在计时已经开始的情况下，找出状态将发生变化的led，判断是由哪盏灯引起的计时终止。
            if(isLedOneLit!=isToLitLed1){
                ledTerminate='A';
                stopTiming();
            }else if(isLedTwoLit!=isToLitLed2){
                ledTerminate='B';
                stopTiming();
            }
        }
    }

    protected synchronized void stopTiming() {
        isTiming=false;
    }

    protected synchronized void startTiming() {
        if(!isTiming){
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
                        timeMilli+=60;
                        publishProgress(timeMilli);
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Long... values) {
                    tv_timing.setText(toTimingFormat(values[0]));
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    StringBuffer sb=new StringBuffer();
                    sb.append("计时结束，")
                            .append("从")
                            .append(ledTrigger)
                            .append("到")
                            .append(ledTerminate)
                            .append(",间隔时间：")
                            .append(tv_timing.getText().toString());
                    updateConsole(sb.toString());
                }
            }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    /**
     * 更新console的信息。
     * @param msg 新的业务信息
     */
    private void updateConsole(final String msg) {
        BaseApplication.post(new Runnable() {
            @Override
            public void run() {
                arr_records.add(msg);
                mAdapter.notifyDataSetChanged();
                lstv_record.smoothScrollToPosition(arr_records.size()-1);
            }
        });
    }


    @Override
    protected void onShowLog(String msg) {
        updateConsole(msg);
    }

    @Override
    protected void onShowToast(String msg) {
        updateConsole(msg);
    }

    @Override
    protected void onGattClose() {
        mLedStatusViewer.setConnected(false);
        isTiming=false;
        isCharNotifyFound=false;
        isCharWriteFound=false;
    }

}
