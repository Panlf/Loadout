package com.loadout.xk;

import java.util.Date;
/**
 *
 * @author panlf
 * @date 2026/5/6
 */
public class DateTimeTest {
    public static void main(String[] args) {
        Date now = new Date();

        // 基础格式化
        System.out.println("当前日期: " + DateTimeUtils.formatDefaultDate(now));
        System.out.println("当前日期时间: " + DateTimeUtils.formatDefaultDateTime(now));
        System.out.println("自定义格式: " + DateTimeUtils.format(now, "yyyy年MM月dd日 HH:mm:ss"));

        // 农历
        System.out.println("农历年份: " + DateTimeUtils.getLunarYear(now));
        System.out.println("农历月: " + DateTimeUtils.getLunarMonthNameCn(now));
        System.out.println("农历日: " + DateTimeUtils.getLunarDayNameCn(now));
        System.out.println("完整农历: " + DateTimeUtils.getFullLunarDateCn(now));
        System.out.println("生肖: " + DateTimeUtils.getChineseZodiac(now));
        System.out.println("干支: " + DateTimeUtils.getGanZhi(now));

        // 节气
        String term = DateTimeUtils.getSolarTerm(now);
        if (term != null) {
            System.out.println("节气: " + term);
        }

        // 节日
        String chineseHoliday = DateTimeUtils.getChineseHoliday(now);
        if (chineseHoliday != null && !chineseHoliday.isEmpty()) {
            System.out.println("农历节日: " + chineseHoliday);
        }
        String localHoliday = DateTimeUtils.getLocalHoliday(now);
        if (!localHoliday.isEmpty()) {
            System.out.println("公历节日: " + localHoliday);
        }

        // 时间计算
        Date nextWeek = DateTimeUtils.plusDays(now, 7);
        System.out.println("一周后: " + DateTimeUtils.formatDefaultDate(nextWeek));

        // 天数差
        Date newYear = DateTimeUtils.parseDate("2026-01-01", "yyyy-MM-dd");
        long daysToNewYear = DateTimeUtils.daysBetween(now, newYear);
        System.out.println("距离2026年元旦还有 " + daysToNewYear + " 天");
    }
}
