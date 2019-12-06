package com.example.chapter5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class RemoteViewTest extends AppCompatActivity {
    private static final String TAG="RemoteViewTest";
    private LinearLayout mRemoteViewsContent;

    //使用广播传递remoteview，虽然可以采用binder，但是这里简单起见
    private BroadcastReceiver mRemoteViewReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteViews=intent.getParcelableExtra(MyConstants.EXTRA_REMOTE_VIEWS);
            if(remoteViews!=null){
                updateUI(remoteViews);
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_view_test);
        initView();
    }

    private void initView(){
        mRemoteViewsContent=(LinearLayout)findViewById(R.id.remote_view_content);
        IntentFilter filter=new IntentFilter(MyConstants.REMOTE_ACTION);
        //动态注册
        registerReceiver(mRemoteViewReceiver,filter);

    }

    private void updateUI(RemoteViews remoteViews){
        //获得资源
        int layoutId=getResources().getIdentifier(
                "layout_simulated_notification","layout",getPackageName()
        );
        //得到view
        View view=getLayoutInflater().inflate(layoutId,mRemoteViewsContent,false);
        //apply会加载布局并更新view，reapply只会更新view
        remoteViews.reapply(this,view);
        mRemoteViewsContent.addView(view);


    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mRemoteViewReceiver);
        super.onDestroy();
    }

    public void onButtonClick(View v){
        if(v.getId()==R.id.remote_view_button1){
            Intent intent=new Intent(this,DemoActivity1.class);
            startActivity(intent);
        }else if(v.getId()==R.id.remote_view_button2){
            Intent intent=new Intent(this,DemoActivity2.class);
            startActivity(intent);
        }
    }
}
