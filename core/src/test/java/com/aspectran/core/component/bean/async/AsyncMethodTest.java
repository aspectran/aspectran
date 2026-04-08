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
import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.component.bean.BeanRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AspectranTest(
    basePackages = "com.aspectran.core.component.bean.async",
    async = true
)
class AsyncMethodTest {

    @Test
    void testNoActivity(@NonNull ActivityContext context) throws InterruptedException, ExecutionException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = testBean.getCallingThreadName();

        Future<String> future1 = testBean.noActivitySameThread();
        Future<String> future2 = testBean.noActivityDifferentThread();

        String asyncThread1 = future1.get();
        String asyncThread2 = future2.get();

        assertNotNull(asyncThread1);
        assertNotEquals(mainThread, asyncThread1);
        assertNotEquals(mainThread, asyncThread2);
    }

    @Test
    void testVoidMethod(@NonNull ActivityContext context) throws ActivityPerformException, InterruptedException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = testBean.getCallingThreadName();

        InstantActivity activity = new InstantActivity(context);
        activity.perform(() -> {
            testBean.voidMethod();
            return null;
        });

        Thread.sleep(500);

        ActivityData activityData = activity.getActivityData();
        assertNotNull(activityData);

        String asyncThread = activityData.get("threadName").toString();
        assertNotNull(asyncThread);
        assertNotEquals(mainThread, asyncThread);
        assertTrue(asyncThread.startsWith("SimpleAsyncTaskExecutor-"));
    }

    @Test
    void testNestedInvocation1(@NonNull ActivityContext context) throws InterruptedException, ExecutionException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        Future<String> future = testBean.nestedInvocation();
        String result = future.get();
        assertEquals("advisableMethod and unadvisableMethod and noAdvisableMethod", result);
    }

    @Test
    void testNestedInvocation2(@NonNull ActivityContext context) throws ActivityPerformException, ExecutionException, InterruptedException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        InstantActivity activity = new InstantActivity(context);
        Future<String> future = activity.perform(testBean::nestedInvocation);
        future.get();

        ActivityData data = activity.getActivityData();

        String result = data.get("first") + " and " + data.get("second") + " and " + data.get("third") + " and " + data.get("fourth");
        assertEquals("nestedInvocation and advisableMethod and unadvisableMethod and noAdvisableMethod", result);
    }

    @Test
    void testFutureMethod(@NonNull ActivityContext context) throws ExecutionException, InterruptedException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = testBean.getCallingThreadName();
        Future<String> future = testBean.futureMethod();

        String asyncThread = future.get();
        assertNotNull(asyncThread);
        assertNotEquals(mainThread, asyncThread);
        assertTrue(asyncThread.startsWith("SimpleAsyncTaskExecutor-"));
    }

    @Test
    void testCustomExecutor(@NonNull ActivityContext context) throws ExecutionException, InterruptedException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        String mainThread = testBean.getCallingThreadName();
        Future<String> future = testBean.customExecutorMethod();

        String asyncThread = future.get();
        assertNotNull(asyncThread);
        assertNotEquals(mainThread, asyncThread);
        assertTrue(asyncThread.startsWith("MyCustomTaskExecutor-"));
    }

    @Test
    void testActivityPropagation(@NonNull ActivityContext context) throws ExecutionException, InterruptedException, ActivityPerformException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        InstantActivity activity = new InstantActivity(context);
        Future<Activity> future = activity.perform(testBean::activityPropagationMethod);

        Activity asyncActivity = future.get();
        ActivityData data = asyncActivity.getActivityData();

        assertEquals("it's me", data.get("who"));
        assertInstanceOf(InstantActivity.class, asyncActivity);
    }

    @Test
    void errorOccurredMethod(@NonNull ActivityContext context) throws InterruptedException {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        AsyncTestBean testBean = beanRegistry.getBean("asyncTestBean");
        assertNotNull(testBean);

        testBean.errorOccurredMethod();

        Thread.sleep(500);
    }

}
