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
