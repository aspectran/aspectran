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
package com.aspectran.web.activity;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.WebAspectranTest;
import com.aspectran.test.web.WebAspectranTester;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for WebAspectranTester.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
@WebAspectranTest(rules = "classpath:config/web-test-config.xml")
class WebActivityTest {

    @Test
    void testHello(@NonNull WebAspectranTester tester) {
        tester.perform(MethodType.GET, "/hello");
        assertEquals("Hello, Web World!", tester.getWrittenResponse());
    }

}
