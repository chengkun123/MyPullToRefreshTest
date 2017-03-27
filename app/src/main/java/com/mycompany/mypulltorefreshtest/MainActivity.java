package com.mycompany.mypulltorefreshtest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    private MyPullToRefreshView mMyPullToRefreshView;
    private ListView mListView;
    ArrayAdapter<String> adapter;
    String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "I", "J", "K", "L" };
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mMyPullToRefreshView = (MyPullToRefreshView)findViewById(R.id.refreshable_view);
        mListView = (ListView)findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mListView.setAdapter(adapter);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //setTitle("进行了刷新！");
                //items[0] = "";
                Toast.makeText(MainActivity.this, "进行了刷新", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                //更新完之后回滚,注意这里伴随着AsyncTask的初始化，必须在主线程中调用
                mMyPullToRefreshView.finishRefreshing();
            }
        };
        mMyPullToRefreshView.setOnRefreshListener(new MyPullToRefreshView.PullToRefreshListener() {
            @Override

            public void onRefresh() {
                //形成延迟
                Message msg = Message.obtain();
                mHandler.sendMessageDelayed(msg, 3000);
            }
        });
    }
}
