package com.sprd.calendar.newmonth;

public interface OnCalendarClickListener {
    void onClickDate(int year, int month, int day);

    void onPageChange(int year, int month, int day);
}
