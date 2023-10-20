package com.sprd.calendar.newmonth.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.sprd.calendar.newmonth.OnCalendarClickListener;
import com.sprd.calendar.newmonth.RecyclerDecoration;
import com.sprd.calendar.newmonth.adapter.ScheduleAdapter;
import com.sprd.calendar.newmonth.adapter.ScheduleOnItemClickListener;
import com.sprd.calendar.newmonth.listener.OnTaskFinishedListener;
import com.sprd.calendar.newmonth.schedule.ScheduleLayout;
import com.sprd.calendar.newmonth.schedule.ScheduleRecyclerView;
import com.sprd.calendar.newmonth.task.schedule.LoadScheduleTask;

import java.util.Calendar;
import java.util.List;

public class ScheduleFragment extends Fragment implements
        OnCalendarClickListener, OnTaskFinishedListener<List<Event>>,
        CalendarController.EventHandler {

    private ScheduleLayout slSchedule;
    private ScheduleRecyclerView rvScheduleList;
    private RelativeLayout rLNoTask;
    private boolean mIsUpdate;

    private ScheduleAdapter mScheduleAdapter;
    private int mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay;
    private Calendar mSelectTime = Calendar.getInstance();
    private Time mSelectedDay = new Time();
    private Time mCurrentTime = new Time();
    private Time initTime = new Time();
    private Time eventTime = new Time();


    /* SPRD: Add for bug732533,1145293 Add init date with select date @{ */
    public ScheduleFragment() {

    }

    public static ScheduleFragment newInstance(long timeMillis) {
        Bundle args = new Bundle();
        args.putLong("timeMillis", timeMillis);
        ScheduleFragment f = new ScheduleFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        long timeMillis = System.currentTimeMillis();
        if (getArguments() != null) {
            timeMillis = getArguments().getLong("timeMillis");
        }
        initTime.set(timeMillis);
        /* UNISOC: Modify for bug1169506 @{ */
        String tz = Utils.getTimeZone(getActivity(), null);
        initTime.switchTimezone(tz);
        /* @} */
    }
    /* @} */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container,
                false);
        initDate(view);
        return view;
    }

    /* SPRD: Add for bug730019, Unable to show schedule on time @{ */
    @Override
    public void onResume() {
        eventsChanged();
        /* UNISOC: Modify for bug1169506 @{ */
        mCurrentTime = new Time(Utils.getTimeZone(getActivity(), null));
        mCurrentTime.setToNow();
        setCurrentSelectDate(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay);
        /* @} */
        /* SPRD: Modify for bug756857, Unable to show Current date when change date in Settings @{ */
        if(slSchedule.getWeekCalendar().getCurrentWeekView() != null){
            slSchedule.getWeekCalendar().getCurrentWeekView().invalidate();
        }
        if(slSchedule.getMonthCalendar().getCurrentMonthView() != null){
            slSchedule.getMonthCalendar().getCurrentMonthView().invalidate();
        }
        /* @} */
        super.onResume();
    }
    /* @} */

    public void resetScheduleList() {
        /*SPRD: Modified for bug 844759 @{*/
        new LoadScheduleTask(getActivity(), this, mCurrentSelectYear,
                mCurrentSelectMonth, mCurrentSelectDay)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        /*}@*/
    }

    /* SPRD: Add for bug732533, Add init date with select date @{ */
    private void initDate(View view) {
        mCurrentTime = new Time(Utils.getTimeZone(getActivity(), null)); // UNISOC: Modify for bug1169506
        mCurrentTime.setToNow();
        setCurrentSelectDate(mCurrentTime.year, mCurrentTime.month, mCurrentTime.monthDay);// UNISOC: Modify for bug1429035
        slSchedule = (ScheduleLayout) view.findViewById(R.id.slSchedule);
        rLNoTask = (RelativeLayout) view.findViewById(R.id.rlNoTask);
        slSchedule.setOnCalendarClickListener(this);
        if (getActivity() instanceof AllInOneActivity) {
            /* UNISOC: Modify for bug 1030121 @{ */
            ((AllInOneActivity) getActivity()).setTodayMenuShow(needtodayMenuShow());
            /* @} */
        }
        initScheduleList();
        /* SPRD: modify for bug 762656 @{ */
        //resetScheduleList();
        /* @} */
        if (slSchedule != null && slSchedule.getMonthCalendar() != null) {
           slSchedule.getMonthCalendar().setSelectToView(initTime);
        }
    }
    /* @} */

    @Override
    public void onClickDate(int year, int month, int day) {
        mCurrentTime = new Time(Utils.getTimeZone(getActivity(), null)); // UNISOC: Modify for bug1169506
        mCurrentTime.setToNow();
        setCurrentSelectDate(year, month, day);
        resetScheduleList();
    }

    @Override
    public void onPageChange(int year, int month, int day) {
        resetScheduleList();
    }

    private void initScheduleList() {
        rvScheduleList = slSchedule.getSchedulerRecyclerView();
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvScheduleList.setLayoutManager(manager);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        rvScheduleList.setItemAnimator(itemAnimator);
        mScheduleAdapter = new ScheduleAdapter(getActivity(), this);
        rvScheduleList.setAdapter(mScheduleAdapter);
        mScheduleAdapter
                .setOnItemClickListener(new ScheduleOnItemClickListener() {
                    @Override
                    public void OnItemClickListener(View view, int position) {
                    }
                });
        rvScheduleList.addItemDecoration(new RecyclerDecoration(getActivity(),
                RecyclerDecoration.VERTICAL_LIST));
    }

    private void setCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
        mSelectTime.set(Calendar.YEAR, year);
        mSelectTime.set(Calendar.MONTH, month);
        mSelectTime.set(Calendar.DAY_OF_MONTH, day);
        /* UNISOC: Modify for bug1169506 @{ */
        String tz = Utils.getTimeZone(getActivity(), null);
        mSelectedDay.switchTimezone(tz);
        /* @} */
        mSelectedDay.year = year;
        mSelectedDay.month = month;
        mSelectedDay.monthDay = day;
        mSelectedDay.hour = mCurrentTime.hour;
        mSelectedDay.minute = mCurrentTime.minute;
        setMonthDisplayed(mSelectedDay);
        if (getActivity() instanceof AllInOneActivity) {
            ((AllInOneActivity) getActivity())
                    .setDateForActionButton(mSelectTime.getTimeInMillis());
            ((AllInOneActivity) getActivity())
                    .setTodayMenuShow(needtodayMenuShow());
        }
    }

    @Override
    public void onTaskFinished(List<Event> data) {
        mScheduleAdapter.changeAllData(data);
        rLNoTask.setVisibility(data.size() == 0 ? View.VISIBLE : View.GONE);
        updateTaskHintUi(data.size());
    }

    @Override
    public void setNeedUpdate(boolean update) {
        mIsUpdate = update;
    }

    private void updateTaskHintUi(int size) {
        if (mIsUpdate) {
           slSchedule.refreshTaskHints();
        } else if(size == 0) {
           slSchedule.removeTaskHint(mCurrentSelectDay);
        } else {
           slSchedule.addTaskHint(mCurrentSelectDay);
        }
    }

    public int getCurrentCalendarPosition() {
        return slSchedule.getMonthCalendar().getCurrentItem();
    }

    @Override
    public long getSupportedEventTypes() {
        // TODO Auto-generated method stub
        return EventType.GO_TO | EventType.EVENTS_CHANGED;
    }

    @Override
    public void handleEvent(EventInfo event) {
        /* SPRD: bug761530, The month in the upper left does not match the calendar {@ */
        eventTime.set(event.selectedTime);
        if (event.eventType == EventType.GO_TO
                && event.id == -1
                && event.viewType == 4
                && ((event.extraLong & CalendarController.EXTRA_GOTO_TODAY) != 0 || (event.extraLong & CalendarController.EXTRA_GOTO_DATE) != 0 || eventTime.year != mSelectTime.get(Calendar.YEAR) || eventTime.month != mSelectTime.get(Calendar.MONTH) || eventTime.monthDay != mSelectTime.get(Calendar.DAY_OF_MONTH))) {
            if (slSchedule != null && slSchedule.getMonthCalendar() != null) {
                slSchedule.getMonthCalendar()
                        .setSelectToView(event.selectedTime);
            }
            resetScheduleList();
        } else if (event.eventType == EventType.EVENTS_CHANGED) {
            eventsChanged();
        }
        /* @} */

    }

    public void eventsChanged() {
        resetScheduleList();
        // EventDao dao = EventDao.getInstance(getContext());
        // slSchedule.addTaskHints(dao.getTaskHintByMonth(mCurrentSelectYear,
        // mCurrentSelectMonth));
    }

    private boolean needtodayMenuShow() {
        Time now = new Time(Utils.getTimeZone(getActivity(), null)); // UNISOC: Modify for bug1169506
        now.setToNow();
        if (mCurrentSelectYear == now.year && mCurrentSelectMonth == now.month
                && mCurrentSelectDay == now.monthDay) {
            return false;
        } else {
            return true;
        }
    }

    private void setMonthDisplayed(Time time) {
        /* SPRD: bug743423, com.android.calendar happens JavaCrash {@ */
        Context context = getContext();
        if(context == null) {
            return;
        }
        /* @} */
        CalendarController controller = CalendarController
                .getInstance(context);
        long newTime = time.normalize(true);
        if (newTime != controller.getTime()) {
            controller.setTime(newTime);
        }
        controller.sendEvent(this, EventType.UPDATE_TITLE, time, time, time,
                -1, ViewType.CURRENT, DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_NO_MONTH_DAY
                        | DateUtils.FORMAT_SHOW_YEAR, null, null);
    }

}
