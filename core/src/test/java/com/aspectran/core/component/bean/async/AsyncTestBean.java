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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.Async;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
@Bean("asyncTestBean")
public class AsyncTestBean extends InstantActivitySupport {

    private String getCallingThreadName() {
        return Thread.currentThread().getName();
    }

    @Async
    public Future<String> noActivitySameThread() {
        System.out.println("Executing voidMethod on thread: " + getCallingThreadName());
        return CompletableFuture.supplyAsync(
                this::getCallingThreadName
        );
    }

    @Async
    public Future<String> noActivityDifferentThread() {
        System.out.println("Executing voidMethod on thread: " + getCallingThreadName());
        return CompletableFuture.supplyAsync(
                this::getCallingThreadName
        );
    }

    @Async
    public void voidMethod() {
        System.out.println("Executing voidMethod on thread: " + getCallingThreadName());
        getCurrentActivity().getActivityData().put("threadName", getCallingThreadName());
    }

    @Async
    public Future<String> futureMethod() {
        System.out.println("Executing futureMethod on thread: " + getCallingThreadName());
        return CompletableFuture.supplyAsync(
                this::getCallingThreadName
        );
    }

    @Async("customTaskExecutor")
    public Future<String> customExecutorMethod() {
        System.out.println("Executing customExecutorMethod on thread: " + getCallingThreadName());
        return CompletableFuture.supplyAsync(
                this::getCallingThreadName
        );
    }

    @Async
    public Future<Activity> activityPropagationMethod() {
        final Activity currentActivity = getCurrentActivity();
        return CompletableFuture.supplyAsync(
                () -> currentActivity
        );
    }

}
