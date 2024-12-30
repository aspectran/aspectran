package com.aspectran.utils.thread;

/**
 * <p>Created: 2024. 12. 30.</p>
 */
public interface ThrowingRunnable<T extends Throwable> {

    void run() throws T;

}
