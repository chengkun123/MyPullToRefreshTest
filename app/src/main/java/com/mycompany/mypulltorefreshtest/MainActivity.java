package com.mycompany.mypulltorefreshtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {


    private MyPullToRefreshView mMyPullToRefreshView;
    private ListView mListView;
    ArrayAdapter<String> adapter;
    String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "I", "J", "K", "L" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mMyPullToRefreshView = (MyPullToRefreshView)findViewById(R.id.refreshable_view);
        mListView = (ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mListView.setAdapter(adapter);
        mMyPullToRefreshView.setOnRefreshListener(new MyPullToRefreshView.PullToRefreshListener() {
            @Override
            //其实这个接口可以在子线程中调用，因为刷新数据的动作一般也是在子线程中进行最后才在主线程中更新UI
            public void onRefresh() {
                setTitle("进行了刷新！");
                items[0] = "";
                adapter.notifyDataSetChanged();
                //更新完之后回滚
                mMyPullToRefreshView.finishRefreshing();
            }
        });
    }
}
