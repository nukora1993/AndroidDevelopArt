package com.example.chapter13;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;


public class TestApp extends Application {


    private static TestApp sInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance=this;

        CrashHandler crashHandler=CrashHandler.getInstance();
        crashHandler.init(this);

    }

    public static TestApp getInstance(){
        return sInstance;
    }
}
