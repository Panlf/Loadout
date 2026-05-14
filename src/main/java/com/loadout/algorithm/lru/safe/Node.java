package com.loadout.algorithm.lru.safe;


/**
 * 节点类
 * @author panlf
 * @date 2026/5/14
 */
public class Node<K, V> {
    public K key;
    public V value;
    public Node<K, V> preNode;
    public Node<K, V> nextNode;

    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
