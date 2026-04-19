package com.loadout.segment;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.loadout.usual.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HanLp分词工具类
 * @author panlf
 * @date 2026/4/15
 */
public class HanLpUtils {
    private static final Segment STANDARD_SEGMENT;

    static {

        STANDARD_SEGMENT = HanLP.newSegment()
                .enableNameRecognize(false)
                .enablePlaceRecognize(false)
                .enableOrganizationRecognize(false)
                .enableOffset(true);
    }

    private HanLpUtils() {}

    // ====================== 标准分词 ======================
    public static List<String> segment(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        List<Term> terms = STANDARD_SEGMENT.seg(text);
        return terms.stream().map(t -> t.word).collect(Collectors.toList());
    }

    // ====================== 索引分词 ======================
    public static List<String> segmentForIndex(String text) {
        if (StringUtils.isBlank(text)) {
            return new ArrayList<>();
        }
        return STANDARD_SEGMENT.seg(text).stream().map(t -> t.word).collect(Collectors.toList());
    }

    // ====================== 分词+词性 ======================
    public static List<Term> segmentWithPos(String text) {
        return StringUtils.isBlank(text) ? new ArrayList<>() : STANDARD_SEGMENT.seg(text);
    }

    // ====================== 关键词 ======================
    public static List<String> extractKeywords(String text, int topN) {
        return StringUtils.isBlank(text) || topN <= 0 ? new ArrayList<>() : HanLP.extractKeyword(text, topN);
    }

    // ====================== 摘要 ======================
    public static List<String> extractSummary(String text, int length) {
        return StringUtils.isBlank(text) || length <= 0 ? new ArrayList<>() : HanLP.extractSummary(text, length);
    }

    // ====================== 简繁转换 ======================
    public static String toTraditional(String text) {
        return StringUtils.isBlank(text) ? StringUtils.EMPTY : HanLP.convertToTraditionalChinese(text);
    }

    public static String toSimplified(String text) {
        return StringUtils.isBlank(text) ? StringUtils.EMPTY : HanLP.convertToSimplifiedChinese(text);
    }

    // ====================== 拼音 ======================
    public static String toPinyin(String text) {
        return StringUtils.isBlank(text) ? StringUtils.EMPTY : HanLP.convertToPinyinString(text, "", false);
    }

    // ====================== 拼音首字母 ======================
    public static String toPinyinFirstChar(String text) {
        if (StringUtils.isBlank(text)) return StringUtils.EMPTY;
        List<Pinyin> pinyinList = HanLP.convertToPinyinList(text);
        StringBuilder sb = new StringBuilder();
        for (Pinyin pinyin : pinyinList) {
            if (pinyin != null) sb.append(pinyin.getFirstChar());
        }
        return sb.toString();
    }

        // ====================== 去除停用词======================

        public static List<String> removeStopWords(String text) {
            if (StringUtils.isBlank(text)) {
                return new ArrayList<>();
            }
            List<Term> terms = STANDARD_SEGMENT.seg(text);
            return terms.stream()
                    .map(term -> term.word) // 提取词语
                    .filter(word -> !CoreStopWordDictionary.contains(word)) // 核心：判断是否在停用词表中
                    .collect(Collectors.toList());
        }
}
