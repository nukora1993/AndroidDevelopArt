package com.example.chapter7;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivityLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView helloWorld=findViewById(R.id.hello_world);
        //加载xml定义的动画并应用
//        Animation animation= AnimationUtils.loadAnimation(this,R.anim.animation_test);
//        helloWorld.startAnimation(animation);
//
//        //代码代码定义动画并应用
//        AlphaAnimation alphaAnimation=new AlphaAnimation(0,1);
//
//        alphaAnimation.setDuration(5000);
//        helloWorld.startAnimation(alphaAnimation);
//
//        //连续多次设置动画时，只有最后一次动画是生效的
//        Rotate3dAnimation rotate3dAnimation=new Rotate3dAnimation(0,90,0,0,0,false);
//        rotate3dAnimation.setDuration(5000);
//        helloWorld.startAnimation(rotate3dAnimation);
//
//        //帧动画(帧动画可以和View动画一起使用，因为其本质是改变background)
//        helloWorld.setBackgroundResource(R.drawable.frame_animation);
//        AnimationDrawable drawable=(AnimationDrawable)helloWorld.getBackground();
//        drawable.start();

        ObjectAnimator.ofFloat(helloWorld,"translationX",helloWorld.getHeight()).setDuration(5000).start();


        //代码中指定LayoutAnimation
//        ListView listView=(ListView)findViewById(R.id.list);
//        animation=AnimationUtils.loadAnimation(this,R.anim.anim_item);
//        LayoutAnimationController controller=new LayoutAnimationController(animation);
//        controller.setDelay(0.5f);
//        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
//        listView.setLayoutAnimation(controller);

        //设置Activity的切换效果,必须在startActivity之后被调用
//        Intent intent=new Intent(this,SecondActivity.class);
        //注意startActivity并不是立刻执行的
//        startActivity(intent);
//        overridePendingTransition(R.anim.animation_test,R.anim.anim_item);
        Log.d(TAG,"onCreateFinish");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }
}
