/** SPRD: Modify for bug473571, add lunar info */
package com.sprd.calendar.lunar;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LunarCalendarConvertUtil {

    public static final boolean SUPPORT_LUNAR = true;
    /* UNISOC: Modify for bug1400819  {@ */
    private final static int[] mLunarCalendarBaseInfo = new int[] {
            19416, 19168, 42352, 21717, 53856, 55632, 21844, 22191, 39632, 21970, //1900-1909
            19168, 42422, 42192, 53840, 53909, 46415, 54944, 44450, 38320, 18807, //1910-1919
            18815, 42160, 46261, 27216, 27968, 43860, 11119, 38256, 21234, 18800, //1920-1929
            25958, 54432, 59984, 27285, 23263, 11104, 34531, 37615, 51415, 51551, //1930-1939
            54432, 55462, 46431, 22176, 42420, 9695,  37584, 53938, 43344, 46423, //1940-1949
            27808, 46416, 21333, 19887, 42416, 17779, 21183, 43432, 59728, 27296, //1950-1959
            44710, 43856, 19296, 43748, 42352, 21088, 62051, 55632, 23383, 22176, //1960-1969
            38608, 19925, 19152, 42192, 54484, 53840, 54616, 46400, 46752, 38310, //1970-1979
            38335, 18864, 43380, 42160, 45690, 27216, 27968, 44870, 43872, 38256, //1980-1989
            19189, 18800, 25776, 29859, 59984, 27480, 23232, 43872, 38613, 37600, //1990-1999
            51552, 55636, 54432, 55888, 30034, 22176, 43959, 9680,  37584, 51893, //2000-2009
            43344, 46240, 47780, 44368, 21977, 19360, 42416, 20854, 21183, 43312, //2010-2019
            31060, 27296, 44368, 23378, 19296, 42726, 42208, 53856, 60005, 54576, //2020-2029
            23200, 30371, 38608, 19195, 19152, 42192, 53430, 53855, 54560, 56645, //2030-2039
            46496, 22224, 21938, 18864, 42359, 42160, 43600, 45653, 27951, 44448};//2040-2049
    /* }@ */
    private final static byte[] mLunarCalendarSpecialInfo = new byte[] { 0x08,
            0x00, 0x00, 0x05, 0x00, 0x00, 0x14, 0x00, 0x00, 0x02, 0x00, 0x06,
            0x00, 0x00, 0x15, 0x00, 0x00, 0x02, 0x00, 0x17, 0x00, 0x00, 0x05,
            0x00, 0x00, 0x14, 0x00, 0x00, 0x02, 0x00, 0x06, 0x00, 0x00, 0x05,
            0x00, 0x00, 0x13, 0x00, 0x17, 0x00, 0x00, 0x16, 0x00, 0x00, 0x14,
            0x00, 0x00, 0x02, 0x00, 0x07, 0x00, 0x00, 0x15, 0x00, 0x00, 0x13,
            0x00, 0x08, 0x00, 0x00, 0x06, 0x00, 0x00, 0x04, 0x00, 0x00, 0x03,
            0x00, 0x07, 0x00, 0x00, 0x05, 0x00, 0x00, 0x04, 0x00, 0x08, 0x00,
            0x00, 0x16, 0x00, 0x00, 0x04, 0x00, 0x0a, 0x00, 0x00, 0x06, 0x00,
            0x00, 0x05, 0x00, 0x00, 0x03, 0x00, 0x08, 0x00, 0x00, 0x05, 0x00,
            0x00, 0x04, 0x00, 0x00, 0x02, 0x00, 0x07, 0x00, 0x00, 0x05, 0x00,
            0x00, 0x04, 0x00, 0x09, 0x00, 0x00, 0x16, 0x00, 0x00, 0x04, 0x00,
            0x00, 0x02, 0x00, 0x06, 0x00, 0x00, 0x05, 0x00, 0x00, 0x03, 0x00,
            0x07, 0x00, 0x00, 0x16, 0x00, 0x00, 0x05, 0x00, 0x00, 0x02, 0x00,
            0x07, 0x00, 0x00, 0x15, 0x00, 0x00 };
    private final static long[] mSolarTermInfo = new long[] { 0, 21208, 42467,
            63836, 85337, 107014, 128867, 150921, 173149, 195551, 218072,
            240693, 263343, 285989, 308563, 331033, 353350, 375494, 397447,
            419210, 440795, 462224, 483532, 504758 };
    private final static int[] mAllLunarDays = new int[] {25219,
            25573, 25928, 26312, 26666, 27020, 27404, 27758, 28142, 28496, 28851,
            29235, 29590, 29944, 30328, 30682, 31066, 31420, 31774, 32158, 32513,
            32868, 33252, 33606, 33960, 34343, 34698, 35082, 35436, 35791, 36175,
            36529, 36883, 37267, 37621, 37976, 38360, 38714, 39099, 39453, 39807,
            40191, 40545, 40899, 41283, 41638, 42022, 42376, 42731, 43115, 43469,
            43823, 44207, 44561, 44916, 45300, 45654, 46038, 46392, 46746, 47130,
            47485, 47839, 48223, 48578, 48962, 49316, 49670, 50054, 50408, 50762 };
    private final static int[] mLunarDays = new int[] { 354,
            355, 384, 354, 354, 384, 354, 384, 354, 355, 384,
            355, 354, 384, 354, 384, 354, 354, 384, 355, 355,
            384, 354, 354, 383, 355, 384, 354, 355, 384, 354,
            354, 384, 354, 355, 384, 354, 385, 354, 354, 384,
            354, 354, 384, 355, 384, 354, 355, 384, 354, 354,
            384, 354, 355, 384, 354, 384, 354, 354, 384, 355,
            354, 384, 355, 384, 354, 354, 384, 354, 354, 384,
            355, 355, 384, 354, 384, 354, 354, 384, 354, 355 };
    private final static int mBaseYear = 1900;
    private final static int mStartYear = 1969;
    private static int mBeginYear = 1969;
    private final static int mOutBoundYear = 2050;
    private static long mBaseDayTime = 0;
    private final static int mBigMonthDays = 30;
    private final static int mSmallMonthDays = 29;

    static {
        // use Calendar.getTime.getTime() to return milliseconds for we don't need timezone info
        // 1900-1-31, it is the first day of Gengzi year in lunar
        /* UNISOC: Modify for bug1391668  {@ */
        Calendar tempDate  = Calendar.getInstance();
        tempDate.set(1900, 0, 31);
        mBaseDayTime = tempDate.getTime().getTime();
        /* }@ */
    }

    /*
     * correct the algorithm of getting solar terms
     */
    public static int getSolarTermDayOfMonth(int year, int n) {
        //correct the algorithm of getting solar terms
        /* UNISOC: Modify for bug1213747{@ */
        Calendar mOffDateCalendar = Calendar.getInstance();
        mOffDateCalendar.set(1900, 0, 6, 2, 5, 0);
        long mMilliSecondsForSolarTerm = mOffDateCalendar.getTime().getTime();
        /* }@ */
        mOffDateCalendar.setTime(new Date((long) ((31556925974.7 * (year - 1900)
                + mSolarTermInfo[n] * 60000L) + mMilliSecondsForSolarTerm)));
        return mOffDateCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getLunarMonthDays(int lunarYear, int lunarMonth) {
        if (isLunarBigMonth(lunarYear, lunarMonth)) {
            return mBigMonthDays;
        } else {
            return mSmallMonthDays;
        }
    }

    public static boolean isLunarBigMonth(int lunarYear, int lunarMonth) {
        /* UNISOC: Modify for bug1400819 {@ */
        int lunarYearBaseInfo = mLunarCalendarBaseInfo[lunarYear - mBaseYear];
        if ((lunarYearBaseInfo & (0x10000 >> lunarMonth)) != 0) {
            return true;
        } else {
            return false;
        }
        /* }@ */
    }

    final public static int getYearDays(int lunarYear) {
        int retSum = 0;
        for (int iLunarMonth = 1; iLunarMonth <= 12; iLunarMonth++) {
            retSum += getLunarMonthDays(lunarYear, iLunarMonth);
        }
        return (retSum + getLeapMonthDays(lunarYear));
    }

    final public static int getLeapMonth(int lunarYear) {
        return mLunarCalendarSpecialInfo[lunarYear - mBaseYear] & 0xf;
    }

    final public static int getLeapMonthDays(int lunarYear) {
        if (getLeapMonth(lunarYear) == 0) {
            return 0;
        } else if ((mLunarCalendarSpecialInfo[lunarYear - mBaseYear] & 0x10) != 0) {
            return mBigMonthDays;
        } else {
            return mSmallMonthDays;
        }
    }

    public static void parseLunarCalendar(int year, int month, int day,
            LunarCalendar lunarCalendar) {
        if (lunarCalendar == null) {
            return;
        }

        int leapLunarMonth = 0;
        Date presentDate = null;
        boolean isLeapMonth = false;
        presentDate = new Date(year - mBaseYear, month, day);
        // we use Math.ceil() here because offsetDayNum some time be truncate
        // this will cause we lost one day
        int offsetDayNum = (int) Math
                .ceil((presentDate.getTime() - mBaseDayTime) * 1.0 / 86400000L);

        mBeginYear = year-1;
        if(mBeginYear<1969){
            mBeginYear = 1969;
        }
        offsetDayNum -= mAllLunarDays[mBeginYear-mStartYear];

        int lunarYear = 0;
        int lunarMonth = 0;
        int lunarDay = 0;

        for (lunarYear = mBeginYear; lunarYear < mOutBoundYear; lunarYear++) {
            int daysOfLunarYear = mLunarDays[lunarYear-mStartYear];
            if (offsetDayNum < daysOfLunarYear) {
                break;
            }
            offsetDayNum -= daysOfLunarYear;
        }
        if (offsetDayNum < 0 || lunarYear == mOutBoundYear) {
            return;
        }

        leapLunarMonth = getLeapMonth(lunarYear);

        for (lunarMonth = 1; lunarMonth <= 12; lunarMonth++) {
            int daysOfLunarMonth = 0;
            if (isLeapMonth) {
                daysOfLunarMonth = getLeapMonthDays(lunarYear);
            } else {
                daysOfLunarMonth = getLunarMonthDays(lunarYear, lunarMonth);
            }

            if (offsetDayNum < daysOfLunarMonth) {
                break;
            } else {
                offsetDayNum -= daysOfLunarMonth;
                if (lunarMonth == leapLunarMonth) {
                    if (!isLeapMonth) {
                        lunarMonth--;
                        isLeapMonth = true;
                    } else {
                        isLeapMonth = false;
                    }
                }
            }
        }

        lunarDay = offsetDayNum + 1;

        lunarCalendar.mLunarYear = lunarYear;
        lunarCalendar.mLunarMonth = lunarMonth;
        lunarCalendar.mLunarDay = lunarDay;
        lunarCalendar.mIsLeapMonth = isLeapMonth;

        lunarCalendar.mSolarYear = year;
        lunarCalendar.mSolarMonth = month;
        lunarCalendar.mSolarDay = day;
    }

    // lunar optimization
    public static void parseLunarCalendarYear(int year, int month, int day,
            LunarCalendar lunarCalendar) {
        if (lunarCalendar == null) {
            return;
        }

        Date presentDate = null;
        presentDate = new Date(year - mBaseYear, month, day);
        // we use Math.ceil() here because offsetDayNum some time be truncate
        // this will cause we lost one day
        int offsetDayNum = (int) Math
                .ceil((presentDate.getTime() - mBaseDayTime) * 1.0 / 86400000L);

        mBeginYear = year - 1;
        if (mBeginYear < 1969) {
            mBeginYear = 1969;
        }
        offsetDayNum -= mAllLunarDays[mBeginYear - mStartYear];

        int lunarYear = 0;

        for (lunarYear = mBeginYear; lunarYear < mOutBoundYear; lunarYear++) {
            int daysOfLunarYear = mLunarDays[lunarYear - mStartYear];
            if (offsetDayNum < daysOfLunarYear) {
                break;
            }
            offsetDayNum -= daysOfLunarYear;
        }
        if (offsetDayNum < 0 || lunarYear == mOutBoundYear) {
            return;
        }

        lunarCalendar.mLunarYear = lunarYear;
    }

    public static boolean isLunarSetting() {
        String language = getLanguageEnv();

        if (language != null
                && (language.trim().equals("zh-CN") || language.trim().equals(
                        "zh-TW"))) {
            return true;
        } else {
            return false;
        }
    }

    private static String getLanguageEnv() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry().toLowerCase();
        if ("zh".equals(language)) {
            if ("cn".equals(country)) {
                language = "zh-CN";
            } else if ("tw".equals(country)) {
                language = "zh-TW";
            }
        } else if ("pt".equals(language)) {
            if ("br".equals(country)) {
                language = "pt-BR";
            } else if ("pt".equals(country)) {
                language = "pt-PT";
            }
        }
        return language;
    }

}