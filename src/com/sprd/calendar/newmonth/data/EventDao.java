package com.sprd.calendar.newmonth.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;

import com.android.calendar.Event;
import com.android.calendar.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDao {

    private Context mContext;
    private static final String SORT_EVENTS_BY =
            "begin ASC, end DESC, title ASC";                    //add event display order in month view
    private static final String TAG = "EventDao";
    private static final String INSTANCES_SORT_ORDER = Instances.START_DAY + "," + Instances.START_MINUTE + "," + Instances.TITLE;
    private static final String SELECTION = "visible=1";

    private EventDao(Context context) {
        mContext = context;
    }

    public static EventDao getInstance(Context context) {
        return new EventDao(context);
    }

    /* UNISOC: Modify for bug1169506 @{ */
    public List<Event> getEventByDate(int year, int month, int day) {
        List<Event> schedules = new ArrayList<>();
        ArrayList<Event> evets = new ArrayList<Event>();

        String tz = Utils.getTimeZone(mContext, null);
        Time startDate = new Time(tz);
        Time endDate = new Time(tz);
        startDate.set(0, 0, 0, day, month, year);
        endDate.set(0, 0, 0, day + 1, month, year);
        startDate.switchTimezone(Time.getCurrentTimezone());
        endDate.switchTimezone(Time.getCurrentTimezone());

        Cursor cursor = null;
        Uri.Builder uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder,startDate.toMillis(true));
        ContentUris.appendId(uriBuilder,(endDate.toMillis(true) - 1));
        /* SPRD: Modify for bug 736271 calendar crashed when ram nearly full @{ */
        /* SPRD: Modify for bug 751039 com.android.calendar happens JavaCrash @{ */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(mContext != null && PackageManager.PERMISSION_GRANTED == mContext.checkSelfPermission("android.permission.READ_CALENDAR")){
               try {
                    cursor = mContext.getContentResolver().query(uriBuilder.build(),Event.EVENT_PROJECTION,"visible=1",null, SORT_EVENTS_BY);
                } catch (SQLiteException e) {
                    Log.e(TAG,"e==="+e);
                    cursor = null;
                }
            }else{
                cursor = null;
            }
        }else{
            try {
                cursor = mContext.getContentResolver().query(uriBuilder.build(),Event.EVENT_PROJECTION,"visible=1",null, SORT_EVENTS_BY);
            } catch (SQLiteException e) {
                Log.e(TAG,"e==="+e);
                cursor = null;
            }
        }
        /* @} */
        /* @} */
        Time mTime =new Time();
        mTime.year = year;
        mTime.month = month;
        mTime.monthDay = day;
        mTime.normalize(true);
        Event.buildEventsFromCursor(evets,cursor,mContext,Time.getJulianDay(mTime.toMillis(true), mTime.gmtoff),Time.getJulianDay(mTime.toMillis(true), mTime.gmtoff)+1);

        Event event;
        try {
            if(cursor != null){
                for(int i = 0;i< evets.size();i++){
                    event = new Event();
                    event.id = evets.get(i).id;
                    event.title = evets.get(i).title;
                    event.location = evets.get(i).location;
                    event.startMillis = evets.get(i).startMillis;
                    event.endMillis = evets.get(i).endMillis;
                    event.allDay = evets.get(i).allDay;
                    event.eventTimezone = evets.get(i).eventTimezone;
                    schedules.add(event);
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return schedules;
    }
    /* @} */

    public List<Integer> getTaskHintByMonth(int year, int month) {
        List<Integer> taskHint = new ArrayList<>();
        ArrayList<Event> evets = new ArrayList<Event>();

        /* UNISOC: Modify for bug1169506 @{ */
        String tz = Utils.getTimeZone(mContext, null);
        Time selectDate = new Time(tz);
        Time selectDate1 = new Time(tz);
        selectDate.set(0, 0, 0, 1, month, year);
        selectDate1.set(0, 0, 0, 1, month + 1, year);
        selectDate.switchTimezone(Time.getCurrentTimezone());
        selectDate1.switchTimezone(Time.getCurrentTimezone());
        /* @} */
        Cursor cursor = null;
        Uri.Builder uriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(uriBuilder,selectDate.toMillis(true));
        ContentUris.appendId(uriBuilder,selectDate1.toMillis(true));
        /* SPRD: Modify for bug 736271 calendar crashed when ram nearly full @{ */
        /* SPRD: Modify for bug 751039 com.android.calendar happens JavaCrash @{ */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(mContext != null && PackageManager.PERMISSION_GRANTED == mContext.checkSelfPermission("android.permission.READ_CALENDAR")){
               try {
                    cursor = mContext.getContentResolver().query(uriBuilder.build(),Event.EVENT_PROJECTION,SELECTION,null,INSTANCES_SORT_ORDER);
                } catch (SQLiteException e) {
                    Log.e(TAG,"e==="+e);
                    cursor = null;
                }
            }else{
                cursor = null;
            }
        }else{
            try {
                cursor = mContext.getContentResolver().query(uriBuilder.build(),Event.EVENT_PROJECTION,SELECTION,null,INSTANCES_SORT_ORDER);
            } catch (SQLiteException e) {
                Log.e(TAG,"e==="+e);
                cursor = null;
            }
        }
        /* @} */
        /* @} */

        selectDate.normalize(true);
        selectDate1.normalize(true);
        Event.buildEventsFromCursor(evets,cursor,mContext,Time.getJulianDay(selectDate.toMillis(true), selectDate.gmtoff),Time.getJulianDay(selectDate1.toMillis(true), selectDate1.gmtoff)-1);
        try {
            if(cursor != null){
                for(int i=Time.getJulianDay(selectDate.toMillis(true), selectDate.gmtoff);i<Time.getJulianDay(selectDate1.toMillis(true), selectDate1.gmtoff);i++) {
                    for(int j=0;j< evets.size();j++) {
                        if(i >= evets.get(j).startDay && i <= evets.get(j).endDay) { // Modify for bug 1054962
                            taskHint.add(i - Time.getJulianDay(selectDate.toMillis(true), selectDate.gmtoff)+1);
                        } else {
                            continue;
                        }
                    }
                }
            }
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return taskHint;
    }
}
