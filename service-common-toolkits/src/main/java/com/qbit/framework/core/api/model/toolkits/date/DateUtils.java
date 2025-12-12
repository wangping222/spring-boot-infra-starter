package com.qbit.framework.core.api.model.toolkits.date;

import com.qbit.framework.core.api.model.toolkits.constants.DateTimeFormatConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期工具类（补充Apache Commons Lang）
 * <p>
 * 优先使用 {@link org.apache.commons.lang3.time.DateUtils} 和 {@link org.apache.commons.lang3.time.DateFormatUtils}
 * <p>
 * 本工具类提供以下补充功能：
 * <ul>
 *   <li>Date与Java 8时间API的互转</li>
 *   <li>业务场景的日期计算（月初/月末、周初/周末等）</li>
 *   <li>日期范围判断与比较</li>
 *   <li>时间戳转换</li>
 * </ul>
 *
 * @author Qbit Framework
 */
public final class DateUtils {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private DateUtils() {
        throw new AssertionError("No DateUtils instances for you!");
    }

    // ==================== Date与Java 8时间API互转 ====================

    /**
     * Date转LocalDateTime
     *
     * @param date Date对象
     * @return LocalDateTime，如果date为null则返回null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }

    /**
     * Date转LocalDate
     *
     * @param date Date对象
     * @return LocalDate，如果date为null则返回null
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDate();
    }

    /**
     * Date转LocalTime
     *
     * @param date Date对象
     * @return LocalTime，如果date为null则返回null
     */
    public static LocalTime toLocalTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalTime();
    }

    /**
     * LocalDateTime转Date
     *
     * @param localDateTime LocalDateTime对象
     * @return Date，如果localDateTime为null则返回null
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    /**
     * LocalDate转Date（时间部分为00:00:00）
     *
     * @param localDate LocalDate对象
     * @return Date，如果localDate为null则返回null
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }

    // ==================== 时间戳转换 ====================

    /**
     * 获取当前时间戳（秒）
     *
     * @return 当前时间戳（秒）
     */
    public static long currentSecond() {
        return Instant.now().getEpochSecond();
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳（毫秒）
     */
    public static long currentMilli() {
        return System.currentTimeMillis();
    }

    /**
     * 时间戳（秒）转Date
     *
     * @param second 时间戳（秒）
     * @return Date对象
     */
    public static Date fromSecond(long second) {
        return Date.from(Instant.ofEpochSecond(second));
    }

    /**
     * 时间戳（毫秒）转Date
     *
     * @param milli 时间戳（毫秒）
     * @return Date对象
     */
    public static Date fromMilli(long milli) {
        return new Date(milli);
    }

    /**
     * Date转时间戳（秒）
     *
     * @param date Date对象
     * @return 时间戳（秒），如果date为null则返回0
     */
    public static long toSecond(Date date) {
        if (date == null) {
            return 0L;
        }
        return date.toInstant().getEpochSecond();
    }

    /**
     * Date转时间戳（毫秒）
     *
     * @param date Date对象
     * @return 时间戳（毫秒），如果date为null则返回0
     */
    public static long toMilli(Date date) {
        if (date == null) {
            return 0L;
        }
        return date.getTime();
    }

    // ==================== 日期格式化 ====================

    /**
     * 格式化Date为字符串（yyyy-MM-dd HH:mm:ss）
     *
     * @param date Date对象
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String formatDateTime(Date date) {
        return format(date, DateTimeFormatConstants.DATETIME_FORMAT);
    }

    /**
     * 格式化Date为字符串（yyyy-MM-dd）
     *
     * @param date Date对象
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String formatDate(Date date) {
        return format(date, DateTimeFormatConstants.DATE_FORMAT);
    }

    /**
     * 格式化Date为字符串（HH:mm:ss）
     *
     * @param date Date对象
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String formatTime(Date date) {
        return format(date, DateTimeFormatConstants.TIME_FORMAT);
    }

    /**
     * 格式化Date为字符串（自定义格式）
     *
     * @param date      Date对象
     * @param formatter DateTimeFormatter
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String format(Date date, DateTimeFormatter formatter) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date);
        return localDateTime.format(formatter);
    }

    /**
     * 格式化Date为字符串（自定义格式）
     *
     * @param date    Date对象
     * @param pattern 格式模板
     * @return 格式化后的字符串，如果date为null则返回null
     */
    public static String format(Date date, String pattern) {
        return format(date, DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 日期解析 ====================

    /**
     * 解析字符串为Date（yyyy-MM-dd HH:mm:ss）
     *
     * @param dateTimeStr 日期时间字符串
     * @return Date对象，如果dateTimeStr为null或空则返回null
     */
    public static Date parseDateTime(String dateTimeStr) {
        return parse(dateTimeStr, DateTimeFormatConstants.DATETIME_FORMAT);
    }

    /**
     * 解析字符串为Date（yyyy-MM-dd）
     *
     * @param dateStr 日期字符串
     * @return Date对象，如果dateStr为null或空则返回null
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatConstants.DATE_FORMAT);
        return toDate(localDate);
    }

    /**
     * 解析字符串为Date（自定义格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @param formatter   DateTimeFormatter
     * @return Date对象，如果dateTimeStr为null或空则返回null
     */
    public static Date parse(String dateTimeStr, DateTimeFormatter formatter) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return toDate(localDateTime);
    }

    /**
     * 解析字符串为Date（自定义格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     格式模板
     * @return Date对象，如果dateTimeStr为null或空则返回null
     */
    public static Date parse(String dateTimeStr, String pattern) {
        return parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ==================== 日期计算 ====================

    /**
     * 获取当天开始时间（00:00:00）
     *
     * @return 当天开始时间
     */
    public static Date beginOfDay() {
        return beginOfDay(new Date());
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     *
     * @param date 指定日期
     * @return 开始时间，如果date为null则返回null
     */
    public static Date beginOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).with(LocalTime.MIN);
        return toDate(localDateTime);
    }

    /**
     * 获取当天结束时间（23:59:59）
     *
     * @return 当天结束时间
     */
    public static Date endOfDay() {
        return endOfDay(new Date());
    }

    /**
     * 获取指定日期的结束时间（23:59:59）
     *
     * @param date 指定日期
     * @return 结束时间，如果date为null则返回null
     */
    public static Date endOfDay(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).with(LocalTime.MAX);
        return toDate(localDateTime);
    }

    /**
     * 获取当月第一天的开始时间
     *
     * @return 当月第一天的开始时间
     */
    public static Date beginOfMonth() {
        return beginOfMonth(new Date());
    }

    /**
     * 获取指定日期所在月份的第一天的开始时间
     *
     * @param date 指定日期
     * @return 第一天的开始时间，如果date为null则返回null
     */
    public static Date beginOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.firstDayOfMonth())
                .with(LocalTime.MIN);
        return toDate(localDateTime);
    }

    /**
     * 获取当月最后一天的结束时间
     *
     * @return 当月最后一天的结束时间
     */
    public static Date endOfMonth() {
        return endOfMonth(new Date());
    }

    /**
     * 获取指定日期所在月份的最后一天的结束时间
     *
     * @param date 指定日期
     * @return 最后一天的结束时间，如果date为null则返回null
     */
    public static Date endOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.lastDayOfMonth())
                .with(LocalTime.MAX);
        return toDate(localDateTime);
    }

    /**
     * 获取本周第一天（周一）的开始时间
     *
     * @return 本周第一天的开始时间
     */
    public static Date beginOfWeek() {
        return beginOfWeek(new Date());
    }

    /**
     * 获取指定日期所在周的第一天（周一）的开始时间
     *
     * @param date 指定日期
     * @return 第一天的开始时间，如果date为null则返回null
     */
    public static Date beginOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIN);
        return toDate(localDateTime);
    }

    /**
     * 获取本周最后一天（周日）的结束时间
     *
     * @return 本周最后一天的结束时间
     */
    public static Date endOfWeek() {
        return endOfWeek(new Date());
    }

    /**
     * 获取指定日期所在周的最后一天（周日）的结束时间
     *
     * @param date 指定日期
     * @return 最后一天的结束时间，如果date为null则返回null
     */
    public static Date endOfWeek(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .with(LocalTime.MAX);
        return toDate(localDateTime);
    }

    /**
     * 获取当年第一天的开始时间
     *
     * @return 当年第一天的开始时间
     */
    public static Date beginOfYear() {
        return beginOfYear(new Date());
    }

    /**
     * 获取指定日期所在年份的第一天的开始时间
     *
     * @param date 指定日期
     * @return 第一天的开始时间，如果date为null则返回null
     */
    public static Date beginOfYear(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.firstDayOfYear())
                .with(LocalTime.MIN);
        return toDate(localDateTime);
    }

    /**
     * 获取当年最后一天的结束时间
     *
     * @return 当年最后一天的结束时间
     */
    public static Date endOfYear() {
        return endOfYear(new Date());
    }

    /**
     * 获取指定日期所在年份的最后一天的结束时间
     *
     * @param date 指定日期
     * @return 最后一天的结束时间，如果date为null则返回null
     */
    public static Date endOfYear(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date)
                .with(TemporalAdjusters.lastDayOfYear())
                .with(LocalTime.MAX);
        return toDate(localDateTime);
    }

    // ==================== 日期比较 ====================

    /**
     * 判断两个日期是否为同一天（忽略时间部分）
     *
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        LocalDate localDate1 = toLocalDate(date1);
        LocalDate localDate2 = toLocalDate(date2);
        return localDate1.equals(localDate2);
    }

    /**
     * 判断日期是否在指定范围内（包含边界）
     *
     * @param date  待判断日期
     * @param start 开始日期
     * @param end   结束日期
     * @return 是否在范围内
     */
    public static boolean isBetween(Date date, Date start, Date end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        long time = date.getTime();
        return time >= start.getTime() && time <= end.getTime();
    }

    /**
     * 判断日期是否为今天
     *
     * @param date 待判断日期
     * @return 是否为今天
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, new Date());
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 相差天数，如果任一参数为null则返回0
     */
    public static long daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        LocalDate start = toLocalDate(startDate);
        LocalDate end = toLocalDate(endDate);
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期之间相差的小时数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 相差小时数，如果任一参数为null则返回0
     */
    public static long hoursBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        LocalDateTime start = toLocalDateTime(startDate);
        LocalDateTime end = toLocalDateTime(endDate);
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期之间相差的分钟数
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 相差分钟数，如果任一参数为null则返回0
     */
    public static long minutesBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        LocalDateTime start = toLocalDateTime(startDate);
        LocalDateTime end = toLocalDateTime(endDate);
        return ChronoUnit.MINUTES.between(start, end);
    }

    // ==================== 日期增减 ====================

    /**
     * 增加天数
     *
     * @param date 原始日期
     * @param days 要增加的天数（负数表示减少）
     * @return 新日期，如果date为null则返回null
     */
    public static Date plusDays(Date date, long days) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).plusDays(days);
        return toDate(localDateTime);
    }

    /**
     * 增加小时数
     *
     * @param date  原始日期
     * @param hours 要增加的小时数（负数表示减少）
     * @return 新日期，如果date为null则返回null
     */
    public static Date plusHours(Date date, long hours) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).plusHours(hours);
        return toDate(localDateTime);
    }

    /**
     * 增加分钟数
     *
     * @param date    原始日期
     * @param minutes 要增加的分钟数（负数表示减少）
     * @return 新日期，如果date为null则返回null
     */
    public static Date plusMinutes(Date date, long minutes) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).plusMinutes(minutes);
        return toDate(localDateTime);
    }

    /**
     * 增加月份
     *
     * @param date   原始日期
     * @param months 要增加的月份数（负数表示减少）
     * @return 新日期，如果date为null则返回null
     */
    public static Date plusMonths(Date date, long months) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).plusMonths(months);
        return toDate(localDateTime);
    }

    /**
     * 增加年份
     *
     * @param date  原始日期
     * @param years 要增加的年份数（负数表示减少）
     * @return 新日期，如果date为null则返回null
     */
    public static Date plusYears(Date date, long years) {
        if (date == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(date).plusYears(years);
        return toDate(localDateTime);
    }

    // ==================== 时区转换 ====================

    /**
     * 将Date从一个时区转换到另一个时区
     *
     * @param date       原始日期
     * @param fromZoneId 源时区
     * @param toZoneId   目标时区
     * @return 转换后的日期，如果date为null则返回null
     */
    public static Date convertTimeZone(Date date, ZoneId fromZoneId, ZoneId toZoneId) {
        if (date == null) {
            return null;
        }
        ZonedDateTime fromZonedDateTime = date.toInstant().atZone(fromZoneId);
        ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(toZoneId);
        return Date.from(toZonedDateTime.toInstant());
    }

    /**
     * 将Date转换为UTC时区
     *
     * @param date 原始日期
     * @return UTC时区的日期，如果date为null则返回null
     */
    public static Date toUtc(Date date) {
        return convertTimeZone(date, DEFAULT_ZONE_ID, ZoneId.of("UTC"));
    }

    /**
     * 将UTC时区的Date转换为系统默认时区
     *
     * @param utcDate UTC时区的日期
     * @return 系统默认时区的日期，如果utcDate为null则返回null
     */
    public static Date fromUtc(Date utcDate) {
        return convertTimeZone(utcDate, ZoneId.of("UTC"), DEFAULT_ZONE_ID);
    }
}
