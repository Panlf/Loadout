package com.loadout.retry;

import java.util.concurrent.*;

/**
 * 重试执行器（同步 + 异步）
 * @author panlf
 * @date 2026/6/10
 */
public class RetryExecutor<T> {
    private final RetryConfig<T> config;
    private final ScheduledExecutorService scheduler;

    private RetryExecutor(RetryConfig<T> config, ScheduledExecutorService scheduler) {
        this.config = config;
        this.scheduler = scheduler;
    }

    /**
     * 创建执行器（自动创建守护线程调度器）
     */
    public static <T> RetryExecutor<T> create(RetryConfig<T> config) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "retry-scheduler");
            t.setDaemon(true);
            return t;
        });
        return new RetryExecutor<>(config, scheduler);
    }

    /**
     * 创建执行器（复用外部调度器）
     */
    public static <T> RetryExecutor<T> create(RetryConfig<T> config, ScheduledExecutorService sharedScheduler) {
        return new RetryExecutor<>(config, sharedScheduler);
    }

    // ==================== 同步执行 ====================
    public T execute(Callable<T> action) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= config.maxAttempts; attempt++) {
            fireBeforeAttempt(attempt);
            try {
                T result = executeWithTimeout(action);
                if (isSuccessResult(result)) {
                    fireSuccess(attempt, result);
                    return result;
                }
                fireResultMismatch(attempt, result);
                lastException = null;
            } catch (Exception e) {
                lastException = e;
                fireException(attempt, e);
                if (shouldRetryOnException(e)) {
                    throw e;
                }
            }

            if (attempt == config.maxAttempts) {
                if (lastException != null) {
                    throw lastException;
                } else {
                    throw new RetryExhaustedException("All attempts completed but result condition not satisfied");
                }
            }

            long wait = config.waitStrategy.computeWaitTime(attempt);
            if (wait > 0) {
                fireWait(attempt, wait);
                Thread.sleep(wait);
            }
        }
        throw new IllegalStateException("Unexpected exit");
    }

    // ==================== 异步执行 ====================
    public CompletableFuture<T> executeAsync(Callable<T> action) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        doAsyncAttempt(action, 1, promise);
        return promise;
    }

    private void doAsyncAttempt(Callable<T> action, int attempt, CompletableFuture<T> promise) {
        fireBeforeAttempt(attempt);
        CompletableFuture<T> attemptFuture = executeWithTimeoutAsync(action);
        attemptFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
                fireException(attempt, cause);
                if (shouldRetryOnException(cause) || attempt >= config.maxAttempts) {
                    promise.completeExceptionally(cause);
                    return;
                }
                scheduleRetry(action, attempt, cause, promise);
            } else {
                if (isSuccessResult(result)) {
                    fireSuccess(attempt, result);
                    promise.complete(result);
                } else {
                    fireResultMismatch(attempt, result);
                    if (attempt >= config.maxAttempts) {
                        promise.completeExceptionally(new RetryExhaustedException("Result condition never satisfied"));
                    } else {
                        scheduleRetry(action, attempt, null, promise);
                    }
                }
            }
        });
    }

    private void scheduleRetry(Callable<T> action, int attempt, Throwable lastException, CompletableFuture<T> promise) {
        long wait = config.waitStrategy.computeWaitTime(attempt);
        if (wait <= 0) {
            doAsyncAttempt(action, attempt + 1, promise);
        } else {
            fireWait(attempt, wait);
            scheduler.schedule(() -> doAsyncAttempt(action, attempt + 1, promise), wait, TimeUnit.MILLISECONDS);
        }
    }

    // ==================== 辅助私有方法 ====================
    private T executeWithTimeout(Callable<T> action) throws Exception {
        if (config.timeoutMillis <= 0) {
            return action.call();
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(action);
        try {
            return future.get(config.timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("Execution timeout", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private CompletableFuture<T> executeWithTimeoutAsync(Callable<T> action) {
        if (config.timeoutMillis <= 0) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return action.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            future.completeExceptionally(new TimeoutException("Timeout after " + config.timeoutMillis + " ms"));
        }, config.timeoutMillis, TimeUnit.MILLISECONDS);
        CompletableFuture.supplyAsync(() -> {
            try {
                return action.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }).whenComplete((result, ex) -> {
            timeoutTask.cancel(false);
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                future.complete(result);
            }
        });
        return future;
    }

    private boolean shouldRetryOnException(Throwable t) {
        return config.exceptionPredicate == null || !config.exceptionPredicate.test(t);
    }

    private boolean isSuccessResult(T result) {
        return config.resultPredicate == null || config.resultPredicate.test(result);
    }

    private void fireBeforeAttempt(int attempt) {
        config.listeners.forEach(l -> l.onBeforeAttempt(attempt, config.maxAttempts));
    }

    private void fireSuccess(int attempt, T result) {
        config.listeners.forEach(l -> l.onSuccess(attempt, result));
    }

    private void fireException(int attempt, Throwable t) {
        config.listeners.forEach(l -> l.onException(attempt, t));
    }

    private void fireResultMismatch(int attempt, T result) {
        config.listeners.forEach(l -> l.onResultMismatch(attempt, result));
    }

    private void fireWait(int attempt, long wait) {
        config.listeners.forEach(l -> l.onWait(attempt, wait));
    }
}
