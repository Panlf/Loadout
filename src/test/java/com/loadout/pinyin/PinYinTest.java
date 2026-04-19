package com.loadout.pinyin;

import java.util.List;

import com.loadout.usual.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


/**
 * @author panlf
 * @date 2026/4/14
 */
@DisplayName("拼音工具类测试")
public class PinYinTest {
    private static final String TEST_TEXT = "我爱中国";
    private static final String EMPTY_TEXT = "";
    private static final String BLANK_TEXT = "   ";
    private static final String NULL_TEXT = null;

    @Test
    @DisplayName("测试空值、空格、null 安全")
    void testEmpty() {
        assertEquals(StringUtils.EMPTY, PinyinUtil.toPinyin(NULL_TEXT));
        assertEquals(StringUtils.EMPTY, PinyinUtil.toPinyin(EMPTY_TEXT));
        assertEquals(StringUtils.EMPTY, PinyinUtil.toPinyin(BLANK_TEXT));
    }

    @Test
    @DisplayName("转换为带声调拼音")
    void toPinyin() {
        String result = PinyinUtil.toPinyin(TEST_TEXT);
        assertEquals("wǒ ài zhōng guó", result);
    }

    @Test
    @DisplayName("转换为无声调拼音")
    void toPinyinNormal() {
        String result = PinyinUtil.toPinyinNormal(TEST_TEXT);
        assertEquals("wo ai zhong guo", result);
    }

    @Test
    @DisplayName("转换为数字声调拼音")
    void toPinyinNum() {
        String result = PinyinUtil.toPinyinNum(TEST_TEXT);
        assertEquals("wo3 ai4 zhong1 guo2", result);
    }

    @Test
    @DisplayName("获取首字母（空格分隔）")
    void toFirstLetter() {
        String result = PinyinUtil.toFirstLetter(TEST_TEXT);
        assertEquals("w a z g", result);
    }

    @Test
    @DisplayName("获取大写拼音缩写")
    void toAbbrDefault() {
        String result = PinyinUtil.toAbbr(TEST_TEXT);
        assertEquals("WAZG", result);
    }

    @Test
    @DisplayName("获取小写拼音缩写")
    void toAbbrLowerCase() {
        String result = PinyinUtil.toAbbr(TEST_TEXT, false);
        assertEquals("wazg", result);
    }

    @Test
    @DisplayName("转换为首字母大写格式")
    void toCapitalize() {
        String result = PinyinUtil.toCapitalize(TEST_TEXT);
        assertEquals("Wo Ai Zhong Guo", result);
    }

    @Test
    @DisplayName("获取多音字拼音列表")
    void listPinyin() {
        List<String> hao = PinyinUtil.listPinyin('好');
        assertTrue(hao.contains("hǎo"));
        assertTrue(hao.contains("hào"));

        List<String> xing = PinyinUtil.listPinyin('行');
        assertTrue(xing.contains("xíng"));
        assertTrue(xing.contains("háng"));
    }

    @Test
    @DisplayName("判断是否为汉字")
    void isChinese() {
        assertTrue(PinyinUtil.isChinese('中'));
        assertTrue(PinyinUtil.isChinese('国'));
        assertFalse(PinyinUtil.isChinese('A'));
        assertFalse(PinyinUtil.isChinese('1'));
        assertFalse(PinyinUtil.isChinese('!'));
    }
}
