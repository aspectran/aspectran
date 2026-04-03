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

@AspectranTest(
    basePackages = "com.aspectran.core.component.bean.proxy"
)
class HintTest {

    @Test
    void testHint(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            HintTestService testService = activity.getBean(HintTestService.class);
            activity.getRequestAdapter().setAttribute("testCase", "testHint");
            testService.testHint();
            return null;
        });
    }

    @Test
    void testHintPropagated(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            HintTestService testService = activity.getBean(HintTestService.class);
            activity.getRequestAdapter().setAttribute("testCase", "inner");
            activity.getRequestAdapter().setAttribute("isolated", false);
            testService.outerPropagated();
            return null;
        });
    }

    @Test
    void testHintIsolated(@NonNull ActivityTester tester) throws ActivityPerformException {
        tester.perform(activity -> {
            HintTestService testService = activity.getBean(HintTestService.class);
            activity.getRequestAdapter().setAttribute("testCase", "inner");
            activity.getRequestAdapter().setAttribute("isolated", true);
            testService.outerIsolated();
            return null;
        });
    }

}
