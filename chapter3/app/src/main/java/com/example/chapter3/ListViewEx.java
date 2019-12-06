package com.example.chapter3;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ListViewEx extends ListView {
    private static final String TAG="ListViewEx";
    private HorizontalScrollViewEx2 mHorizontalScrollViewEx2;

    //分别记录上次滑动的坐标
    private int mLastX=0;
    private int mLastY=0;

    public ListViewEx(Context context){
        super(context);
    }

    public ListViewEx(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public ListViewEx(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }

    public void setHorizontalScrollViewEx2(
            HorizontalScrollViewEx2 horiziontalScrollViewEx2
    ){
        mHorizontalScrollViewEx2=horiziontalScrollViewEx2;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x=(int)ev.getX();
        int y=(int)ev.getY();

        switch(ev.getAction()){
            //当事件为DOWN时，不允许拦截，但是实际上应该不影响，因为DOWN会清除掉这个标志
            case MotionEvent.ACTION_DOWN:
                mHorizontalScrollViewEx2.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastX;
                int deltaY=y-mLastY;
                //当检测为水平滑动时，这时允许ViewGroup要去拦截
                //ViewGroup要设置为拦截除了DOWN之外的所有事件，这样一旦设置了标志位，那么后续事件就会交给ViewGroup
                if(Math.abs(deltaX)>Math.abs(deltaY)){
                    mHorizontalScrollViewEx2.requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
                default:break;

        }
        mLastX=x;
        mLastY=y;


        return super.dispatchTouchEvent(ev);
    }
}
