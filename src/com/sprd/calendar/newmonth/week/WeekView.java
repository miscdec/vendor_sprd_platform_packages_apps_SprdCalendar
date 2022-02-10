package com.sprd.calendar.newmonth.week;

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
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.text.format.Time;
import com.android.calendar.Utils;
import com.android.calendar.R;

import com.sprd.calendar.newmonth.CalendarUtils;
import com.sprd.calendar.newmonth.data.EventDao;
import com.sprd.calendar.lunar.LunarCalendar;
import com.sprd.calendar.lunar.LunarCalendarConvertUtil;
import com.sprd.calendar.foreigncalendar.ForeignFestivalCalendar;

import org.joda.time.DateTime;
import java.util.Calendar;
import java.util.List;

public class WeekView extends View {

    private static final int NUM_COLUMNS = 7;
    private Paint mPaint;
    private Paint mLunarPaint;
    private int mNormalDayColor;
    private int mSelectDayColor;
    private int mSelectBGColor;
    private int mSelectBGTodayColor;
    private int mHintCircleColor;
    private int mLunarTextColor;
    private int mHolidayTextColor;
    private int mCurrYear, mCurrMonth, mCurrDay;
    private int mSelYear, mSelMonth, mSelDay;
    private int mInitYear, mInitMonth;
    private int mColumnSize, mRowSize, mSelectCircleSize;
    private int mDaySize;
    private int mLunarTextSize;
    private int mCircleRadius = 3;
    private int[] mHolidays;
    private String mHolidayOrLunarText[];
    protected boolean[] mLunarDayNumbersIsFest;
    private LunarCalendar mLunarCalendar;
    private ForeignFestivalCalendar mForeignFestivalCalendar;
    private boolean mIsShowHint;
    private boolean mIsShowHolidayHint;
    private DateTime mStartDate;
    private DisplayMetrics mDisplayMetrics;
    private OnWeekClickListener mOnWeekClickListener;
    private GestureDetector mGestureDetector;
    private List<Integer> mTaskHintList;
    private List<Integer> mTaskHintNextMonthList;
    private List<Integer> mTaskHintLastMonthList;
    int mLastMonth;
    int mNextMonth;
    int firstOfWeek;
    private Bitmap mRestBitmap, mWorkBitmap;

    public WeekView(Context context, DateTime dateTime) {
        this(context, null, dateTime);
    }

    public WeekView(Context context, TypedArray array, DateTime dateTime) {
        this(context, array, null, dateTime);
    }

    public WeekView(Context context, TypedArray array, AttributeSet attrs,
            DateTime dateTime) {
        this(context, array, attrs, 0, dateTime);
    }

    public WeekView(Context context, TypedArray array, AttributeSet attrs,
            int defStyleAttr, DateTime dateTime) {
        super(context, attrs, defStyleAttr);
        initAttrs(array, dateTime);
        initPaint();
        initWeek();
        initGestureDetector();
        if (Utils.mSupportForeignFestivalCalendar) {
            mForeignFestivalCalendar = new ForeignFestivalCalendar(context);
        } else if (Utils.mLunarFlag) {
            mLunarCalendar = new LunarCalendar(context);
        }
    }

    private void initTaskHint(DateTime startDate, DateTime endDate) {
        if (mIsShowHint) {
            int startDateMonth = startDate.getMonthOfYear();
            int endDateMonth = endDate.getMonthOfYear();
            if (mTaskHintList != null) {
                mTaskHintList.clear();
            }
            if (mTaskHintLastMonthList != null) {
                mTaskHintLastMonthList.clear();
            }
            if (mTaskHintNextMonthList != null) {
                mTaskHintNextMonthList.clear();
            }
            mLastMonth = mSelMonth - 1;
            mNextMonth = mSelMonth + 1;
            Log.d("WeekView","Start Query  agenda...");
            EventDao dao = EventDao.getInstance(getContext());
            mTaskHintList = CalendarUtils.getInstance(getContext())
                    .addTaskHints(mSelYear, mSelMonth,
                            dao.getTaskHintByMonth(mSelYear, mSelMonth));

            if (startDateMonth != endDateMonth) {
               if (startDateMonth-1 == mSelMonth) {
                   if (mNextMonth != 13) {
                       mTaskHintNextMonthList = CalendarUtils.getInstance(getContext())
                           .addTaskHints(mSelYear, mNextMonth,
                                dao.getTaskHintByMonth(mSelYear, mNextMonth));
                   } else {
                       mTaskHintNextMonthList = CalendarUtils.getInstance(getContext())
                           .addTaskHints(mSelYear+1, 1,
                                dao.getTaskHintByMonth(mSelYear+1, 1));
                   }
               } else if (endDateMonth-1 == mSelMonth) {
                   if (mLastMonth != 0) {
                       mTaskHintLastMonthList = CalendarUtils.getInstance(getContext())
                           .addTaskHints(mSelYear, mLastMonth,
                               dao.getTaskHintByMonth(mSelYear, mLastMonth));
                   } else {
                       mTaskHintLastMonthList = CalendarUtils.getInstance(getContext())
                           .addTaskHints(mSelYear-1, 12,
                               dao.getTaskHintByMonth(mSelYear-1 , 12));
                   }
               }
            }
            Log.d("WeekView", "End Query agenda...");
        }
    }

    private void initAttrs(TypedArray array, DateTime dateTime) {
        if (array != null) {
            mSelectDayColor = array.getColor(
                    R.styleable.WeekCalendarView_week_selected_text_color,
                    Color.parseColor("#FFFFFF"));
            mSelectBGColor = array.getColor(
                    R.styleable.WeekCalendarView_week_selected_circle_color,
                    Color.parseColor("#E8E8E8"));
            mSelectBGTodayColor = array
                    .getColor(
                            R.styleable.WeekCalendarView_week_selected_circle_today_color,
                            Color.parseColor("#24B1BB"));
            mNormalDayColor = array.getColor(
                    R.styleable.WeekCalendarView_week_normal_text_color,
                    getResources().getColor(R.color.normal_text_color)); // UNISOC: Modify for bug1232836
            mHintCircleColor = array.getColor(
                    R.styleable.WeekCalendarView_week_hint_circle_color,
                    Color.parseColor("#b2b2b2"));
            mLunarTextColor = array.getColor(
                    R.styleable.WeekCalendarView_week_lunar_text_color,
                    Color.parseColor("#ACA9BC"));
            mHolidayTextColor = array.getColor(
                    R.styleable.WeekCalendarView_week_holiday_color,
                    Color.parseColor("#A68BFF"));
            mDaySize = array.getInteger(
                    R.styleable.WeekCalendarView_week_day_text_size, 14);
            mLunarTextSize = array.getInteger(
                    R.styleable.WeekCalendarView_week_day_lunar_text_size, 8);
            mIsShowHint = array.getBoolean(
                    R.styleable.WeekCalendarView_week_show_task_hint, true);
            mIsShowHolidayHint = array.getBoolean(
                    R.styleable.WeekCalendarView_week_show_holiday_hint, true);
        } else {
            mSelectDayColor = Color.parseColor("#FFFFFF");
            mSelectBGColor = Color.parseColor("#E8E8E8");
            mSelectBGTodayColor = Color.parseColor("#24B1BB");
            mNormalDayColor = Color.parseColor("#101010");
            mHintCircleColor = Color.parseColor("#b2b2b2");
            mLunarTextColor = Color.parseColor("#ACA9BC");
            mHolidayTextColor = Color.parseColor("#A68BFF");
            mDaySize = 14;
            mLunarTextSize = 8; // UNISOC: Modify for bug1208061
            mIsShowHint = true;
            mIsShowHolidayHint = true;
        }
        mStartDate = dateTime;
        DateTime tempDate;
        firstOfWeek = Utils.getFirstDayOfWeek(getContext());

        if (firstOfWeek == Time.SATURDAY) {
            tempDate = dateTime.plusDays(-1);
        } else if (firstOfWeek == Time.MONDAY) {
            tempDate = dateTime.plusDays(1);
        } else {
            tempDate = dateTime;
        }
        mRestBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_rest_day);
        mWorkBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_work_day);
        int holidays[] = CalendarUtils.getInstance(getContext()).getHolidays(
                tempDate.getYear(), tempDate.getMonthOfYear());
        int row = CalendarUtils.getWeekRow(getContext(), tempDate.getYear(),
                tempDate.getMonthOfYear() - 1, tempDate.getDayOfMonth());
        mHolidays = new int[7];
        System.arraycopy(holidays, row * 7, mHolidays, 0, mHolidays.length);
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

    private void initWeek() {
        initCurrentDate();                 //Sprd Modify for bug756857
        //DateTime endDate = mStartDate.plusDays(7);
        DateTime tempDateTime;
        int firstOfWeek = Utils.getFirstDayOfWeek(getContext());
        if (firstOfWeek == Time.SATURDAY) {
            tempDateTime = mStartDate.plusDays(-1);
        } else if (firstOfWeek == Time.MONDAY) {
            tempDateTime = mStartDate.plusDays(1);
        } else {
            tempDateTime = mStartDate;
        }
        DateTime endDate = tempDateTime.plusDays(7);
        if (tempDateTime.getMillis() <= System.currentTimeMillis()
                && endDate.getMillis() > System.currentTimeMillis()) {
            /* UNISOC: Modify for bug 1030121 @{ */
            int date[] = Utils.limitMaxDate(mCurrYear, mCurrMonth, mCurrDay);
            int mLimitCurrDay = date[2];
            if (tempDateTime.getMonthOfYear() != endDate.getMonthOfYear()) {
                if (mLimitCurrDay < tempDateTime.getDayOfMonth()) {
                    if (tempDateTime.getYear() == endDate.getYear()) {
                        setSelectYearMonth(tempDateTime.getYear(),
                            endDate.getMonthOfYear() - 1, mLimitCurrDay);
                    } else {
                        setSelectYearMonth(endDate.getYear(),
                            endDate.getMonthOfYear() - 1, mLimitCurrDay);
                    }
                } else {
                    setSelectYearMonth(tempDateTime.getYear(),
                            tempDateTime.getMonthOfYear() - 1, mLimitCurrDay);
                }
            } else {
                setSelectYearMonth(tempDateTime.getYear(),
                        tempDateTime.getMonthOfYear() - 1, mLimitCurrDay);
            }
            /* @} */
        } else {
            setSelectYearMonth(tempDateTime.getYear(),
                    tempDateTime.getMonthOfYear() - 1, tempDateTime.getDayOfMonth());
        }
        mInitYear = mSelYear;
        mInitMonth = mSelMonth;
        initTaskHint(tempDateTime, endDate);

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
                        doClickAction((int) e.getX(), (int) e.getY());
                        return true;
                    }
                });
    }

    public void setSelectYearMonth(int year, int month, int day) {
        mSelYear = year;
        mSelMonth = month;
        mSelDay = day;
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
        int selected = drawThisWeek(canvas);
        if (Utils.mLunarFlag || Utils.mSupportForeignFestivalCalendar) {
            drawLunarText(canvas, selected);
        }
        // drawHoliday(canvas);
    }

    private void clearData() {
        mHolidayOrLunarText = new String[7];
        mLunarDayNumbersIsFest = new boolean[7];
    }

    private void initSize() {
        mColumnSize = getWidth() / NUM_COLUMNS;
        mRowSize = getHeight();
        mSelectCircleSize = (int) (mColumnSize / 2.3);         //Sprd Modify for bug746291
        while (mSelectCircleSize > mRowSize / 2) {
            mSelectCircleSize = (int) (mSelectCircleSize / 1.1);
        }
    }

    private int drawThisWeek(Canvas canvas) {
        int selected = -1;
        DateTime dateTime;
        int firstOfWeek = Utils.getFirstDayOfWeek(getContext());
        int offset = 0;
        if (firstOfWeek == Time.SATURDAY) {
            dateTime = mStartDate.plusDays(-1);
            offset = 1;
        } else if (firstOfWeek == Time.MONDAY) {
            dateTime = mStartDate.plusDays(1);
            if ((mSelMonth == dateTime.getMonthOfYear()-1 && mSelDay < dateTime.getDayOfMonth()) || (mSelMonth <  dateTime.getMonthOfYear()-1 && mSelDay > 27)) {
               dateTime = mStartDate.plusDays(-6);
            }
            offset = 1;
        } else {
            dateTime = mStartDate;
            offset = 1;
        }
        //mSelYear is not the current select year.
        if (mSelYear != dateTime.getYear() || mSelYear != dateTime.plusDays(6).getYear()) {
             if (mSelDay > 23) {
                 mSelYear = dateTime.getYear();
             } else if (mSelDay < 7) {
                 mSelYear = dateTime.plusDays(6).getYear();
             }
        }
        int weekday = CalendarUtils.getFirstDayWeek(getContext(),mSelYear, mSelMonth, mSelDay);
        for (int i = 0; i < 7; i++) {
            DateTime date = dateTime.plusDays(i);
            int day = date.getDayOfMonth();
            String dayString = String.valueOf(day);
            int startX = (int) (mColumnSize * i + (mColumnSize - mPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize / 2 - (mPaint.ascent() + mPaint
                    .descent()) / 2);
            if ((i+offset) == weekday) {
                int startRecX = mColumnSize * i;
                int endRecX = startRecX + mColumnSize;
                if (day == mSelDay && date.getYear() == mCurrYear
                        && date.getMonthOfYear() - 1 == mCurrMonth
                        && day == mCurrDay) {
                    mPaint.setColor(mSelectBGTodayColor);
                } else {
                    mPaint.setColor(mSelectBGColor);
                    mPaint.setColor(mSelectBGColor);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(2);
                }
                canvas.drawCircle((startRecX + endRecX) / 2, mRowSize / 2 + 2,
                        mSelectCircleSize, mPaint);                             //Sprd Modify for bug746291
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(1);
            }
            //drawHintCircle(i, day, canvas);
            if (dateTime.getYear() != dateTime.plusDays(6).getYear()) {
                if (date.getYear() < mInitYear) {
                    drawHintCircle(i, day, true, false, canvas);
                } else if (date.getYear() > mInitYear) {
                    drawHintCircle(i, day, false, true, canvas);
                } else {
                    drawHintCircle(i, day, false, false, canvas);
                }
            } else {
                if (dateTime.getMonthOfYear()-1 == dateTime.plusDays(6).getMonthOfYear()-1 || date.getMonthOfYear() -1 == mInitMonth) {
                    drawHintCircle(i, day, false, false, canvas);
                } else if (date.getMonthOfYear()-1 < mInitMonth){
                    drawHintCircle(i, day, true, false, canvas);
                } else {
                    drawHintCircle(i, day, false, true, canvas);
                }
            }

            if (day == mSelDay && mCurrMonth == mSelMonth
                    && mCurrYear == mSelYear && mCurrDay == mSelDay) {
                selected = i;
                mPaint.setColor(mSelectDayColor);
            } else if (mCurrMonth == mSelMonth && mCurrYear == mSelYear
                    && mCurrDay != mSelDay) {
                mPaint.setColor(mNormalDayColor);
            } else {
                mPaint.setColor(mNormalDayColor);
            }
            canvas.drawText(dayString, startX, startY, mPaint);
            /* SPRD: Add for bug733823, add solar term and foreign festival info. @{ */
            if (Utils.mSupportForeignFestivalCalendar) {
                mForeignFestivalCalendar.setDate(date.getYear(),
                        date.getMonthOfYear() - 1, day);
                mHolidayOrLunarText[i] = mForeignFestivalCalendar
                        .getForeignFestivalInfo();
                mLunarDayNumbersIsFest[i] = mForeignFestivalCalendar.isFastival;
            } else if (Utils.mLunarFlag) {
                LunarCalendarConvertUtil.parseLunarCalendar(date.getYear(),
                        date.getMonthOfYear() - 1, day, mLunarCalendar);
                mHolidayOrLunarText[i] = mLunarCalendar.getLunarDayInfo();
                mLunarDayNumbersIsFest[i] = mLunarCalendar.mIsFastival;
            }
            /* @} */
        }
        return selected;
    }

    /**
     *
     *
     * @param canvas
     * @param selected
     */
    private void drawLunarText(Canvas canvas, int selected) {
        for (int i = 0; i < 7; i++) {
            mLunarPaint.setColor(mHolidayTextColor);
            String dayString = mHolidayOrLunarText[i];
            if (!mLunarDayNumbersIsFest[i]) {
                mLunarPaint.setColor(mLunarTextColor);
            }
            if (i == selected) {
                mLunarPaint.setColor(mSelectDayColor);
            }
            int startX = (int) (mColumnSize * i + (mColumnSize - mLunarPaint
                    .measureText(dayString)) / 2);
            int startY = (int) (mRowSize * 0.72
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
                rectF.set(mColumnSize * (column + 1) - mRestBitmap.getWidth()
                        - distance, distance, mColumnSize * (column + 1)
                        - distance, mRestBitmap.getHeight() + distance);
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
     * @param column
     * @param day
     * @param canvas
     */
    private void drawHintCircle(int column, int day, boolean lastMonth, boolean nextMonth, Canvas canvas) {
        if (mIsShowHint) {
            if (!lastMonth && !nextMonth) {
               if (!(mTaskHintList != null && mTaskHintList.size() > 0) || !mTaskHintList.contains(day)) {
                  return;
               }
            } else if (lastMonth) {
               if (!(mTaskHintLastMonthList != null && mTaskHintLastMonthList.size() > 0 ) || !mTaskHintLastMonthList.contains(day)) {
                  return;
               }
            } else {
               if (!(mTaskHintNextMonthList != null && mTaskHintNextMonthList.size() > 0 ) || !mTaskHintNextMonthList.contains(day)) {
                  return;
               }
            }
            mPaint.setColor(mHintCircleColor);
            float circleX = (float) (mColumnSize * column + mColumnSize * 0.5);
            float circleY = (float) (mRowSize * 0.25);
            canvas.drawCircle(circleX, circleY, mCircleRadius, mPaint);
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

    private void doClickAction(int x, int y) {
        DateTime dateTime;
        int firstOfWeek = Utils.getFirstDayOfWeek(getContext());
        if (firstOfWeek == Time.SATURDAY) {
            dateTime = mStartDate.plusDays(-1);
        } else if (firstOfWeek == Time.MONDAY) {
            dateTime = mStartDate.plusDays(1);
            if ((mSelMonth == dateTime.getMonthOfYear()-1 && mSelDay < dateTime.getDayOfMonth()) || (mSelMonth <  dateTime.getMonthOfYear()-1 && mSelDay > 27)) {
               dateTime = mStartDate.plusDays(-6);
            }
        } else {
            dateTime = mStartDate;
        }
        if (y > getHeight() || mColumnSize == 0)    //Sprd Modify for bug755779
            return;
        int column = x / mColumnSize;
        column = Math.min(column, 6);
        DateTime date = dateTime.plusDays(column);
        clickThisWeek(date.getYear(), date.getMonthOfYear() - 1,
                date.getDayOfMonth());
    }

    public void clickThisWeek(int year, int month, int day) {
        /* SPRD: Add for bug738565, can click date before 1970 or after 2037. @{ */
        if (year > Utils.YEAR_MAX || year < Utils.YEAR_MIN) {
            return;
        }
        /* @} */
        if (mOnWeekClickListener != null) {
            mOnWeekClickListener.onClickDate(year, month, day);
        }
        setSelectYearMonth(year, month, day);
        invalidate();
    }

    public void setOnWeekClickListener(OnWeekClickListener onWeekClickListener) {
        mOnWeekClickListener = onWeekClickListener;
    }

    public DateTime getStartDate() {
        DateTime dateTime;
        int firstOfWeek = Utils.getFirstDayOfWeek(getContext());
        if (firstOfWeek == Time.SATURDAY) {
            dateTime = mStartDate.plusDays(-1);
        } else if (firstOfWeek == Time.MONDAY) {
            dateTime = mStartDate.plusDays(1);
        } else {
            dateTime = mStartDate;
        }
        return dateTime;
    }

    public DateTime getEndDate() {
        DateTime dateTime;
        int firstOfWeek = Utils.getFirstDayOfWeek(getContext());
        if (firstOfWeek == Time.SATURDAY) {
            dateTime = mStartDate.plusDays(-1);
        } else if (firstOfWeek == Time.MONDAY) {
            dateTime = mStartDate.plusDays(1);
        } else {
            dateTime = mStartDate;
        }
        return dateTime.plusDays(6);
    }

    public int getFirstDay() {
        return firstOfWeek;
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
     * @return
     */
    public int getSelectMonth() {
        return mSelMonth;
    }

    /**
     *
     * @return
     */
    public int getSelectDay() {
        return this.mSelDay;
    }

    /**
     *
     * @param taskHintList
     */
    public void setTaskHintList(List<Integer> taskHintList) {
        mTaskHintList = taskHintList;
        invalidate();
    }

    /**
     *
     * @param day
     */
    public void addTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (!mTaskHintList.contains(day)) {
                mTaskHintList.add(day);
                invalidate();
            }
        }
    }

    /**
     *
     * @param day
     */
    public void removeTaskHint(Integer day) {
        if (mTaskHintList != null) {
            if (mTaskHintList.remove(day)) {
                invalidate();
            }
        }
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
