package com.example.chapter2.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {
    private static final String TAG="BMS";
    private AtomicBoolean mIsServiceDestroyed=new AtomicBoolean(false);
    private CopyOnWriteArrayList<Book> mBookList=new CopyOnWriteArrayList<Book>();
//    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList=new CopyOnWriteArrayList<IOnNewBookArrivedListener>();
    //由于binder之间传输是使用的是序列化的对象，所以不能直接删除，需要使用RemoteCallbackList
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList=new RemoteCallbackList<IOnNewBookArrivedListener>();
    //IBookManager中的Stub时实际的Binder类
    private Binder mBinder=new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        //onTransact是在客户端调用时被触发，此时可以做一些验证操作
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int check=checkCallingOrSelfPermission("com.wmm.ACCESS_BOOK_SERVICE");
            Log.d(TAG,"check="+check);
            if(check== PackageManager.PERMISSION_DENIED){
                return false;
            }

            String packageName=null;
            String[] packages=getPackageManager().getPackagesForUid(getCallingUid());
            if(packages!=null&&packages.length>0){
                packageName=packages[0];
            }
            Log.d(TAG, Arrays.toString(packages));
            Log.d(TAG,"onTransact:"+packageName);
            //返回false会导致客户端调用失败
            if(!packageName.startsWith("com.example")){
                return false;
            }

            return super.onTransact(code, data, reply, flags);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException{
            Log.d(TAG,"now register listener:"+listener);
//            if(!mListenerList.contains(listener)){
//                mListenerList.add(listener);
//            }else{
//                Log.d(TAG,"listener already exists");
//            }
//            Log.d(TAG,"registerListener,size:"+mListenerList.size());
            mListenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException{
//            if(mListenerList.contains(listener)){
//                mListenerList.remove(listener);
//                Log.d(TAG,"unregister listener succeed.");
//            }else{
//                Log.d(TAG,"not found,can not unregister");
//            }
//            Log.d(TAG,"unregisterListener,current size:"+mListenerList.size());
            mListenerList.unregister(listener);
        }






    };

    public BookManagerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        int check=checkCallingOrSelfPermission("com.wmm.ACCESS_BOOK_SERVICE");
        if(check==PackageManager.PERMISSION_DENIED){
            return null;
        }
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        //注意到虽然return的时Binder，但是这个return时给Android OS的，Activity拿到的并不是原来的Binder，如果时进程间，Activity得到的时一个Proxy，该Proxy implement了IBookManger的
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1,"Android"));
        mBookList.add(new Book(2,"IOS"));
        new Thread(new ServiceWorker()).start();
    }

    private void onNewBookArrived(Book book) throws RemoteException{
        mBookList.add(book);
//        Log.d(TAG,"onNewBookArrived,notify listeners:"+mListenerList.size());
//        for (int i = 0; i <mListenerList.size() ; i++) {
//            IOnNewBookArrivedListener listener=mListenerList.get(i);
//            Log.d(TAG,"onNewBookArrived,notify listener:"+listener);
//            listener.onNewBookArrived(book);
//        }
        final int N=mListenerList.beginBroadcast();
        for (int i = 0; i <N ; i++) {
            IOnNewBookArrivedListener l=mListenerList.getBroadcastItem(i);
            if(l!=null){
                try{
                    l.onNewBookArrived(book);
                }catch(RemoteException e){
                    e.printStackTrace();
                }
            }
        }
        mListenerList.finishBroadcast();
    }

    //后台执行，每5s加一个新书，并通知所有listener
    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while(!mIsServiceDestroyed.get()){
                try{
                    Thread.sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }

                int bookId=mBookList.size()+1;
                Book newBook=new Book(bookId,"new book#"+bookId);
                try{
                    onNewBookArrived(newBook);
                }catch(RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
