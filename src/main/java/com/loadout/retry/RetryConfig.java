package com.loadout.retry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 重试配置（泛型 T 为返回结果类型）
 * @author panlf
 * @date 2026/6/10
 */
public class RetryConfig<T>  {
    final int maxAttempts;
    final WaitStrategy waitStrategy;
    final long timeoutMillis;
    final Predicate<Throwable> exceptionPredicate;
    final Predicate<T> resultPredicate;        // 返回 true 表示成功，停止重试
    final List<RetryListener> listeners;

    private RetryConfig(Builder<T> builder) {
        this.maxAttempts = builder.maxAttempts;
        this.waitStrategy = Objects.requireNonNull(builder.waitStrategy);
        this.timeoutMillis = builder.timeoutMillis;
        this.exceptionPredicate = builder.exceptionPredicate;
        this.resultPredicate = builder.resultPredicate;
        this.listeners = new ArrayList<>(builder.listeners);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private int maxAttempts = 3;
        private WaitStrategy waitStrategy = WaitStrategies.fixed(1000);
        private long timeoutMillis = 0;          // 0 表示不限制超时
        private Predicate<Throwable> exceptionPredicate = null;
        private Predicate<T> resultPredicate = null;
        private final List<RetryListener> listeners = new ArrayList<>();

        public Builder<T> maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts >= 1");
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder<T> waitStrategy(WaitStrategy waitStrategy) {
            this.waitStrategy = waitStrategy;
            return this;
        }

        public Builder<T> timeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        /** 仅重试能匹配给定异常的异常 */
        public Builder<T> retryOnException(Class<? extends Exception> exceptionType) {
            this.exceptionPredicate = exceptionType::isInstance;
            return this;
        }

        /** 自定义异常重试条件 */
        public Builder<T> retryOnException(Predicate<Throwable> predicate) {
            this.exceptionPredicate = predicate;
            return this;
        }

        /** 重试直到结果满足 predicate（predicate 返回 true 时停止重试并视为成功） */
        public Builder<T> retryIfResult(Predicate<T> predicate) {
            this.resultPredicate = predicate;
            return this;
        }

        public Builder<T> addListener(RetryListener listener) {
            this.listeners.add(listener);
            return this;
        }

        public RetryConfig<T> build() {
            return new RetryConfig<>(this);
        }
    }
}
