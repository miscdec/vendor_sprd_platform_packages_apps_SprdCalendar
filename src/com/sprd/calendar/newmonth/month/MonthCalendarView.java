package com.sprd.calendar.newmonth.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewTreeObserver;

import com.android.calendar.R;
import com.sprd.calendar.newmonth.OnCalendarClickListener;

import java.util.Calendar;
import com.android.calendar.Utils;

public class MonthCalendarView extends ViewPager implements
        OnMonthClickListener {

    private MonthAdapter mMonthAdapter;
    private OnCalendarClickListener mOnCalendarClickListener;

    public MonthCalendarView(Context context) {
        this(context, null);
    }

    public MonthCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initMonthAdapter(context, context.obtainStyledAttributes(attrs,
                R.styleable.MonthCalendarView));
    }

    private void initMonthAdapter(Context context, TypedArray array) {
        mMonthAdapter = new MonthAdapter(context, array, this);
        setAdapter(mMonthAdapter);
        /* UNISOC: Modify for bug 1030121 @{ */
        setCurrentItem(Utils.getMonthDefaultPosition(), false);
        /* @} */
    }

    @Override
    public void onClickThisMonth(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    @Override
    public void onClickLastMonth(int year, int month, int day) {
        if(year < Utils.YEAR_MIN){
            return;
        }
        MonthView monthDateView = mMonthAdapter.getViews().get(
                getCurrentItem() - 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
        }
        setCurrentItem(getCurrentItem() - 1, true);
    }

    @Override
    public void onClickNextMonth(int year, int month, int day) {
        if(year > Utils.YEAR_MAX){
            return;
        }
        MonthView monthDateView = mMonthAdapter.getViews().get(
                getCurrentItem() + 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
            monthDateView.invalidate();
        }
        onClickThisMonth(year, month, day);
        setCurrentItem(getCurrentItem() + 1, true);
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(final int position) {
            MonthView monthView = mMonthAdapter.getViews()
                    .get(getCurrentItem());
            if (monthView != null) {
                monthView.clickThisMonth(monthView.getSelectYear(),
                        monthView.getSelectMonth(), monthView.getSelectDay());
                if (mOnCalendarClickListener != null) {
                    mOnCalendarClickListener.onPageChange(
                            monthView.getSelectYear(),
                            monthView.getSelectMonth(),
                            monthView.getSelectDay());
                }
            } else {
                MonthCalendarView.this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onPageSelected(position);
                    }
                }, 50);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /* SPRD: for bug730714, show wrong month when slide month view after select time@{ */
    public void setSelectToView(Time time) {
        Calendar calendar = Calendar.getInstance();
        /* UNISOC: Modify for bug 1030121 @{ */
        int position = Utils.getMonthDefaultPosition() + (((time.year - calendar.get(Calendar.YEAR)) * 12) + (time.month - calendar.get(Calendar.MONTH)));
        /* @} */
        setCurrentItem(position, false);
        MonthView monthView = mMonthAdapter.getViews().get(position);
        if (monthView != null) {
            monthView.clickThisMonth(time.year, time.month, time.monthDay);
            monthView.invalidate();
        }
    }
    /* @} */

    /**
     *
     *
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(
            OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    public SparseArray<MonthView> getMonthViews() {
        return mMonthAdapter.getViews();
    }

    public MonthView getCurrentMonthView() {
        return getMonthViews().get(getCurrentItem());
    }
}
