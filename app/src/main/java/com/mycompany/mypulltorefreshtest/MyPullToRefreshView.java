package com.mycompany.mypulltorefreshtest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


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


    /*
    * 初始化header并添加到LinearLayout中
    *
    * */
    public MyPullToRefreshView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        //获取header
        mHeader = LayoutInflater.from(context).inflate(R.layout.header, null, true);
        mHeaderProgressbar = (ProgressBar)mHeader.findViewById(R.id.header_progressbar);
        mHeaderTextView = (TextView)mHeader.findViewById(R.id.header_textview);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //设置方向
        setOrientation(VERTICAL);
        //添加到第0个位置中
        addView(mHeader, 0);
    }

    /*
    *
    * 由于header是动态添加，这里重新布局
    *
    * */
    protected void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        //只布局一次
        if(changed && !isLoaded){
            //将header移到整个ViewGroup的上面
            invisibleHeight = - mHeader.getMeasuredHeight();
            mHeaderLayoutParams = (MarginLayoutParams)mHeader.getLayoutParams();
            mHeaderLayoutParams.topMargin = invisibleHeight;
            //mHeader.setLayoutParams(mHeaderLayoutParams);
            mListView = (ListView)getChildAt(1);
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
        //如果在刷新，禁止一切手势
        if(mCurrentStatus == STATUS_REFRESHING){
            return true;
        }
        //每次触摸都要判断，当已经在下拉的时候一定返回true
        judgeIfAbleToPull(event);
        //如果能下拉
        if (ableToPull){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //获取移动的差值
                    yMove = event.getRawY();
                    int distance = (int) (yMove - yDown);

                    //如果在能下拉的时候向上滑动了或者向上滑动到了原位
                    if(distance <= 0 && mHeaderLayoutParams.topMargin <= invisibleHeight) {
                        mCurrentStatus = STATUS_REFRESH_FINISHED;
                        return false;
                    }
                    if (distance < touchSlop){
                        mCurrentStatus = STATUS_REFRESH_FINISHED;
                        return false;
                    }

                    if(mCurrentStatus != STATUS_REFRESHING){
                        //移动header
                        mHeaderLayoutParams.topMargin = invisibleHeight + (distance / 2);
                        mHeader.setLayoutParams(mHeaderLayoutParams);

                        //“继续下拉以刷新”和“释放即可刷新的分界点”，改变状态
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
            //在完成手势相应的动作后，如果当前状态处于这两个状态，可能伴随着header的更新。
            if(mCurrentStatus == STATUS_PULL_TO_REFRESH
                    || mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
                //检查更新
                updateHeaderView();
                mLastStatus = mCurrentStatus;

                mListView.setPressed(false);
                mListView.setFocusable(false);
                mListView.setFocusableInTouchMode(false);
                //当正在下拉时，消费手势，屏蔽掉Listview的点击事件。
                return true;
            }
        }
        //如果不能下拉，该ViewGroup不拦截Touch事件，Touch事件交给ListView处理
        mCurrentStatus = STATUS_REFRESH_FINISHED;
        return false;
    }



    /*
    *
    * 如果Up的时候是STATUS_RELEASE_TO_REFRESH，那么回滚到header上边界与ViewGroup上边界重合。
    * */
    class RefreshingTask extends AsyncTask<Void, Integer, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            int topMargin = mHeaderLayoutParams.topMargin;
            while(true){
                topMargin = topMargin + SCROLL_SPEED;
                //回滚完成
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

            /*//回滚结束，在子线程中回调更新函数
            publishProgress(topMargin);
            if(mListener != null){
                mListener.onRefresh();
            }*/
            return topMargin;
        }

        protected void onProgressUpdate(Integer... topMargin){
            //回滚的时候progressbar显示
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
            /*mCurrentStatus = STATUS_REFRESH_FINISHED;
            updateHeaderView();
            mLastStatus = mCurrentStatus;*/
            mHeaderLayoutParams.topMargin = topMargin[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }

        protected void onPostExecute(Integer topMargin){
            mHeaderLayoutParams.topMargin = topMargin;
            mHeader.setLayoutParams(mHeaderLayoutParams);
            mCurrentStatus = STATUS_REFRESH_FINISHED;
            updateHeaderView();
            mLastStatus = mCurrentStatus;
        }
    }

    private void judgeIfAbleToPull(MotionEvent event){
        //根据ListView的源码，这里是获得显示在ListView上的第一个子View
        View firstChild = mListView.getChildAt(0);
        if(firstChild != null){
            //获得ListView中第一个显示出的条目在所有数据中的位置
            int firstVisiblePos = mListView.getFirstVisiblePosition();
            //如果是第0个条目而且第一个显示的条目的上边界和ListView的上边界重合
            if(firstVisiblePos == 0 && firstChild.getTop() == 0){
                //在由不能到能下滑的时刻，记录下滑点的位置
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
        //当上一个状态不等于现在的状态时，会进行header状态的更新
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
         * 这个回调在主线程中进行，所以要另开线程进行耗时操作
         *
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
