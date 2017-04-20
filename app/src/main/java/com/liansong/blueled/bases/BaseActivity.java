package com.liansong.blueled.bases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.liansong.blueled.utils.AlertDialogueUtils;
import com.liansong.blueled.utils.LogUtil;
import com.liansong.blueled.utils.ToastUtil;

/**
 * Created by 廖华凯 on 2017/4/19.
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

    @Override
    public void onBackPressed() {
        confirmBackPress();
    }

    protected void confirmBackPress(){
        AlertDialogueUtils.showHint(
                this,
                "您确定要退出此程序吗？",
                new Runnable() {
                    @Override
                    public void run() {
                        BaseActivity.super.onBackPressed();
                    }
                });
    }

    protected void showToast(String msg){
        ToastUtil.showToast(msg);
    }

    protected void showLog(String msg){
        LogUtil.showLog(BaseActivity.class.getSimpleName(),msg);
    }
}
