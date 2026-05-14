package com.loadout.algorithm.lru.brevity;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 极简、高性能LRU缓存淘汰策略工具类
 * 非线程安全
 * @author panlf
 * @date 2026/5/14
 */
public class LinkedLruCache<K,V> extends LinkedHashMap<K,V> {
    /**
     * 缓存限制
     */
    private final int capacity;

    public LinkedLruCache(int capacity){
        super(capacity,0.75F,true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > capacity;
    }
}
