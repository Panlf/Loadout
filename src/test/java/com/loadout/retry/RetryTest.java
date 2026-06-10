package com.loadout.retry;


import java.rmi.AccessException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author panlf
 * @date 2026/6/10
 */
public class RetryTest {
    static class MyService {
        static String fetchData() throws AccessException {
            // 模拟失败
            throw new AccessException("no auth");
        }

        static String fetchWithNull() {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        // ---------- 同步示例：重试 AccessException ----------
        RetryConfig<String> config = RetryConfig.<String>builder()
                .maxAttempts(5)
                .waitStrategy(WaitStrategies.exponential(1000, 2))
                .retryOnException(AccessException.class)
                .addListener(new RetryListener() {
                    @Override
                    public void onException(int attempt, Throwable t) {
                        System.out.println("Attempt " + attempt + " failed: " + t.getMessage());
                    }
                })
                .build();

        RetryExecutor<String> executor = RetryExecutor.create(config);
        try {
            String result = executor.execute(MyService::fetchData);
            System.out.println("Sync success: " + result);
        } catch (Exception e) {
            System.err.println("Sync failed: " + e);
        }

        // ---------- 异步示例：重试直到结果非空 ----------
        RetryConfig<String> config2 = RetryConfig.<String>builder()
                .maxAttempts(4)
                .waitStrategy(WaitStrategies.random(500, 1500))
                .retryIfResult(Objects::nonNull)   // 只有返回非空才停止重试
                .build();

        RetryExecutor<String> executor2 = RetryExecutor.create(config2);
        CompletableFuture<String> future = executor2.executeAsync(MyService::fetchWithNull);
        future.whenComplete((res, ex) -> {
            if (ex != null) System.err.println("Async failed: " + ex);
            else System.out.println("Async success: " + res);
        });

        // 等待异步完成（演示用）
        Thread.sleep(8000);
    }
}
