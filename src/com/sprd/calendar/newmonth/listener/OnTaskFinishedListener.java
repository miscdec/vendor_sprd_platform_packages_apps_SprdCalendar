package com.sprd.calendar.newmonth.listener;

public interface OnTaskFinishedListener<T> {
    void setNeedUpdate(boolean update);
    void onTaskFinished(T data);
}
