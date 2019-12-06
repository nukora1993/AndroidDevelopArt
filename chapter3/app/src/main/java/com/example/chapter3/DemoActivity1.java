package com.example.chapter3;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DemoActivity1 extends AppCompatActivity {
    private static final String TAG="DemoActivity1";
    private HorizontalScrollViewEx mListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo1);
        Log.d(TAG,"onCreate");
        initView();
    }

    private void initView(){
        LayoutInflater inflater=getLayoutInflater();
        mListContainer=(HorizontalScrollViewEx)findViewById(R.id.list_container);
        final int screenWidth=MyUtils.getScreenMetrics(this).widthPixels;
        final int screenHeight=MyUtils.getScreenMetrics(this).heightPixels;

        for (int i = 0; i <3 ; i++) {
            //获得一个view
            ViewGroup layout=(ViewGroup)inflater.inflate(R.layout.content_layout,mListContainer,false);
            layout.getLayoutParams().width=screenWidth;
            TextView textView=(TextView)layout.findViewById(R.id.title);
            textView.setText("page "+(i+1));
            layout.setBackgroundColor(Color.rgb(255/(i+1),255/(i+1),0));
            createList(layout);
            mListContainer.addView(layout);
        }


    }

    private void createList(ViewGroup layout){
        ListView listView=(ListView)layout.findViewById(R.id.list);
        ArrayList<String> data=new ArrayList<String>();
        for (int i = 0; i <50 ; i++) {
            data.add("name "+i);
        }

        //注意到这里使用的默认的ArrayAdapter，所以为了告诉用哪一个控件去显示文字，使用该构造函数
        //如果是自定义的Adapter，那么不需要，所有的显示都可以自定义
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.content_list_item,R.id.name,data);
        listView.setAdapter(adapter);
    }
}
