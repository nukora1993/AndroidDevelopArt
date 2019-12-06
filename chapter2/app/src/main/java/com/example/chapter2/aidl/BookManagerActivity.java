package com.example.chapter2.aidl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.example.chapter2.R;

import java.util.List;

public class BookManagerActivity extends AppCompatActivity {
    private static final String TAG="BookManagerActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED=1;

    private IBookManager mRemoteBookManager;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG,"receive new book:"+msg.obj);
                    break;
                    default:
                        super.handleMessage(msg);
            }

        }
    };

    private ServiceConnection mConnection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IBookManager bookManager=IBookManager.Stub.asInterface(service);

            try{
                mRemoteBookManager=bookManager;
                List<Book> list=bookManager.getBookList();
                Log.d(TAG,"query book list,list type:"+list.getClass().getCanonicalName());
                Log.d(TAG,"query book list:"+list.toString());
                Book newBook=new Book(3,"Android开发艺术探索");
                bookManager.addBook(newBook);
                Log.d(TAG,"add book:"+newBook);
                List<Book> newList=bookManager.getBookList();
                Log.d(TAG,"query book list:"+newList.toString());

                Log.d(TAG,"client register listener:"+mOnNewBookArrivedListener);
                bookManager.registerListener(mOnNewBookArrivedListener);
            }catch(RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager=null;
            Log.d(TAG,"binder died");
        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener=new IOnNewBookArrivedListener.Stub() {
        //该方法运行在binder池，所以通过异步消息发送给Handler处理
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,newBook).sendToTarget();
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_manager);
        Intent intent=new Intent(this,BookManagerService.class);
        bindService(intent,mConnection,BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if(mRemoteBookManager!=null&&mRemoteBookManager.asBinder().isBinderAlive()){
            try{
                Log.d(TAG,"unregister listener:"+mOnNewBookArrivedListener);
                mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
            }catch(RemoteException e){
                e.printStackTrace();
            }
        }

        unbindService(mConnection);
        super.onDestroy();
    }
}
