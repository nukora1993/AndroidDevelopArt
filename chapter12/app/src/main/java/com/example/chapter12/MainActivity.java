package com.example.chapter12;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {
    private static final String TAG="MainActivityLog";

    private List<String> mUrList=new ArrayList<>();
    private boolean mIsGridViewIdle=true;
    private int mImageWidth=0;
    private boolean mIsWifi=false;
    private ImageLoader mImageLoader;
    private boolean mCanGetBitmapFromNetWork=false;

    private GridView mImageGridView;
    private BaseAdapter mImageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"main thread running at:"+Thread.currentThread().getName());

//        ImageResizer imageResizer=new ImageResizer();
//        ImageView imageView=(ImageView)findViewById(R.id.image_view);
//        imageView.setImageBitmap(imageResizer.decodeSampledBitmapFromResource(
//                getResources(),R.drawable.image_default,100,200
//        ));

        initData();
        initView();
        mImageLoader=ImageLoader.build(this);




    }

    //初始化数据
    private void initData(){
        String[] imageUrls = {
                "http://e.hiphotos.baidu.com/image/h%3D300/sign=a9e671b9a551f3dedcb2bf64a4eff0ec/4610b912c8fcc3cef70d70409845d688d53f20f7.jpg",
                "http://e.hiphotos.baidu.com/image/h%3D300/sign=907f6e689ddda144c5096ab282b6d009/dc54564e9258d1092f7663c9db58ccbf6c814d30.jpg"
        };

        for(String url:imageUrls){
            mUrList.add(url);
        }

        int screenWidth=MyUtils.getScreenMetrics(this).widthPixels;
        //20dp转换为像素
        int space=(int)MyUtils.dp2px(this,20f);
        //每张图的大小，和屏幕边缘保持一定的距离
        mImageWidth=(screenWidth-space)/3;
        mIsWifi=MyUtils.isWifi(this);
        if(mIsWifi){
            mCanGetBitmapFromNetWork=true;
        }

    }

    private void initView(){
        mImageGridView=(GridView)findViewById(R.id.gridView1);
        mImageAdapter=new ImageAdapter(this);
        mImageGridView.setAdapter(mImageAdapter);
        mImageGridView.setOnScrollListener(this);

        if(!mIsWifi){
            AlertDialog.Builder builder=new AlertDialog.Builder(this)
                    .setMessage("初次使用会从网络下载图片，确认要下载吗？")
                    .setTitle("注意")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCanGetBitmapFromNetWork=true;
                            //通知gridview去重新加载
                            mImageAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("NO",null);
            //这里的逻辑感觉有问题，因为即使不能访问网络，也应该尝试从磁盘缓存加载（微博就是这样，应该有缓存有效期的设置）
            builder.show();
        }
    }



    //为gridView设置Adapter
    private class ImageAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        private Drawable mDefaultBitmapDrawable;

        private ImageAdapter(Context context){
            mInflater=LayoutInflater.from(context);
            mDefaultBitmapDrawable=context.getResources().getDrawable(R.drawable.image_default);
        }

        @Override
        public int getCount() {
            return mUrList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUrList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null){
                convertView=mInflater.inflate(R.layout.image_list_item,parent,false);
                holder=new ViewHolder();
                holder.imageView=(ImageView)convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            }else{
                holder=(ViewHolder)convertView.getTag();
            }

            ImageView imageView=holder.imageView;
            final String tag=(String)imageView.getTag();
            final String uri=(String)getItem(position);
            //获得imageview绑定的uri，如果uri和给定的uri不相同，那么显示默认图片
            if(!uri.equals(tag)){
                imageView.setImageDrawable(mDefaultBitmapDrawable);
            }
            //为了优化体验，只有在滑动停止的时候采取异步加载图片
            if(mIsGridViewIdle&&mCanGetBitmapFromNetWork){
                imageView.setTag(uri);
                //核心代码，通过缓存加载图片
                mImageLoader.bindBitmap(uri,imageView,mImageWidth,mImageWidth);
            }
            return convertView;
        }
    }

    private static class ViewHolder{
        public ImageView imageView;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //SCROLL_STATE_IDLE表示滚动停止
        if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            mIsGridViewIdle=true;
            mImageAdapter.notifyDataSetChanged();
        }else{
            mIsGridViewIdle=false;
        }
    }
}
