package com.sprd.calendar.newmonth.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.android.calendar.R;
import com.sprd.calendar.newmonth.data.EventDao;
import com.sprd.calendar.newmonth.CalendarUtils;
import com.android.calendar.Utils;

import com.sprd.calendar.lunar.LunarCalendar;
import com.sprd.calendar.lunar.LunarCalendarConvertUtil;
import com.sprd.calendar.foreigncalendar.ForeignFestivalCalendar;

import java.util.Calendar;
import java.util.List;
import android.text.format.Time;

public class MonthView extends View {

    private static final int NUM_COLUMNS = 7;
    private static final int NUM_ROWS = 6;
    private Paint mPaint;
    private Paint mLunarPaint;
    private int mNormalDayColor;
    private int mSelectDayColor;
    private int mSelectBGColor;
    private int mSelectBGTodayColor;
    private int mHintCircleColor;
    private int mLunarTextColor;
    private int mHolidayTextColor;
    private int mLastOrNextMonthTextColor;
    private int mCurrYear, mCurrMonth, mCurrDay;
    private int mSelYear, mSelMonth, mSelDay;
    private int mColumnSize, mRowSize, mSelectCircleSize;
    private int mDaySize;
    private int mLunarTextSize;
    private int mWeekRow;
    private int mCircleRadius = 3;
    private int[][] mDaysText;
    private int[] mHolidays;
    private String[][] mHolidayOrLunarText;
    private boolean mIsShowHint;
    private boolean mIsShowHolidayHint;
    private DisplayMetrics mDisplayMetrics;
    private OnMonthClickListener mDateClickListener;
    private GestureDetector mGestureDetector;
    private Bitmap mRestBitmap, mWorkBitmap;

    protected boolean[][] mLunarDayNumbersIsFest;
    private LunarCalendar mLunarCalendar;
    private ForeignFestivalCalendar mForeignFestivalCalendar;

    public MonthView(Context context, int year, int month) {
        this(context, null, year, month);
    }

    public MonthView(Context context, TypedArray array, int year, int month) {
        this(context, array, null, year, month);
    }

    public MonthView(Context context, TypedArray array, AttributeSet attrs,
            int year, int month) {
        this(context, array, attrs, 0, year, month);
    }

    public MonthView(Context context, TypedArray array, AttributeSet attrs,
            int defStyleAttr, int year, int month) {
        super(context, attrs, defStyleAttr);
        initAttrs(array, year, month);
        initPaint();
        initMonth();
        initGestureDetector();
        initTaskHint();
        if (Utils.mSupportForeignFestivalCalendar) {
            mForeignFestivalCalendar = new ForeignFestivalCalendar(context);
        } else if (Utils.mLunarFlag) {
            mLunarCalendar = new LunarCalendar(context);
        }
    }

    private void initTaskHint() {
        if (mIsShowHint) {
            EventDao dao = EventDao.getInstance(getContext());
            CalendarUtils.getInstance(getContext()).addTaskHints(mSelYear,
                    mSelMonth, dao.getTaskHintByMonth(mSelYear, mSelMonth));
        }
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if ((int) e.getX() == 0 || (int) e.getY() == 0) {
                            return true;
                        }
                        doClickAction((int) e.getX(), (int) e.getY());
                        return true;
                    }
                });
    }

    private void initAttrs(TypedArray array, int year, int month) {
        if (array != null) {
            mSelectDayColor = array.getColor(
                    R.styleable.MonthCalendarView_month_selected_text_color,
                    Color.parseColor("#FFFFFF"));
            mSelectBGColor = array.getColor(
                    R.styleable.MonthCalendarView_month_selected_circle_color,
                    Color.parseColor("#24b1bb"));
            mSelectBGTodayColor = array
                    .getColor(
                            R.styleable.MonthCalendarView_month_selected_circle_today_color,
                            Color.parseColor("#24B1BB"));
            mNormalDayColor = array.getColor(
                    R.styleable.MonthCalendarView_month_normal_text_color,
                    getResources().getColor(R.color.normal_text_color)); // UNISOC: Modify for bug1232836
            mHintCircleColor = array.getColor(
                    R.styleable.MonthCalendarView_month_hint_circle_color,
                    Color.parseColor("#b2b2b2"));
            mLastOrNextMonthTextColor = array
                    .getColor(
                            R.styleable.MonthCalendarView_month_last_or_next_month_text_color,
                            getResources().getColor(R.color.month_last_or_next_month_text)); // UNISOC: Modify for bug1227302
            mLunarTextColor = array.getColor(
                    R.styleable.MonthCalendarView_month_lunar_text_color,
                    Color.parseColor("#b2b2b2"));
            mHolidayTextColor = array.getColor(
                    R.styleable.MonthCalendarView_month_holiday_color,
                    Color.parseColor("#A68BFF"));
            mDaySize = array.getInteger(
                    R.styleable.MonthCalendarView_month_day_text_size, 14);
            mLunarTextSize = array.getInteger(
                    R.styleable.MonthCalendarView_month_day_lunar_text_size, 8);
            mIsShowHint = array.getBoolean(
                    R.styleable.MonthCalendarView_month_show_task_hint, true);
            mIsShowHolidayHint = array
                    .getBoolean(
                            R.styleable.MonthCalendarView_month_show_holiday_hint,
                            true);
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF");
            mSelectBGColor = Color.parseColor("#24b1bb");
            mSelectBGTodayColor = Color.parseColor("#24B1BB");
            mNormalDayColor = Color.parseColor("#101010");
            mHintCircleColor = Color.parseColor("#b2b2b2");
            mLastOrNextMonthTextColor = getResources().getColor(R.color.month_last_or_next_month_text); // UNISOC: Modify for bug1227302
            mHolidayTextColor = Color.parseColor("#A68BFF");
            mDaySize = 14;
            mLunarTextSize = 8;
            mIsShowHint = true;
            mIsShowHolidayHint = true;
        }
        mSelYear = year;
        mSelMonth = month;
        mRestBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_rest_day);
        mWorkBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_work_day);
        mHolidays = CalendarUtils.getInstance(getContext()).getHolidays(
                mSelYear, mSelMonth + 1);
    }

    private void initPaint() {
        mDisplayMetrics = getResources().getDisplayMetrics();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mDaySize * mDisplayMetrics.scaledDensity);

        mLunarPaint = new Paint();
        mLunarPaint.setAntiAlias(true);
        mLunarPaint.setTextSize(mLunarTextSize * mDisplayMetrics.scaledDensity);
        mLunarPaint.setColor(mLunarTextColor);
    }

    private void initMonth() {
        initCurrentDate();                 //Sprd Modify for bug756857
        /* UNISOC: Modify for bug 1030121 @{ */
        int date[] = Utils.limitMaxDate(mCurrYear, mCurrMonth, mCurrDay);
        if (mSelYear == date[0] && mSelMonth == date[1]) {
            setSelectYearMonth(mSelYear, mSelMonth, date[2]);
        } else {
            setSelectYearMonth(mSelYear, mSelMonth, 1);
        }
        /* @} */
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = mDisplayMetrics.densityDpi * 200;
        }
        if (widthMode == MeasureSpec.AT_MOST) {
            widthSize = mDisplayMetrics.densityDpi * 300;
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initCurrentDate();                 //Sprd Modify for bug756857
        initSize();
        clearData();
        drawLastMonth(canvas);
        int selected[] = drawThisMonth(canvas);
        drawNextMonth(canvas);
        drawHintCircle(canvas);
        if (Utils.mLunarFlag || Utils.mSupportForeignFestivalCalendar) {
            drawLunarText(canvas, selected);
        }
        // drawHoliday(canvas);
    }

    private void initSize() {
        mColumnSize = getWidth() / NUM_COLUMNS;
        mRowSize = getHeight() / NUM_ROWS;
        mSelectCircleSize = (int) (mColumnSize / 2.3);             //Sprd Modify for bug746291
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.1);
        }
    }

    private void clearData() {
        mDaysText = new int[6][7];
        mHolidayOrLunarText = new String[6][7];
        mLunarDayNumbersIsFest = new boolean[6][7];
    }

    private void drawLastMonth(Canvas canvas) {
        int lastYear, lastMonth;
        if (mSelMonth == 0) {
            lastYear = mSelYear - 1;
            lastMonth = 11;
        } else {
            lastYear = mSelYear;
            lastMonth = mSelMonth - 1;
        }
        mPaint.setColor(mLastOrNextMonthTextColor);
        int monthDays = CalendarUtils.getMonthDays(lastYear, lastMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(getContext(), mSelYear, mSelMonth, 1);
        for (int day = 0; day < weekNumber - 1; day++) {
            mDaysText[0][day] = monthDays - weekNumber + day + 2;
            String dayString = String.valueOf(mDaysText[0][day]);
            int startX = (int) (mColumnSize * day + (mColumnSize - mPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint
                    .descent()) / 2);
            canvas.drawText(dayString, startX, startY, mPaint);
            /* SPRD: Add for bug733823, add solar term and foreign festival info. @{ */
            if (Utils.mSupportForeignFestivalCalendar) {
                mForeignFestivalCalendar.setDate(lastYear, lastMonth,
                        mDaysText[0][day]);
                mHolidayOrLunarText[0][day] = mForeignFestivalCalendar
                        .getForeignFestivalInfo();
                mLunarDayNumbersIsFest[0][day] = mForeignFestivalCalendar.isFastival;
            } else if (Utils.mLunarFlag) {
                LunarCalendarConvertUtil.parseLunarCalendar(lastYear,
                        lastMonth, mDaysText[0][day], mLunarCalendar);
                mHolidayOrLunarText[0][day] = mLunarCalendar.getLunarDayInfo();
                mLunarDayNumbersIsFest[0][day] = mLunarCalendar.mIsFastival;
            }
            /* @} */
        }
    }

    private int[] drawThisMonth(Canvas canvas) {
        String dayString;
        int selectedPoint[] = new int[3];
        int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(getContext(), mSelYear, mSelMonth, 1);
        for (int day = 0; day < monthDays; day++) {
            dayString = String.valueOf(day + 1);
            int col = (day + weekNumber - 1) % 7;
            int row = (day + weekNumber - 1) / 7;
            mDaysText[row][col] = day + 1;
            int startX = (int) (mColumnSize * col + (mColumnSize - mPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint
                    .ascent() + mPaint.descent()) / 2);
            if (dayString.equals(String.valueOf(mSelDay))) {
                int startRecX = mColumnSize * col;
                int startRecY = mRowSize * row;
                int endRecX = startRecX + mColumnSize;
                int endRecY = startRecY + mRowSize;
                if (mSelYear == mCurrYear && mCurrMonth == mSelMonth
                        && day + 1 == mCurrDay) {
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    mPaint.setColor(mSelectBGColor);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(2);
                }
                canvas.drawCircle((startRecX + endRecX) / 2,
                        (startRecY + endRecY) / 2 + 2, mSelectCircleSize, mPaint);              //Sprd Modify for bug746291
                mWeekRow = row + 1;
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(1);
            }
            if (dayString.equals(String.valueOf(mSelDay))
                    && mCurrMonth == mSelMonth && mCurrYear == mSelYear
                    && mCurrDay == mSelDay) {
                selectedPoint[0] = row;
                selectedPoint[1] = col;
                selectedPoint[2] = 1;
                mPaint.setColor(mSelectDayColor);
            } else if (dayString.equals(String.valueOf(mCurrDay))
                    && mCurrDay != mSelDay && mCurrMonth == mSelMonth
                    && mCurrYear == mSelYear) {
                mPaint.setColor(mNormalDayColor);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            /* SPRD: Add for bug733823, add solar term and foreign festival info. @{ */
            if (Utils.mSupportForeignFestivalCalendar) {
                mForeignFestivalCalendar.setDate(mSelYear, mSelMonth,
                        mDaysText[row][col]);
                mHolidayOrLunarText[row][col] = mForeignFestivalCalendar
                        .getForeignFestivalInfo();
                mLunarDayNumbersIsFest[row][col] = mForeignFestivalCalendar.isFastival;
            } else if (Utils.mLunarFlag) {
                LunarCalendarConvertUtil.parseLunarCalendar(mSelYear,
                        mSelMonth, mDaysText[row][col], mLunarCalendar);
                mHolidayOrLunarText[row][col] = mLunarCalendar
                        .getLunarDayInfo();
                mLunarDayNumbersIsFest[row][col] = mLunarCalendar.mIsFastival;
            }
            /* @} */
        }
        return selectedPoint;
    }

    private void drawNextMonth(Canvas canvas) {
        mPaint.setColor(mLastOrNextMonthTextColor);
        int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
        int weekNumber = CalendarUtils.getFirstDayWeek(getContext(), mSelYear, mSelMonth, 1);
        int nextMonthDays = 42 - monthDays - weekNumber + 1;
        int nextMonth = mSelMonth + 1;
        int nextYear = mSelYear;
        if (nextMonth == 12) {
            nextMonth = 0;
            nextYear += 1;
        }
        for (int day = 0; day < nextMonthDays; day++) {
            int column = (monthDays + weekNumber - 1 + day) % 7;
            int row = 5 - (nextMonthDays - day - 1) / 7;
            try {
                mDaysText[row][column] = day + 1;
                /* SPRD: Add for bug733823, add solar term and foreign festival info. @{ */
                if (Utils.mSupportForeignFestivalCalendar) {
                    mForeignFestivalCalendar.setDate(nextYear, nextMonth,
                            mDaysText[row][column]);
                    mHolidayOrLunarText[row][column] = mForeignFestivalCalendar
                            .getForeignFestivalInfo();
                    mLunarDayNumbersIsFest[row][column] = mForeignFestivalCalendar.isFastival;
                } else if (Utils.mLunarFlag) {
                    LunarCalendarConvertUtil.parseLunarCalendar(nextYear,
                            nextMonth, mDaysText[row][column], mLunarCalendar);
                    mHolidayOrLunarText[row][column] = mLunarCalendar
                            .getLunarDayInfo();
                    mLunarDayNumbersIsFest[row][column] = mLunarCalendar.mIsFastival;
                }
                /* @} */
            } catch (Exception e) {
                e.printStackTrace();
            }
            String dayString = String.valueOf(mDaysText[row][column]);
            int startX = (int) (mColumnSize * column + (mColumnSize - mPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 2 - (mPaint
                    .ascent() + mPaint.descent()) / 2);
            canvas.drawText(dayString, startX, startY, mPaint);
        }
    }

    /**
     *
     *
     * @param canvas
     * @param selected
     */
    private void drawLunarText(Canvas canvas, int[] selected) {
        for (int i = 0; i < 42; i++) {
            int column = i % 7;
            int row = i / 7;
            if (!mLunarDayNumbersIsFest[row][column]) {
                mLunarPaint.setColor(mLunarTextColor);
            } else {
                mLunarPaint.setColor(mHolidayTextColor);
            }
            String dayString = mHolidayOrLunarText[row][column];
            if (row == 0 && mDaysText[row][column] >= 23 || row >= 4
                    && mDaysText[row][column] <= 14) {
                mLunarPaint.setColor(mLastOrNextMonthTextColor);
            }
            if (selected[0] == row && selected[1] == column && selected[2] == 1) {
                mLunarPaint.setColor(mSelectDayColor);
            }
            int startX = (int) (mColumnSize * column + (mColumnSize - mLunarPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize * row + mRowSize * 0.72
                    - (mLunarPaint.ascent() + mLunarPaint.descent()) / 2 + 6);
            canvas.drawText(dayString, startX, startY, mLunarPaint);
        }
    }

    private void drawHoliday(Canvas canvas) {
        if (mIsShowHolidayHint) {
            Rect rect = new Rect(0, 0, mRestBitmap.getWidth(),
                    mRestBitmap.getHeight());
            Rect rectF = new Rect();
            int distance = (int) (mSelectCircleSize / 2.5);
            for (int i = 0; i < mHolidays.length; i++) {
                int column = i % 7;
                int row = i / 7;
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth()
                        - distance, mRowSize * row + distance, mColumnSize
                        * (column + 1) - distance,
                        mRowSize * row + mRestBitmap.getHeight() + distance);
                if (mHolidays[i] == 1) {
                    canvas.drawBitmap(mRestBitmap, rect, rectF, null);
                } else if (mHolidays[i] == 2) {
                    canvas.drawBitmap(mWorkBitmap, rect, rectF, null);
                }
            }
        }
    }

    /**
     * 
     * 
     * @param
     * @param
     * @param canvas
     */
    private void drawHintCircle(Canvas canvas) {
        if (mIsShowHint) {
            List<Integer> hints = CalendarUtils.getInstance(getContext())
                    .getTaskHints(mSelYear, mSelMonth);
            if (hints.size() > 0) {
                mPaint.setColor(mHintCircleColor);
                int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
                int weekNumber = CalendarUtils.getFirstDayWeek(getContext(), mSelYear,
                        mSelMonth, 1);
                for (int day = 0; day < monthDays; day++) {
                    int col = (day + weekNumber - 1) % 7;
                    int row = (day + weekNumber - 1) / 7;
                    if (!hints.contains(day + 1))
                        continue;
                    float circleX = (float) (mColumnSize * col + mColumnSize * 0.5);
                    float circleY = (float) (mRowSize * row + mRowSize * 0.25);
                    canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
                }
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    public void setSelectYearMonth(int year, int month, int day) {
        mSelYear = year;
        mSelMonth = month;
        mSelDay = day;
    }

    private void doClickAction(int x, int y) {
        if (y > getHeight() || mRowSize == 0 || mColumnSize == 0)
            return;
        /* UNISOC: Modify for bug1236177 {@ */
        boolean curMonthRow = CalendarUtils.getMonthRows(getContext(),
                mSelYear, mSelMonth) <= 5;
        int row = y / mRowSize;
        if (curMonthRow && (row > 4)) {
            return;
        }
        /* }@ */
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        int clickYear = mSelYear, clickMonth = mSelMonth;
        if (row == 0) {
            if (mDaysText[row][column] >= 23) {
                if (mSelMonth == 0) {
                    clickYear = mSelYear - 1;
                    clickMonth = 11;
                } else {
                    clickYear = mSelYear;
                    clickMonth = mSelMonth - 1;
                }
                if (mDateClickListener != null) {
                    mDateClickListener.onClickLastMonth(clickYear, clickMonth,
                            mDaysText[row][column]);
                }
            } else {
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        } else {
            int monthDays = CalendarUtils.getMonthDays(mSelYear, mSelMonth);
            int weekNumber = CalendarUtils.getFirstDayWeek(getContext(), mSelYear, mSelMonth, 1);
            int nextMonthDays = 42 - monthDays - weekNumber + 1;
            if (column >= 0 && row < 6 && column < 7 && row >= 4 && mDaysText[row][column] <= nextMonthDays) {     //Sprd modify for bug744064,774495
                if (mSelMonth == 11) {
                    clickYear = mSelYear + 1;
                    clickMonth = 0;
                } else {
                    clickYear = mSelYear;
                    clickMonth = mSelMonth + 1;
                }
                if (mDateClickListener != null) {
                    mDateClickListener.onClickNextMonth(clickYear, clickMonth,
                            mDaysText[row][column]);
                }
            } else if (column >= 0 && row >= 0 && row < 6 && column < 7) {     //Sprd modify for bug774495
                clickThisMonth(clickYear, clickMonth, mDaysText[row][column]);
            }
        }
    }

    /**
     * 
     * 
     * @param year
     * @param month
     * @param day
     */
    public void clickThisMonth(int year, int month, int day) {
        if (mDateClickListener != null) {
            mDateClickListener.onClickThisMonth(year, month, day);
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }

    /**
     * 
     * 
     * @return
     */
    public int getSelectYear() {
        return mSelYear;
    }

    /**
     * 
     * 
     * @return
     */
    public int getSelectMonth() {
        return mSelMonth;
    }

    /**
     * 
     * 
     * @return
     */
    public int getSelectDay() {
        return this.mSelDay;
    }

    public int getRowSize() {
        return mRowSize;
    }

    public int getWeekRow() {
        return mWeekRow;
    }

    /**
     * 
     * 
     * @param hints
     */
    public void addTaskHints(List<Integer> hints) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(getContext()).addTaskHints(mSelYear,
                    mSelMonth, hints);
            invalidate();
        }
    }

    /**
     * 
     * 
     * @param hints
     */
    public void removeTaskHints(List<Integer> hints) {
        if (mIsShowHint) {
            CalendarUtils.getInstance(getContext()).removeTaskHints(mSelYear,
                    mSelMonth, hints);
            invalidate();
        }
    }

    /**
     * 
     * 
     * @param day
     */
    public boolean addTaskHint(Integer day) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(getContext()).addTaskHint(mSelYear,
                    mSelMonth, day)) {
                invalidate();
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * 
     * @param day
     */
    public boolean removeTaskHint(Integer day) {
        if (mIsShowHint) {
            if (CalendarUtils.getInstance(getContext()).removeTaskHint(
                    mSelYear, mSelMonth, day)) {
                invalidate();
                return true;
            }
        }
        return false;
    }

    /**
     *
     *
     * @param dateClickListener
     */
    public void setOnDateClickListener(OnMonthClickListener dateClickListener) {
        this.mDateClickListener = dateClickListener;
    }

    /* SPRD: Modify for bug756857,1169506, Unable to show Current date when change date in Settings @{ */
    private void initCurrentDate(){
        Time currTime = new Time(Utils.getTimeZone(getContext(), null));
        currTime.setToNow();
        mCurrYear = currTime.year;
        mCurrMonth = currTime.month;
        mCurrDay = currTime.monthDay;
    }
    /* @} */
}

