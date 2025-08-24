package com.aspectran.core.component.bean.async;

import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Created: 2025-08-24</p>
 */
@Component
@Bean("customTaskExecutor")
public class CustomTaskExecutor implements AsyncTaskExecutor {

    @Override
    public void execute(Runnable task) {

    }

    @Override
    public <V> CompletableFuture<V> submit(Callable<V> task) {
        return CompletableFuture.supplyAsync(() -> {
            return (V)Thread.currentThread().getName();
        });
    }

}
