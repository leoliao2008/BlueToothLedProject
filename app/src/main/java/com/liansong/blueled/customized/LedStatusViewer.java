package com.liansong.blueled.customized;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseApplication;
import com.liansong.blueled.utils.LogUtil;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class LedStatusViewer extends LinearLayout {
    private int width;
    private int height;
    private Context mContext;
    private int mLayoutMargin;
    private int mPadding;

    public LedStatusViewer(Context context) {
        this(context,null);
    }

    public LedStatusViewer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LedStatusViewer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs!=null){
            int count = attrs.getAttributeCount();
            for(int i=0;i<count;i++){
                if(attrs.getAttributeName(i).equals("layout_margin")){
                    mLayoutMargin= getResources().getDimensionPixelOffset(attrs.getAttributeResourceValue(i,0));
                }else if(attrs.getAttributeName(i).equals("padding")){
                    mPadding=getResources().getDimensionPixelOffset(attrs.getAttributeResourceValue(i,0));
                }
            }
        }
        mContext=context;
        setBackgroundResource(R.drawable.shape_type_1);
        width= BaseApplication.getScreenWidth()-mLayoutMargin*2;
        height=width/4;
        setOrientation(HORIZONTAL);
        char text='A';
        int ledViewDiameter = height - mPadding * 2;
        int ledViewLeftMargin = (int) ((width-mPadding*2-ledViewDiameter*4)/3.f+0.5f);
        for(int i=0;i<4;i++){
            final LedView ledView=new LedView(mContext, ledViewDiameter,String.valueOf(text++));
            addView(ledView);
            if(i>0){
                MarginLayoutParams layoutParams = (MarginLayoutParams) ledView.getLayoutParams();
                layoutParams.leftMargin= ledViewLeftMargin;
                ledView.setLayoutParams(layoutParams);
            }
            ledView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ledView.toggleColor(!ledView.isLit());
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        widthMeasureSpec=MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY);
        heightMeasureSpec=MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private void showLog(String msg){
        LogUtil.showLog(LedStatusViewer.class.getName(),msg);
    }


}
