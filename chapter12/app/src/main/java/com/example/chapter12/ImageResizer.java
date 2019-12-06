package com.example.chapter12;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

public class ImageResizer {
    private static final String TAG="ImageResizer";

    public ImageResizer(){}

    //从资源中采样bitmap到指定大小
    public Bitmap decodeSampledBitmapFromResource(
            Resources res,int resId,int reqWidth,int reqHeight
    ){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        //为了加载宽高,不会实际加载bitmap
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeResource(res,resId,options);

        options.inSampleSize=calculateInSampleSize(options,reqWidth,reqHeight);

        options.inJustDecodeBounds=false;
        //使用计算好的采用率实际加载图片,注意如果decodeResource加载的是xml，那么会返回null
        return BitmapFactory.decodeResource(res,resId,options);
    }

    //从文件中加载并采样
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd,
                                                        int reqWidth,int reqHeight){
        final BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize=calculateInSampleSize(options,reqWidth,reqHeight);

        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth,int reqHeight){
        //参数不和法返回原始大小
        if(reqWidth==0||reqHeight==0){
            return 1;
        }

        final int height= options.outHeight;
        final int width=options.outWidth;
        Log.d(TAG,"origin,w,h="+width+","+height);
        int inSampleSize=1;

        //如果原始值大于要求值，首先将要求值减半，否则返回原始值
        if(height>reqHeight||width>reqWidth){

            final int halfHeight=height/2;
            final int halfWidth=width/2;
            //这里判断采样过后是否都超过要求至，如果超过则inSample*2被
            //但是上面首先除了了，那么inSampleSize不应该初始值为2么？
            while((halfHeight/inSampleSize)>=reqHeight&&(halfWidth/inSampleSize)>reqWidth){
                inSampleSize*=2;
            }
        }

        Log.d(TAG,"sampleSize:"+inSampleSize);
        return inSampleSize;
    }
}
