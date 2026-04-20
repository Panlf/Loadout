package com.loadout.wordcloud;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kumo 词云工具类
 * 支持：中文分词、自定义尺寸、颜色、字体、圆形背景、输出 PNG
 * @author panlf
 * @date 2026/4/20
 */
public class WordCloudUtils {
    /**
     * 根据词频 Map 生成词云图片
     *
     * @param wordFreqMap 词频映射 (单词 -> 权重)
     * @param width       图片宽度
     * @param height      图片高度
     * @param outputPath  输出图片路径 (例如 "wordcloud.png")
     * @param fontPath    中文字体文件路径 (如 "C:/Windows/Fonts/simhei.ttf")，为 null 时使用默认字体（可能不支持中文）
     * @throws IOException IO异常
     */
    public static void generateFromWordFreq(Map<String, Integer> wordFreqMap,
                                            int width, int height,
                                            String outputPath,
                                            String fontPath) throws IOException {
        // 1. 转换为 WordFrequency 列表
        List<WordFrequency> wordFrequencies = wordFreqMap.entrySet().stream()
                .map(entry -> new WordFrequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 2. 创建词云配置
        WordCloud wordCloud = buildWordCloud(width, height, fontPath);

        // 3. 构建词云
        wordCloud.build(wordFrequencies);

        // 4. 输出图片
        exportImage(wordCloud, outputPath);
    }

    /**
     * 根据原始文本生成词云（自动分词、统计词频）
     *
     * @param text       原始文本
     * @param width      图片宽度
     * @param height     图片高度
     * @param outputPath 输出路径
     * @param fontPath   中文字体路径（可选）
     * @throws IOException IO异常
     */
    public static void generateFromText(String text, int width, int height,
                                        String outputPath, String fontPath) throws IOException {
        // 使用 FrequencyAnalyzer 进行分词和词频统计
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        // 设置中文分词器（来自 kumo-tokenizers）
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        // 设置最小词频（过滤低频词）
        frequencyAnalyzer.setMinWordLength(2);
        // 设置需要加载的词汇数量（前N个高频词）
        frequencyAnalyzer.setWordFrequenciesToReturn(200);

        List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(text);

        WordCloud wordCloud = buildWordCloud(width, height, fontPath);
        wordCloud.build(wordFrequencies);
        exportImage(wordCloud, outputPath);
    }

    /**
     * 构建并配置 WordCloud 对象
     */
    private static WordCloud buildWordCloud(int width, int height, String fontPath) {
        // 1. 使用正确的构造函数：Dimension + CollisionMode
        WordCloud wordCloud = new WordCloud(new Dimension(width, height), CollisionMode.PIXEL_PERFECT);

        // 2. 背景设置（源码中默认已经是 RectangleBackground，但可自定义颜色）
        wordCloud.setBackgroundColor(Color.WHITE);  // 将默认黑色背景改为白色

        // 3. 字体设置（支持中文）
        if (fontPath != null && new File(fontPath).exists()) {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
                wordCloud.setKumoFont(new KumoFont(font));
            } catch (Exception e) {
                System.err.println("加载自定义字体失败，使用默认字体：" + e.getMessage());
                wordCloud.setKumoFont(new KumoFont(new Font("宋体", Font.PLAIN, 24)));
            }
        } else {
            wordCloud.setKumoFont(new KumoFont(new Font("宋体", Font.PLAIN, 24)));
        }

        // 4. 设置字体缩放范围（覆盖默认的 LinearFontScalar(10,40)）
        wordCloud.setFontScalar(new LinearFontScalar(20, 80));

        // 5. 颜色调色板
        wordCloud.setColorPalette(new ColorPalette(
                new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1),
                new Color(0x40C5F1), new Color(0x40D3F1), new Color(0x36BCF1)
        ));

        // 6. 文字角度（全部水平）
        wordCloud.setAngleGenerator(new AngleGenerator(0));

        // 7. 单词间距
        wordCloud.setPadding(2);

        return wordCloud;
    }

    /**
     * 将词云对象输出为图片文件
     */
    private static void exportImage(WordCloud wordCloud, String outputPath) throws IOException {
        BufferedImage bufferedImage = wordCloud.getBufferedImage();
        String format = outputPath.substring(outputPath.lastIndexOf('.') + 1);
        ImageIO.write(bufferedImage, format, new File(outputPath));
    }
}
