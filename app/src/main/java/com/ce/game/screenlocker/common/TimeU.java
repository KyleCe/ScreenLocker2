package com.ce.game.screenlocker.common;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by KyleCe on 2016/6/1.
 *
 * @author: KyleCe
 */
public class TimeU {

    // 时间格式模板
    /**
     * yyyy-MM-dd
     */
    public static final String TIME_FORMAT_ONE = "yyyy-MM-dd";
    /**
     * yyyy-MM-dd HH:mm
     */
    public static final String TIME_FORMAT_TWO = "yyyy-MM-dd HH:mm";
    /**
     * yyyy-MM-dd HH:mmZ
     */
    public static final String TIME_FORMAT_THREE = "yyyy-MM-dd HH:mmZ";
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static final String TIME_FORMAT_FOUR = "yyyy-MM-dd HH:mm:ss";
    /**
     * yyyy-MM-dd HH:mm:ss.SSSZ
     */
    public static final String TIME_FORMAT_FIVE = "yyyy-MM-dd HH:mm:ss.SSSZ";
    /**
     * yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final String TIME_FORMAT_SIX = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    /**
     * HH:mm:ss
     */
    public static final String TIME_FORMAT_SEVEN = "HH:mm:ss";
    /**
     * HH:mm:ss.SS
     */
    public static final String TIME_FORMAT_EIGHT = "HH:mm:ss.SS";
    /**
     * yyyy.MM.dd
     */
    public static final String TIME_FORMAT_9 = "yyyy.MM.dd";
    /**
     * MM月dd�?
     */
    public static final String TIME_FORMAT_10 = "MM月dd日";
    public static final String TIME_FORMAT_11 = "MM-dd HH:mm";
    public static final String TIME_FORMAT_12 = "yyMM";
    public static final String TIME_FORMAT_13 = "yyyyMMdd-HH";
    /**
     * HH:mm
     */
    public static final String TIME_FORMAT_14 = "HH:mm";
    public static final String TIME_FORMAT_15 = "MM-dd";
    public static final String TIME_FORMAT_16 = "yy-MM-dd";
    public static final String TIME_FORMAT_17 = "dd/MM E HH:mm";
    public static final String TIME_FORMAT_18 = "MM-dd HH:mm:ss";


    // 时间常量
    public static final long SECOND_MS = 1000;
    public static final long MINUTE_MS = 60 * SECOND_MS;
    public static final long HOUR_MS = 60 * MINUTE_MS;
    public static final long DAY_MS = 24 * HOUR_MS;
    public static final long MONTH_MS = 30 * DAY_MS;
    public static final long MONTH_MS_28D = 28 * DAY_MS;
    public static final long MONTH_MS_29D = 29 * DAY_MS;
    public static final long MONTH_MS_30D = MONTH_MS;
    public static final long MONTH_MS_31D = 31 * DAY_MS;
    public static final long YEAR_MS = 365 * DAY_MS;

    public static final int M_OF_1HOUR = 60;
    public static final int M_OF_1DAY = 60 * 24;


    /**
     * 根据时间格式获得当前时间
     */
    public static String getCurrentTime(String formater) {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(formater,
                Locale.SIMPLIFIED_CHINESE);
        return dateFormat.format(date);
    }

    /**
     * 格式化时�?
     */
    public static String formatTime(long time, String format) {
        return new SimpleDateFormat(format).format(new Date(time));
    }

    /**
     * 判断是否是合法的时间
     */
    public static boolean isValidDate(String dateString, String format) {
        return parseTime(dateString, format) > -1;
    }

    /**
     * 日期转换
     */
    public static long parseTime(String dateString, String format) {
        if (dateString == null || dateString.length() == 0) {
            return -1;
        }
        try {
            return new SimpleDateFormat(format).parse(dateString).getTime();
        } catch (ParseException e) {

        }
        return -1;
    }

    public static int getDaysBetween(String date1, String date2, String format) {
        return getDaysBetween(parseTime(date1, format),
                parseTime(date2, format));
    }

    public static int getDaysBetween(long date1, long date2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(date1);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date2);
        c2.set(Calendar.HOUR_OF_DAY, 0);
        c2.set(Calendar.MINUTE, 0);
        c2.set(Calendar.SECOND, 0);
        c2.set(Calendar.MILLISECOND, 0);

        return (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / (24 * 3600 * 1000));
    }

    /**
     * Unix时间戳转换成日期
     */
    public static String TimeStamp2Date(String timestampString, String formater) {
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new SimpleDateFormat(formater, Locale.SIMPLIFIED_CHINESE)
                .format(new Date(timestamp));
        return date;
    }

    public static long getTodayTimeMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String getTimeByLong4Msg(long tLong) {
        String strDate = "";
        tLong = tLong * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(tLong);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
        // strDate = cal.getTime().toLocaleString();
        strDate = sdf.format(cal.getTime());
        return strDate;
    }

    /**
     * 获取默认格式的时�?
     *
     * @return "MM-dd HH:mm"
     */
    public static String getDefaultTime() {
        String strDate = "";
        long currentTime = System.currentTimeMillis();
        currentTime = currentTime * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTime);
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        // strDate = cal.getTime().toLocaleString();
        strDate = sdf.format(cal.getTime());
        return strDate;
    }

    /**
     * 用户自己设置指定时间格式的日期
     *
     * @param format 日期格式
     * @return 日期
     */
    public static String getTimeByLong(String format) {
        String strDate = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        strDate = sdf.format(cal.getTime());
        return strDate;
    }

    /**
     * 去除字符串中的空格�?回车、换行符、制表符
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * String转long
     */
    public static long parseLong(String str, long defaultValue) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 判断字符串是否有�?
     *
     * @deprecated
     */
    public static boolean isValidate(String str) {
        if (str != null && str.length() > 0) {
            return true;
        }
        return false;
    }


    /**
     * 返回指定日期距离19700101的时间差（毫秒）
     */
    public static String get15TimeStap() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -16);
        return Long.toString(c.getTimeInMillis());
    }


    /*
    * minute 为分钟数（可以超过60）
    * 格式为 1d 2h 33 m
    * */
    public static String getFormatTime(long minute) {
        if (minute < 0)
            return "0";
        long d = 0, h = 0, m = 0;
        if (minute > 60) {
            h = minute / 60;
            if (h > 24) {
                d = h / 24;
                if (d > 0)
                    h = h % 24;
            }
            m = minute % 60;
        } else m = minute;
        if (d > 0) return d + "d " + h + "h " + m + "m";
        else if (h > 0) return h + "h " + m + "m";
        else if (m >= 0) return m + "m";
        else return "0";
    }


    /*
   * 格式为 1d 2h 33 m, x h xxm， xx m 格式的字符串中转换为分钟数
   *       1DD2HH33MM
   * */
    public static long getMinutesFromString(final String tStr) {
        if (tStr == null || tStr.length() == 0) return 0;
        char[] chs = tStr.toCharArray();
        long result = 0;
        StringBuilder num = new StringBuilder();
        int len = chs.length;
        long key;
        for (int i = 0; i < len; i++) {
            if (charIsNumber(chs[i])) {
                num.append(chs[i]);
                if (i != len - 1 && -1 != (key = charIsD_H_M(chs[i + 1]))) {
                    result += Integer.valueOf(num.toString()) * key;
                    num.delete(0, num.length());
                    i++;
                } else continue;
            }
        }
        return result;
    }

    private static boolean charIsNumber(char ch) {
        char[] chs = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char c : chs) if (c == ch) return true;
        return false;
    }

    private static int charIsD_H_M(char c) {
        if (c == 'd' || c == 'D') return 24 * 60;
        if (c == 'h' || c == 'H') return 60;
        if (c == 'm' || c == 'M') return 1;
        return -1;
    }

    /* 将Server传送的UTC时间转换为指定时区的时间 */
    public static String converTime(String srcTime, TimeZone timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dspFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String convertTime;

        Date result_date;
        long result_time = 0;

        // 如果传入参数异常，使用本地时间
        if (null == srcTime) {
            result_time = System.currentTimeMillis();
        } else {
            // 将输入时间字串转换为UTC时间
            try {
                sdf.setTimeZone(TimeZone.getTimeZone("GMT00:00"));
                result_date = sdf.parse(srcTime);

                result_time = result_date.getTime();
            } catch (Exception e) {
                // 出现异常时，使用本地时间
                result_time = System.currentTimeMillis();
                dspFmt.setTimeZone(TimeZone.getDefault());
                convertTime = dspFmt.format(result_time);
                return convertTime;
            }
        }

        // 设定时区
        dspFmt.setTimeZone(timezone);
        convertTime = dspFmt.format(result_time);

        Log.e("current zone:", "id=" + sdf.getTimeZone().getID()
                + "  name=" + sdf.getTimeZone().getDisplayName());

        return convertTime;
    }


    public static String getTimeByZoneFormat(String timeZone, String timeFormat) {
//        timeZone = "Asia/Shanghai";
//        timeFormat = TIME_FORMAT_14;
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));   //修改时区 // TODO: 2015/9/15 zone
        SimpleDateFormat dateFormat = new SimpleDateFormat(timeFormat);  //HH:24小时制  hh:12小时制
        return dateFormat.format(new Date());
    }


}
