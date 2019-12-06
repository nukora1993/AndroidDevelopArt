package com.example.chapter2.binderpool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.CountDownLatch;

public class BinderPool {
    private static final String TAG="BinderPool";
    public static final int BINDER_NONE=-1;
    public static final int BINDER_COMPUTE=0;
    public static final int BINDER_SECURITY_CENTER=1;

    private Context mContext;
    private IBinderPool mBinderPool;
    private static volatile BinderPool sInstance;
    private CountDownLatch mConnectBinderPoolCountDownLatch;

    //在创建时就和Service连接，注意到BinderPool必须要在Activity中执行，才能bind
    private BinderPool(Context context){
        mContext= context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context){
        if(sInstance==null){
            synchronized (BinderPool.class){
                if(sInstance==null){
                    sInstance=new BinderPool(context);
                }
            }
        }
        return sInstance;
    }

    private synchronized void connectBinderPoolService(){
        mConnectBinderPoolCountDownLatch=new CountDownLatch(1);
        Intent service=new Intent(mContext,BinderPoolService.class);
        mContext.bindService(service,mBinderPoolConnection,
                Context.BIND_AUTO_CREATE);

        try{
            //等待与Service连接
            mConnectBinderPoolCountDownLatch.await();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public IBinder queryBinder(int binderCode){
        IBinder binder=null;
        try{
            if(mBinderPool!=null){
                binder=mBinderPool.queryBinder(binderCode);
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
        return binder;
    }

    private ServiceConnection mBinderPoolConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool=IBinderPool.Stub.asInterface(service);
            try{
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipent,0);
            }catch(RemoteException e){
                e.printStackTrace();
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipent=new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG,"binder died");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipent,0);
            mBinderPool=null;
            //重新与Service连接
            connectBinderPoolService();
        }
    };

    public static class BinderPoolImpl extends IBinderPool.Stub{
        public BinderPoolImpl(){
            super();
        }

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            IBinder binder=null;
            switch (binderCode){
                case BINDER_SECURITY_CENTER:
                    binder=new SecurityCenterImpl();
                    break;
                case BINDER_COMPUTE:
                    binder= new ComputeImpl();
                    break;
                    default:
                        break;

            }
            return binder;
        }
    }



}
