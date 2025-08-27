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
import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.activity.InstantActivitySupport;
import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.core.component.bean.annotation.Async;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
@Bean("asyncTestBean")
public class AsyncTestBean extends InstantActivitySupport {

    public String getCallingThreadName() {
        return Thread.currentThread().getName();
    }

    @Async
    public Future<String> noActivitySameThread() {
        String threadName = getCallingThreadName();
        System.out.println("Executing noActivitySameThread on thread: " + threadName);
        return CompletableFuture.supplyAsync(() -> threadName);
    }

    @Async
    public Future<String> noActivityDifferentThread() {
        String threadName = getCallingThreadName();
        System.out.println("Executing noActivityDifferentThread on thread: " + threadName);
        return CompletableFuture.supplyAsync(() -> threadName);
    }

    @Async
    public void voidMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing voidMethod on thread: " + threadName);
        Activity currentActivity = getCurrentActivity();
        System.out.println("Current activity: " + currentActivity);
//        ActivityData activityData = currentActivity.getActivityData();
//        activityData.put("threadName", threadName);
    }

    /**
     * Asynchronously executes a nested invocation and returns a Future.
     * The inner methods {@code advisableMethod} and {@code unadvisableMethod}
     * are executed in the same thread as this method.
     * @return a {@code Future} holding the concatenated result string
     */
    @Async
    public Future<String> nestedInvocation() {
        String threadName = getCallingThreadName();
        System.out.println("Executing nestedInvocation on thread: " + threadName);
        getCurrentActivity().getActivityData().put("first", "nestedInvocation");
        String result = advisableMethod() + " and " + unadvisableMethod();
        return CompletableFuture.completedFuture(result);
    }

    @Advisable
    public String advisableMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing advisableMethod on thread: " + threadName);
        getCurrentActivity().getActivityData().put("second", "advisableMethod");
        return "advisableMethod";
    }

    @Advisable
    public String unadvisableMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing unadvisableMethod on thread: " + threadName);
        getCurrentActivity().getActivityData().put("third", "unadvisableMethod");
        return "unadvisableMethod and " + noAdvisableMethod();
    }

    public String noAdvisableMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing noAdvisableMethod on thread: " + threadName);
        getCurrentActivity().getActivityData().put("fourth", "noAdvisableMethod");
        return "noAdvisableMethod";
    }

    @Async
    public Future<String> futureMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing futureMethod on thread: " + threadName);
        //System.out.println(ClassUtils.getDefaultClassLoader());
        return CompletableFuture.supplyAsync(() -> {
            //System.out.println(ClassUtils.getDefaultClassLoader());
            return threadName;
        });
    }

    @Async("myCustomTaskExecutor")
    public Future<String> customExecutorMethod() {
        String threadName = getCallingThreadName();
        System.out.println("Executing customExecutorMethod on thread: " + threadName);
        return CompletableFuture.supplyAsync(() -> threadName);
    }

    @Async
    public Future<Activity> activityPropagationMethod() {
        Activity currentActivity = getCurrentActivity();
        return CompletableFuture.supplyAsync(() -> currentActivity);
    }

    @Async
    public void errorOccurredMethod() {
        final String threadName = getCallingThreadName();
        System.out.println("Executing errorOccurredMethod on thread: " + threadName);
        throw new RuntimeException("Exception in async method!");
    }

}
