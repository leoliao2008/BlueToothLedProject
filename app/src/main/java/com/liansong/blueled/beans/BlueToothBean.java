package com.liansong.blueled.beans;

import android.bluetooth.BluetoothDevice;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class BlueToothBean {
    private BluetoothDevice mDevice;
    private int RSSI;
    private boolean isConnect;
    private String deviceAddress;

    public BlueToothBean(BluetoothDevice device, int RSSI) {
        mDevice = device;
        this.RSSI = RSSI;
        deviceAddress=device.getAddress();
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlueToothBean that = (BlueToothBean) o;

        return deviceAddress.equals(that.deviceAddress);

    }

    @Override
    public int hashCode() {
        return deviceAddress.hashCode();
    }
}
