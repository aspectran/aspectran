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

import java.io.Serial;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Exception thrown when a {@link AsyncTaskExecutor} rejects to accept
 * a given task for execution.
 */
public class AsyncTaskRejectedException extends RejectedExecutionException {

    @Serial
    private static final long serialVersionUID = 7614436492146483943L;

    /**
     * Create a new {@code TaskRejectedException}
     * with the specified detail message and no root cause.
     * @param msg the detail message
     */
    public AsyncTaskRejectedException(String msg) {
        super(msg);
    }

    /**
     * Create a new {@code TaskRejectedException}
     * with the specified detail message and the given root cause.
     * @param msg the detail message
     * @param cause the root cause (usually from using an underlying
     * API such as the {@code java.util.concurrent} package)
     * @see java.util.concurrent.RejectedExecutionException
     */
    public AsyncTaskRejectedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new {@code TaskRejectedException}
     * with a default message for the given executor and task.
     * @param executor the {@code Executor} that rejected the task
     * @param task the task object that got rejected
     * @param cause the original {@link RejectedExecutionException}
     * @see ExecutorService#isShutdown()
     * @see java.util.concurrent.RejectedExecutionException
     */
    public AsyncTaskRejectedException(Executor executor, Object task, RejectedExecutionException cause) {
        super(executorDescription(executor) + " did not accept task: " + task, cause);
    }

    private static String executorDescription(Executor executor) {
        if (executor instanceof ExecutorService executorService) {
            try {
                return "ExecutorService in " + (executorService.isShutdown() ? "shutdown" : "active") + " state";
            }
            catch (Exception ex) {
                // UnsupportedOperationException/IllegalStateException from ManagedExecutorService.isShutdown()
                // Falling back to toString() below.
            }
        }
        return executor.toString();
    }

}
