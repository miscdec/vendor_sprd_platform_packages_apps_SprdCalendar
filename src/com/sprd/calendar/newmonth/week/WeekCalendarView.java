package com.sprd.calendar.newmonth.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import com.android.calendar.Utils;
import com.android.calendar.R;

import org.joda.time.DateTime;
import com.sprd.calendar.newmonth.OnCalendarClickListener;

public class WeekCalendarView extends ViewPager implements OnWeekClickListener {

    private OnCalendarClickListener mOnCalendarClickListener;
    private WeekAdapter mWeekAdapter;

    public WeekCalendarView(Context context) {
        this(context, null);
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initWeekAdapter(context, context.obtainStyledAttributes(attrs,
                R.styleable.WeekCalendarView));
    }

    private void initWeekAdapter(Context context, TypedArray array) {
        mWeekAdapter = new WeekAdapter(context, array, this);
        setAdapter(mWeekAdapter);
        setCurrentItem(mWeekAdapter.mWeekDefaultPostion, false);
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        if (mOnCalendarClickListener != null) {
            mOnCalendarClickListener.onClickDate(year, month, day);
        }
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(final int position) {
            WeekView weekView = mWeekAdapter.getViews().get(position);
            WeekView preView = (position-1 >= 0) ? mWeekAdapter.getViews().get(position - 1) : null;
            WeekView nextView = (position+1 < mWeekAdapter.getViews().size()) ? mWeekAdapter.getViews().get(position + 1) : null;
            if (weekView != null) {
                int selectDay = weekView.getSelectDay();
                if (weekView.getFirstDay() != Utils.getFirstDayOfWeek(getContext()) || Utils.getFirstDayOfWeek(getContext()) == 1) {
                    if (preView != null) {
                        if ((preView.getStartDate().getMonthOfYear() == preView.getEndDate().getMonthOfYear())
                               && weekView.getSelectDay() >=  preView.getStartDate().getDayOfMonth() && weekView.getSelectDay() <= preView.getEndDate().getDayOfMonth()) {
                            selectDay = weekView.getStartDate().getDayOfMonth();
                        } else if (preView.getStartDate().getMonthOfYear() != preView.getEndDate().getMonthOfYear()
                                     && ((weekView.getSelectDay() >=  preView.getStartDate().getDayOfMonth() && weekView.getSelectDay() <=31)
                                             || (weekView.getSelectDay() <= preView.getEndDate().getDayOfMonth()))) {
                            selectDay = weekView.getStartDate().getDayOfMonth();
                        }
                    }

                    if (nextView != null) {
                        if ((nextView.getStartDate().getMonthOfYear() == nextView.getEndDate().getMonthOfYear())
                               && weekView.getSelectDay() >=  nextView.getStartDate().getDayOfMonth() && weekView.getSelectDay() <= nextView.getEndDate().getDayOfMonth()) {
                            selectDay = weekView.getStartDate().getDayOfMonth();
                        } else if (nextView.getStartDate().getMonthOfYear() != nextView.getEndDate().getMonthOfYear()
                                     && ((weekView.getSelectDay() >=  nextView.getStartDate().getDayOfMonth() && weekView.getSelectDay() <=31)
                                             || (weekView.getSelectDay() <= nextView.getEndDate().getDayOfMonth()))) {
                            selectDay = weekView.getStartDate().getDayOfMonth();
                        }
                    }
                }
                if (mOnCalendarClickListener != null) {
                    mOnCalendarClickListener.onPageChange(
                            weekView.getSelectYear(),
                            weekView.getSelectMonth(), selectDay);
                }
                weekView.clickThisWeek(weekView.getSelectYear(),
                        weekView.getSelectMonth(), selectDay);
            } else {
                WeekCalendarView.this.postDelayed(new Runnable() {
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

    /**
     * @param onCalendarClickListener
     */
    public void setOnCalendarClickListener(
            OnCalendarClickListener onCalendarClickListener) {
        mOnCalendarClickListener = onCalendarClickListener;
    }

    public SparseArray<WeekView> getWeekViews() {
        return mWeekAdapter.getViews();
    }

    public WeekAdapter getWeekAdapter() {
        return mWeekAdapter;
    }

    public WeekView getCurrentWeekView() {
        return getWeekViews().get(getCurrentItem());
    }

}
