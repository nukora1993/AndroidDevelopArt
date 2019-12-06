package com.example.chapter2.messenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.example.chapter2.MyConstants;
import com.example.chapter2.R;

public class MessengerActivity extends AppCompatActivity {
    private static final String TAG="MessengerActivity";
    private Messenger mService;
    private Messenger mGetReplyMessenger=new Messenger(new MessengerHandler());

    private static class MessengerHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case MyConstants.MSG_FROM_SERVICE:
                    Log.d(TAG,"receive msg from service"+msg.getData().getString("reply"));
                    break;
                    default:
                        super.handleMessage(msg);
            }

        }
    }

    private ServiceConnection mConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService=new Messenger(service);
            Message msg=Message.obtain(null, MyConstants.MSG_FROM_CLIENT);
            Bundle data=new Bundle();
            data.putString("msg","你好，这是客户端");
            msg.setData(data);
            msg.replyTo=mGetReplyMessenger;
            try{
                mService.send(msg);
            }catch(RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        Intent intent=new Intent(this,MessengerService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
