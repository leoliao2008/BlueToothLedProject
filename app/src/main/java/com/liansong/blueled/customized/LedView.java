package com.liansong.blueled.customized;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class LedView extends View {
    private int mDiameter;
    private int mRadius;
    private Paint mPaint;
    private Paint mTextPaint;
    private int mColor;
    private String mText;
    public LedView(Context context,int diameter,String text) {
        this(context,diameter,text,null);
    }

    public LedView(Context context, int diameter, String text, @Nullable AttributeSet attrs) {
        this(context, diameter,text,attrs,0);
    }

    public LedView(Context context, int diameter, String text, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDiameter =diameter;
        mText=text;
        mRadius =diameter/2;
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mColor=Color.WHITE;
        mPaint.setColor(mColor);
        mTextPaint=new Paint();
        mTextPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec=widthMeasureSpec=MeasureSpec.makeMeasureSpec(mDiameter,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void toggleColor(){
        if(mColor==Color.WHITE){
            mColor=Color.YELLOW;
        }else {
            mColor=Color.WHITE;
        }
        mPaint.setColor(mColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mRadius, mRadius, mRadius,mPaint);
        canvas.drawText(mText.toCharArray(),0,1,0,0,mTextPaint);
    }
}
