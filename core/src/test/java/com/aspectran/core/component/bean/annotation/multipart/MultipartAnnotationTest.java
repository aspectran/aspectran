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
package com.aspectran.core.component.bean.annotation.multipart;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link com.aspectran.core.component.bean.annotation.Multipart} annotation
 * and automatic detection of file parameters.
 */
@AspectranTest(
    basePackages = "com.aspectran.core.component.bean.annotation.multipart"
)
class MultipartAnnotationTest {

    @Test
    void testMethodLevelMultipartAnnotation(@NonNull ActivityContext context) {
        TransletRule rule1 = context.getTransletRuleRegistry().getTransletRule("/test/upload1", MethodType.POST);
        assertNotNull(rule1);
        RequestRule requestRule1 = rule1.getRequestRule();
        assertNotNull(requestRule1);
        assertTrue(requestRule1.isMultipart());
        assertEquals("", requestRule1.getMultipartFormDataParser());

        TransletRule rule2 = context.getTransletRuleRegistry().getTransletRule("/test/upload2", MethodType.POST);
        assertNotNull(rule2);
        RequestRule requestRule2 = rule2.getRequestRule();
        assertNotNull(requestRule2);
        assertTrue(requestRule2.isMultipart());
        assertEquals("customParser", requestRule2.getMultipartFormDataParser());
    }

    @Test
    void testFileParameterDetection(@NonNull ActivityContext context) {
        TransletRule rule3 = context.getTransletRuleRegistry().getTransletRule("/test/upload3", MethodType.POST);
        assertNotNull(rule3);
        RequestRule requestRule3 = rule3.getRequestRule();
        assertNotNull(requestRule3);
        assertTrue(requestRule3.isMultipart());
        assertEquals("", requestRule3.getMultipartFormDataParser());

        TransletRule rule4 = context.getTransletRuleRegistry().getTransletRule("/test/upload4", MethodType.POST);
        assertNotNull(rule4);
        RequestRule requestRule4 = rule4.getRequestRule();
        assertNotNull(requestRule4);
        assertTrue(requestRule4.isMultipart());
        assertEquals("", requestRule4.getMultipartFormDataParser());

        TransletRule rule5 = context.getTransletRuleRegistry().getTransletRule("/test/upload5", MethodType.POST);
        assertNotNull(rule5);
        RequestRule requestRule5 = rule5.getRequestRule();
        assertNotNull(requestRule5);
        assertTrue(requestRule5.isMultipart());
        assertEquals("", requestRule5.getMultipartFormDataParser());
    }

    @Test
    void testClassLevelMultipartAnnotation(@NonNull ActivityContext context) {
        TransletRule rule1 = context.getTransletRuleRegistry().getTransletRule("/test/class-level/upload1", MethodType.POST);
        assertNotNull(rule1);
        RequestRule requestRule1 = rule1.getRequestRule();
        assertNotNull(requestRule1);
        assertTrue(requestRule1.isMultipart());
        assertEquals("classLevelParser", requestRule1.getMultipartFormDataParser());

        TransletRule rule2 = context.getTransletRuleRegistry().getTransletRule("/test/class-level/upload2", MethodType.POST);
        assertNotNull(rule2);
        RequestRule requestRule2 = rule2.getRequestRule();
        assertNotNull(requestRule2);
        assertTrue(requestRule2.isMultipart());
        assertEquals("overrideParser", requestRule2.getMultipartFormDataParser());
    }

    @Test
    void testNormalActionWithoutMultipart(@NonNull ActivityContext context) {
        TransletRule normalRule = context.getTransletRuleRegistry().getTransletRule("/test/normal", MethodType.POST);
        assertNotNull(normalRule);
        RequestRule requestRule = normalRule.getRequestRule();
        if (requestRule != null) {
            assertFalse(requestRule.isMultipart());
            assertNull(requestRule.getMultipartFormDataParser());
        }
    }

}
