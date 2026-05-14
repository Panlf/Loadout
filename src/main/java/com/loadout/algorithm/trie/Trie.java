package com.loadout.algorithm.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Trie 树（字典树）
 * @author panlf
 * @date 2026/5/14
 */
public class Trie {
    // 字典树节点
    @Getter
    public static class TrieNode {
        // 子节点：使用 HashMap 保证查找效率 O(1)，打印时再排序
        private final Map<Character, TrieNode> children;
        // 是否是单词结尾
        @Setter
        private boolean isWord;
        // 单词词频（统计这个词被添加了多少次）
        private int count;

        public TrieNode() {
            this.children = new HashMap<>();
            this.isWord = false;
            this.count = 0;
        }

        public void incCount() {
            this.count++;
        }

        public void decCount() {
            if (this.count > 0) {
                this.count--;
            }
        }
    }

    // 根节点
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // ====================== 1. 添加单词（支持中英） ======================

    /**
     * 添加一个单词，自动增加词频
     * @param word 要添加的单词，不为 null 且非空
     */
    public void add(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.getChildren().putIfAbsent(c, new TrieNode());
            node = node.getChildren().get(c);
        }
        node.setWord(true);
        node.incCount();
    }

    /**
     * 批量添加单词
     * @param words 单词列表
     */
    public void addAll(String... words) {
        if (words == null) {
            return;
        }
        for (String w : words) {
            add(w);
        }
    }

    // ====================== 2. 查询单词是否存在 ======================

    /**
     * 判断单词是否存在（至少添加过一次）
     * @param word 要查询的单词
     * @return 存在返回 true，否则 false
     */
    public boolean contains(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isWord();
    }

    // ====================== 3. 获取单词词频 ======================

    /**
     * 获取单词的词频（添加次数）
     * @param word 要查询的单词
     * @return 词频，若不存在则返回 0
     */
    public int getWordCount(String word) {
        TrieNode node = findNode(word);
        return (node != null && node.isWord()) ? node.getCount() : 0;
    }

    // ====================== 4. 前缀搜索 ======================

    /**
     * 获取所有以给定前缀开头的单词（包含前缀本身若是单词）
     * @param prefix 前缀字符串
     * @return 匹配的单词列表，按字典序排序（由 TreeMap 特性保证）
     */
    public List<String> getWordsWithPrefix(String prefix) {
        List<String> result = new ArrayList<>();
        if (prefix == null) {
            return result;
        }
        TrieNode startNode = findNode(prefix);
        if (startNode == null) {
            return result;
        }
        dfsGetWords(startNode, new StringBuilder(prefix), result);
        return result;
    }

    /**
     * 获取 Trie 树中所有单词
     * @return 所有单词列表
     */
    public List<String> getAllWords() {
        return getWordsWithPrefix("");
    }

    // 递归搜集所有单词
    private void dfsGetWords(TrieNode node, StringBuilder sb, List<String> result) {
        if (node.isWord()) {
            result.add(sb.toString());
        }
        // 对子节点按字符排序，保证输出有序
        List<Character> sortedKeys = new ArrayList<>(node.getChildren().keySet());
        Collections.sort(sortedKeys);
        for (char c : sortedKeys) {
            sb.append(c);
            dfsGetWords(node.getChildren().get(c), sb, result);
            sb.deleteCharAt(sb.length() - 1);
        }
    }

    // ====================== 5. 删除单词（减少词频） ======================

    /**
     * 删除一个单词的 1 次出现（词频减 1）
     * 若词频降为 0，则标记为非单词；若该节点无子节点且不再是单词，则物理移除
     * @param word 要删除的单词
     * @return 删除成功返回 true，单词原本不存在返回 false
     */
    public boolean remove(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return remove(root, word.toCharArray(), 0);
    }

    /**
     * 递归删除辅助方法
     * @param node  当前节点
     * @param chars 单词字符数组
     * @param index 当前处理到的字符索引
     * @return 是否允许父级删除此节点（即当前节点无子节点且不再是单词）
     */
    private boolean remove(TrieNode node, char[] chars, int index) {
        if (index == chars.length) {
            // 到达单词末尾
            if (!node.isWord()) {
                return false;   // 单词不存在
            }
            node.decCount();
            if (node.getCount() == 0) {
                node.setWord(false);
            }
            // 条件：无子节点 且 不再是单词结尾（词频已归零）
            return node.getChildren().isEmpty() && !node.isWord();
        }

        char c = chars[index];
        TrieNode child = node.getChildren().get(c);
        if (child == null) {
            return false;   // 路径中断，单词不存在
        }

        boolean shouldDelete = remove(child, chars, index + 1);
        if (shouldDelete) {
            node.getChildren().remove(c);
            // 父节点也需要判断是否可以删除：无其他子节点 且 父节点不是单词
            return node.getChildren().isEmpty() && !node.isWord();
        }
        return false;
    }

    // ====================== 6. 辅助方法：查找节点 ======================

    /**
     * 根据单词查找对应的节点（若路径中断返回 null）
     * @param word 单词字符串
     * @return 单词最后一个字符对应的节点，若不存在则返回 null
     */
    private TrieNode findNode(String word) {
        if (word == null) {
            return null;
        }
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren().get(c);
            if (node == null) {
                return null;
            }
        }
        return node;
    }

    /**
     * 判断是否存在以给定前缀开头的单词（包括前缀本身是单词）
     * @param prefix 前缀字符串
     * @return 存在返回 true，否则 false
     */
    public boolean hasPrefix(String prefix) {
        return findNode(prefix) != null;
    }

    // ====================== 7. 打印整棵树（美化版） ======================

    /**
     * 打印整棵 Trie 树的结构（带树形符号）
     */
    public void printTree() {
        System.out.println("=== Trie 树结构 ===");
        if (root.getChildren().isEmpty()) {
            System.out.println("(空树)");
            return;
        }
        printPretty(root, "", true);
    }

    /**
     * 递归打印，使用 ├── 和 └── 符号，显示词频
     * @param node      当前节点
     * @param prefix    当前行前缀缩进
     * @param isTail    当前节点是否是父节点的最后一个子节点
     */
    private void printPretty(TrieNode node, String prefix, boolean isTail) {
        List<Map.Entry<Character, TrieNode>> entries = new ArrayList<>(node.getChildren().entrySet());
        // 按字符排序，保证输出稳定
        entries.sort(Map.Entry.comparingByKey());

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Character, TrieNode> entry = entries.get(i);
            char ch = entry.getKey();
            TrieNode child = entry.getValue();
            boolean last = (i == entries.size() - 1);

            // 打印当前字符
            System.out.print(prefix);
            System.out.print(last ? "└── " : "├── ");
            System.out.print(ch);
            if (child.isWord()) {
                System.out.printf(" (词频: %d)", child.getCount());
            }
            System.out.println();

            // 递归子节点
            printPretty(child, prefix + (last ? "    " : "│   "), last);
        }
    }

}
