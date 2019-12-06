package com.example.chapter2.binderpool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.chapter2.R;

public class BinderPoolActivity extends AppCompatActivity {
    private static final String TAG="BinderPoolActivity";

    private ISecurityCenter mSecurityCenter;
    private ICompute mCompute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binder_pool);
        new Thread(new Runnable() {
            @Override
            public void run() {
                doWork();
            }
        }).start();
    }

    private void doWork(){
        BinderPool binderPool=BinderPool.getInstance(BinderPoolActivity.this);
        IBinder securityBinder=binderPool.queryBinder(BinderPool.BINDER_SECURITY_CENTER);
        mSecurityCenter=(ISecurityCenter)SecurityCenterImpl.asInterface(securityBinder);
        Log.d(TAG,"visit ISecurityCenter");
        String msg="hello,world-android";
        Log.d(TAG,"content:"+msg);
        try{
            String password=mSecurityCenter.encrypt(msg);
            Log.d(TAG,"encrypt:"+password);
            Log.d(TAG,"decrypt:"+mSecurityCenter.decrypt(password));
        }catch (RemoteException e){
            e.printStackTrace();
        }

        Log.d(TAG,"visit ICompute");
        IBinder computerBinder=binderPool.queryBinder(BinderPool.BINDER_COMPUTE);
        mCompute=ICompute.Stub.asInterface(computerBinder);
        try{
            Log.d(TAG,"3+5="+mCompute.add(3,5));
        }catch(RemoteException e){
            e.printStackTrace();
        }

    }
}
