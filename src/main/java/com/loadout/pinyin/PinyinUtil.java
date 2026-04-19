package com.loadout.pinyin;

import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import com.loadout.usual.StringUtils;

import java.util.*;

/**
 * 汉字拼音工具类
 * 基于 com.github.houbb:pinyin 实现
 * @author panlf
 * @date 2026/4/14
 */
public class PinyinUtil {
    // ====================== 基础全拼转换 ======================

    /**
     * 转换为带声调拼音
     * 示例：中国 → zhōng guó
     */
    public static String toPinyin(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return PinyinHelper.toPinyin(text.trim());
    }

    /**
     * 转换为无声调拼音
     * 示例：中国 → zhong guo
     */
    public static String toPinyinNormal(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return PinyinHelper.toPinyin(text.trim(), PinyinStyleEnum.NORMAL);
    }

    /**
     * 转换为数字声调拼音
     * 示例：中国 → zhong1 guo2
     */
    public static String toPinyinNum(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return PinyinHelper.toPinyin(text.trim(), PinyinStyleEnum.NUM_LAST);
    }

    // ====================== 首字母 & 缩写 ======================

    /**
     * 获取每个字的首字母（小写，空格分隔）
     * 示例：我爱中国 → w a z g
     */
    public static String toFirstLetter(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        return PinyinHelper.toPinyin(text.trim(), PinyinStyleEnum.FIRST_LETTER);
    }

    /**
     * 获取拼音大写首字母缩写（无分隔符，最常用）
     * 示例：我爱中国 → WAZG
     */
    public static String toAbbr(String text) {
        return toAbbr(text, true);
    }

    /**
     * 获取拼音首字母缩写（支持大小写）
     */
    public static String toAbbr(String text, boolean upperCase) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }
        String letters = toFirstLetter(text).replaceAll("\\s+", StringUtils.EMPTY);
        return upperCase ? letters.toUpperCase() : letters.toLowerCase();
    }

    // ====================== 格式化输出 ======================

    /**
     * 转换为每个单词首字母大写格式
     * 示例：我爱中国 → Wo Ai Zhong Guo
     */
    public static String toCapitalize(String text) {
        if (StringUtils.isBlank(text)) {
            return StringUtils.EMPTY;
        }

        String pinyin = toPinyinNormal(text);
        String[] words = pinyin.split(StringUtils.SPACE);
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (StringUtils.isBlank(word)) {
                continue;
            }
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(StringUtils.SPACE);
        }

        return sb.toString().trim();
    }

    // ====================== 多音字相关 ======================

    /**
     * 获取单个汉字的所有拼音（多音字）
     */
    public static List<String> listPinyin(char ch) {
        if (!isChinese(ch)) {
            return Collections.emptyList();
        }
        return PinyinHelper.toPinyinList(ch);
    }

    // ====================== 工具方法 ======================

    /**
     * 判断是否为汉字
     */
    public static boolean isChinese(char ch) {
        return Character.toString(ch).matches("[\\u4e00-\\u9fa5]");
    }

}
