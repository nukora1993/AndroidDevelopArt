package com.example.chapter11;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //默认串行执行
//                new MyAsyncTask("task1").execute();
//                new MyAsyncTask("task2").execute();
//                new MyAsyncTask("task3").execute();
//                new MyAsyncTask("task4").execute();
//                new MyAsyncTask("task5").execute();
                //改成并行执行
                new MyAsyncTask("task1").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                new MyAsyncTask("task2").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                new MyAsyncTask("task3").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                new MyAsyncTask("task4").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
                new MyAsyncTask("task5").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
            }
        });
    }
}
