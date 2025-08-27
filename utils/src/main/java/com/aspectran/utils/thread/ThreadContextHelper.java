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
 * A helper class for managing the thread context ClassLoader.
 *
 * <p>Created: 2024-12-30</p>
 */
public abstract class ThreadContextHelper {

    /**
     * Override the thread context ClassLoader with the given ClassLoader if necessary,
     * i.e. if the given ClassLoader is not equivalent to the thread context
     * ClassLoader already.
     * @param classLoader the ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or {@code null} if not overridden
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
     * Restores the original thread context ClassLoader.
     * @param classLoader the original thread context ClassLoader
     */
    public static void restoreClassLoader(@Nullable ClassLoader classLoader) {
        if (classLoader != null) {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * Executes a {@link Runnable} in the given ClassLoader context.
     * @param classLoader the ClassLoader to use
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
     * Executes a {@link ThrowingRunnable} in the given ClassLoader context.
     * @param classLoader the ClassLoader to use
     * @param runnable the {@code ThrowingRunnable} to execute
     * @param <T> the type of the throwable
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
     * Executes a {@link Callable} in the given ClassLoader context.
     * @param classLoader the ClassLoader to use
     * @param callable the {@code Callable} to execute
     * @param <V> the type of the return value
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
     * A runnable that can throw a checked exception.
     * @param <T> the type of the throwable
     */
    public interface ThrowingRunnable<T extends Throwable> {

        /**
         * Executes the runnable.
         * @throws T if an error occurs
         */
        void run() throws T;

    }

}
