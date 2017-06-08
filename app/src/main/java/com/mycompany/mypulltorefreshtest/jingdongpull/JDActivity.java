package com.mycompany.mypulltorefreshtest.jingdongpull;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mycompany.mypulltorefreshtest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDActivity extends AppCompatActivity {

    private ListView mListView;
    private List<String> mDatas;
    private ArrayAdapter<String> mAdapter;
    private Handler mHandler;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jd);
        mListView = (ListView) findViewById(R.id.jd_list_view);
        String[] data = new String[]{"hello world","hello world","hello world","hello world",
                "hello world","hello world","hello world","hello world","hello world",
                "hello world","hello world","hello world","hello world","hello world",};
        mDatas = new ArrayList<String>(Arrays.asList(data));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,mDatas);
        mListView.setAdapter(mAdapter);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.jd_pull_to_refresh_layout);
        mPullToRefreshLayout.setAfterPullListener(new com.mycompany.mypulltorefreshtest.meituanpull.PullToRefreshLayout.AfterPullListener() {
            @Override
            public void onRefresh() {
                Message message = Message.obtain();
                //延迟3秒
                mHandler.sendMessageDelayed(message, 3000);

            }
        });

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //setTitle("进行了刷新！");
                //items[0] = "";
                //更新完之后回滚,注意这里伴随着AsyncTask的初始化，必须在主线程中调用
                mPullToRefreshLayout.finishRefreshing();
            }
        };

    }
}
