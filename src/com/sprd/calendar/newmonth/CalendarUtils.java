package com.sprd.calendar.newmonth;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.android.calendar.Utils;
import android.text.format.Time;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CalendarUtils {

    private volatile static CalendarUtils sUtils;//UNISOC: Modify for bug1228196
    private Map<String, int[]> sAllHolidays = new HashMap<>();
    private Map<String, List<Integer>> sMonthTaskHint = new HashMap<>();

    public static CalendarUtils getInstance(Context context) {
        /* UNISOC: Modify for bug1228196 {@ */
        if (null == sUtils) {
            synchronized(CalendarUtils.class) {
                if (null == sUtils) {
                    sUtils = new CalendarUtils();
                }
            }
        }
        /* }@ */
        return sUtils;
    }

    public synchronized List<Integer> addTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            hints.removeAll(days);
            hints.addAll(days);
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.addAll(days);
        }
        return hints;
    }

    public synchronized List<Integer> removeTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            hints.removeAll(days);
        }
        return hints;
    }

    public synchronized boolean addTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            hints.add(day);
            sUtils.sMonthTaskHint.put(key, hints);
            return true;
        } else {
            if (!hints.contains(day)) {
                hints.add(day);
                return true;
            } else {
                return false;
            }
        }
    }

    public synchronized boolean updateTaskHints(int year, int month, List<Integer> days) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        boolean needUpdate = false;
        if (days != null && days.size() != 0) {
            if (hints == null) {
                hints = new ArrayList<>();
                hints.addAll(days);
                needUpdate = true;
            } else {
                if (hints.size() != days.size()) {
                    hints.clear();
                    hints.addAll(days);
                    needUpdate = true;
                } else {
                    for (int i=0; i < hints.size(); i++){
                        if(hints.get(i) != days.get(i)) {
                            needUpdate = true;
                            hints.clear();
                            hints.addAll(days);
                            break;
                        }
                   }
                }
            }
        } else if (days != null && days.size() == 0) { // UNISOC: Modify for bug1206678
            if (hints != null && hints.size() != 0) {
                hints.clear();
                needUpdate = true;
            }
        }
        if (needUpdate) {
            sUtils.sMonthTaskHint.put(key, hints);
        }
        return needUpdate;
    }

    public synchronized boolean removeTaskHint(int year, int month, int day) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        } else {
            if (hints.contains(day)) {
                Iterator<Integer> i = hints.iterator();
                while (i.hasNext()) {
                    Integer next = i.next();
                    if (next == day) {
                        i.remove();
                        break;
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public synchronized List<Integer> getTaskHints(int year, int month) {
        String key = hashKey(year, month);
        List<Integer> hints = sUtils.sMonthTaskHint.get(key);
        if (hints == null) {
            hints = new ArrayList<>();
            sUtils.sMonthTaskHint.put(key, hints);
        }
        return hints;
    }

    private static String hashKey(int year, int month) {
        return String.format("%s:%s", year, month);
    }

    /**
     *
     *
     * @param year
     * @param month
     * @return
     */
    public static int getMonthDays(int year, int month) {
        month++;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return -1;
        }
    }

    /**
     *
     *
     * @param year
     * @param month
     * @return
     */
    public static int getFirstDayWeek(Context context, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        getFirstWeekCalednar(context, calendar, year, month, day);
       // calendar.setFirstDayOfWeek(Utils.getFirstDayOfWeek(mContext));
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     *
     *
     * @return
     */
    public static int getWeeksAgo(Context context, int lastYear, int lastMonth, int lastDay, int year, int month, int day) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        getFirstWeekCalednar(context, start, lastYear, lastMonth, lastDay);
        getFirstWeekCalednar(context, end, year, month, day);
        int week = start.get(Calendar.DAY_OF_WEEK);
        start.add(Calendar.DATE, -week);
        week = end.get(Calendar.DAY_OF_WEEK);
        end.add(Calendar.DATE, 7 -week);
        float v = (end.getTimeInMillis() - start.getTimeInMillis()) / (3600 * 1000 * 24 * 7 * 1.0f);
        return (int) (v - 1);
    }

    /**
     *
     *
     * @return
     */
    public static int getMonthsAgo(int lastYear, int lastMonth, int year, int month) {
        return (year - lastYear) * 12 + (month - lastMonth);
    }

    public static int getWeekRow(Context context, int year, int month, int day) {
        int week = getFirstDayWeek(context, year, month, 1);
        Calendar calendar = Calendar.getInstance();
        getFirstWeekCalednar(context, calendar,year, month, day);
        //calendar.set(year, month, day);
        int lastWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (lastWeek == 7)
            day--;
        return (day + week - 1) / 7;
    }


    public synchronized int[] getHolidays(int year, int month) { //UNISOC: Modify for bug1228196
        int holidays[];
        if (sUtils.sAllHolidays != null) {
            holidays = sUtils.sAllHolidays.get(year + "" + month);
            if (holidays == null) {
                holidays = new int[42];
            }
        } else {
            holidays = new int[42];
        }
        return holidays;
    }

    public static int getMonthRows(Context context, int year, int month) {
        int size = getFirstDayWeek(context, year, month, 1) + getMonthDays(year, month) - 1;
        return size % 7 == 0 ? size / 7 : (size / 7) + 1;
    }

    private static Calendar getFirstWeekCalednar(Context context, Calendar calendar,int year, int month, int day){
        int firstOfWeek = Utils.getFirstDayOfWeek(context);
        if(firstOfWeek == Time.SATURDAY){
            calendar.set(year, month, day+1);
        } else if(firstOfWeek == Time.MONDAY){
            calendar.set(year, month, day+6);
        } else {
            calendar.set(year, month, day);
        }
        return calendar;
    }
}

