package com.easypan.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 时间工具类
 * @Author: KunSpireUp
 */
public class DateUtils {

    private static final Object lookObj = new Object();

    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<>();

    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> threadLocal = sdfMap.get(pattern);
        if (threadLocal == null) {
            synchronized (lookObj) {
                threadLocal = sdfMap.get(pattern);
                if (threadLocal == null) {
                    threadLocal = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                }
            }
        }
        return threadLocal.get();
    }

    public static String format(Date date, String pattern) {
        return getSdf(pattern).format(date);
    }

    public static Date parse(String date, String pattern) {
        try {
            return getSdf(pattern).parse(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Date getAfterDate(Integer day){
        Calendar calendar=Calendar.getInstance();
        calendar.add(calendar.DAY_OF_YEAR,day);
        return calendar.getTime();
    }
}
