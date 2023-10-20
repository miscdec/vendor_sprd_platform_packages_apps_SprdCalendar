package com.sprd.calendar.newmonth.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import androidx.viewpager.widget.PagerAdapter;

import org.joda.time.DateTime;

import com.android.calendar.Utils;

public class MonthAdapter extends PagerAdapter {

    private SparseArray<MonthView> mViews;
    private Context mContext;
    private TypedArray mArray;
    private MonthCalendarView mMonthCalendarView;
    private int mMonthCount = 816;
    public static int mMonthDefaultPostion = Utils.getMonthDefaultPosition();         //Calculate the initial position of the month

    public MonthAdapter(Context context, TypedArray array, MonthCalendarView monthCalendarView) {
        mContext = context;
        mArray = array;
        mMonthCalendarView = monthCalendarView;
        mViews = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mMonthCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            int date[] = getYearAndMonth(position);
            MonthView monthView = new MonthView(mContext, mArray, date[0],
                    date[1]);
            monthView.setId(position);
            monthView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            monthView.invalidate();
            monthView.setOnDateClickListener(mMonthCalendarView);
            mViews.put(position, monthView);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    private int[] getYearAndMonth(int position) {
        int date[] = new int[2];
        DateTime time = new DateTime();
        /* UNISOC: Modify for bug 1030121 @{ */
        time = time.plusMonths(position - Utils.getMonthDefaultPosition());
        /* @} */
        date[0] = time.getYear();
        date[1] = time.getMonthOfYear() - 1;
        return date;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public SparseArray<MonthView> getViews() {
        return mViews;
    }

    public int getMonthCount() {
        return mMonthCount;
    }

}
