package com.liansong.blueled.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Window;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseApplication;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DisplayMetrics metrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        BaseApplication.setScreenWidth(metrics.widthPixels);
        BaseApplication.setScreenHeight(metrics.heightPixels);
        BaseApplication.postDelay(new Runnable() {
            @Override
            public void run() {
                MainActivity.startActivity(SplashActivity.this);
                finish();
            }
        },500);
    }
}
