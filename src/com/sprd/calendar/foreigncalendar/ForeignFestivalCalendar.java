/** SPRD: Add for bug467636, add foreign festival info. @{ */
package com.sprd.calendar.foreigncalendar;

import jxl.*;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import jxl.Cell;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.R.integer;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.format.Time;
import android.util.Log;

import com.android.calendar.R;
import com.android.calendar.Utils;


public class ForeignFestivalCalendar {
    Context mContext;

    public int mSolarYear = 0;
    public int mSolarMonth = 0;
    public int mSolarDay = 0;
    public boolean isFastival = false;
    //To get the foreign festival files from system property, the default is Indoesia.xls
    //public static String mCountryStr = SystemProperties.get("persist.support.securetest" ,"Indoesia.xls");
    public static String mCountryStr = "Indoesia.xls";

    private static boolean mHasInitialedRes;
    private static final String TAG = "ForeignFestivalCalendar";
    private static String[][] mForeignCountoryFestivalStrs;
    private static int mRows = 0;
    private static int mCols = 0;
    private static String[] mForeignCountoryFestivalStr;
    private static String[] mForeignCountoryFestivalDates;
    private static boolean[] mForeignCountoryFestivalFlag;
    private static int mLanguageIndex = 2;

    private final static int mDayStringLength = 8;
    private final static int mSmallDivMonth = 9;
    private final static int mSmallDivDay = 10;

    static {
        mHasInitialedRes = false;
    }

    /* SPRD : add foreign festival info. */
    public ForeignFestivalCalendar(Context context) {
        mContext = context;
        if (!mHasInitialedRes) {
            reloadLanguageResources(mContext); //UNISOC: Modify for bug1211080
            mHasInitialedRes = true;
        }
    }

    //read the excel file, and load the festival strings and their dates.
    public static void reloadLanguageResources(Context ctx) {
        readExcelFile(ctx);
        Log.d(TAG, "mRows==" + mRows);
        mLanguageIndex = getLanguageIndex();
        //load the festival strings
        if (mForeignCountoryFestivalStr == null) {
            mForeignCountoryFestivalStr = new String[mRows - 1];
        }
        for (int i = 1; i < mRows; i++) {
            mForeignCountoryFestivalStr[i - 1] = mForeignCountoryFestivalStrs[mLanguageIndex][i];
            Log.d(TAG, "mForeignCountoryFestivalStr["+(i-1)+"]===="+mForeignCountoryFestivalStr[i-1]);
        }
        //load the festival dates
        if (mForeignCountoryFestivalDates == null) {
            mForeignCountoryFestivalDates = new String[mRows - 1];
        }
        for (int i = 1; i < mRows; i++) {
            mForeignCountoryFestivalDates[i - 1] = mForeignCountoryFestivalStrs[1][i];
            Log.d(TAG, "mForeignCountoryFestivalDates["+(i-1)+"]===="+mForeignCountoryFestivalDates[i-1]);
        }
        //set festival flag
        if (mForeignCountoryFestivalFlag == null) {
            mForeignCountoryFestivalFlag = new boolean[mRows - 1];
        }
        for (int i = 0; i < mRows - 1; i++) {
            mForeignCountoryFestivalFlag[i] = false;
        }
    }

    public static void clearLanguageResourcesRefs() {
        mForeignCountoryFestivalStr = null;
        mForeignCountoryFestivalDates = null;
        mForeignCountoryFestivalFlag = null;
    }

    private static String getString(Context ctx, int resId) {
        return ctx.getString(resId);
    }

    private int getForeignCountoryFestivalStr() {
        Log.d(TAG, "mSolarYear : " + mSolarYear);
        Log.d(TAG, "mSolarMonth : " + mSolarMonth);
        Log.d(TAG, "mSolarDay : " + mSolarDay);
        return getForeignCountoryFestivalStr(mSolarYear, mSolarMonth, mSolarDay);
    }

    //get the date's corresponding festival string
    public String getForeignFestivalInfo() {
        //get the index of the festival string according to the date
        int mForeignCountoryFestival = getForeignCountoryFestivalStr();
        String advanStr = null;
        isFastival = false;
        if (mForeignCountoryFestival > 0) {
            advanStr = mForeignCountoryFestivalStr[mForeignCountoryFestival - 1];
            if (!advanStr.trim().equals("")
                    && (mForeignCountoryFestivalFlag[mForeignCountoryFestival - 1])) {
                //mark as a festival
                isFastival = true;
            }
            Log.d(TAG, "advanStr : "+advanStr);
            //return the featival info
            return advanStr;
        }
        return "";
    }

    //read the excel file
    private static void readExcelFile(Context context) {
        AssetManager am = context.getAssets();
        try (InputStream in = am.open(mCountryStr)) { // UNISOC: Modify for bug1179182
            Workbook book = Workbook.getWorkbook(in);
            Sheet sheet = book.getSheet(0);
            mRows = sheet.getRows();
            mCols = sheet.getColumns();
            mForeignCountoryFestivalStrs = new String[mCols][mRows];
            for (int i = 0; i < mRows; ++i) {
                for (int j = 0; j < mCols; ++j) {
                    mForeignCountoryFestivalStrs[j][i] = (sheet.getCell(j, i))
                            .getContents();
                }
            }
            book.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    //Festivals in different countries are put in different columns.
    private static int getLanguageIndex() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry().toLowerCase();
        int mIndex = 2;
        for (int i = 2; i < mCols; i++) {
            if (language.equals(mForeignCountoryFestivalStrs[i][0])
                    || country.equals(mForeignCountoryFestivalStrs[i][0])) {
                mIndex = i;
            }
        }
        return mIndex;
    }

    private int getForeignCountoryFestivalStr(int year, int month, int day) {
        int pos = 0;
        if (mForeignCountoryFestivalDates != null) {
            //the capacity is set to mDayStringLength, e.g.20140101
            StringBuilder dateStrBuilder = new StringBuilder(mDayStringLength);
            dateStrBuilder.setLength(0);
            dateStrBuilder.append(year);
            //if the month is less than mSmallDivMonth, append 0 before month
            if (month < mSmallDivMonth) {
                dateStrBuilder.append(0);
            }
            dateStrBuilder.append(month + 1);
            //if the day is less than mSmallDivDay, append 0 before day
            if (day < mSmallDivDay) {
                dateStrBuilder.append(0);
            }
            dateStrBuilder.append(day);
            int index = -1;
            for (String dateStr : mForeignCountoryFestivalDates) {
                if (dateStr == null) {
                    continue;
                }
                //To make sure whether the datestr contains the input date.
                index = dateStr.indexOf(dateStrBuilder.toString());
                if (index != -1) {
                    //festival day, get its line number
                    pos = Integer.valueOf(dateStr.substring(dateStr
                            .lastIndexOf('|') + 1));
                    mForeignCountoryFestivalFlag[pos - 1] = true;
                    break;
                }
            }
        }
        return pos;
    }

    //if support foreign festival, return true, else return false.
    public static boolean isForeignFestivalSetting() {
        if (Utils.mSupportForeignFestivalCalendar) {
               return true;
        }
        return false;
    }

    //set the solar date
    public void setDate(int year, int month, int day) {
        mSolarYear = year;
        mSolarMonth = month;
        mSolarDay = day;
    }

}
/** @} */
