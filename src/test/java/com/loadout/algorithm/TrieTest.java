package com.loadout.algorithm;


import com.loadout.algorithm.trie.Trie;

/**
 *
 * @author panlf
 * @date 2026/5/14
 */
public class TrieTest {
    public static void main(String[] args) {
        Trie trie = new Trie();

        // 测试添加
        trie.add("apple");
        trie.add("apple");      // 两次添加，词频应为2
        trie.add("app");
        trie.add("你好");
        trie.add("中国");
        trie.add("中国人");

        // 打印树
        trie.printTree();

        // 查询词频
        System.out.println("\n=== 词频查询 ===");
        System.out.println("apple: " + trie.getWordCount("apple"));
        System.out.println("你好: " + trie.getWordCount("你好"));

        // 前缀搜索
        System.out.println("\n=== 前缀搜索 'app' ===");
        System.out.println(trie.getWordsWithPrefix("app"));
        System.out.println("\n=== 前缀搜索 '中' ===");
        System.out.println(trie.getWordsWithPrefix("中"));

        // 测试删除（多词频）
        System.out.println("\n=== 删除测试 ===");
        System.out.println("remove apple (第一次): " + trie.remove("apple"));
        System.out.println("apple词频: " + trie.getWordCount("apple"));
        System.out.println("contains apple: " + trie.contains("apple"));
        System.out.println("remove apple (第二次): " + trie.remove("apple"));
        System.out.println("apple词频: " + trie.getWordCount("apple"));
        System.out.println("contains apple: " + trie.contains("apple"));
        System.out.println("remove apple (第三次，不存在): " + trie.remove("apple"));

        // 测试批量添加
        trie.addAll("hello", "world", "hero");
        System.out.println("\n=== 批量添加后，所有单词 ===");
        System.out.println(trie.getAllWords());
    }
}
