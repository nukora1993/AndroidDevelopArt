package com.example.chapter10;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class TestActivity extends AppCompatActivity {
    private static final String TAG="TestActivityLog";
    private ThreadLocal<Boolean> mBooleanThreadLocal=new ThreadLocal<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //主线程设置
        mBooleanThreadLocal.set(true);
        Log.d(TAG,Thread.currentThread().getName()+":"+mBooleanThreadLocal.get());

        new Thread("Thread#1"){
            @Override
            public void run() {
                mBooleanThreadLocal.set(false);
                Log.d(TAG,Thread.currentThread().getName()+":"+mBooleanThreadLocal.get());
            }
        }.start();

        new Thread("Thread#2"){
            @Override
            public void run() {
//                mBooleanThreadLocal.set(false);
                //注意如果没有set那么取出来的是null而不是默认值
                Log.d(TAG,Thread.currentThread().getName()+":"+mBooleanThreadLocal.get());
            }
        }.start();


    }
}
