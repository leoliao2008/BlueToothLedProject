package com.liansong.blueled.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liansong.blueled.R;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class ScanResultViewHolder extends RecyclerView.ViewHolder {
    private TextView tv_devName;
    private TextView tv_devAddress;
    private TextView tv_devType;
    private TextView tv_devRSSI;
    private TextView tv_devStatus;
    private Button btn_connect;
    public ScanResultViewHolder(View itemView) {
        super(itemView);
        tv_devName= (TextView) itemView.findViewById(R.id.main_tv_dev_name);
        tv_devAddress= (TextView) itemView.findViewById(R.id.main_tv_dev_address);
        tv_devType= (TextView) itemView.findViewById(R.id.main_tv_dev_type);
        tv_devRSSI= (TextView) itemView.findViewById(R.id.main_tv_dev_rssi);
        tv_devStatus= (TextView) itemView.findViewById(R.id.main_tv_dev_status);
        btn_connect= (Button) itemView.findViewById(R.id.main_btn_connect_gatt);
    }

    public TextView getTv_devName() {
        return tv_devName;
    }

    public TextView getTv_devAddress() {
        return tv_devAddress;
    }

    public TextView getTv_devType() {
        return tv_devType;
    }

    public TextView getTv_devRSSI() {
        return tv_devRSSI;
    }

    public TextView getTv_devStatus() {
        return tv_devStatus;
    }

    public Button getBtn_connect() {
        return btn_connect;
    }
}
