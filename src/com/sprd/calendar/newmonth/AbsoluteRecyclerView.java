package com.sprd.calendar.newmonth;

import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class AbsoluteRecyclerView extends RecyclerView {

    public AbsoluteRecyclerView(Context context) {
        super(context);
        setFocusable(false);
    }

    public AbsoluteRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
    }

    public AbsoluteRecyclerView(Context context, @Nullable AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }

}
