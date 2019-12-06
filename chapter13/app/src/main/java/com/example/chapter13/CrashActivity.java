package com.example.chapter13;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CrashActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
        initView();
    }

    private void initView(){
        findViewById(android.R.id.content).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        throw new RuntimeException("自定义异常");
    }
}
