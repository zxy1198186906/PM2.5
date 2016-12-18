package com.example.pm;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by liuhaodong1 on 15/11/14.
 */
public class MyApplication extends Application {

    public Vibrator mVibrator;
    public static boolean isExit=false;
    private static int countSteps;

    public static boolean isExit() {
        return isExit;
    }


    public static int getCountSteps() {
        return countSteps;
    }

    public static void setCountSteps(int num) {
        countSteps = num;
    }
    public static void setIsExit(boolean isExit) {
        MyApplication.isExit = isExit;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        countSteps=0;
        Log.v("Crysa_Log", "MyApplication.onCreate");
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
    }

}
