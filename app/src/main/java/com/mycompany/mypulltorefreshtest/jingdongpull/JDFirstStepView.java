package com.mycompany.mypulltorefreshtest.jingdongpull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mycompany.mypulltorefreshtest.R;

/**
 * Created by Lenovo on 2017/6/5.
 */

public class JDFirstStepView extends View {
    private Bitmap mGoods;
    private Bitmap mPerson;
    private Bitmap mPersonWithGoods;
    private Paint mPaint;
    private Bitmap mFinalPerson;
    private Bitmap mFinalGoods;
    private float mProgress;
    private int mAlpha;
    private int mMeasuredWidth;
    private int mMeasuredHeight;



    public JDFirstStepView(Context context) {
        this(context, null);
    }

    public JDFirstStepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JDFirstStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public JDFirstStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init(){
        mPerson = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources()
                , R.mipmap.node_modules_jdreactcorelib_libraries_jdscrollview_images_app_refresh_people_0));
        mGoods = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.node_modules_jdreactcorelib_libraries_jdscrollview_images_app_refresh_goods_0));
        //用来确定View的大小
        mPersonWithGoods = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(),
                R.mipmap.node_modules_jdreactcorelib_libraries_jdscrollview_images_app_refresh_people_3));
        mPaint = new Paint();
        mPaint.setAlpha(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resultWidth = getResultWidth(widthMeasureSpec);

        //根据标准图片成比例设置View的宽高
        setMeasuredDimension(resultWidth
                , (int)(resultWidth * ((float)mPersonWithGoods.getHeight() / (float)mPersonWithGoods.getWidth())));

    }

    /*
    * 支持at_most
    * */
    private int getResultWidth(int widthMeasureSpec){
        int result = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        switch (mode){
            case MeasureSpec.EXACTLY:
                return result;
            case MeasureSpec.AT_MOST:
                result = Math.min(result, mPersonWithGoods.getWidth());
                break;
        }
        return result;
    }

    //为了得到测量后的宽高
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();
        //把将要绘制的Bitmap进行缩放
        mFinalPerson = Bitmap.createScaledBitmap(mPerson, mMeasuredWidth, mMeasuredHeight, true);
        mFinalGoods = Bitmap.createScaledBitmap(mGoods, mMeasuredWidth / 3, mMeasuredHeight /5, true);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画包裹
        canvas.save();
        //从包裹中心开始缩放
        canvas.scale(mProgress, mProgress
                , mMeasuredWidth - mFinalGoods.getWidth() / 2, mMeasuredHeight / 2);
        mPaint.setAlpha(mAlpha);
        canvas.drawBitmap(mFinalGoods
                , mMeasuredWidth - mFinalGoods.getWidth()
                , mMeasuredHeight / 2 - mFinalGoods.getHeight() / 2
                , mPaint);
        canvas.restore();

        //画快递员
        canvas.save();
        //从快递员左边缘缩放，形成“接到包裹”的效果
        canvas.scale(mProgress, mProgress, 0, mMeasuredHeight / 2);
        mPaint.setAlpha(mAlpha);
        canvas.drawBitmap(mFinalPerson
                , 0, 0, mPaint);
        canvas.restore();


    }

    /*
    * 对外暴露设置进度的接口
    * */
    public void setProgress(float progress){
        mProgress = progress;
        mAlpha = (int)(progress * 255);
    }



}

