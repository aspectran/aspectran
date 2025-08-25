package com.aspectran.core.component.bean.async;

import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.Method;

/**
 * A strategy for handling uncaught exceptions thrown from asynchronous methods.
 *
 * <p>An asynchronous method usually returns a {@link java.util.concurrent.Future}
 * instance that gives access to the underlying exception. When the method does
 * not provide that return type, this handler can be used to manage such
 * uncaught exceptions.
 */
@FunctionalInterface
public interface AsyncUncaughtExceptionHandler {

    /**
     * Handle the given uncaught exception thrown from an asynchronous method.
     * @param ex the exception thrown from the asynchronous method
     * @param method the asynchronous method
     * @param params the parameters used to invoke the method
     */
    void handleUncaughtException(Throwable ex, Method method, @Nullable Object... params);

}

