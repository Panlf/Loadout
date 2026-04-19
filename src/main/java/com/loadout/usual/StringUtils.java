package com.loadout.usual;

/**
 * 字符串工具类
 * @author panlf
 * @date 2026/4/14
 */
public final class StringUtils {
    public static final String EMPTY = "";
    public static final String SPACE = " ";

    private StringUtils() {
        throw new AssertionError("StringUtil 不可实例化");
    }

    /**
     * 判断字符串是否为空（null / 空 / 全空格）
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 安全trim，null返回空串
     */
    public static String trim(String str) {
        return str == null ? EMPTY : str.trim();
    }
}
