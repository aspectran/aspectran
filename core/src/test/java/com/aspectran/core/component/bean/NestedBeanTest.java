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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@AspectranTest(
    profiles = {"dev", "debug"},
    rules = "/config/bean/nested-bean-test-config.xml",
    debugMode = true
)
class NestedBeanTest {

    @Test
    void stringBean(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        assertEquals("String Bean", beanRegistry.getBean("stringBean"));

        Map<?, ?> map = beanRegistry.getBean("mapBean");
        assertEquals("{item2=value2, item1=value1}", map.toString());

        List<?> list = beanRegistry.getBean("listBean");
        assertEquals("[value1, value2]", list.toString());
    }

    @Test
    void nestedStringBean(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        assertEquals("Nested String Bean", beanRegistry.getBean("nestedStringBean"));
    }

    @Test
    void nestedStringBean_1(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        String result = beanRegistry.getBean("nestedStringBean-1");
        assertEquals("Nested String Bean", result);
    }

    @Test
    void nestedStringBeanDepth3(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        String result = beanRegistry.getBean("nestedStringBeanDepth3");
        assertEquals("Nested String Bean", result);
    }

    @Test
    void testNewNodeletParser(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        String result = beanRegistry.getBean("nestedStringBeanDepth3");
        assertEquals("Nested String Bean", result);
    }

    @Test
    void testDeepNestedStringBean(@NonNull ActivityContext context) {
        BeanRegistry beanRegistry = context.getBeanRegistry();
        String result = beanRegistry.getBean("deepNestedStringBean");
        assertEquals("Nested String Bean", result);
    }

}
