package com.liansong.blueled.bases;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.liansong.blueled.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT =911;
    private static final int REQUEST_PERMISSIONS = 912;
    private AlertDialog mAlertDialog;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private boolean isBlueToothReady;
    private UUID[] mUUIDs=new UUID[]{UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")};
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayout());
        initView();
        initData();
        initListener();
    }

    protected abstract int setLayout();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        requestPermissions();
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED
                    ||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_DENIED){
                String[] permissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
                for(String pm:permissions){
                    if(shouldShowRequestPermissionRationale(pm)){
                        requestPermissions(permissions, REQUEST_PERMISSIONS);
                    }
                }
            }
        }
    }

    protected void scanLeDevices(){
        if(initBlueTooth()){
            startScanLeDevices();
        }
    }

    private void startScanLeDevices() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                        showScanResultView(result.getDevice(),result.getRssi());
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            };
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
            BaseApplication.postDelay(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    }
                }
            },10000);
        }else {
            mLeScanCallback=new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //// TODO: 2017/4/18
                    mBluetoothAdapter.stopLeScan(this);
                    showScanResultView(device,rssi);
                }
            };
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            BaseApplication.postDelay(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            },10000);
//        mBluetoothAdapter.startLeScan(mUUIDs,mLeScanCallback);

        }
    }


    protected boolean initBlueTooth(){
        if(!isBlueToothReady){
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
        }
        return isBlueToothReady;
    }

    private void connectToGATTSever(BluetoothDevice device) {
        BluetoothGatt bluetoothGatt = device.connectGatt(this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== REQUEST_PERMISSIONS){
            for(int i:grantResults){
                if(i==PackageManager.PERMISSION_DENIED){
                    alertDialogRequestPermission();
                    break;
                }
            }
        }
    }

    private void alertDialogRequestPermission() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        mAlertDialog=builder.setTitle("关于申请系统权限")
                .setMessage("为了保证本机的蓝牙功能正常运行，必须要获得GPS定位权限。按确认重新申请，按取消退出程序。")
                .setCancelable(false)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions();
                        mAlertDialog.dismiss();
                    }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create();
        mAlertDialog.show();

    }

    @Override
    public void onBackPressed() {
        confirmBackPress();
    }

    protected void confirmBackPress(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        mAlertDialog = builder.setTitle("退出程序")
                .setCancelable(true)
                .setMessage("您确定要退出此程序吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();
                        BaseActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog.dismiss();

                    }
                })
                .create();
        mAlertDialog.show();
    }

    private void showScanResultView(final BluetoothDevice device, int rssi){
        View rootView = View.inflate(this, R.layout.scan_le_device_layout, null);
        TextView tv_devName= (TextView) rootView.findViewById(R.id.main_tv_dev_name);
        TextView tv_devAddress= (TextView) rootView.findViewById(R.id.main_tv_dev_address);
        TextView tv_devType= (TextView) rootView.findViewById(R.id.main_tv_dev_type);
        TextView tv_devRSSI= (TextView) rootView.findViewById(R.id.main_tv_dev_rssi);
        TextView tv_devStatus= (TextView) rootView.findViewById(R.id.main_tv_dev_status);
        Button btn_connect= (Button) rootView.findViewById(R.id.main_btn_connect_gatt);
        tv_devName.setText("Device Name:"+device.getName());
        tv_devAddress.setText("Device Address:"+device.getAddress());
        String temp="null";
        switch (device.getType()){
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
                break;
        }
        tv_devType.setText("Device Type:"+temp);
        tv_devRSSI.setText("Device RSSI"+rssi);
        tv_devStatus.setText("Device Status: Disconnected");
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToGATTSever(device);
            }
        });
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        mAlertDialog=builder.setView(rootView).create();
        mAlertDialog.show();
    }


}
