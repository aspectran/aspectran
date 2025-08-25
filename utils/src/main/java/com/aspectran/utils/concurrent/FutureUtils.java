package com.aspectran.utils.concurrent;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Convenience utilities for working with {@link java.util.concurrent.Future}
 * and implementations.
 */
public abstract class FutureUtils {

    /**
     * Return a new {@code CompletableFuture} that is asynchronously completed
     * by a task running in the {@link ForkJoinPool#commonPool()} with
     * the value obtained by calling the given {@code Callable}.
     * @param callable a function that returns the value to be used, or throws
     * an exception
     * @return the new CompletableFuture
     * @see CompletableFuture#supplyAsync(Supplier)
     */
    @NonNull
    public static <T> CompletableFuture<T> callAsync(Callable<T> callable) {
        Assert.notNull(callable, "Callable must not be null");

        CompletableFuture<T> result = new CompletableFuture<>();
        return result.completeAsync(toSupplier(callable, result));
    }

    /**
     * Return a new {@code CompletableFuture} that is asynchronously completed
     * by a task running in the given executor with the value obtained
     * by calling the given {@code Callable}.
     * @param callable a function that returns the value to be used, or throws
     * an exception
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletableFuture
     * @see CompletableFuture#supplyAsync(Supplier, Executor)
     */
    @NonNull
    public static <T> CompletableFuture<T> callAsync(Callable<T> callable, Executor executor) {
        Assert.notNull(callable, "Callable must not be null");
        Assert.notNull(executor, "Executor must not be null");

        CompletableFuture<T> result = new CompletableFuture<>();
        return result.completeAsync(toSupplier(callable, result), executor);
    }

    @NonNull
    private static <T> Supplier<T> toSupplier(Callable<T> callable, CompletableFuture<T> result) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception ex) {
                // wrap the exception just like CompletableFuture::supplyAsync does
                result.completeExceptionally((ex instanceof CompletionException) ? ex : new CompletionException(ex));
                return null;
            }
        };
    }

}
