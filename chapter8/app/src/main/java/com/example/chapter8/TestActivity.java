package com.example.chapter8;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

public class TestActivity extends AppCompatActivity {
    private static final String TAG="WindowTest";

    private Button mFloatingButton;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //获得WindowManager
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mFloatingButton=new Button(this);
        mFloatingButton.setText("button");

        mLayoutParams=new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0,0,
                PixelFormat.TRANSPARENT
        );
//        mLayoutParams.type= WindowManager.LayoutParams.TYPE_APPLICATION;
        //Window是有层次的，高层Winodw会覆盖低级
        //window分为应用级（对应Activity）、子窗口（必须依附在应用窗口）和系统级
        //应用window范围是1-99，子窗口是1000-1999，系统级是2000-2999

        //不知道是不是因为设置的这个原因，即使退出了app（所有activity都销毁），但是window还是显示？，只有彻底从preview中关掉才能，因该是结束了Application才能销毁
        //如果想要手动销毁window，只要销毁window里面的view即可

        mLayoutParams.type= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        //一些Flag
        //FLAG_NOT_FOCUSABLE，window不需要获取焦点，不要接受输入事件，事件会直接传递给下层具有焦点的window，注意到window也是有层次结构的
        //FLAG_NOT_TOUCH_MODAL，window只处理落在自己区域内的单击事件，不再自己区域的传递
        //FLAG_SHOW_WHEN_LOCKED，window显示在锁屏界面(?)
        mLayoutParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        mLayoutParams.gravity= Gravity.LEFT|Gravity.TOP;

        //Window的极限范围不包含系统状态栏
        mLayoutParams.x=0;
        mLayoutParams.y=0;
        //通过windowManager添加button

        mFloatingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int rawX=(int)event.getRawX();
                int rawY=(int)event.getRawY();
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        mLayoutParams.x=rawX;
                        mLayoutParams.y=rawY;
                        mWindowManager.updateViewLayout(mFloatingButton,mLayoutParams);
                        break;
                        default:break;
                }
                return false;
            }
        });

        mWindowManager.addView(mFloatingButton,mLayoutParams);

//        mFloatingButton.getParent();


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //需要手动remove，不然除非彻底关闭application才能移除window
        mWindowManager.removeView(mFloatingButton);
        Log.d(TAG,"onDestroy");
    }
}
