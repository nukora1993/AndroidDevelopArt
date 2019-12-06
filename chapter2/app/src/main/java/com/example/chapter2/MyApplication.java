package com.example.chapter2;

import android.app.Application;
import android.os.Process;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG="MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        String processName=MyUtils.getProcessName(getApplicationContext(), Process.myPid());
        Log.d(TAG,"application start,process name:"+processName);
    }
}
