package com.example.chapter12;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {
    private static final String TAG="ImageLoader";
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private Context mContext;

    private static final int TAG_KEY_URI=R.id.imageloader_uri;

    private ImageResizer mImageResizer = new ImageResizer();
    //下载缓冲区大小
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX=0;
    //磁盘缓存大小
    private static final long DISK_CACHE_SIZE=1024*1024*50;
    //磁盘缓存是否被创建
    private boolean mIsDisLruCacheCreated=false;

    public static final int MESSAGE_POST_RESULT=1;

    //线程池配置
    private static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE=CPU_COUNT+1;
    private static final int MAXIMUM_POOL_SIZE=CPU_COUNT*2+1;
    private static final long KEEP_ALIVE=10L;

    private static final ThreadFactory sThreadFactory=new ThreadFactory() {
        private final AtomicInteger mCount=new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"ImageLoader#"+mCount.getAndDecrement());
        }
    };



    //创建一个和主线程绑定的handler，其handleMessage方法会在主线程被调用
    //所以加载图片这个UI操作也是在主线程被调用
    private Handler mMainHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            //获取消息附带的加载结果
            LoaderResult result=(LoaderResult)msg.obj;
            ImageView imageView=result.imageView;
            //获取imageview绑定的uri
            String uri=(String)imageView.getTag(TAG_KEY_URI);
            //如果加载的uri和imageview绑定的uri不同，那么不进行加载
            if(uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else{
                Log.d(TAG,"set image bitmap,but uri has changed,ignored");
            }
        }
    };

    //异步加载所要使用的线程池
    public static final Executor THREAD_POOL_EXECUTOR=new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            sThreadFactory
    );



    private ImageLoader(Context context){
        mContext=context.getApplicationContext();
        //最大内存
        int maxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);
        //使用缓存大小
        int cacheSize=maxMemory/8;
        mMemoryCache=new LruCache<String,Bitmap>(cacheSize){
            //计算每个缓存图片占用的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight()/1024;
            }
        };

        File diskCacheDir=getDiskCacheDir(mContext,"bitmap");
        //若文件夹不存在则创建
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        //若可用控件大于指定的缓存大小，那么说明可以创建disk缓存
        if(getUsableSpace(diskCacheDir)>DISK_CACHE_SIZE){
            try{
                mDiskLruCache=DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDisLruCacheCreated=true;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //从内存cache或者磁盘cache或者网络加载（同步加载)
    public Bitmap loadBitmap(String uri,int reqWidth,int reqHeight){
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if(bitmap!=null){
            Log.d(TAG,"loadBitmapFromMemCache,uri:"+uri);
            return bitmap;
        }

        try{
            bitmap=loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
            if(bitmap!=null){
                Log.d(TAG,"loadBitmapFromDisk,uri:"+uri);
                return bitmap;
            }
            bitmap=loadBitmapFromHttp(uri,reqWidth,reqHeight);
            Log.d(TAG,"loadBitmapFromHttp,uri:"+uri);
        }catch(IOException e){
            e.printStackTrace();
        }

        //如果没有创建磁盘缓存，那么直接从网络加载
        if(bitmap==null&&!mIsDisLruCacheCreated){
            Log.d(TAG,"encounter error,disk lru cached is not created");
            bitmap=downloadBitmapFromUrl(uri);
        }
        return bitmap;
    }

    //异步加载
    public void bindBitmap(final String uri, final ImageView imageView,
                           final int reqWidth,final int reqHeight){
        imageView.setTag(TAG_KEY_URI,uri);
        //尝试从内存加载
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask=new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=loadBitmap(uri,reqWidth,reqHeight);
                if(bitmap!=null){
                    LoaderResult result=new LoaderResult(imageView,uri,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };

        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);

    }

    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    //只下载，不缓存，因为这里没有diskcache才执行
    private Bitmap downloadBitmapFromUrl(String urlString){
        Bitmap bitmap=null;
        HttpURLConnection urlConnection=null;
        BufferedInputStream in=null;

        try{
            final URL url=new URL(urlString);
            urlConnection=(HttpURLConnection)url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap= BitmapFactory.decodeStream(in);
        }catch(final Exception e){
            Log.d(TAG,"error in downloadBitmap:"+e);
        }finally {
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
            MyUtils.close(in);
        }
        return bitmap;
    }

    private Bitmap loadBitmapFromMemCache(String url){
        final String key=hashKeyFromUrl(url);
        Bitmap bitmap=getBitmapFromCache(key);
        return bitmap;
    }


    //添加内存缓存，如果不存在则添加
    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromCache(key)==null){
            mMemoryCache.put(key,bitmap);
        }
    }

    //从内存缓存获取
    private Bitmap getBitmapFromCache(String key){
        return mMemoryCache.get(key);
    }

    //获得磁盘cache的dir
    public File getDiskCacheDir(Context context,String uniqueName){
        //磁盘是否可用
        boolean externalStorageAvailable= Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        //一个是外部磁盘cache，一个应用安装目录的cache
        if(externalStorageAvailable){
            cachePath=context.getExternalCacheDir().getPath();
        }else {
            cachePath=context.getCacheDir().getPath();
        }
        return new File(cachePath+File.separator+uniqueName);
    }

    //从网络加载
    private Bitmap loadBitmapFromHttp(String url,int reqWidth,int reqHeight) throws IOException{
        //判断当前线程绑定的looper是否是主线程的looper
        //如果是，则说明该代码会在主线程执行，而主线程无法访问网络
        if(Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread");
        }
        //如果没有开启磁盘缓存，那么直接返回
        if(mDiskLruCache==null){
            return null;
        }

        //使用url获得hash值作为key
        String key=hashKeyFromUrl(url);
        //写入磁盘缓存
        DiskLruCache.Editor editor=mDiskLruCache.edit(key);
        if(editor!=null){
            OutputStream outputStream=editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(url,outputStream)){
                editor.commit();
            }else{
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url,int reqWidth,int reqHeight)throws IOException{
        //加载属于耗时操作，不推荐在主线程执行
        if(Looper.myLooper()==Looper.getMainLooper()){
            Log.d(TAG,"load bitmap from UI Thread,not recommend");
        }
        if(mDiskLruCache==null){
            return null;
        }

        Bitmap bitmap=null;
        String key=hashKeyFromUrl(url);
        //使用SnapShot完成磁盘缓存加载
        DiskLruCache.Snapshot snapShot=mDiskLruCache.get(key);
        if(snapShot!=null){
            FileInputStream fileInputStream=(FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor=fileInputStream.getFD();
            bitmap=mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
            if(bitmap!=null){
                addBitmapToMemoryCache(key,bitmap);
            }

        }
        return bitmap;
    }

    //实际的下载函数
    public boolean downloadUrlToStream(String urlString,OutputStream outputStream){
        HttpURLConnection urlConnection=null;
        BufferedOutputStream out=null;
        BufferedInputStream in=null;

        try{
            final URL url=new URL(urlString);
            urlConnection=(HttpURLConnection)url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            out=new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);
            int b;
            //注意in.read返回的是int，但是写入的仍然是byte，这是防止读取到的byte是-1，而-1是流末尾的标志
            while((b=in.read())!=-1){
                out.write(b);
            }
            return true;
        }catch(IOException e){
            Log.d(TAG,"downloadBitmap Failed"+e);
        }finally {
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
            MyUtils.close(out);
            MyUtils.close(in);
        }
        return false;
    }

    //获得目录可用空间大小，分版本判断
    private long getUsableSpace(File path){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs statFs=new StatFs(path.getPath());
        return (long)statFs.getBlockSize()*(long)statFs.getAvailableBlocks();
    }

    private String hashKeyFromUrl(String url){
        String cacheKey;
        try{
            final MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey=bytesToHexString(messageDigest.digest());
        }catch(NoSuchAlgorithmException e){
            cacheKey=String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes){
        StringBuilder sb=new StringBuilder();
        //将byte转换为16进制字符串
        for (int i = 0; i <bytes.length ; i++) {
            String hex=Integer.toHexString(0XFF&bytes[i]);
            if(hex.length()==1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    //用于异步加载的结果保存
    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView,String uri,Bitmap bitmap){
            this.imageView=imageView;
            this.uri=uri;
            this.bitmap=bitmap;
        }
    }




}
