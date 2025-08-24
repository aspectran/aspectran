/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.config.AsyncConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.resource.InvalidResourceException;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsyncMethodTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    private BeanRegistry beanRegistry;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException, InvalidResourceException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        ContextConfig contextConfig = new ContextConfig();
        contextConfig.setBasePackage(new String[]{"com.aspectran.core.component.bean.async"});
        AsyncConfig asyncConfig = contextConfig.touchAsyncConfig();
        asyncConfig.setEnabled(true);

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.configure(contextConfig);
        context = builder.build();
        beanRegistry = context.getBeanRegistry();
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testNoActivity() throws InterruptedException, ExecutionException {
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = Thread.currentThread().getName();
        String asyncThread = testBean.noActivitySameThread();
        Future<String> future = testBean.noActivityDifferentThread();
        String asyncThread2 = future.get();

        // Give the async method some time to execute
//        Thread.sleep(500);

        assertNotNull(asyncThread);
        assertEquals(mainThread, asyncThread);
        assertNotEquals(mainThread, asyncThread2);
    }

    @Test
    void testVoidMethod() throws ActivityPerformException {
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = Thread.currentThread().getName();

        InstantActivity activity = new InstantActivity(context);
        ActivityData activityData = activity.perform(() -> {
            testBean.voidMethod();
            return activity.getActivityData();
        });

        // Give the async method some time to execute
//        Thread.sleep(500);

        String asyncThread = activityData.get("threadName").toString();
        assertNotNull(asyncThread);
        assertEquals(mainThread, asyncThread);
    }

    @Test
    void testFutureMethod() throws ExecutionException, InterruptedException, ActivityPerformException {
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = Thread.currentThread().getName();

        InstantActivity activity = new InstantActivity(context);
        CompletableFuture<String> future = activity.perform(() -> testBean.futureMethod());

        String asyncThread = future.get();
        assertNotNull(asyncThread);
        assertNotEquals(mainThread, asyncThread);
        assertTrue(asyncThread.startsWith("SimpleAsyncTaskExecutor-"));
    }

    @Test
    void testCustomExecutor() throws ExecutionException, InterruptedException {
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = testBean.getCallingThreadName();
        Future<String> future = testBean.customExecutorMethod();

        String asyncThread = future.get();
        assertNotNull(asyncThread);
        assertNotEquals(mainThread, asyncThread);
        assertTrue(asyncThread.startsWith("MyCustomExecutor-"));
    }

    @Test
    void testActivityPropagation() throws ExecutionException, InterruptedException {
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        Future<Activity> future = testBean.activityPropagationMethod();

        Activity asyncActivity = future.get();
        assertNotNull(asyncActivity);
        assertEquals(context.getCurrentActivity(), asyncActivity);
    }

}
