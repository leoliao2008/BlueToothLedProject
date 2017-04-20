package com.liansong.blueled.customized;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.liansong.blueled.R;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class LedView extends View {
    private int mDiameter;
    private int mRadius;
    private Paint mPaint;
    private TextPaint mTextPaint;
    private int mColor;
    private String mText;
    private float[] textWidths=new float[1];
    private boolean isLit;

    public LedView(Context context,int diameter,String text) {
        this(context,diameter,text,null);
    }

    public LedView(Context context, int diameter, String text, @Nullable AttributeSet attrs) {
        this(context, diameter,text,attrs,0);
    }

    public LedView(Context context, int diameter, String text, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDiameter =diameter;
        mText=String.copyValueOf(text.toCharArray(),0,1);
        mRadius =diameter/2;
        mPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mColor=Color.WHITE;
        mPaint.setColor(mColor);
        mTextPaint=new TextPaint();
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_type_1));
        mTextPaint.setFlags(TextPaint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec=widthMeasureSpec=MeasureSpec.makeMeasureSpec(mDiameter,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public synchronized void toggleColor(boolean isToLit){
        if(isToLit){
            animateColorFading(Color.WHITE,Color.YELLOW);
        }else {
            animateColorFading(Color.YELLOW,Color.WHITE);
        }
    }


    private void animateColorFading(final int fromColor, int toColor){
        ValueAnimator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator=ValueAnimator.ofArgb(fromColor,toColor);
        }else {
            animator=ValueAnimator.ofInt(fromColor,toColor);
        }
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPaint.setColor((Integer) animation.getAnimatedValue());
                invalidate();
            }

        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isLit=!isLit;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mRadius, mRadius, mRadius,mPaint);
        mTextPaint.getTextWidths(mText,textWidths);
        canvas.drawText(mText, mRadius - (textWidths[0] / 2+0.5f),mRadius + (textWidths[0] / 2+0.5f),mTextPaint);
    }

    public boolean isLit() {
        return isLit;
    }


}
