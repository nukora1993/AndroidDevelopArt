package com.example.chapter15;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewStub;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //动态加载ViewStub，当ViewStub加载之后，ViewStub就不属于当前布局，而内部布局成了当前layout的成员
//        ((ViewStub)findViewById(R.id.stub_import)).setVisibility(View.VISIBLE);

        //ui线程耗时操作导致anr
//        SystemClock.sleep(30*1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                testANR();
            }
        }).start();

        SystemClock.sleep(10);
        initView();
    }

    private synchronized void testANR(){
        SystemClock.sleep(30*1000);
    }
    //由于testANR先获得锁，所以initView会被阻塞而导致ANR
    private synchronized void initView(){

    }
}
