package com.loadout.retry;


/**
 * 重试事件监听器（所有方法默认空实现，按需覆盖）
 * @author panlf
 * @date 2026/6/10
 */
public interface  RetryListener {
    default void onBeforeAttempt(int attempt, int maxAttempts) {}

    default void onSuccess(int attempt, Object result) {}

    default void onException(int attempt, Throwable t) {}

    default void onResultMismatch(int attempt, Object result) {}

    default void onWait(int attempt, long waitMillis) {}
}
