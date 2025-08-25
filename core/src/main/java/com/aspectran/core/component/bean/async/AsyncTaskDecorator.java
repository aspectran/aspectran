package com.aspectran.core.component.bean.async;

/**
 * A callback interface for a decorator to be applied to any {@link Runnable}
 * about to be executed.
 *
 * <p>Note that such a decorator is not necessarily being applied to the
 * user-supplied {@code Runnable}/{@code Callable} but rather to the actual
 * execution callback (which may be a wrapper around the user-supplied task).
 *
 * <p>The primary use case is to set some execution context around the task's
 * invocation, or to provide some monitoring/statistics for task execution.
 *
 * <p><b>NOTE:</b> Exception handling in {@code TaskDecorator} implementations
 * may be limited. Specifically in case of a {@code Future}-based operation,
 * the exposed {@code Runnable} will be a wrapper which does not propagate
 * any exceptions from its {@code run} method.
 *
 * @see SimpleAsyncTaskExecutor#setTaskDecorator
 */
@FunctionalInterface
public interface AsyncTaskDecorator {

    /**
     * Decorate the given {@code Runnable}, returning a potentially wrapped
     * {@code Runnable} for actual execution, internally delegating to the
     * original {@link Runnable#run()} implementation.
     * @param runnable the original {@code Runnable}
     * @return the decorated {@code Runnable}
     */
    Runnable decorate(Runnable runnable);

}
