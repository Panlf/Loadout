package com.loadout.retry;


import java.util.concurrent.ThreadLocalRandom;

/**
 * 常用退避策略工厂（固定、指数、随机）
 * @author panlf
 * @date 2026/6/10
 */
public final class WaitStrategies {

    private WaitStrategies() {}

    /** 固定间隔 */
    public static WaitStrategy fixed(long periodMillis) {
        return attempt -> periodMillis;
    }

    /** 指数退避： initialMillis * multiplier^(attempt-1) */
    public static WaitStrategy exponential(long initialMillis, double multiplier) {
        return attempt -> (long) (initialMillis * Math.pow(multiplier, attempt - 1));
    }

    /** 随机间隔 [min, max] */
    public static WaitStrategy random(long minMillis, long maxMillis) {
        return attempt -> ThreadLocalRandom.current().nextLong(minMillis, maxMillis + 1);
    }
}
