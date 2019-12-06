package com.example.chapter3;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Scroller;

//用于测试一些函数
public class TestActivity extends AppCompatActivity {
    private static final String TAG="UITest";
    private RelativeLayout centerRelativeLayout;
    private Button centerButton;

    private VelocityTracker velocityTracker;
    private GestureDetector gestureDetector;
    private Scroller scroller;

    private TestButton testButton;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        centerRelativeLayout=findViewById(R.id.center_relative_layout);
        centerButton=findViewById(R.id.center_button);
        scroller=new Scroller(this);
        testButton=findViewById(R.id.test_button);

        findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注意使用scroller的方向如果向右向下则是负值
//                testButton.smoothScrollTo(-100,-100);
                //使用anim方式
//                testButton.smoothScrollToWithAnim();
                //使用延时消息方式
//                testButton.smoothScrollToWithDelayMessage();
                //启动DemoActivity1
//                Intent intent=new Intent(TestActivity.this,DemoActivity1.class);
                //启动DemoActivity2
//                Intent intent=new Intent(TestActivity.this,DemoActivity2.class);
//                startActivity(intent);
                //启动StickyLayoutActivity
                Intent intent=new Intent(TestActivity.this,StickyLayoutActivity.class);
                startActivity(intent);
            }
        });



        gestureDetector=new GestureDetector(this,new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                //触摸屏幕瞬间
                Log.d(TAG,"onDown");
                //大致有三种方式实现view的变化效果：1.view动画2.属性动画3.改变LayoutParam4.scrollTo,前三种都是直接作用于view，最后一种是改变view的内容

                //view.scrollTo实现绝对滑动，scrollby实现相对滑动
                //但是scrollTo和scrollBy改变的是view中内容的位置而不是view本身的位置
                //view中的mScrollX表示内容和view左边缘的距离，mScrollY表示内容和view上便于的距离的距离
                //注意正值表示内容向左，向上移动
//                centerRelativeLayout.scrollBy(100,0);
                //属性动画可以移动view本身，主要时Android3.0以下无法使用属性动画
//                ObjectAnimator.ofFloat(centerButton,"translationX",0,100).setDuration(100).start();
//                ObjectAnimator.ofFloat(centerRelativeLayout,"translationX",0,100).setDuration(100).start();
                //改变layoutParam，注意layoutParam可以直接改变View的位置，但是可能存在一些问题
                //比如如果时RelativeLayout设置了centerInParent，那么再设置marginLeft时无效的
                ViewGroup.MarginLayoutParams params=(ViewGroup.MarginLayoutParams)centerButton.getLayoutParams();
//                params.width+=200;
                params.leftMargin+=100;
                Log.d(TAG,"current center button leftMargin:"+params.leftMargin);
//                centerButton.requestLayout();
                centerButton.setLayoutParams(params);
                return false;
            }


            @Override
            public void onShowPress(MotionEvent e) {
                //onDown之后立即触发
                Log.d(TAG,"onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //单击
                Log.d(TAG,"onSingleTapUp");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //拖动
                Log.d(TAG,"onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //长按
                Log.d(TAG,"onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //快速滑动
                Log.d(TAG,"onFling");
                return false;
            }


        });
        //一般来说，如果只是监听简单的滑动单击行为，可以直接使用onTouchEvent，如果是要检测双击，那么可以使用gestureDector
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //严格的单击行为，也就是说不可能是双击中的一次单击
                Log.d(TAG,"onSingleTapConfirmed");
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                //双击
                Log.d(TAG,"onDoubleTap");
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                //发生了双击行为
                return false;
            }
        });

        velocityTracker=VelocityTracker.obtain();



        centerRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //匿名内部类引用成员变量时，成员变量是不需要加final的
                gestureDetector.onTouchEvent(event);
                //检测某个事件的滑动速度（可能有多点触控的可能）
                velocityTracker.addMovement(event);
                //计算前一段时间内划过的速度，向右向下为正，按道理不应该在onTouch中计算，因为onTouch调用的非常频繁
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity=velocityTracker.getXVelocity();
                float yVelocity=velocityTracker.getYVelocity();
                Log.d(TAG,"Xvelocity and Yvelocity are:"+xVelocity+","+yVelocity);

                return true;
            }
        });

        centerButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //onTouch只要是触摸控件就会调用，使用event表示当前touch事件类型，getX和getY是相对于当前控件位置，getRawX和getRawY是相对于屏幕左上角位置
                //按道理getRawX和getRawY应该是不会变化的，但是实测在touch时会有一些微小的变化
                Log.d(TAG,"centerButton is touched, x,y and rawX,rawY are: "+"("+event.getX()+","+event.getY()+")"+
                ","+"("+event.getRawX()+","+event.getRawY()+")"+"and Action is："+event.getAction());
                return true;
            }
        });

        //touchSlop是一个常量，和设备有关，表示被系统认为是滑动的最小滑动距离
        //通常可以检测两次滑动事件之间的距离以判定是否滑动
        int touchSlop=ViewConfiguration.get(this).getScaledTouchSlop();
        Log.d(TAG,"TouchSlop is:"+touchSlop);



    }



    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //topleft和rightbottom是相对于父view而言
        //但是注意不能再onCreate/onAttachedToWindow获取，因为此时view还没有被真正的绘制在屏幕上
        //并且注意到获得的坐标单位是px，而不是xml中的dp
        Log.d(TAG,"centerRelativeLayout left,top and right,bottom are: ("+centerRelativeLayout.getLeft()+","+centerRelativeLayout.getTop()+")"+
                ","+"("+centerRelativeLayout.getRight()+","+centerRelativeLayout.getBottom()+")");
        //x,y是相对于父容器的左上角，transX和transY是相对于父容器的偏移，默认是0
        Log.d(TAG,"centerRelativeLayout x,y and translationX,translationY are: ("+centerRelativeLayout.getX()+","+centerRelativeLayout.getY()+")"+
                ","+"("+centerRelativeLayout.getTranslationX()+","+centerRelativeLayout.getTranslationY()+")");

        //left,right,top,bottom都是相对于父view，并且在平移过程中时不变的，改变的时view的x，y和translationX和translationY
        Log.d(TAG,"centerButton left,top and right,bottom are: ("+centerButton.getLeft()+","+centerButton.getTop()+")"+
                ","+"("+centerButton.getRight()+","+centerButton.getBottom()+")");
        //x,y是相对于父容器的左上角，transX和transY是相对于父容器的偏移，默认是0
        Log.d(TAG,"centerButton x,y and translationX,translationY are: ("+centerButton.getX()+","+centerButton.getY()+")"+
                ","+"("+centerButton.getTranslationX()+","+centerButton.getTranslationY()+")");
    }
}
