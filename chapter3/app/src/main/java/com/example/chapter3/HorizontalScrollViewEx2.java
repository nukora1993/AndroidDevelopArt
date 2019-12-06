package com.example.chapter3;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class HorizontalScrollViewEx2 extends ViewGroup {
    private static final String TAG="HorizontalScrollViewEx2";

    private int mChildrenSize;
    private int mChildWidth;
    private int mChildIndex;

    private int mLastX=0;
    private int mLastY=0;

    private int mLastXIntercept=0;
    private int mLastYIntercept=0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public HorizontalScrollViewEx2(Context context){
        super(context);
        init();
    }

    public HorizontalScrollViewEx2(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }

    public HorizontalScrollViewEx2(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private void init(){
        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    //滑动冲突解决：内部拦截，默认拦截除了DOWN之外的所有事件，是否拦截由子View设置标志位控制
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x=(int)ev.getX();
        int y=(int)ev.getY();
        int action=ev.getAction();
        if(action==ev.ACTION_DOWN){
            mLastX=x;
            mLastY=y;
            if(!mScroller.isFinished()){
                mScroller.abortAnimation();
                return true;
            }
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x=(int)event.getX();
        int y=(int)event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastX;
                int deltaY=y-mLastY;
                //注意scroll的偏移符号
                scrollBy(-deltaX,0);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX=getScrollX();
                int scrollToChildIndex=scrollX/mChildWidth;
                mVelocityTracker.computeCurrentVelocity(1000);
                //获得x速度
                float xVelocity=mVelocityTracker.getXVelocity();
                //如果x速度超过50，判断是往哪边滑动，注意如果是往右边滑动，那么idx-1，否则idx+1
                if(Math.abs(xVelocity)>=50){
                    Log.d(TAG,"previous childIdx:"+mChildIndex);
                    mChildIndex=xVelocity>0?mChildIndex-1:mChildIndex+1;
                    Log.d(TAG,"cur childIdx:"+mChildIndex);
                    Log.d(TAG,"cur scrollX:"+scrollX);
                }else{
                    mChildIndex=(scrollX+mChildWidth/2)/mChildWidth;
                }
                mChildIndex=Math.max(0,Math.min(mChildIndex,mChildrenSize-1));
                //滑动的距离等于第几个Child的整数倍,划到第几页还剩下的距离
                //为啥scrollTo向左滑动向上滑动为正呢，猜想是因为向左，向上划符合习惯
                int dx=mChildIndex*mChildWidth-scrollX;
                smoothScrollBy(dx,0);
                mVelocityTracker.clear();
                break;
            default: break;
        }
        mLastX=x;
        mLastY=y;
        return true;
    }

    //实现弹性滑动
    private void smoothScrollBy(int dx,int dy){
        mScroller.startScroll(getScrollX(),0,dx,0,500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth=0;
        int measuredHeight=0;
        final int childCount=getChildCount();
        //该方法调用所有子View的measure方法
        measureChildren(widthMeasureSpec,heightMeasureSpec);

        int widthSepcSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthSepcMode=MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);

        if(childCount==0){
            setMeasuredDimension(0,0);
            //如果高度是wrap content，那么高度设置为子View的高度（默认所有子View高度相同？）
        }else if(heightSpecMode==MeasureSpec.AT_MOST){
            final View childView=getChildAt(0);
            measuredHeight=childView.getMeasuredHeight();
            setMeasuredDimension(widthSepcSize,childView.getMeasuredHeight());
            //如果宽度是wrap content，那么宽度设置为所有子View之和（默认所有子View宽度相同？）
        }else if (widthSepcMode==MeasureSpec.AT_MOST){
            final View childView=getChildAt(0);
            measuredWidth=childView.getMeasuredWidth()*childCount;
            setMeasuredDimension(measuredWidth,heightSpecSize);
        }else{
            final View childView=getChildAt(0);
            measuredWidth=childView.getMeasuredWidth();
            measuredHeight=childView.getMeasuredHeight()*childCount;
            setMeasuredDimension(measuredWidth,measuredHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //默认左上角是0
        int childLeft=0;
        final int childCount=getChildCount();
        mChildrenSize=childCount;
        for (int i = 0; i <childCount ; i++) {
            final View childView=getChildAt(i);
            if(childView.getVisibility()!= View.GONE){
                final int childWidth=childView.getMeasuredWidth();
                mChildWidth=childWidth;
                childView.layout(childLeft,0,childLeft+childWidth,childView.getMeasuredHeight());
                //然后向右排
                childLeft+=childWidth;
            }

        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
