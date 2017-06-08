package com.mycompany.mypulltorefreshtest.jingdongpull;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mycompany.mypulltorefreshtest.R;

/**
 * Created by Lenovo on 2017/6/5.
 */

public class PullToRefreshLayout extends LinearLayout implements View.OnTouchListener{
    //header相关
    private View mHeader;
    private int invisibleHeight;
    private MarginLayoutParams mHeaderLayoutParams;
    private ListView mListView;
    private View mFirstStepView;
    private View mSecondStepView;
    private AnimationDrawable mSecondAnimIn;
    private AnimationDrawable mSecondAnimOut;
    private View mThirdStepView;
    private AnimationDrawable mThirdAnim;
    private TextView mHeaderText;


    //滑动相关
    private boolean ableToPull;
    private float yDown;
    private float yMove;
    private static final int SCROLL_SPEED = -30;
    private int touchSlop;
    private boolean isAutoRoll;

    //和header显示有关的四个状态
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;
    private int mLastStatus = mCurrentStatus;
    public static final int STATUS_PULL_TO_REFRESH = 0;
    public static final int STATUS_RELEASE_TO_REFRESH = 1;
    public static final int STATUS_REFRESHING = 2;
    public static final int STATUS_REFRESH_FINISHED = 3;


    private boolean isLoaded;
    private com.mycompany.mypulltorefreshtest.meituanpull.PullToRefreshLayout.AfterPullListener mListener;

    public PullToRefreshLayout(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        //得到header相关
        mHeader = LayoutInflater
                .from(context)
                .inflate(R.layout.header_jing_dong, this, false);
        mFirstStepView = mHeader.findViewById(R.id.first_step_view);
        mSecondStepView = mHeader.findViewById(R.id.second_step_view);
        mHeaderText = (TextView) mHeader.findViewById(R.id.jd_tv_pull_to_refresh);

        //背景设置
        mSecondStepView.setBackgroundResource(R.drawable.jd_pull_to_refresh_second_anim);
        mSecondAnimIn = (AnimationDrawable) mSecondStepView.getBackground();

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        addView(mHeader, 0);

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed && !isLoaded){
            //将header移出可见范围
            invisibleHeight = - mHeader.getMeasuredHeight();
            mHeaderLayoutParams = (MarginLayoutParams) mHeader.getLayoutParams();
            mHeaderLayoutParams.topMargin = invisibleHeight;
            mListView = (ListView) getChildAt(1);
            mListView.setOnTouchListener(this);
            isLoaded = true;
        }

    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //不做任何动作返回
        if(mCurrentStatus == STATUS_REFRESHING || isAutoRoll){
            return true;
        }

        if(isAbleToPull(event)){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    yMove = event.getRawY();
                    int distance = (int)(yMove - yDown);

                    //如果在刚好能下拉的时候向上滑动了或者向上滑动到了原位
                    if(distance <= 0 && mHeaderLayoutParams.topMargin <= invisibleHeight) {
                        mCurrentStatus = STATUS_REFRESH_FINISHED;
                        return false;
                    }
                    //如果小于touchslop不滑动
                    if (distance < touchSlop){
                        mCurrentStatus = STATUS_REFRESH_FINISHED;
                        return false;
                    }

                    //只要不是refreshing状态，在move的时候就产生滑动
                    if(mCurrentStatus != STATUS_REFRESHING){
                        //通过LayoutParams滑动
                        mHeaderLayoutParams.topMargin = invisibleHeight + (distance / 2);
                        mHeader.setLayoutParams(mHeaderLayoutParams);

                        //可能改变状态
                        if(mHeaderLayoutParams.topMargin > 0){
                            mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
                        }else{
                            mCurrentStatus = STATUS_PULL_TO_REFRESH;
                        }

                        //在STATUS_PULL_TO_REFRESH下还要改变FirstView的大小。
                        if(mCurrentStatus == STATUS_PULL_TO_REFRESH){
                            float progress = ((float)distance/2) / (float)(-invisibleHeight);
                            if(progress > 1){
                                progress = 1;
                            }
                            ((JDFirstStepView)mFirstStepView).setProgress(progress);
                            mFirstStepView.postInvalidate();
                        }

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //如果在release_to_refresh，则先回滚到header完全显示，然后刷新
                    if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH){
                        new RefreshingTask().execute();
                    }else{//如果在pull_to_refresh，则回滚到finished
                        isAutoRoll = true;
                        new RollToFinishedTask().execute();
                    }
                    break;
            }

            //有可能发生改变
            if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH ||
                    mCurrentStatus == STATUS_PULL_TO_REFRESH){
                updateHeader();
                mLastStatus = mCurrentStatus;

                mListView.setPressed(false);
                mListView.setFocusable(false);
                mListView.setFocusableInTouchMode(false);
                return true;
            }
        }

        mCurrentStatus = STATUS_REFRESH_FINISHED;
        return false;
    }

    private void updateHeader(){
        if(mCurrentStatus != mLastStatus){
            switch (mCurrentStatus){
                case STATUS_REFRESH_FINISHED:
                    mFirstStepView.setVisibility(GONE);
                    mSecondStepView.setVisibility(GONE);
                    mSecondAnimIn.stop();
                    mHeaderText.setText("");
                case STATUS_PULL_TO_REFRESH:
                    mFirstStepView.setVisibility(VISIBLE);
                    mSecondStepView.setVisibility(GONE);
                    mSecondAnimIn.stop();
                    mHeaderText.setText("继续下拉以刷新");
                    break;
                case STATUS_RELEASE_TO_REFRESH:
                    mFirstStepView.setVisibility(VISIBLE);
                    mSecondStepView.setVisibility(GONE);
                    mSecondAnimIn.stop();
                    mHeaderText.setText("释放以刷新");
                    break;
                case STATUS_REFRESHING:
                    mFirstStepView.setVisibility(GONE);
                    mSecondStepView.setVisibility(VISIBLE);
                    mSecondAnimIn.start();
                    mHeaderText.setText("正在刷新");
                    break;
            }
        }
    }


    private boolean isAbleToPull(MotionEvent event){
        View firstChild = mListView.getChildAt(0);

        if(firstChild != null){
            int firstVisiblePos = mListView.getFirstVisiblePosition();
            if(firstVisiblePos == 0 && firstChild.getTop() == 0){
                //yDown只能赋值一次
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
            ableToPull = false;
        }
        Log.e("能否滑动",ableToPull+"");
        return ableToPull;
    }

    class RefreshingTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            int top = mHeaderLayoutParams.topMargin;
            while(top > 0){
                top = top + SCROLL_SPEED;
                if(top <= 0){
                    top = 0;
                    break;
                }
                publishProgress(top);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return top;
        }


        protected void onProgressUpdate(Integer... top){
            mHeaderLayoutParams.topMargin = top[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
        }

        protected void onPostExecute(Integer top){
            mHeaderLayoutParams.topMargin = top;
            mHeader.setLayoutParams(mHeaderLayoutParams);
            mCurrentStatus = STATUS_REFRESHING;
            updateHeader();
            mLastStatus = mCurrentStatus;
            if(mListener != null){
                mListener.onRefresh();
            }
        }

    }

    class RollToFinishedTask extends AsyncTask<Void, Integer, Integer>{
        @Override
        protected Integer doInBackground(Void... params) {
            int top = mHeaderLayoutParams.topMargin;
            while(true){
                top += SCROLL_SPEED;
                if(top < invisibleHeight){
                    top = invisibleHeight;
                    break;
                }
                publishProgress(top);

                //让主线程运行
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }

            return top;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            //滑动
            mHeaderLayoutParams.topMargin = values[0];
            mHeader.setLayoutParams(mHeaderLayoutParams);
            //改变图像大小
            float cProgress = 1 - ((float)values[0] / (float)invisibleHeight);
            ((JDFirstStepView)mFirstStepView).setProgress(cProgress);
            mFirstStepView.postInvalidate();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mHeaderLayoutParams.topMargin = integer;
            mHeader.setLayoutParams(mHeaderLayoutParams);
            mCurrentStatus = STATUS_REFRESH_FINISHED;
            updateHeader();
            mLastStatus = mCurrentStatus;
            isAutoRoll = false;
        }
    }


    public void setAfterPullListener(com.mycompany.mypulltorefreshtest.meituanpull.PullToRefreshLayout.AfterPullListener listener){
        mListener = listener;
    }


    //从freshing到finished
    public void finishRefreshing(){
        mCurrentStatus = STATUS_PULL_TO_REFRESH;
        updateHeader();
        mLastStatus = mCurrentStatus;
        isAutoRoll = true;
        new RollToFinishedTask().execute();
    }

}

