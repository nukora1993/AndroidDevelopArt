package com.example.chapter5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//过时的方式
//        Notification notification =new Notification();
//        notification.icon=R.drawable.ic_launcher_background;
//        notification.tickerText="hello world";
//        notification.when=System.currentTimeMillis();
//        notification.flags=Notification.FLAG_AUTO_CANCEL;
//        Intent intent=new Intent(this,DemoActivity1.class);
//        PendingIntent pi=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//
//
//        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(1,notification);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        //channel id
        String id="my_channel";
        //channel name
        CharSequence name="my notification channel";
        //channel description
        String description="my channel description";
        int importance =NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel myChannel=new NotificationChannel(id,name,importance);
        myChannel.setDescription(description);
        //闪光灯
        myChannel.enableLights(true);
        myChannel.setLightColor(Color.RED);
        //声音
//        myChannel.setSound();
        //震动,实测震动不需要权限
//        myChannel.enableVibration(true);
//        myChannel.setVibrationPattern(new long[]{100,200,300,400,500,400,300,200,400});
        myChannel.setImportance(importance);
        //创建该channel
        notificationManager.createNotificationChannel(myChannel);
        Intent intent=new Intent(this,DemoActivity1.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


        //使用remoteviews自定义notification样式
        RemoteViews remoteViews=new RemoteViews(getPackageName(),
                R.layout.layout_notification);

        remoteViews.setTextViewText(R.id.msg,"TextViewText");
        remoteViews.setImageViewResource(R.id.icon,R.drawable.ic_launcher_background);

        //PendingIntent
        PendingIntent openActivity2PendingIntent=PendingIntent.getActivity(this,
                0,new Intent(this,DemoActivity2.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        //remoteViews给控件添加单击事件必须通过PendingIntent，设置文本和图像都需要通过指定方法而不能直接设置
        //因为remoteView实际展示在系统进程中，Activity和RemoteViews的通信属于进程间通信
        remoteViews.setOnClickPendingIntent(R.id.open_activity2,openActivity2PendingIntent);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,id);
        Notification notification=builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker("hello world")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setCustomContentView(remoteViews)
                .build();


        //PengdingIntent和通知的关系
        //当notify id相同，那么后面的通知直接覆盖前面的通知
        //当notify id不同时，当PendingIntent相同时，需要根据标志位讨论
        //当标志位时FLAG_ONE_SHOT,那么后续通知回合第一条保持一致，单击任意一条，其余均无法打开
        //当标志位位FLAG_CANCEL_CURRENT,只有最新的可以打开，其余均不能打开
        //当标志位时FLAG_UPDATE_CURRENT,所有之前的会更新位当前，并且都可以打开
        notificationManager.notify(1,notification);
        notificationManager.notify(1,notification);

        //remoteview不是所有的view都支持，只支持一部分的view
        //remoteview不能通过findViewById，而是采用set方法，其内部采用反射

        //remoteview原理
        //实际的view运行在系统进程，对应的manager运行在自己的进程，通过binder联系
        //remoteview通过binder传递给系统进程，系统根据其配置inflate，然后通过binder通信更新
        //实际上每次set调用会产生Action对象，当manager要求update是,Action会传递给系统进程，系统进程逐个更新
        //所以remoteview不是立刻更新的，而是要notfiy或者update才会实际更新
    }
}
