package com.example.chapter3;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

//允许水平滑动的ViewGroup
public class HorizontalScrollViewEx extends ViewGroup {
    private static final String TAG="HoriziontalScrollViewEx";
    private int mChildrenSize;
    private int mChildWidth;
    private int mChildIndex;

    //记录上次滑动的坐标
    private int mLastX=0;
    private int mLastY=0;

    private int mLastXIntercept=0;
    private int mLastYIntercept=0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public HorizontalScrollViewEx(Context context){
        super(context);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
    }


    public HorizontalScrollViewEx(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        init();
    }

    private void init(){
        mScroller=new Scroller(getContext());
        mVelocityTracker=VelocityTracker.obtain();
    }

    //解决滑动冲突：外部拦截，即使用ViewGroup根据事件决定是否要去拦截
    //外部拦截法比较简单易用
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted=false;
        //点击事件相对坐标
        int x=(int)ev.getX();
        int y=(int)ev.getY();

        switch(ev.getAction()){
            //默认不拦截DOWN，因为拦截了DOWN，后续事件就都被ViewGroup处理
            case MotionEvent.ACTION_DOWN:
                intercepted=false;
                //下面主要是为了优化滑动体验，如果用户水平滑动的时候又开始竖直滑动，那么该View就会处于一个中间状态
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                    intercepted=true;
                }
                break;
                //根据滑动的竖直和水平差值判定是上下还是水平滑动，若水平距离大则是水平滑动，需要拦截
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastXIntercept;
                int deltaY=y-mLastYIntercept;
                if(Math.abs(deltaX)>Math.abs(deltaY)){
                    intercepted=true;
                }else{
                    intercepted=false;
                }
                break;
                //一般不拦截UP，因为没啥用
            case MotionEvent.ACTION_UP:
                intercepted=false;
                break;
                default:
                    break;
        }
        Log.d(TAG,"intercept="+intercepted);
        //更新最后事件的位置
        mLastX=x;
        mLastY=y;
        //最后拦截的位置
        mLastXIntercept=x;
        mLastYIntercept=y;
        return intercepted;
    }

    //如果拦截，那么该方法会被调用，也就是执行水平滑动
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
    //确定子view的位置
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

    //确定子View的大小
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
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }
}
