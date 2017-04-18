package com.liansong.blueled.bases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public abstract class BaseActivity extends AppCompatActivity {
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
}
