package com.liansong.blueled.utils;

import android.widget.Toast;

import com.liansong.blueled.bases.BaseApplication;

/**
 * Created by 廖华凯 on 2017/4/19.
 */

public class ToastUtil {
    private static Toast toast;
    public static void showToast(String msg){
        if(toast==null){
            toast=Toast.makeText(BaseApplication.getContext(),msg,Toast.LENGTH_SHORT);
        }else {
            toast.setText(msg);
        }
        toast.show();
    }
}
