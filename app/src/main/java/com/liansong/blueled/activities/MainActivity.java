package com.liansong.blueled.activities;

import android.app.Activity;
import android.content.Intent;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseActivity;

public class MainActivity extends BaseActivity {

    public static void startActivity(Activity context){
        context.startActivity(new Intent(context,MainActivity.class));
    }


    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {

    }
}
