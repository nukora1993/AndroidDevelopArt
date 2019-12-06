package com.example.chapter3.PinnedHeaderExpandableListView.expandable;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

//可折叠的pinnedListView,实际就是在expandlistview里加了一个header，通过重写expandlistview的onmeasure方法以控制header的显示
public class PinnedHeaderExpandableListView extends ExpandableListView
implements AbsListView.OnScrollListener {
    private static final String TAG="TestView";

    public interface OnHeaderUpdateListener{
        //得到固定在顶端的header
        View getPinnedHeader();
        //更新顶端header
        void updatePinnedHeader(View headerView,int firstVisibleGroupPos);
    }

    //固定在顶端的Header
    private View mHeaderView;
    private int mHeaderWidth;
    private int mHeaderHeight;

    private View mTouchTarget;

    private AbsListView.OnScrollListener mScrollListener;
    private OnHeaderUpdateListener mHeaderUpdateListener;

    private boolean mActionDownHappened=false;
    protected boolean mIsHeaderGroupClickable=true;

    public PinnedHeaderExpandableListView(Context context){
        super(context);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs){
        super(context,attrs);
        initView();
    }

    public PinnedHeaderExpandableListView(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        initView();
    }

    private void initView(){
        //设置拉滚动条时边框渐变的方向和大小(?)
        setFadingEdgeLength(0);
        //由于extend了ExpandableListView,所以有这个方法，其他View应该没有这个方法
        setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if(l!=this){
            mScrollListener=l;
        }else{
            mScrollListener=null;
        }
        super.setOnScrollListener(this);
    }

    //设置group的click的listener
    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener,
                                        boolean isHeaderGroupClickable){
        Log.d(TAG,"expandViewSetOnGroupClickListener");
        mIsHeaderGroupClickable=isHeaderGroupClickable;
        super.setOnGroupClickListener(onGroupClickListener);
    }

    //设置header更新时的listener
    public void setOnHeaderUpdateListener(OnHeaderUpdateListener listener){
        mHeaderUpdateListener=listener;
        //如果不设置，那么header没有存在的必要
        if(listener==null){
            mHeaderView=null;
            mHeaderWidth=mHeaderHeight=0;
            return;
        }
        //获得listview的header，实际就是group.xml
        mHeaderView=listener.getPinnedHeader();
        //第一个可见的子元素
        int firstVisiblePos=getFirstVisiblePosition();
        //第一个可见的group
        int firstVisibleGroupPos=getPackedPositionGroup(
                getExpandableListPosition(firstVisiblePos)
        );

        //使用第一个可见的group更新header
        listener.updatePinnedHeader(mHeaderView,firstVisibleGroupPos);
        //实际上requestLayout就会重新导致控件的onMeausre->onDraw,而postInvalidate只会重新onDraw
        requestLayout();
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //如果没有headerView，那么measure完毕
        if(mHeaderView==null){
            return;
        }
        //否则需要测量Header的大小，测量headerView的大小
        measureChild(mHeaderView,widthMeasureSpec,heightMeasureSpec);
        mHeaderWidth=mHeaderView.getMeasuredWidth();
        mHeaderHeight=mHeaderView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //调用super.onLayout会确定expandlistview本身的位置
        super.onLayout(changed, l, t, r, b);
        if(mHeaderView==null){
            return;
        }
        //这里为什么能直接得到headerView的top,top属性不应该时先要layout才能得到么？
        //但是正常情况下header应该显示左上角，所以直接getTop应该是0,也没问题
        int delta=mHeaderView.getTop();
        Log.d(TAG,"headerview delta is:"+delta);
        mHeaderView.layout(0,delta,mHeaderWidth,mHeaderHeight+delta);
    }

    //绘制子View
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(mHeaderView!=null){
            drawChild(canvas,mHeaderView,getDrawingTime());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x=(int)ev.getX();
        int y=(int)ev.getY();
        int pos=pointToPosition(x,y);
        //如果点击的时header
        if(mHeaderView!=null&&y>=mHeaderView.getTop()&&
        y<=mHeaderView.getBottom()){
            //如果时DOWN
            if(ev.getAction()==MotionEvent.ACTION_DOWN){
                mTouchTarget=getTouchTarget(mHeaderView,x,y);
                mActionDownHappened=true;
                //如果时UP
            }else if(ev.getAction()==MotionEvent.ACTION_UP){
                View touchTarget=getTouchTarget(mHeaderView,x,y);
                //
                if(touchTarget==mTouchTarget&&mTouchTarget.isClickable()){
                    mTouchTarget.performClick();
                    //重绘指定区域
                    invalidate(new Rect(0,0,mHeaderWidth,mHeaderHeight));
                //
                }else if(mIsHeaderGroupClickable){
                    int groupPosition=getPackedPositionGroup(
                            getExpandableListPosition(pos)
                    );
                    if(groupPosition!=INVALID_POSITION&&mActionDownHappened){
                        if(isGroupExpanded(groupPosition)){
                            collapseGroup(groupPosition);
                        }else{
                            expandGroup(groupPosition);
                        }
                    }
                }
                mActionDownHappened=false;
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private View getTouchTarget(View view,int x,int y){
        if(!(view instanceof ViewGroup)){
            return view;
        }
        ViewGroup parent=(ViewGroup)view;
        int childrenCount=parent.getChildCount();
        final boolean customOrder=isChildrenDrawingOrderEnabled();
        View target=null;
        for (int i = childrenCount-1; i >=0 ; i--) {
            final int childIndex=customOrder?getChildDrawingOrder(
                    childrenCount,i
            ):i;
            final View child=parent.getChildAt(childIndex);
            if(isTouchPointInView(child,x,y)){
                target=child;
                break;
            }
        }
        if(target==null){
            target=parent;
        }
        return target;
    }

    private boolean isTouchPointInView(View view,int x,int y){
        if(view.isClickable()&&y>=view.getTop()&&y<=view.getBottom()
        &&x>=view.getLeft()&&x<=view.getRight()){
            return true;
        }
        return false;
    }

    private void requestRefreshHeader(){
        refreshHeader();
        invalidate(new Rect(0,0,mHeaderWidth,mHeaderHeight));
    }

    protected void refreshHeader(){
        if(mHeaderView==null){
            return;
        }
        int firstVisiblePos=getFirstVisiblePosition();
        int pos=firstVisiblePos+1;
        int firstVisibleGroupPos=getPackedPositionGroup(
                getExpandableListPosition(firstVisiblePos)
        );

        int group=getPackedPositionGroup(getExpandableListPosition(pos));

        Log.d(TAG,"refreshHeader firstVisibleGroupPos="+firstVisibleGroupPos);

        if(group==firstVisibleGroupPos+1){
            View view=getChildAt(1);
            if(view==null){
                Log.d(TAG,"refreshHeader getChildAt(1)=null");
                return;
            }
            if(view.getTop()<=mHeaderHeight){
                int delta=mHeaderHeight-view.getTop();
                mHeaderView.layout(0,-delta,mHeaderWidth,mHeaderHeight-delta);
            }else{
                mHeaderView.layout(0,0,mHeaderWidth,mHeaderHeight);
            }
        }else{
            mHeaderView.layout(0,0,mHeaderWidth,mHeaderHeight);
        }

        if(mHeaderUpdateListener!=null){
            mHeaderUpdateListener.updatePinnedHeader(mHeaderView,firstVisibleGroupPos);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(mScrollListener!=null){
            mScrollListener.onScrollStateChanged(view,scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(totalItemCount>0){
            refreshHeader();
        }
        if(mScrollListener!=null){
            mScrollListener.onScroll(view,firstVisibleItem,
            visibleItemCount,totalItemCount);
        }
    }
}
