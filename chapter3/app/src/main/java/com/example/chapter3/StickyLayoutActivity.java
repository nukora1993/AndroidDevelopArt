package com.example.chapter3;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chapter3.PinnedHeaderExpandableListView.expandable.PinnedHeaderExpandableListView;
import com.example.chapter3.PinnedHeaderExpandableListView.expandable.StickyLayout;

import java.util.ArrayList;
import java.util.List;

public class StickyLayoutActivity extends AppCompatActivity
implements ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        PinnedHeaderExpandableListView.OnHeaderUpdateListener,
        StickyLayout.OnGiveUpTouchEventListener {
    private PinnedHeaderExpandableListView expandableListView;
    private StickyLayout stickyLayout;
    private ArrayList<Group> groupList;
    private ArrayList<List<People>> childList;

    private MyExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticky_layout);

        expandableListView=(PinnedHeaderExpandableListView)findViewById(R.id.expandablelist);
        stickyLayout=(StickyLayout)findViewById(R.id.sticky_layout);
        initData();

        adapter=new MyExpandableListAdapter(this);
        expandableListView.setAdapter(adapter);

        //展开所有group
        for (int i = 0,count = expandableListView.getCount(); i<count ; i++) {
            expandableListView.expandGroup(i);
        }

        expandableListView.setOnHeaderUpdateListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setOnGroupClickListener(this,false);
        stickyLayout.setOnGiveUpTouchEventListener(this);
    }

    //初始化数据
    private void initData(){
        groupList=new ArrayList<Group>();
        Group group=null;
        for (int i = 0; i <3 ; i++) {
            group=new Group();
            group.setTitle("group-"+i);
            groupList.add(group);
        }

        childList=new ArrayList<List<People>>();
        for (int i = 0; i <groupList.size() ; i++) {
            ArrayList<People> childTemp;
            if(i==0){
                childTemp=new ArrayList<People>();
                for (int j = 0; j <13 ; j++) {
                    People people=new People();
                    people.setName("yy-"+j);
                    people.setAge(30);
                    people.setAddress("sh-"+j);

                    childTemp.add(people);
                }
            }else if(i==1){
                childTemp = new ArrayList<People>();
                for (int j = 0; j < 8; j++) {
                    People people = new People();
                    people.setName("ff-" + j);
                    people.setAge(40);
                    people.setAddress("sh-" + j);

                    childTemp.add(people);
                }
            }else{
                childTemp = new ArrayList<People>();
                for (int j = 0; j < 23; j++) {
                    People people = new People();
                    people.setName("hh-" + j);
                    people.setAge(20);
                    people.setAddress("sh-" + j);

                    childTemp.add(people);
                }
            }
            childList.add(childTemp);
        }
    }






    //expandablelistview的adapter
    class MyExpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private LayoutInflater inflater;

        public MyExpandableListAdapter(Context context){
            this.context=context;
            inflater=LayoutInflater.from(this.context);
        }

        //父列表个数
        @Override
        public int getGroupCount() {
            return groupList.size();
        }

        //子列表个数
        @Override
        public int getChildrenCount(int groupPosition) {
            return childList.get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childList.get(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            GroupHolder groupHolder=null;
            //是否有缓存，如果没有则创建
            if(convertView==null){
                groupHolder=new GroupHolder();
                convertView=inflater.inflate(R.layout.group,null);
                groupHolder.textView=(TextView)convertView.findViewById(R.id.group);
                groupHolder.imageView=(ImageView)convertView.findViewById(R.id.image);
                convertView.setTag(groupHolder);
            }else{
                //否则，从缓存中取出
                groupHolder=(GroupHolder)convertView.getTag();
            }

            //设置group标题
            groupHolder.textView.setText(((Group)getGroup(groupPosition)).getTitle());
            //根据列表是否展开设置对应背景
            if(isExpanded){
                groupHolder.imageView.setImageResource(R.drawable.ic_launcher_background);
            }else{
                groupHolder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ChildHolder childHolder=null;
            if(convertView==null){
                childHolder=new ChildHolder();
                convertView=inflater.inflate(R.layout.child,null);

                childHolder.textName=(TextView)convertView.findViewById(R.id.name);
                childHolder.textAge=(TextView)convertView.findViewById(R.id.age);
                childHolder.textAddress=(TextView)convertView.findViewById(R.id.address);
                childHolder.imageView=(ImageView)convertView.findViewById(R.id.image);
                Button button=(Button)convertView.findViewById(R.id.button1);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(StickyLayoutActivity.this,"clicked group pos,child pos="+groupPosition+","+childPosition,Toast.LENGTH_SHORT).show();
                    }
                });
                convertView.setTag(childHolder);
            }else {
                childHolder=(ChildHolder)convertView.getTag();
            }
            //设置name
            childHolder.textName.setText(((People)getChild(groupPosition,childPosition)).getName());
            childHolder.textAge.setText(String.valueOf(((People)getChild(groupPosition,childPosition)).getAge()));
            childHolder.textAddress.setText(((People)getChild(groupPosition,childPosition)).getAddress());
            return convertView;
        }

        //child默认可以选择
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    //implememnt OnGroupClickListener
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return false;
    }

    //implement OnChildClickListener
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Toast.makeText(StickyLayoutActivity.this,childList.get(groupPosition).get(childPosition).getName(),Toast.LENGTH_SHORT).show();
        return false;
    }

    class GroupHolder{
        TextView textView;
        ImageView imageView;
    }

    class ChildHolder{
        TextView textName;
        TextView textAge;
        TextView textAddress;
        ImageView imageView;
    }

    //implement OnHeaderUpdateListener
    //获取pinHeader
    @Override
    public View getPinnedHeader() {
        View headerView=(ViewGroup)getLayoutInflater().inflate(R.layout.group,null);
        headerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return headerView;
    }

    //更新pinHeader
    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        Group firstVisibleGroup=(Group)adapter.getGroup(
                firstVisibleGroupPos
        );

        TextView textView=(TextView)headerView.findViewById(R.id.group);
        textView.setText(firstVisibleGroup.getTitle());
    }

    //implement OnGiveUpTouchEventListener
    @Override
    public boolean giveUpTouchEvent(MotionEvent event) {
        if(expandableListView.getFirstVisiblePosition()==0){
            View view=expandableListView.getChildAt(0);
            if(view!=null&&view.getTop()>=0){
                return true;
            }
        }
        return false;
    }
}
