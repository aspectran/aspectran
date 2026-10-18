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
package com.aspectran.core.component.bean.proxy;

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.test.ActivityTester;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases verifying the AOP advice lifecycle execution order in {@link AbstractBeanProxy}.
 *
 * <p>Created: 2026. 10. 18.</p>
 */
@AspectranTest(
    basePackages = "com.aspectran.core.component.bean.proxy"
)
class ProxyLifecycleTest {

    @Test
    void testSuccessLifecycle(@NonNull ActivityTester tester) throws ActivityPerformException {
        List<String> events = new ArrayList<>();
        tester.perform(activity -> {
            activity.getRequestAdapter().setAttribute("events", events);
            ProxyLifecycleTestService testService = activity.getBean(ProxyLifecycleTestService.class);
            String result = testService.doSuccess(events);
            assertEquals("success", result);
            return null;
        });

        assertEquals(List.of("before", "method:success", "after", "finally"), events);
    }

    @Test
    void testFailureLifecycleOrder(@NonNull ActivityTester tester) {
        List<String> events = new ArrayList<>();
        assertThrows(Exception.class, () -> tester.perform(activity -> {
            activity.getRequestAdapter().setAttribute("events", events);
            ProxyLifecycleTestService testService = activity.getBean(ProxyLifecycleTestService.class);
            testService.doFailure(events);
            return null;
        }));

        // Crucial: "thrown" MUST execute BEFORE "finally", and "after" must be skipped!
        assertEquals(List.of("before", "method:failure", "thrown", "finally"), events);
    }

}
