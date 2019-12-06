package com.example.chapter12;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

//自定义的ImageView，但是完全没有新加任何东西
public class SquareImageView extends AppCompatImageView {
    private static final String TAG="SquareImageViewLog";
    public SquareImageView(Context context){
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public SquareImageView(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
    }

    //唯一改变的就是将onMeasure方法的宽高都设置为一样
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        Log.d(TAG,"measure thread running at:"+Thread.currentThread().getName());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG,"layout thread running at:"+Thread.currentThread().getName());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG,"draw thread running at:"+Thread.currentThread().getName());
    }
}
