package com.example.chapter11;

import android.os.AsyncTask;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyAsyncTask extends AsyncTask<String,Integer,String> {
    private String TAG="AsyncTaskLog";

    public MyAsyncTask(String TAG){
        super();
        this.TAG=TAG;
    }

    @Override
    protected String doInBackground(String... strings) {
        try{
            Thread.sleep(3000);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return TAG;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.d(TAG,s+" execute finished at "+df.format(new Date()));
    }
}
