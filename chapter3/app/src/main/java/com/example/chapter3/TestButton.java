package com.example.chapter3;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Scroller;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

public class TestButton extends AppCompatTextView {
    //弹性滑动的实现方式：
    //1.Scroller：Scroller#startScroll方法，其内部invalidate导致重绘，调用draw，draw调用computeScroll根据时间比例计算当前要滑动到的位置进行实际的滑动，然后又调用postInvalidate又会导致重绘，如此反复直到移动到终点
    //2.属性动画：天然就是弹性的，并且可以实现Scroller类似的效果
    //3.延时策略:使用延时消息或者sleep实现不断scrollTo

    private static final String TAG = "UITest";
    private int mScaledTouchSlop;
    private Scroller scroller;

    private final int startX=0;
    final int deltaX=-100;

    private static final int MESSAGE_SCROLL_TO=1;
    private static final int FRAME_COUNT=30;
    private static final int DELAYED_TIME=33;
    private int mCount=0;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case MESSAGE_SCROLL_TO:
                    mCount++;
                    if(mCount<=FRAME_COUNT){
                        float fraction=mCount/(float)FRAME_COUNT;
                        int scrollX=(int)(fraction*deltaX);
                        scrollTo(scrollX,0);
                        mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL_TO,DELAYED_TIME);
                        break;
                    }
                    default:
                        break;
            }
        }
    };




    //记录上次滑动的坐标
    private int mLastX;
    private int mLastY;

    public TestButton(Context context) {
        this(context, null);
    }

    public TestButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        scroller=new Scroller(getContext());
        Log.d(TAG, "slop is: " + mScaledTouchSlop);
    }

    //使用Scroller实现View内容的弹性滑动
    public void smoothScrollTo(int destX,int destY){
        //获取mScrollX
        int scrollX=getScrollX();
        int scrollY=getScrollY();
        int deltaX=destX-scrollX;
        int deltaY=destY-scrollY;
        scroller.startScroll(scrollX,scrollY,deltaX,deltaY,1000);
        invalidate();
    }

    //使用Anim实现弹性滑动
    public void smoothScrollToWithAnim(){
        ValueAnimator animator=ValueAnimator.ofInt(0,1).setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction=animation.getAnimatedFraction();
                scrollTo(startX+(int)(deltaX*fraction),0);

            }
        });
        //别忘了开始
        animator.start();
    }

    public void smoothScrollToWithDelayMessage(){
        mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL_TO,DELAYED_TIME);
    }

    @Override
    public void computeScroll() {
        if(scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(),scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                Log.d(TAG, "move,delta X and delta Y are: " + deltaX + "," + deltaY);
                int translationX = (int) ViewHelper.getTranslationX(this) + deltaX;
                int translationY = (int) ViewHelper.getTranslationY(this) + deltaY;

                ViewHelper.setTranslationX(this, translationX);
                ViewHelper.setTranslationY(this, translationY);
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;

        }
        mLastX = x;
        mLastY = y;
        return true;

    }
}
