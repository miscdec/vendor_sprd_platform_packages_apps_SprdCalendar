/** SPRD: Modify for bug473571, add lunar info */
package com.sprd.calendar.lunar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import android.R.integer;
import android.content.Context;
import android.os.Build;
import android.text.format.Time;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.LongSparseArray;
import android.annotation.SuppressLint;

import androidx.annotation.RequiresApi;

import com.android.calendar.R;
import com.sprd.calendar.lunar.SolarTermUtil;

public class LunarCalendar {

    private static String[] mLunarCalendarNumber = null;

    private static String[] mLunarCalendarTen = null;

    private static String[] mYearOfBirth = null;

    public static String[] mLunarTerm = null;//Modify for bug1391668

    private static String mLunarLeapTag = null, mLunarMonthTag = null,
            zhengyueTag = null;

    Context mContext;

    public int mLunarYear = 0;

    public int mLunarMonth = 0;

    public int mLunarDay = 0;

    public int mSolarYear = 0;

    public int mSolarMonth = 0;

    public int mSolarDay = 0;

    public boolean mIsLeapMonth = false;

    public boolean mIsFastival = false;

    // special solar term dates
    private static String[] mSpecialSolarTermDates;
    // update lunar language resources info
    private static boolean mHasInitialedRes;
    // such as Mid-Autumn Day
    private static String[] mTraditionalFestivalStr;
    // such as Valentine's Day
    private static String[] mFestivalStr;
    // such as Jia, Yi, Bing, Ding
    private static String[] mYearStemStr;
    // such as Zi, Chou, Yin, Mao
    private static String[] mYearBranchStr;

    static {
        mHasInitialedRes = false;
    }

    public LunarCalendar(Context context) {
        mContext = context;
        if (!mHasInitialedRes) {
            reloadLanguageResources(mContext); //UNISOC: Modify for bug1211080
            mHasInitialedRes = true;
        }
    }

    /* UNISOC: Modify for bug1391668 save 24 solarTerms {@ */
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String[]> SOLAR_TERMS = new HashMap<>();
    /* }@ */

    public static void reloadLanguageResources(Context ctx) {
        if (mLunarCalendarNumber == null) {
            mLunarCalendarNumber = new String[12];
        }
        mLunarCalendarNumber[0] = getString(ctx, R.string.chineseNumber1);
        mLunarCalendarNumber[1] = getString(ctx, R.string.chineseNumber2);
        mLunarCalendarNumber[2] = getString(ctx, R.string.chineseNumber3);
        mLunarCalendarNumber[3] = getString(ctx, R.string.chineseNumber4);
        mLunarCalendarNumber[4] = getString(ctx, R.string.chineseNumber5);
        mLunarCalendarNumber[5] = getString(ctx, R.string.chineseNumber6);
        mLunarCalendarNumber[6] = getString(ctx, R.string.chineseNumber7);
        mLunarCalendarNumber[7] = getString(ctx, R.string.chineseNumber8);
        mLunarCalendarNumber[8] = getString(ctx, R.string.chineseNumber9);
        mLunarCalendarNumber[9] = getString(ctx, R.string.chineseNumber10);
        mLunarCalendarNumber[10] = getString(ctx, R.string.chineseNumber11);
        mLunarCalendarNumber[11] = getString(ctx, R.string.chineseNumber12);

        if (mLunarCalendarTen == null) {
            mLunarCalendarTen = new String[5];
        }
        mLunarCalendarTen[0] = getString(ctx, R.string.chineseTen0);
        mLunarCalendarTen[1] = getString(ctx, R.string.chineseTen1);
        mLunarCalendarTen[2] = getString(ctx, R.string.chineseTen2);
        mLunarCalendarTen[3] = getString(ctx, R.string.chineseTen3);
        mLunarCalendarTen[4] = getString(ctx, R.string.chineseTen4);

        if (mYearOfBirth == null) {
            mYearOfBirth = new String[12];
        }
        mYearOfBirth[0] = getString(ctx, R.string.animals0);
        mYearOfBirth[1] = getString(ctx, R.string.animals1);
        mYearOfBirth[2] = getString(ctx, R.string.animals2);
        mYearOfBirth[3] = getString(ctx, R.string.animals3);
        mYearOfBirth[4] = getString(ctx, R.string.animals4);
        mYearOfBirth[5] = getString(ctx, R.string.animals5);
        mYearOfBirth[6] = getString(ctx, R.string.animals6);
        mYearOfBirth[7] = getString(ctx, R.string.animals7);
        mYearOfBirth[8] = getString(ctx, R.string.animals8);
        mYearOfBirth[9] = getString(ctx, R.string.animals9);
        mYearOfBirth[10] = getString(ctx, R.string.animals10);
        mYearOfBirth[11] = getString(ctx, R.string.animals11);

        mLunarLeapTag = getString(ctx, R.string.leap_month);
        mLunarMonthTag = getString(ctx, R.string.month);
        zhengyueTag = getString(ctx, R.string.zheng);

        if (mLunarTerm == null) {
            mLunarTerm = new String[24];
        }
        /* UNISOC: Modify for bug1391668 {@ */
        mLunarTerm[0] = getString(ctx, R.string.terms5);
        mLunarTerm[1] = getString(ctx, R.string.terms6);
        mLunarTerm[2] = getString(ctx, R.string.terms7);
        mLunarTerm[3] = getString(ctx, R.string.terms8);
        mLunarTerm[4] = getString(ctx, R.string.terms9);
        mLunarTerm[5] = getString(ctx, R.string.terms10);
        mLunarTerm[6] = getString(ctx, R.string.terms11);
        mLunarTerm[7] = getString(ctx, R.string.terms12);
        mLunarTerm[8] = getString(ctx, R.string.terms13);
        mLunarTerm[9] = getString(ctx, R.string.terms14);
        mLunarTerm[10] = getString(ctx, R.string.terms15);
        mLunarTerm[11] = getString(ctx, R.string.terms16);
        mLunarTerm[12] = getString(ctx, R.string.terms17);
        mLunarTerm[13] = getString(ctx, R.string.terms18);
        mLunarTerm[14] = getString(ctx, R.string.terms19);
        mLunarTerm[15] = getString(ctx, R.string.terms20);
        mLunarTerm[16] = getString(ctx, R.string.terms21);
        mLunarTerm[17] = getString(ctx, R.string.terms22);
        mLunarTerm[18] = getString(ctx, R.string.terms23);
        mLunarTerm[19] = getString(ctx, R.string.terms0);
        mLunarTerm[20] = getString(ctx, R.string.terms1);
        mLunarTerm[21] = getString(ctx, R.string.terms2);
        mLunarTerm[22] = getString(ctx, R.string.terms3);
        mLunarTerm[23] = getString(ctx, R.string.terms4);
        /* }@ */

        if (mTraditionalFestivalStr == null) {
            mTraditionalFestivalStr = new String[9];
        }
        mTraditionalFestivalStr[0] = getString(ctx, R.string.chunjie);
        mTraditionalFestivalStr[1] = getString(ctx, R.string.yuanxiao);
        mTraditionalFestivalStr[2] = getString(ctx, R.string.duanwu);
        mTraditionalFestivalStr[3] = getString(ctx, R.string.qixi);
        mTraditionalFestivalStr[4] = getString(ctx, R.string.zhongqiu);
        mTraditionalFestivalStr[5] = getString(ctx, R.string.chongyang);
        mTraditionalFestivalStr[6] = getString(ctx, R.string.laba);
        mTraditionalFestivalStr[7] = getString(ctx, R.string.xiaonian);
        mTraditionalFestivalStr[8] = getString(ctx, R.string.chuxi);

        if (mFestivalStr == null) {
            mFestivalStr = new String[13];
        }
        mFestivalStr[0] = getString(ctx, R.string.new_Year_day);
        mFestivalStr[1] = getString(ctx, R.string.valentin_day);
        mFestivalStr[2] = getString(ctx, R.string.women_day);
        mFestivalStr[3] = getString(ctx, R.string.arbor_day);
        mFestivalStr[4] = getString(ctx, R.string.labol_day);
        mFestivalStr[5] = getString(ctx, R.string.youth_day);
        mFestivalStr[6] = getString(ctx, R.string.children_day);
        mFestivalStr[7] = getString(ctx, R.string.Communist_day);
        mFestivalStr[8] = getString(ctx, R.string.army_day);
        mFestivalStr[9] = getString(ctx, R.string.teacher_day);
        mFestivalStr[10] = getString(ctx, R.string.national_day);
        mFestivalStr[11] = getString(ctx, R.string.christmas_day);
        mFestivalStr[12] = getString(ctx, R.string.fool_day);

        if (mYearStemStr == null) {
            mYearStemStr = new String[10];
        }
        mYearStemStr[0] = getString(ctx, R.string.jia);
        mYearStemStr[1] = getString(ctx, R.string.yi);
        mYearStemStr[2] = getString(ctx, R.string.bing);
        mYearStemStr[3] = getString(ctx, R.string.ding);
        mYearStemStr[4] = getString(ctx, R.string.wutian);
        mYearStemStr[5] = getString(ctx, R.string.ji);
        mYearStemStr[6] = getString(ctx, R.string.geng);
        mYearStemStr[7] = getString(ctx, R.string.xin);
        mYearStemStr[8] = getString(ctx, R.string.ren);
        mYearStemStr[9] = getString(ctx, R.string.gui);

        if (mYearBranchStr == null) {
            mYearBranchStr = new String[12];
        }
        mYearBranchStr[0] = getString(ctx, R.string.zi);
        mYearBranchStr[1] = getString(ctx, R.string.chou);
        mYearBranchStr[2] = getString(ctx, R.string.yin);
        mYearBranchStr[3] = getString(ctx, R.string.mao);
        mYearBranchStr[4] = getString(ctx, R.string.chen);
        mYearBranchStr[5] = getString(ctx, R.string.si);
        mYearBranchStr[6] = getString(ctx, R.string.wudi);
        mYearBranchStr[7] = getString(ctx, R.string.wei);
        mYearBranchStr[8] = getString(ctx, R.string.shen);
        mYearBranchStr[9] = getString(ctx, R.string.you);
        mYearBranchStr[10] = getString(ctx, R.string.xu);
        mYearBranchStr[11] = getString(ctx, R.string.hai);
    }

    public static void clearLanguageResourcesRefs() {
        mLunarCalendarNumber = null;
        mLunarCalendarTen = null;
        mYearOfBirth = null;
        mLunarTerm = null;
        mTraditionalFestivalStr = null;
        mFestivalStr = null;
        mYearStemStr = null;
        mYearBranchStr = null;
        mHasInitialedRes = false;
    }

    private static String getString(Context ctx, int resId) {
        return ctx.getString(resId);
    }

    public String getTraditionalFestival() {
        return getTraditionalFestival(mLunarYear, mLunarMonth, mLunarDay);
    }

    public String getTraditionalFestival(int lunarYear, int lunarMonth,
            int lunarDay) {
        // if is leap month, return empty string
        if (mIsLeapMonth) {
            return "";
        }

        String festivalStr = "";
        // lunar optimization
        switch (lunarMonth) {
            case 1:
                if (lunarDay == 1) {
                    festivalStr = mTraditionalFestivalStr[0];
                } else if (lunarDay == 15) {
                    festivalStr = mTraditionalFestivalStr[1];
                }
                break;
            case 5:
                if (lunarDay == 5) {
                    festivalStr = mTraditionalFestivalStr[2];
                }
                break;
            case 7:
                if (lunarDay == 7) {
                    festivalStr = mTraditionalFestivalStr[3];
                }
                break;
            case 8:
                if (lunarDay == 15) {
                    festivalStr = mTraditionalFestivalStr[4];
                }
                break;
            case 9:
                if (lunarDay == 9) {
                    festivalStr = mTraditionalFestivalStr[5];
                }
                break;
            case 12:
                if (lunarDay == 8) {
                    festivalStr = mTraditionalFestivalStr[6];
                } else if (lunarDay == 23) {
                    festivalStr = mTraditionalFestivalStr[7];
                } else if (lunarDay == LunarCalendarConvertUtil.getLunarMonthDays(
                        lunarYear, lunarMonth)) {
                    festivalStr = mTraditionalFestivalStr[8];
                }
                break;
            default:
                break;
        }

        return festivalStr;
    }

    public String getFestival() {
        return getFestival(mSolarMonth, mSolarDay);
    }

    private String getFestival(int solarMonth, int solarDay) {
        String festivalStr = "";
        // lunar optimization
        switch (solarMonth) {
            case 0:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[0];
                }
                break;
            case 1:
                if (solarDay == 14) {
                    festivalStr = mFestivalStr[1];
                }
                break;
            case 2:
                if (solarDay == 8) {
                    festivalStr = mFestivalStr[2];
                } else if (solarDay == 12) {
                    festivalStr = mFestivalStr[3];
                }
                break;
            case 3:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[12];
                }
                break;
            case 4:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[4];
                } else if (solarDay == 4) {
                    festivalStr = mFestivalStr[5];
                }
                break;
            case 5:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[6];
                }
                break;
            case 6:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[7];
                }
                break;
            case 7:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[8];
                }
                break;
            case 8:
                if (solarDay == 10) {
                    festivalStr = mFestivalStr[9];
                }
                break;
            case 9:
                if (solarDay == 1) {
                    festivalStr = mFestivalStr[10];
                }
                break;
            case 11:
                if (solarDay == 25) {
                    festivalStr = mFestivalStr[11];
                }
                break;
            default:
                break;
        }
        return festivalStr;
    }

    /* UNISOC: Modify for bug1391668 {@ */
    /**
     * correct the algorithm of getting solar terms
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private String getSolarTerm(int year, int month, int day) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // 创建异步执行任务:
            CompletableFuture<String[]> cf = CompletableFuture.supplyAsync(() -> SolarTermUtil.getSolarTerms(year, mLunarTerm));
            // 如果执行成功:
            cf.thenAccept((result) -> {
                Log.d("price finish: " , Arrays.toString(result));
                SOLAR_TERMS.put(year, result);
            });
            // 如果执行异常:
            cf.exceptionally((e) -> {
                e.printStackTrace();
                Log.e("price error:",e.toString());
                return SOLAR_TERMS.put(year, SolarTermUtil.getSolarTerms(year, mLunarTerm));
            });
        }else {
            SOLAR_TERMS.put(year, SolarTermUtil.getSolarTerms(year, mLunarTerm));
        }
        String[] solarTerm = SOLAR_TERMS.get(year);
        String text = String.format("%s%s", year, (month >= 10 ? String.valueOf(month) : "0" + month) + (day >= 10 ? day : "0" + day));
        String termStr = "";
        if (solarTerm != null) {
            for (String solarTermName : solarTerm) {
                if (solarTermName.contains(text)) {
                    termStr = solarTermName.replace(text, "");
                    break;
                }
            }
        }
        return termStr;
    }
    /* }@ */

    private String getChinaMonthString() {
        return getChinaMonthString(mLunarMonth, mIsLeapMonth);
    }

    private String getChinaMonthString(int lunarMonth, boolean isLeapMonth) {
        String chinaMonth = (isLeapMonth ? mLunarLeapTag : "")
                + ((lunarMonth == 1) ? zhengyueTag
                        : mLunarCalendarNumber[lunarMonth - 1]) + mLunarMonthTag;
        return chinaMonth;
    }

    private String getChinaDayString(boolean notDisplayLunarMonthForFirstDay) {
        return getChinaDayString(mLunarMonth, mLunarDay, mIsLeapMonth,
                notDisplayLunarMonthForFirstDay);
    }

    public String getChinaDayString(int lunarMonth, int lunarDay,
            boolean isLeapMonth, boolean notDisplayLunarMonthForFirstDay) {
        if (lunarDay > 30) {
            return "";
        }
        if (lunarDay == 1 && notDisplayLunarMonthForFirstDay) {
            return getChinaMonthString(lunarMonth, isLeapMonth);
        }
        if (lunarDay == 10) {
            return mLunarCalendarTen[0] + mLunarCalendarTen[1];
        }
        if (lunarDay == 20) {
            return mLunarCalendarTen[4] + mLunarCalendarTen[1];
        }

        return mLunarCalendarTen[lunarDay / 10]
                + mLunarCalendarNumber[(lunarDay + 9) % 10];
    }

    private String getChinaYearString() {
        return getChinaYearString(mLunarYear);
    }

    private String getChinaYearString(int lunarYear) {
        return String.valueOf(lunarYear);
    }

    private String getLunarYearString(int num) {
        return (mYearStemStr[num % 10] + mYearBranchStr[num % 12]);
    }

    public String getLunarYear(int year) {
        int num = year - 1900 + 36;
        return getLunarYearString(num);
    }

    public String animalsYear(int year) {
        return mYearOfBirth[(year - 4) % 12];
    }

    public String[] getLunarCalendarInfo(boolean notDisplayLunarMonthForFirstDay) {
        if (mLunarYear == 0 || mLunarMonth == 0 || mLunarDay == 0){
            return null;// new String[]{null,null,null,null,null};
        }
        String lunarYearStr = getChinaYearString();
        String lunarMonthStr = getChinaMonthString();
        String lunarDayStr = getChinaDayString(notDisplayLunarMonthForFirstDay);

        String traditionFestivalStr = getTraditionalFestival();
        String festivalStr = getFestival();
        //correct the algorithm of getting solar terms
        String solarTermStr = getSolarTerm(mSolarYear, mSolarMonth + 1, mSolarDay);// UNISOC: Modify for bug1391668

        return new String[] { lunarYearStr, lunarMonthStr, lunarDayStr,
                traditionFestivalStr, festivalStr, solarTermStr };
    }

    public String getLunarDayInfo() {

            if (mLunarYear == 0 || mLunarMonth == 0 || mLunarDay == 0) {
                return "";
            }
            // if this day is traditional festival, show as it
            String traditionFestivalStr = getTraditionalFestival();
            String festivalStr = getFestival();
            // correct the algorithm of getting solar terms
            String solarTermStr = getSolarTerm(mSolarYear, mSolarMonth + 1, mSolarDay);// UNISOC: Modify for bug1391668
            mIsFastival = !traditionFestivalStr.trim().isEmpty()
                    || !festivalStr.trim().isEmpty()
                    || !solarTermStr.trim().isEmpty();

            if (traditionFestivalStr != null && festivalStr != null
                    && !traditionFestivalStr.trim().isEmpty()
                    && !festivalStr.trim().isEmpty()) {
                return traditionFestivalStr + "/" + festivalStr;
            }

            if (traditionFestivalStr != null && solarTermStr != null
                    && !traditionFestivalStr.trim().isEmpty()
                    && !solarTermStr.trim().isEmpty()) {
                return traditionFestivalStr + "/" + solarTermStr;
            }

            if (festivalStr != null && solarTermStr != null
                    && !festivalStr.trim().isEmpty()
                    && !solarTermStr.trim().isEmpty()) {
                return festivalStr + "/" + solarTermStr;
            }

            if (traditionFestivalStr != null
                    && !traditionFestivalStr.trim().isEmpty()) {
                return traditionFestivalStr;
            }

            // if this day is festival, show as it
            if (festivalStr != null && !festivalStr.trim().isEmpty()) {
                return festivalStr;
            }

            // if this day is solar term, show as it
            if (solarTermStr != null && !solarTermStr.trim().isEmpty()) {
                return solarTermStr;
            }

            // if this day is first day of lunar month, show lunar month number
            String lunarMonthStr = getChinaMonthString();
            if (mLunarDay == 1) {
                return lunarMonthStr;
            }

            // otherwise, show lunar day number
            String lunarDayStr = getChinaDayString(false);
            return lunarDayStr;

    }
}
