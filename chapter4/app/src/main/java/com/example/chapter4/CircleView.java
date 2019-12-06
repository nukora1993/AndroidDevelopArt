package com.example.chapter4;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class CircleView extends View {
    private static final String TAG="CircleView";

    private int mColor= Color.RED;
    private Paint mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context){
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs){
        this(context,attrs,0);
        init();
    }

    public CircleView(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
        //加载自定义属性集合
        TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.CircleView);
        //获取属性值
        mColor=a.getColor(R.styleable.CircleView_circle_color,Color.RED);

        a.recycle();

        init();
    }

    private void init(){
        mPaint.setColor(mColor);
        Log.d(TAG,String.valueOf(mColor==Color.RED));

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽高模式和大小
        int widthSpecMode=MeasureSpec.getMode(widthMeasureSpec);
        int widithSpecSize=MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);

        //处理wrapcontent
        //如果宽高都是自适应的（wrap content),那么设置一个默认宽高
        //为什么只要设置默认宽高就能自动处理wrapcontent呢？
        if(widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,200);
        }else if(widthSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(200,heightSpecSize);
        }else if(heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(widithSpecSize,200);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //padding和wrapContent需要自定义View自己处理
        final int paddingLeft=getPaddingLeft();
        final int paddingRight=getPaddingRight();
        final int paddingTop=getPaddingTop();
        final int paddingBottom=getPaddingBottom();

        //注意这里的宽高是View内容宽高，padding指的是内容和View的边缘距离
        int width=getWidth()-paddingLeft-paddingRight;
        int height=getHeight()-paddingTop-paddingBottom;
        int radius=Math.min(width,height)/2;
        //相应的，圆心也要修改
        canvas.drawCircle(paddingLeft+width/2,paddingTop+height/2,radius,mPaint);
    }
}
