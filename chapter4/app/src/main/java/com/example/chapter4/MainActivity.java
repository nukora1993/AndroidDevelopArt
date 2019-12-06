package com.example.chapter4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ContentFrameLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivityViewTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Activity的最顶层是DecorView，DecorView里面是一个LinearLayout，LinearLayout上面是title，下面是content
        //View的事件最先传递给DecorView，然后才是自己的View
        //title也就是标题栏，content也就是主要部分，content是一个FrameLayout，setContentView就是设置的这个content
        //但是貌似自己找title找不到，返回null，content是可以找到的
//        findViewById(android.R.id.title).setVisibility(View.GONE);
        ContentFrameLayout contentFrameLayout=findViewById(android.R.id.content);
        //找到自己设置的view
        View myContentView=contentFrameLayout.getChildAt(0);
        Log.d(TAG,myContentView.toString());


//        findViewById(android.R.id.content).setVisibility(View.GONE);


        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });



    }
}
