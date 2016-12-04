package com.mycompany.mypulltorefreshtest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Lenovo on 2016/11/30.
 */
public class MyPullToRefreshView extends LinearLayout implements View.OnTouchListener{

    //需要控制的View
    private View mHeader;
    private int invisibleHeight;
    private MarginLayoutParams mHeaderLayoutParams;
    private ListView mListView;
    private ProgressBar mHeaderProgressbar;
    private TextView mHeaderTextView;
    private boolean isLoaded;

    //滑动相关
    private boolean ableToPull;
    private float yDown;
    private float yMove;
    private static final int SCROLL_SPEED = -30;
    private int touchSlop;

    //和header显示有关的四个状态
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;
    private int mLastStatus = mCurrentStatus;
    public static final int STATUS_PULL_TO_REFRESH = 0;
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    public static final int STATUS_REFRESHING = 2;
    public static final int STATUS_REFRESH_FINISHED = 3;

    //可以刷新时回调的接口
    private PullToRefreshListener mListener;



    public MyPullToRefreshView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        //由布局动态获取header
        mHeader = LayoutInflater.from(context).inflate(R.layout.header, null, true);
        mHeaderProgressbar = (ProgressBar)mHeader.findViewById(R.id.header_progressbar);
        mHeaderTextView = (TextView)mHeader.findViewById(R.id.header_textview);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        //给该组合View设置方向
        setOrientation(VERTICAL);
        //添加header到该组合View中
        addView(mHeader, 0);
    }

    /*
    *
    * 作为一个继承LinearLayout的组合View，在此方法中需要管理子View的布局。
    *
    * */
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        //只布局一次
        if(changed && !isLoaded){
            //将header移出屏幕（toolbar的下界上面）
            invisibleHeight = - mHeader.getHeight();
            mHeaderLayoutParams = (MarginLayoutParams)mHeader.getLayoutParams();
            mHeaderLayoutParams.topMargin = invisibleHeight;
            //mHeader.setLayoutParams(mHeaderLayoutParams);

            mListView = (ListView)getChildAt(1);
            //给ListView注册监听者，这个监听者是这个组合View
            mListView.setOnTouchListener(this);
            isLoaded = true;
        }
    }

    /**
     *
     * 当ListView被触摸时调用
     *
     * */
    public boolean onTouch(View view, MotionEvent event){
        //判断是否能下拉
        judgeIfAbleToPull(event);
        //如果能下拉
        if (ableToPull){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yMove = event.getRawY();
                    int distance = (int) (yMove - yDown);
                    if(distance <= 0 && mHeaderLayoutParams.topMargin <= invisibleHeight)
                        return false;
                    if (distance < touchSlop)
                        return false;

                    if(mCurrentStatus != STATUS_REFRESHING){
                        //实时滚动
                        mHeaderLayoutParams.topMargin = (distance / 2) + invisibleHeight;
                        mHeader.setLayoutParams(mHeaderLayoutParams);

                        //根据滚动的距离实时更改状态。
                        if(mHeaderLayoutParams.topMargin > 0){
                            mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
                        }else{
                            mCurrentStatus = STATUS_PULL_TO_REFRESH;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
                        new RefreshingTask().execute();
                    }else if (mCurrentStatus == STATUS_PULL_TO_REFRESH){
                        mCurrentStatus = STATUS_REFRESH_FINISHED;
                        new HideHeaderTask().execute();
                    }
                    break;
            }
            //在滑动中或释放后实时更新header
            if(mCurrentStatus == STATUS_PULL_TO_REFRESH
                    || mCurrentStatus == STATUS_RELEASE_TO_REFRESH){

                updateHeaderView();
                mLastStatus = mCurrentStatus;

                mListView.setPressed(false);
                mListView.setFocusable(false);
                mListView.setFocusableInTouchMode(false);
                //当正在下拉时。消费手势，屏蔽掉Listview的点击事件
                return true;
            }
        }
        return false;
    }

    class RefreshingTask extends AsyncTask<Void, Integer, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            int topMargin = mHeaderLayoutParams.topMargin;
            while(true){
                topMargin = topMargin + SCROLL_SPEED;
                if(topMargin <= 0){
                    topMargin = 0;
                    break;
                }
                publishProgress(topMargin);
                try{
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            return topMargin;
        }

        protected void onProgressUpdate(Integer... topMargin){
            //回滚的时候进度条就显示
            mCurrentStatus = STATUS_REFRESHING;
            updateHeaderView();
            mLastStatus = mCurrentStatus;
            mHeaderLayoutParams.topMargin = topMargin[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }

        protected void onPostExecute(Integer topMargin){
            mHeaderLayoutParams.topMargin = topMargin;
            mHeader.setLayoutParams(mHeaderLayoutParams);
            //回滚结束，开始在主线程中更新
            if(mListener != null){
                mListener.onRefresh();
            }
        }

    }

    class HideHeaderTask extends AsyncTask<Void, Integer, Integer>{
        protected Integer doInBackground(Void... params){
            int topMargin = mHeaderLayoutParams.topMargin;
            while(true){
                topMargin = topMargin + SCROLL_SPEED;
                if(topMargin <= invisibleHeight){
                    topMargin = invisibleHeight;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return topMargin;
        }
        protected void onProgressUpdate(Integer... topMargin){
            mCurrentStatus = STATUS_REFRESH_FINISHED;
            updateHeaderView();
            mLastStatus = mCurrentStatus;
            mHeaderLayoutParams.topMargin = topMargin[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }

        protected void onPostExecute(Integer topMargin){
            mHeaderLayoutParams.topMargin = topMargin;
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }
    }

    private void judgeIfAbleToPull(MotionEvent event){
        View firstChild = mListView.getChildAt(0);
        if(firstChild != null){
            int firstVisiblePos = mListView.getFirstVisiblePosition();
            if(firstVisiblePos == 0 && firstChild.getTop() == 0){
                //这个地方十分重要，在不能下滑拉出header的时候，
                //我们时刻更新手指的y坐标，直至能下滑的时的点当作下拉的起始点。
                if(!ableToPull){
                    yDown = event.getRawY();
                }
                ableToPull = true;
            }else{
                if(mHeaderLayoutParams.topMargin != invisibleHeight){
                    mHeaderLayoutParams.topMargin = invisibleHeight;
                    mHeader.setLayoutParams(mHeaderLayoutParams);
                }
                ableToPull = false;
            }
        }else{
            ableToPull = true;
        }
    }

    private void updateHeaderView(){
        if (mLastStatus != mCurrentStatus){
            if (mCurrentStatus == STATUS_REFRESHING) {
                mHeaderProgressbar.setVisibility(View.VISIBLE);
                mHeaderTextView.setText("");
            }else if(mCurrentStatus == STATUS_PULL_TO_REFRESH){
                mHeaderTextView.setText("继续下拉以刷新");
                mHeaderProgressbar.setVisibility(View.GONE);
            }else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
                mHeaderTextView.setText("释放即可刷新");
                mHeaderProgressbar.setVisibility(View.GONE);
            }else if(mCurrentStatus == STATUS_REFRESH_FINISHED){
                mHeaderTextView.setText("");
                mHeaderProgressbar.setVisibility(View.GONE);
            }
        }
    }
    public interface PullToRefreshListener {

        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
         */
        void onRefresh();

    }
    public void setOnRefreshListener(PullToRefreshListener listener){
        mListener = listener;
    }

    public void finishRefreshing(){

        new HideHeaderTask().execute();
    }
}
