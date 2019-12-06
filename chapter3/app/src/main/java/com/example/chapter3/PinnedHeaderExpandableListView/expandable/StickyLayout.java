package com.example.chapter3.PinnedHeaderExpandableListView.expandable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import com.example.chapter3.R;

import java.util.NoSuchElementException;

//StickLayout的关键思想：
//根据滑动区域决定是否要要拦截
//如果拦截，则根据滑动方向设置header高度实现展现或者隐藏header的效果
public class StickyLayout extends LinearLayout {
    private static final String TAG="StickyLayout";

    public interface OnGiveUpTouchEventListener{
        boolean giveUpTouchEvent(MotionEvent event);
    }


    private View mHeader;
    private View mContent;
    private OnGiveUpTouchEventListener mGiveUpTouchEventListener;

    //header的高度
    private int mOriginalHeaderHeight;
    private int mHeaderHeight;

    //当前是折叠还是展开的
    private int mStatus=STATUS_EXPANDED;
    public static final int STATUS_EXPANDED=1;
    public static final int STATUS_COLLAPSED=2;

    private int mTouchSlop;

    //上次滑动的坐标
    private int mLastX=0;
    private int mLastY=0;

    private int mLastXIntercept=0;
    private int mLastYIntercept=0;

    //滑动角度控制,即deltaY/deltaX>2
    private static final int TAN=2;

    private boolean mIsSticky=true;
    private boolean mInitDataSucceed=false;
    private boolean mDisallowInterceptTouchEventOnHeader=true;

    public StickyLayout(Context context){
        super(context);
    }

    public StickyLayout(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public StickyLayout(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        //当被展示时，但是还没有数据时就初始化数据
        if(hasWindowFocus&&(mHeader==null||mContent==null)){
            initData();
        }
    }

    private void initData(){
        //getResource和R.id.x的区别：getResource可以直接找到对象，但是R.xx只能找到id
        int headerId=getResources().getIdentifier("sticky_header","id",getContext().getPackageName());
        int contentId=getResources().getIdentifier("sticky_content","id",getContext().getPackageName());
        if(headerId!=0&&contentId!=0){
            mHeader=findViewById(headerId);
            mContent=findViewById(contentId);
            //获取header的初始高度
            mOriginalHeaderHeight=mHeader.getMeasuredHeight();
            mHeaderHeight=mOriginalHeaderHeight;
            mTouchSlop= ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if(mHeaderHeight>0){
                mInitDataSucceed=true;
            }
            Log.d(TAG,"mTouchSlop,mHeaderHeight="+mTouchSlop+","+mHeaderHeight);
        }else{
            throw new NoSuchElementException("no view with given id");
        }
    }

    public void setOnGiveUpTouchEventListener(OnGiveUpTouchEventListener l){
        mGiveUpTouchEventListener=l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int intercepted=0;
        int x=(int)ev.getX();
        int y=(int)ev.getY();

        //决定是否要拦截
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastXIntercept=x;
                mLastYIntercept=y;
                mLastX=x;
                mLastY=y;
                intercepted=0;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastXIntercept;
                int deltaY=y-mLastYIntercept;
                //如果滑动位置在header内，且设置了不允许拦截，则不拦截
                if(mDisallowInterceptTouchEventOnHeader&&y<=getHeaderHeight()){
                    intercepted=0;
                    //如果不在header内或者允许拦截，且竖直方向距离小于水平距离，则不是竖直滑动，不拦截
                }else if(Math.abs(deltaY)<=Math.abs(deltaX)){
                    intercepted=0;
                    //如果不在header内或者允许拦截，且是竖直滑动，如果当前状态为展开，且向上滑动距离超过阈值，则允许拦截
                }else if(mStatus==STATUS_EXPANDED&&deltaY<=-mTouchSlop){
                    intercepted=1;
                    //如果设置了listener，且当前向下滑动
                }else if(mGiveUpTouchEventListener!=null){
                    //listener的作用是看当前listview展现的是否是第一个子元素，且该子元素在header的下方，那么此时又是向下滑动，那么就要展开header
                    if(mGiveUpTouchEventListener.giveUpTouchEvent(ev)&&
                    deltaY>=mTouchSlop){
                        intercepted=1;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted=0;
                mLastXIntercept=mLastYIntercept=0;
                break;
                default:break;
        }
        Log.d(TAG,"intercepted="+intercepted);
        return intercepted!=0&&mIsSticky;
    }



    public int getHeaderHeight(){
        return mHeaderHeight;
    }

    //如果决定拦截，那么根据滑动的位置改变header的大小
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mIsSticky){
            return true;
        }
        int x=(int)event.getX();
        int y=(int)event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX=x-mLastX;
                int deltaY=y-mLastY;
                Log.d(TAG,"mHeaderHeight,deltaY,mLastY"+mHeaderHeight+","+deltaY+","+mLastY);
                //上滑就缩小，下滑就增大显示
                mHeaderHeight+=deltaY;
                setHeaderHeight(mHeaderHeight);
                break;
            case MotionEvent.ACTION_UP:
                //当松手时，会自动划向两边，具体向那边滑动要看当前位置
                int destHeight=0;
                //如果当前header的高度小于原始高度0.5，那么折叠（将高度设置为0），否则展开(设置为原始高度）
                if(mHeaderHeight<=mOriginalHeaderHeight*0.5){
                    destHeight=0;
                    mStatus=STATUS_COLLAPSED;
                }else{
                    destHeight=mOriginalHeaderHeight;
                    mStatus=STATUS_EXPANDED;
                }
                //慢慢滑动到终点
                this.smoothSetHeaderHeight(mHeaderHeight,destHeight,500);
                break;
                default: break;
        }
        mLastX=x;
        mLastY=y;
        return true;
    }

    public void smoothSetHeaderHeight(final int from,final int to,long duration){
        smoothSetHeaderHeight(from,to,duration,false);
    }

    public void smoothSetHeaderHeight(final int from,final int to,long duration,
                                      final boolean modifyOriginalHeaderHeight){
        //滑动的帧数
        final int frameCount=(int)(duration/1000f*30)+1;
        //每帧要滑动的距离
        final float partition=(to-from)/(float)frameCount;

        new Thread("Thread#smoothSetHeaderHeight"){
            @Override
            public void run() {
                for (int i = 0; i <frameCount ; i++) {
                    final int height;
                    if(i==frameCount-1){
                        height=to;
                    }else{
                        height=(int)(from+partition*i);
                    }
                    //采用异步消息，将要设置的高度值作为消息添加到消息队列末尾
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setHeaderHeight(height);
                        }
                    });

                    try{
                        sleep(10);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }

                if(modifyOriginalHeaderHeight){
                    setOriginalHeaderHeight(to);
                }
            }
        }.start();
    }

    public void setOriginalHeaderHeight(int originalHeaderHeight){
        mOriginalHeaderHeight=originalHeaderHeight;
    }

    public void setHeaderHeight(int height,boolean modifyOriginalHeaderHeight){
        if(modifyOriginalHeaderHeight){
            setOriginalHeaderHeight(height);
        }
        setHeaderHeight(height);
    }

    public void setHeaderHeight(int height){
        if(!mInitDataSucceed){
            initData();
        }

        Log.d(TAG,"setHeaderHeight="+height);

        if(height<=0){
            height=0;
        }else{
            if(height>mOriginalHeaderHeight){
                height=mOriginalHeaderHeight;
            }

            if(height==0){
                mStatus=STATUS_COLLAPSED;
            }else{
                mStatus=STATUS_EXPANDED;
            }

            if(mHeader!=null&&mHeader.getLayoutParams()!=null){
                mHeader.getLayoutParams().height=height;
                //这里是关键，导致重绘
                mHeader.requestLayout();
                mHeaderHeight=height;
            }else{
                Log.d(TAG,"null layoutparam");
            }
        }
    }

    public void setSticky(boolean isSticky){
        mIsSticky=isSticky;
    }

    public void requestDisallowInterceptTouchEventOnHeader(boolean disallowIntercept){
        mDisallowInterceptTouchEventOnHeader=disallowIntercept;
    }
}
