package com.example.chapter2.messenger;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chapter2.MyConstants;

public class MessengerService extends Service {
    private static final String TAG="MessengerService";

    public MessengerService() {
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //注意Handler是线程异步消息，Messenger是基于Binder实现进程通信，使用Handler作为消息处理工具
    private static class MessenegrHandler extends Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case MyConstants.MSG_FROM_CLIENT:
                    Log.d(TAG,"receive msg from Client:"+msg.getData().getString("msg"));
                    Messenger client=msg.replyTo;
                    Message replyMessage=Message.obtain(null,MyConstants.MSG_FROM_SERVICE);
                    Bundle bundle=new Bundle();
                    bundle.putString("reply","好的，你的消息已收到，稍后回复");
                    replyMessage.setData(bundle);
                    try{
                        client.send(replyMessage);
                    }catch(RemoteException e){
                        e.printStackTrace();
                    }
                    break;
                    default:
                        super.handleMessage(msg);
            }

        }
    }

    private final Messenger mMessenger=new Messenger(new MessenegrHandler());


}
