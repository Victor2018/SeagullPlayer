package com.victor.player.library.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Administrator on 2017/12/11.
 */

public class DateUtil {
    private static String TAG = "DateUtil";
    /**
     * 获取今天的日期
     * @return
     */
    public static String getRequestTime () {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String today = formatter.format(date);
        return today;
    }

    public static String getCurrentYear () {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String today = formatter.format(date);
        return today;
    }
    public static String getCurrentMonth () {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        String today = formatter.format(date);
        return today;
    }
    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    public static String formatPlayTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("00:%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String formatBirthTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy-MM");
        return format.format(date);
    }

    /**
     * 根据年 月 获取对应的月份 天数
     * */
    public static int getDaysByYearMonth(int year, int month) {

        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month - 1);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取当前时间的年
     * @return
     */
    public static String getNowYear() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String today = formatter.format(date);
        return today;
    }

    /**
     * 返回的字符串形式是形如：07-31 12:08
     * */
    public static String formatTimeInMillis(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        Date date = cal.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM-dd HH:mm");
        String fmt = dateFormat.format(date);

        return fmt;
    }

    /**
     * 返回的字符串形式是形如：03/17/1990
     * @param timeInMillis
     * @return
     */
    public static String formatDateInMillis(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        Date date = cal.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "MM/dd/yyyy");
        String fmt = dateFormat.format(date);

        return fmt;
    }

    public static long formatTime2Ms (String date) {
        long ms = 0;
        SimpleDateFormat sdf= new SimpleDateFormat("MM/dd/yyyy");
        Date dt = null;
        try {
            dt = sdf.parse(date);
            //继续转换得到毫秒数的long型
            ms = dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ms;
    }

}
