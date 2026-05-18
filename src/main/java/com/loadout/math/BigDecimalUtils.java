package com.loadout.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
/**
 * BigDecimal 工具类
 * @author panlf
 * @date 2026/5/18
 */
public class BigDecimalUtils {
    /** 默认除法精度（小数点后保留位数） */
    private static final int DEFAULT_DIV_SCALE = 10;
    /** 默认舍入模式：四舍五入 */
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    // ==================== 安全的类型转换 ====================

    /**
     * 将任意对象安全转换为 {@link BigDecimal}。
     * <p>支持的类型：{@link BigDecimal}、{@link String}、{@link BigInteger}、
     * {@link Number}（含 {@code Integer, Long, Float, Double} 等）。</p>
     *
     * @param value 待转换的对象，可为 {@code null}
     * @return 转换后的 {@link BigDecimal}，若输入为 {@code null} 则返回 {@code null}
     * @throws ClassCastException 如果类型不支持转换
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        if (value instanceof Number) {
            // 关键：使用 toString() 避免浮点数精度丢失
            return new BigDecimal(((Number) value).toString());
        }
        throw new ClassCastException("Cannot convert [" + value + "] of type " +
                value.getClass().getName() + " to BigDecimal");
    }

    /**
     * 安全转换为 {@link BigDecimal}，若输入为 {@code null} 则返回 {@link BigDecimal#ZERO}。
     *
     * @param value 待转换对象
     * @return 非 {@code null} 的 {@link BigDecimal}
     */
    public static BigDecimal toBigDecimalOrDefault(Object value) {
        BigDecimal bd = toBigDecimal(value);
        return bd == null ? BigDecimal.ZERO : bd;
    }

    // ==================== 核心算术运算（返回 BigDecimal） ====================

    public static BigDecimal add(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.add(b1);
    }

    public static BigDecimal subtract(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.subtract(b1);
    }

    public static BigDecimal multiply(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.multiply(b1);
    }

    /**
     * 除法运算，使用默认精度 {@value #DEFAULT_DIV_SCALE} 和四舍五入。
     *
     * @param a 被除数
     * @param b 除数（不能为 0）
     * @return 商
     * @throws ArithmeticException 如果除数为 0
     */
    public static BigDecimal divide(Object a, Object b) {
        return divide(a, b, DEFAULT_DIV_SCALE);
    }

    /**
     * 除法运算，指定小数位数，四舍五入。
     *
     * @param a     被除数
     * @param b     除数（不能为 0）
     * @param scale 保留小数位数（>=0）
     * @return 商
     * @throws ArithmeticException 如果除数为 0 或 scale 为负数
     */
    public static BigDecimal divide(Object a, Object b, int scale) {
        return divide(a, b, scale, DEFAULT_ROUNDING);
    }

    /**
     * 除法运算，完全自定义。
     *
     * @param a          被除数
     * @param b          除数（不能为 0）
     * @param scale      保留小数位数（>=0）
     * @param rounding   舍入模式
     * @return 商
     * @throws ArithmeticException 如果除数为 0 或 scale 为负数
     */
    public static BigDecimal divide(Object a, Object b, int scale, RoundingMode rounding) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be >= 0");
        }
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.divide(b1, scale, rounding);
    }

    /**
     * 取余数（返回值的精度与较大数一致）。
     *
     * @param a 被除数
     * @param b 除数
     * @return 余数
     */
    public static BigDecimal remainder(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.remainder(b1);
    }

    /**
     * 取余数并保留指定小数位数。
     *
     * @param a     被除数
     * @param b     除数
     * @param scale 保留小数位数
     * @return 余数（已舍入）
     */
    public static BigDecimal remainder(Object a, Object b, int scale) {
        BigDecimal remainder = remainder(a, b);
        return remainder.setScale(scale, DEFAULT_ROUNDING);
    }

    /**
     * 四舍五入。
     *
     * @param value 原始数值
     * @param scale 保留小数位数
     * @return 舍入后的值
     */
    public static BigDecimal round(Object value, int scale) {
        return round(value, scale, DEFAULT_ROUNDING);
    }

    /**
     * 自定义舍入。
     *
     * @param value    原始数值
     * @param scale    保留小数位数
     * @param rounding 舍入模式
     * @return 舍入后的值
     */
    public static BigDecimal round(Object value, int scale, RoundingMode rounding) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be >= 0");
        }
        BigDecimal bd = toBigDecimalOrDefault(value);
        return bd.setScale(scale, rounding);
    }

    // ==================== 返回 String 的便捷方法（常用于序列化/存储） ====================

    public static String addAsString(Object a, Object b) {
        return add(a, b).toPlainString();
    }

    public static String subtractAsString(Object a, Object b) {
        return subtract(a, b).toPlainString();
    }

    public static String multiplyAsString(Object a, Object b) {
        return multiply(a, b).toPlainString();
    }

    public static String divideAsString(Object a, Object b) {
        return divide(a, b).toPlainString();
    }

    public static String divideAsString(Object a, Object b, int scale) {
        return divide(a, b, scale).toPlainString();
    }

    public static String roundAsString(Object value, int scale) {
        return round(value, scale).toPlainString();
    }

    // ==================== 比较运算====================

    /**
     * 比较两个数：a > b ?
     */
    public static boolean greaterThan(Object a, Object b) {
        return compareTo(a, b) > 0;
    }

    /**
     * 比较两个数：a >= b ?
     */
    public static boolean greaterThanOrEqual(Object a, Object b) {
        return compareTo(a, b) >= 0;
    }

    /**
     * 比较两个数：a < b ?
     */
    public static boolean lessThan(Object a, Object b) {
        return compareTo(a, b) < 0;
    }

    /**
     * 比较两个数：a <= b ?
     */
    public static boolean lessThanOrEqual(Object a, Object b) {
        return compareTo(a, b) <= 0;
    }

    /**
     * 比较两个数：a == b ?
     */
    public static boolean equal(Object a, Object b) {
        return compareTo(a, b) == 0;
    }

    /**
     * 核心比较方法，返回 -1, 0, 1。
     */
    public static int compareTo(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.compareTo(b1);
    }


    /**
     * 判断是否为整数（无小数部分）。
     */
    public static boolean isInteger(Object value) {
        BigDecimal bd = toBigDecimal(value);
        return bd != null && bd.stripTrailingZeros().scale() <= 0;
    }

    /**
     * 获取指定值的小数位数。
     */
    public static int getScale(Object value) {
        BigDecimal bd = toBigDecimalOrDefault(value);
        return bd.scale();
    }

    /**
     * 返回绝对值。
     */
    public static BigDecimal abs(Object value) {
        return toBigDecimalOrDefault(value).abs();
    }

    /**
     * 返回负数（取反）。
     */
    public static BigDecimal negate(Object value) {
        return toBigDecimalOrDefault(value).negate();
    }

    /**
     * 返回最大值。
     */
    public static BigDecimal max(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.max(b1);
    }

    /**
     * 返回最小值。
     */
    public static BigDecimal min(Object a, Object b) {
        BigDecimal a1 = toBigDecimalOrDefault(a);
        BigDecimal b1 = toBigDecimalOrDefault(b);
        return a1.min(b1);
    }
}
