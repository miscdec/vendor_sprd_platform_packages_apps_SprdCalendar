package com.sprd.calendar.newmonth.task.schedule;

import android.content.Context;
import com.sprd.calendar.newmonth.CalendarUtils;
import com.sprd.calendar.newmonth.base.task.BaseAsyncTask;
import com.android.calendar.Event;
import com.sprd.calendar.newmonth.data.EventDao;
import com.sprd.calendar.newmonth.listener.OnTaskFinishedListener;

import java.util.List;

public class LoadScheduleTask extends BaseAsyncTask<List<Event>> {

    private int mYear;
    private int mMonth;
    private int mDay;

    public LoadScheduleTask(Context context,
            OnTaskFinishedListener<List<Event>> onTaskFinishedListener,
            int year, int month, int day) {
        super(context, onTaskFinishedListener);
        mYear = year;
        mMonth = month;
        mDay = day;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        EventDao dao = EventDao.getInstance(mContext);
        if(CalendarUtils.getInstance(mContext).updateTaskHints(mYear,
                mMonth, dao.getTaskHintByMonth(mYear, mMonth))) {
           mNeedUpdate = true;
        } else {
           mNeedUpdate = false;
        }
        return dao.getEventByDate(mYear, mMonth, mDay);
    }
}
