package com.example.chapter13;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG="CrashHandlerLog";

    private static final String PATH= Environment.getExternalStorageDirectory()
            .getPath()+"/CrashTest/log";
    private static final String FILE_NAME="crash";
    private static final String FILE_NAME_SUFFIX=".trace";

    private static CrashHandler sInstance=new CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private CrashHandler(){

    }

    public static CrashHandler getInstance(){
        return sInstance;
    }

    public void init(Context context){
        mDefaultCrashHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext=context.getApplicationContext();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable ex) {
        try{
            //导出异常信息到sd卡
            dumpExceptionToSDCard(ex);
            //上传异常
            uploadExceptionToServer();
        }catch(IOException e){
            e.printStackTrace();
        }

        ex.printStackTrace();
        //如果之前系统设置了默认的handler，则还要交给之前的handler处理
        if(mDefaultCrashHandler!=null){
            mDefaultCrashHandler.uncaughtException(t,ex);
        }else{
            Process.killProcess(Process.myPid());
        }
    }

    private void dumpExceptionToSDCard(Throwable ex)throws IOException{
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.d(TAG,"sd card unmounted,skip");
            return;
        }

        File dir=new File(PATH);
        if(!dir.exists()){
            dir.mkdirs();
        }
        long current=System.currentTimeMillis();
        String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        File file=new File(PATH+FILE_NAME+time+FILE_NAME_SUFFIX);
        Log.d(TAG,file.getAbsolutePath());

        try{
            PrintWriter pw=new PrintWriter(new BufferedWriter(
                    new FileWriter(file)
            ));
            pw.println(time);
            //保存手机信息
            dumPhoneInfo(pw);
            pw.println();
            ex.printStackTrace();
            pw.close();
        }catch(Exception e){
            Log.d(TAG,"dump crash info failed");
            e.printStackTrace();
        }
    }

    private void dumPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException{
        PackageManager pm=mContext.getPackageManager();
        PackageInfo pi=pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        pw.print("App Version:");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //android版本
        pw.print("OS Version:");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        //制造商
        pw.print("Vendor:");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model:");
        pw.println(Build.MODEL);

        //CPU架构
        pw.print("CPU ABI:");
        pw.println(Build.CPU_ABI);

    }

    private void uploadExceptionToServer(){

    }



}
