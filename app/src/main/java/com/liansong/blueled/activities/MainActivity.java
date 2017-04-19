package com.liansong.blueled.activities;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.ArrayList;

public class MainActivity extends BlueToothActivity {
    private ListView lstv_record;
    private TextView tv_timing;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> arr_records =new ArrayList<>();

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

    }


    @Override
    protected BluetoothGattCallback initBlueToothGattCallBack() {
        return new BluetoothGattCallback() {
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
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
            }
        };
    }
}
