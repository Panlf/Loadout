package com.loadout.wordcloud;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author panlf
 * @date 2026/4/20
 */
public class WordCloudTest {
    public static void main(String[] args) throws IOException {
        Map<String, Integer> freqMap = new HashMap<>();
        freqMap.put("Java", 100);
        freqMap.put("Python", 80);
        freqMap.put("Kotlin", 10);
        freqMap.put("Go", 20);
        freqMap.put("Rust", 5);
        freqMap.put("JavaScript", 70);
        freqMap.put("TypeScript", 65);

        WordCloudUtils.generateFromWordFreq(freqMap, 800, 600, "C:\\Users\\Breeze\\Desktop\\wordcloud_en.png", null);
    }
}
