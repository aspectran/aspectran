/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.thread;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.concurrent.Callable;

/**
 * A helper class for managing the thread context {@link ClassLoader}.
 * <p>This utility allows temporarily overriding the thread context ClassLoader
 * for a specific block of code, ensuring that class loading operations within
 * that block occur in the desired ClassLoader context.</p>
 *
 * <p>Created: 2024-12-30</p>
 */
public abstract class ThreadContextHelper {

    /**
     * Overrides the current thread's context {@link ClassLoader} with the given ClassLoader if necessary.
     * <p>This method is typically used in a try-finally block to ensure the original ClassLoader is restored.</p>
     * @param classLoader the ClassLoader to set as the thread context ClassLoader (may be {@code null})
     * @return the original thread context ClassLoader, or {@code null} if no override occurred
     */
    @Nullable
    public static ClassLoader overrideClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader != null) {
            Thread currentThread = Thread.currentThread();
            ClassLoader contextClassLoader = currentThread.getContextClassLoader();
            if (!classLoader.equals(contextClassLoader)) {
                currentThread.setContextClassLoader(classLoader);
                return contextClassLoader;
            }
        }
        return null;
    }

    /**
     * Restores the original thread context {@link ClassLoader}.
     * <p>This method should be called in a {@code finally} block after {@link #overrideClassLoader(ClassLoader)}.</p>
     * @param classLoader the original thread context ClassLoader returned by {@link #overrideClassLoader(ClassLoader)}
     */
    public static void restoreClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader != null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * Executes a {@link Runnable} within the context of the given {@link ClassLoader}.
     * The original thread context ClassLoader is restored after execution.
     * @param classLoader the ClassLoader to use for the execution
     * @param runnable the {@code Runnable} to execute
     */
    public static void run(ClassLoader classLoader, @NonNull Runnable runnable) {
        ClassLoader old = overrideClassLoader(classLoader);
        try {
            runnable.run();
        } finally {
            restoreClassLoader(old);
        }
    }

    /**
     * Executes a {@link ThrowingRunnable} within the context of the given {@link ClassLoader}.
     * The original thread context ClassLoader is restored after execution.
     * @param classLoader the ClassLoader to use for the execution
     * @param runnable the {@code ThrowingRunnable} to execute
     * @param <T> the type of the throwable that the runnable can throw
     * @throws T if the runnable throws an exception
     */
    public static <T extends Throwable> void runThrowable(
            ClassLoader classLoader, @NonNull ThrowingRunnable<T> runnable)
            throws T {
        ClassLoader old = overrideClassLoader(classLoader);
        try {
            runnable.run();
        } finally {
            restoreClassLoader(old);
        }
    }

    /**
     * Executes a {@link Callable} within the context of the given {@link ClassLoader}.
     * The original thread context ClassLoader is restored after execution.
     * @param classLoader the ClassLoader to use for the execution
     * @param callable the {@code Callable} to execute
     * @param <V> the type of the return value of the {@code Callable}
     * @return the result of the {@code Callable}
     * @throws Exception if the callable throws an exception
     */
    public static <V> V call(ClassLoader classLoader, @NonNull Callable<V> callable)
            throws Exception {
        ClassLoader old = overrideClassLoader(classLoader);
        try {
            return callable.call();
        } finally {
            restoreClassLoader(old);
        }
    }

    /**
     * A functional interface for a runnable that can throw a checked exception.
     * @param <T> the type of the throwable that can be thrown
     */
    public interface ThrowingRunnable<T extends Throwable> {

        /**
         * Executes the runnable.
         * @throws T if an error occurs during execution
         */
        void run() throws T;

    }

}
