package com.loadout.retry;


/**
 * 退避策略：根据当前重试次数计算下次重试前需要等待的毫秒数
 * @author panlf
 * @date 2026/6/10
 */
@FunctionalInterface
public interface WaitStrategy {
    long computeWaitTime(int currentAttempt);
}
