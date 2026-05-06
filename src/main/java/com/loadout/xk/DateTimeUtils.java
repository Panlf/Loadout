package com.loadout.xk;

import com.xkzhangsan.time.LunarDate;
import com.xkzhangsan.time.holiday.Holiday;
import com.xkzhangsan.time.converter.DateTimeConverterUtil;
import com.xkzhangsan.time.calculator.DateTimeCalculatorUtil;
import com.xkzhangsan.time.formatter.DateTimeFormatterUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间工具类（基于 xk-time 3.2.4）
 * 提供日期转换、格式化、计算、农历、节气、节日等功能。
 * @author panlf
 * @date 2026/5/6
 */
public class DateTimeUtils {
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 1. 类型转换 ====================
    public static LocalDateTime toLocalDateTime(Date date) {
        return DateTimeConverterUtil.toLocalDateTime(date);
    }

    public static Date toDate(LocalDateTime localDateTime) {
        return DateTimeConverterUtil.toDate(localDateTime);
    }

    public static LocalDate toLocalDate(Date date) {
        return DateTimeConverterUtil.toLocalDate(date);
    }

    public static LocalTime toLocalTime(Date date) {
        return DateTimeConverterUtil.toLocalTime(date);
    }

    public static ZonedDateTime toZonedDateTime(Date date) {
        return DateTimeConverterUtil.toZonedDateTime(date);
    }

    // ==================== 2. 格式化和解析 ====================
    public static String formatDefaultDate(Date date) {
        return DateTimeFormatterUtil.formatToDateStr(date);
    }

    public static String formatDefaultDateTime(Date date) {
        return DateTimeFormatterUtil.formatToDateTimeStr(date);
    }

    public static String format(Date date, String pattern) {
        return DateTimeFormatterUtil.format(date, pattern);
    }

    public static String format(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static Date parseDate(String text, String... patterns) {
        return DateTimeFormatterUtil.parseToDate(text, patterns);
    }

    public static LocalDateTime parseLocalDateTime(String text, String... patterns) {
        return DateTimeFormatterUtil.parseToLocalDateTime(text, patterns);
    }

    // ==================== 3. 时间计算 ====================
    public static Date plusDays(Date date, long days) {
        return DateTimeCalculatorUtil.plusDays(date, days);
    }

    public static Date plusMonths(Date date, long months) {
        return DateTimeCalculatorUtil.plusMonths(date, months);
    }

    public static Date plusYears(Date date, long years) {
        return DateTimeCalculatorUtil.plusYears(date, years);
    }

    public static long daysBetween(Date start, Date end) {
        return Math.abs(DateTimeCalculatorUtil.betweenTotalDays(start, end));
    }

    public static long millisBetween(Date start, Date end) {
        return Math.abs(end.getTime() - start.getTime());
    }

    // ==================== 4. 时间属性提取 ====================
    public static int getYear(Date date) {
        return DateTimeCalculatorUtil.getYear(date);
    }

    public static int getMonth(Date date) {
        return DateTimeCalculatorUtil.getMonth(date);
    }

    public static int getDayOfMonth(Date date) {
        return DateTimeCalculatorUtil.getDayOfMonth(date);
    }

    public static int getDayOfYear(Date date) {
        return DateTimeCalculatorUtil.getDayOfYear(date);
    }

    public static int getDayOfWeek(Date date) {
        return DateTimeCalculatorUtil.getDayOfWeek(date);
    }

    public static String getDayOfWeekCn(Date date) {
        return DateTimeCalculatorUtil.getDayOfWeekCn(date);
    }

    public static String getMonthCn(Date date) {
        return DateTimeCalculatorUtil.getMonthCnLong(date);
    }

    public static int lengthOfMonth(Date date) {
        return DateTimeCalculatorUtil.lengthOfMonth(date);
    }

    // ==================== 5. 农历功能（基于 LunarDate） ====================
    private static LunarDate getLunarDate(Date date) {
        return LunarDate.from(date);
    }

    public static int getLunarYear(Date date) {
        return getLunarDate(date).getlYear();
    }

    public static int getLunarMonth(Date date) {
        return getLunarDate(date).getlMonth();
    }

    public static int getLunarDay(Date date) {
        return getLunarDate(date).getlDay();
    }

    public static boolean isLunarLeapMonth(Date date) {
        return "闰".equals(getLunarDate(date).getLeapMonthCn());
    }

    public static String getLunarMonthNameCn(Date date) {
        return getLunarDate(date).getlMonthCn();
    }

    public static String getLunarDayNameCn(Date date) {
        return getLunarDate(date).getlDayCn();
    }

    public static String getFullLunarDateCn(Date date) {
        return getLunarDate(date).getlDateCn();
    }

    public static String getChineseZodiac(Date date) {
        return getLunarDate(date).getlAnimal();
    }

    public static String getGanZhi(Date date) {
        return getLunarDate(date).getSuiCi();
    }

    // ==================== 6. 节气功能 ====================
    public static String getSolarTerm(Date date) {
        String term = getLunarDate(date).getSolarTerm();
        return (term == null || term.isEmpty()) ? null : term;
    }

    // ==================== 7. 节日功能（基于 Holiday 接口） ====================
    public static String getChineseHoliday(Date date) {
        return Holiday.getChineseHoliday(date);
    }

    public static String getLocalHoliday(Date date) {
        return Holiday.getLocalHoliday(date);
    }

    public static String getAllHoliday(Date date) {
        String chinese = getChineseHoliday(date);
        String local = getLocalHoliday(date);
        if (chinese != null && !chinese.isEmpty()) {
            return !local.isEmpty() ? local + " " + chinese : chinese;
        }
        return local;
    }
}
