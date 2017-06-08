package com.mycompany.mypulltorefreshtest.jingdongpull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mycompany.mypulltorefreshtest.R;

/**
 * Created by Lenovo on 2017/6/5.
 */

public class JDSecondStepView extends View {

    private Bitmap mPersonWithGoods;


    public JDSecondStepView(Context context) {
        this(context, null);
    }

    public JDSecondStepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JDSecondStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public JDSecondStepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPersonWithGoods = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources()
                , R.mipmap.node_modules_jdreactcorelib_libraries_jdscrollview_images_app_refresh_people_3));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getResultWidth(widthMeasureSpec);

        setMeasuredDimension(width
                , (int)(width * ((float)mPersonWithGoods.getHeight() / (float)mPersonWithGoods.getWidth())));
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
}
