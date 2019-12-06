package com.example.chapter2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chapter2.aidl.BookManagerActivity;
import com.example.chapter2.binderpool.BinderPoolActivity;
import com.example.chapter2.messenger.MessengerActivity;
import com.example.chapter2.provider.ProviderActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text_view_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent=new Intent(MainActivity.this,SecondActivity.class);
//                Intent intent=new Intent(MainActivity.this, MessengerActivity.class);
//                Intent intent=new Intent(MainActivity.this, BookManagerActivity.class);
//                Intent intent=new Intent(MainActivity.this, ProviderActivity.class);
                Intent intent=new Intent(MainActivity.this, BinderPoolActivity.class);
                startActivity(intent);
            }
        });
    }
}
