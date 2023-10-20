package com.sprd.calendar.newmonth.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.calendar.R;
import com.android.calendar.Event;
import com.android.calendar.Utils;
import com.sprd.calendar.newmonth.listener.OnTaskFinishedListener;
import com.android.calendar.alerts.AlertUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.Formatter;
import java.util.TimeZone;
import android.text.TextUtils;
import android.text.format.Time;

import androidx.recyclerview.widget.RecyclerView;

public class ScheduleAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int SCHEDULE_TYPE = 1;
    private int SCHEDULE_CENTER = 2;
    private int SCHEDULE_FINISH_TYPE = 3;
    private int SCHEDULE_BOTTOM = 4;

    private Context mContext;
    private Fragment mBaseFragment;
    private List<Event> mSchedules;
    private ScheduleOnItemClickListener itemClickListener;

    public ScheduleAdapter(Context context, Fragment baseFragment) {
        mContext = context;
        mBaseFragment = baseFragment;
        initData();
    }

    private void initData() {
        mSchedules = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        return new ScheduleViewHolder(LayoutInflater.from(mContext).inflate(
                R.layout.item_schedule, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Event schedule = mSchedules.get(position);
        final ScheduleViewHolder viewHolder = (ScheduleViewHolder) holder;
        if (itemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemClickListener.OnItemClickListener(viewHolder.itemView,
                            position);
                    Intent eventIntent = AlertUtils.buildEventViewIntent(
                            mContext, schedule.id, schedule.startMillis,
                            schedule.endMillis);
                    mContext.startActivity(eventIntent);
                }
            });
        }
        viewHolder.tvScheduleTitle.setText(schedule.title);
        if (!schedule.allDay && schedule.startMillis != 0
                && schedule.endMillis != 0) {
            viewHolder.tvScheduleTime.setText(getWhenString(schedule)); // UNISOC: Modify for bug1169506
        } else if (schedule.allDay) {
            viewHolder.tvScheduleTime.setText(mContext.getString(R.string.edit_event_all_day_label));
        } else {
            viewHolder.tvScheduleTime.setText("");
        }
        viewHolder.location.setText(schedule.location);
    }

    /* UNISOC: Modify for bug1169506 @{ */
    private String getWhenString(Event event) {
        int flags = 0;
        String whenString;
        String tzString = Utils.getTimeZone(mContext, null);
        if (event.allDay) {
            tzString = Time.TIMEZONE_UTC;
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME;
        }
        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }

        StringBuilder mStringBuilder = new StringBuilder(50);
        mStringBuilder.setLength(0);
        Formatter mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
        whenString = DateUtils.formatDateRange(mContext, mFormatter, event.startMillis, event.endMillis, flags, tzString)
                .toString();
        if (!event.allDay && !TextUtils.equals(tzString, event.eventTimezone)) {
            String displayName;
            // Figure out if this is in DST
            Time date = new Time(tzString);
            date.set(event.startMillis);

            TimeZone tz = TimeZone.getTimeZone(tzString);
            if (tz == null || tz.getID().equals("GMT")) {
                displayName = tzString;
            } else {
                displayName = tz.getDisplayName(date.isDst != 0, TimeZone.SHORT);
            }
            whenString += " (" + displayName + ")";
        }
        return whenString;
    }
    /* @} */

    @Override
    public int getItemViewType(int position) {
        if (position < mSchedules.size()) {
            return SCHEDULE_TYPE;
        } else if (position == mSchedules.size()) {
            return SCHEDULE_CENTER;
        } else if (position == getItemCount() - 1) {
            return SCHEDULE_BOTTOM;
        } else {
            return SCHEDULE_FINISH_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return mSchedules.size();
    }

    public void setOnItemClickListener(
            ScheduleOnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    protected class ScheduleViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvScheduleTitle;
        protected TextView tvScheduleTime;
        protected TextView location;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            tvScheduleTitle = (TextView) itemView
                    .findViewById(R.id.tvScheduleTitle);
            tvScheduleTime = (TextView) itemView
                    .findViewById(R.id.tvScheduleTime);
            location = (TextView) itemView.findViewById(R.id.location);
        }

    }

    public void changeAllData(List<Event> schedules) {
        distinguishData(schedules);
    }

    public void insertItem(Event schedule) {
        mSchedules.add(schedule);
        notifyItemInserted(mSchedules.size() - 1);
    }

    public void removeItem(Event schedule) {
        if (mSchedules.remove(schedule)) {
            notifyDataSetChanged();
        }
    }

    private void changeScheduleItem(Event schedule) {
        int i = mSchedules.indexOf(schedule);
        if (i != -1) {
            notifyItemChanged(i);
        }
    }

    private void distinguishData(List<Event> schedules) {
        mSchedules.clear();
        for (int i = 0, count = schedules.size(); i < count; i++) {
            Event schedule = schedules.get(i);
            mSchedules.add(schedule);
        }
        notifyDataSetChanged();
    }
}
