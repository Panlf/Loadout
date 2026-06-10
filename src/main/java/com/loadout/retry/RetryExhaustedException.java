package com.loadout.retry;


/**
 *
 * @author panlf
 * @date 2026/6/10
 */
public class RetryExhaustedException extends Exception {
    public RetryExhaustedException(String message) {
        super(message);
    }
}