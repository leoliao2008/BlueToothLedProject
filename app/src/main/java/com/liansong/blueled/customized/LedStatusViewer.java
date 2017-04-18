package com.liansong.blueled.customized;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.liansong.blueled.R;
import com.liansong.blueled.bases.BaseApplication;

/**
 * Created by 廖华凯 on 2017/4/17.
 */

public class LedStatusViewer extends LinearLayout {
    private int width;
    private int height;
    private Context mContext;
    private MarginLayoutParams mLayoutParams;
    public LedStatusViewer(Context context) {
        this(context,null);
    }

    public LedStatusViewer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LedStatusViewer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayoutParams= (MarginLayoutParams) getLayoutParams();
        mContext=context;
        setBackgroundResource(R.drawable.shape_type_1);
        width= BaseApplication.getScreenWidth()-mLayoutParams.leftMargin-mLayoutParams.rightMargin;
        height=width/4;
        setOrientation(HORIZONTAL);
        char text='A';
        for(int i=0;i<4;i++){
            final LedView ledView=new LedView(mContext,height,String.valueOf(text++));
            addView(ledView);
            ledView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ledView.toggleColor();
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


}
