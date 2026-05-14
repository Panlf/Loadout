package com.loadout.algorithm;


import com.loadout.algorithm.lru.brevity.LinkedLruCache;
import com.loadout.algorithm.lru.safe.LRUCache;
import org.junit.jupiter.api.Test;

/**
 *
 * @author panlf
 * @date 2026/5/14
 */
public class LruTest {

    @Test
    public void testLinkedLruCache(){
        LinkedLruCache<String, String> cache = new LinkedLruCache<>(3);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        // 现在缓存满了
        cache.get("a");  // 访问 a → a 变成最近使用
        cache.put("d", "4"); // 加入新数据 → 删除最久没使用的 b

        // 最终缓存：c, a, d
        System.out.println(cache); // 输出 {c=3, a=1, d=4}
    }

    @Test
    public void LRUCacheTest(){
        // 测试
        LRUCache<String, String> lruCache = new LRUCache<>(3);
        lruCache.put("j","jin");
        lruCache.put("y","yin");
        lruCache.put("b","best");
        // 超过容量，淘汰最久未使用
        lruCache.put("w","warm");
        // 覆盖
        lruCache.put("b","best");

        lruCache.remove("w");
        lruCache.get("b");

        System.out.println(lruCache);

    }
}
