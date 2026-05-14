package com.loadout.algorithm.lru.safe;


import java.util.concurrent.ConcurrentHashMap;

/**
 * 手写双向链表 LRU 缓存
 * 高性能 + 线程安全
 * @author panlf
 * @date 2026/5/14
 */
public class LRUCache<K, V> {
    /** 缓存最大容量 */
    private final int capacity;
    /** 当前元素个数 */
    private int currentSize;
    /** 哈希索引：线程安全高性能 ConcurrentHashMap */
    private final ConcurrentHashMap<K, Node<K, V>> nodeCaches;

    /** 双向链表 头尾虚拟节点，简化判空逻辑 */
    private final Node<K, V> head;
    private final Node<K, V> tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.currentSize = 0;
        nodeCaches = new ConcurrentHashMap<>(capacity);
        // 初始化虚拟头尾节点，避免大量 null 判断
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.nextNode = tail;
        tail.preNode = head;
    }

    /**
     * 存入缓存
     */
    public synchronized void put(K key, V value) {
        Node<K, V> node = nodeCaches.get(key);
        // 已存在：更新值 + 移到头部
        if (node != null) {
            node.value = value;
            moveToHead(node);
            return;
        }
        // 不存在：新建节点
        Node<K, V> newNode = new Node<>(key, value);
        // 容量已满：淘汰最久未使用（尾部前一个）
        if (currentSize >= capacity) {
            removeTailNode();
        }
        // 加入头部、加入哈希表
        addToHead(newNode);
        nodeCaches.put(key, newNode);
        currentSize++;
    }

    /**
     * 获取缓存
     */
    public synchronized V get(K key) {
        Node<K, V> node = nodeCaches.get(key);
        if (node == null) {
            return null;
        }
        // 访问后移到头部
        moveToHead(node);
        return node.value;
    }

    /**
     * 根据 key 删除缓存
     */
    public synchronized V remove(K key) {
        Node<K, V> node = nodeCaches.get(key);
        if (node == null) {
            return null;
        }
        // 从链表移除
        removeNode(node);
        // 从哈希表移除
        nodeCaches.remove(key);
        currentSize--;
        return node.value;
    }

    /**
     * 清空缓存
     */
    public synchronized void clear() {
        head.nextNode = tail;
        tail.preNode = head;
        currentSize = 0;
        nodeCaches.clear();
    }

    // ========== 内部链表操作 ==========
    /** 将节点加入头部 */
    private void addToHead(Node<K, V> node) {
        node.preNode = head;
        node.nextNode = head.nextNode;

        head.nextNode.preNode = node;
        head.nextNode = node;
    }

    /** 移除任意节点 */
    private void removeNode(Node<K, V> node) {
        node.preNode.nextNode = node.nextNode;
        node.nextNode.preNode = node.preNode;
    }

    /** 移动到头部：先移除再加入头部 */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    /** 移除尾部节点（淘汰 LRU） */
    private void removeTailNode() {
        Node<K, V> delNode = tail.preNode;
        removeNode(delNode);
        nodeCaches.remove(delNode.key);
        currentSize--;
    }

    /**
     * 重写 toString 遍历链表，不破坏原结构
     */
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        Node<K, V> cur = head.nextNode;
        while (cur != tail) {
            sb.append(cur.key).append(":").append(cur.value).append("\n");
            cur = cur.nextNode;
        }
        return sb.toString();
    }
}
