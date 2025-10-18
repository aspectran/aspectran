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
package com.aspectran.core.component.bean.async;

import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.concurrent.FutureUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * An interface that abstracts asynchronous task execution.
 * Provides a way to submit {@link Runnable} and {@link java.util.concurrent.Callable} tasks
 * for execution in a background thread.
 *
 * <p>This interface extends {@link java.util.concurrent.Executor} and adds
 * methods for submitting tasks that return a {@link java.util.concurrent.CompletableFuture},
 * as well as a mechanism for handling uncaught exceptions.</p>
 *
 * <p>Created: 2024. 8. 24.</p>
 */
public interface AsyncTaskExecutor extends Executor {

    default CompletableFuture<Void> submit(Runnable task) {
        return CompletableFuture.runAsync(task, this);
    }

    default <V> CompletableFuture<V> submit(Callable<V> task) {
        return FutureUtils.callAsync(task, this);
    }

    @Nullable
    default AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

}
