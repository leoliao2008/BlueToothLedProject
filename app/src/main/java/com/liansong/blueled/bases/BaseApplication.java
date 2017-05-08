package com.liansong.blueled.bases;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class BaseApplication extends Application {
    private static int screenWidth;
    private static int screenHeight;
    private static Handler handler;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        handler=new Handler();
    }

    public static Context getContext() {
        return context;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static void setScreenWidth(int screenWidth) {
        BaseApplication.screenWidth = screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static void setScreenHeight(int screenHeight) {
        BaseApplication.screenHeight = screenHeight;
    }

    public static void post(Runnable runnable){
        handler.post(runnable);
    }

    public static void postDelay(Runnable runnable,long millis){
        handler.postDelayed(runnable,millis);
    }

    public static void removeCallback(Runnable runnable){
        handler.removeCallbacks(runnable);
    }
}
