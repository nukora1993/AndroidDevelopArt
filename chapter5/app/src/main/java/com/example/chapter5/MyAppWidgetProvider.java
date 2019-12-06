package com.example.chapter5;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

//小部件的实现类实质是一个Receiver，通过它接受消息
public class MyAppWidgetProvider extends AppWidgetProvider {
    public static final String TAG="MyAppWidgetProvider";
    public static final String CLICK_ACTION="com.wmm.action.CLICK";

    public MyAppWidgetProvider(){
        super();
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG,"onReceive:action="+intent.getAction());
        //是什么样的action
        if(intent.getAction().equals(CLICK_ACTION)){
            Toast.makeText(context,"cicked it",Toast.LENGTH_SHORT).show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //这里不知道为啥，如果采用ic_lanuch就会得到null，而采用作者提供的图片就能正常得到bitmap
                    Bitmap srcBitmap= BitmapFactory.decodeResource(
                            context.getResources(),R.drawable.icon1
                    );

                    Log.d(TAG,srcBitmap.toString());

                    AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
                    for (int i = 0; i <37 ; i++) {
                        float degree=(i*10)%360;
                        //通过remoteviews通过进程通信更新小部件
                        RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget);
                        //旋转小部件中的图片t
                        remoteViews.setImageViewBitmap(R.id.image_view_widget,rotateBitmap(context,srcBitmap,degree));
                        Intent intentClick=new Intent();
//                        intentClick.addFlags(0x01000000);
                        //设置点击事件，该点击事件发送一个广播
                        intentClick.setAction(CLICK_ACTION);
                        //注意高版本系统发送隐式广播必须要指定packageName
                        intentClick.setPackage(context.getPackageName());
                        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intentClick,0);
                        remoteViews.setOnClickPendingIntent(R.id.image_view_widget,pendingIntent);
                        //光设置了remoteView没用，还需要通过appWidgetManager去通知小部件更新
                        appWidgetManager.updateAppWidget(new ComponentName(
                                context,MyAppWidgetProvider.class
                        ),remoteViews);

                        SystemClock.sleep(30);
                    }
                }
            }).start();
        }
    }

    //onUpdate会在小部件被系统更新时调用，更新时期取决于配置的更新周期
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG,"onUpdate");

        final int counter=appWidgetIds.length;
        Log.d(TAG,"counter="+counter);
        //更新组件内容
        for (int i = 0; i <counter ; i++) {
            int appWidgetId=appWidgetIds[i];
            onWidgetUpdate(context,appWidgetManager,appWidgetId);
        }
    }

    private void onWidgetUpdate(Context context,AppWidgetManager appWidgetManager,int appWidgetId){
        Log.d(TAG,"appWidgetId="+appWidgetId);
        RemoteViews remoteViews=new RemoteViews(context.getPackageName(),R.layout.widget);

        Intent intentClick=new Intent();
        intentClick.setAction(CLICK_ACTION);
//        intentClick.addFlags(0x0100000);
        intentClick.setPackage(context.getPackageName());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0,intentClick,0);
        remoteViews.setOnClickPendingIntent(R.id.image_view_widget,pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
    }

    //采用矩阵生成旋转的Bitmap
    private Bitmap rotateBitmap(Context context,Bitmap srcBitmap,float degree){
        Matrix matrix=new Matrix();
        matrix.reset();
        matrix.setRotate(degree);
        Bitmap tmpBitmap=Bitmap.createBitmap(srcBitmap,0,0,srcBitmap.getWidth(),
                srcBitmap.getHeight(),matrix,true);
        return tmpBitmap;
    }
}
