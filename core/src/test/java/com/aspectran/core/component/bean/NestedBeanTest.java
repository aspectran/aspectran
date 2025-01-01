/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NestedBeanTest {

    private ActivityContextBuilder builder;

    private BeanRegistry beanRegistry;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.setActiveProfiles("dev", "debug");
        ActivityContext context = builder.build("/config/bean/nested-bean-test-config.xml");
        beanRegistry = context.getBeanRegistry();
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void test1() {
        assertEquals("String Bean", beanRegistry.getBean("stringBean"));

        Map<?, ?> map = beanRegistry.getBean("mapBean");
        assertEquals("{item2=value2, item1=value1}", map.toString());

        List<?> list = beanRegistry.getBean("listBean");
        assertEquals("[value1, value2]", list.toString());
    }

    @Test
    void test2() {
        assertEquals("Nested String Bean", beanRegistry.getBean("nestedStringBean"));
    }

    @Test
    void nestedStringBean_1() {
        String result = beanRegistry.getBean("nestedStringBean-1");
        assertEquals("Nested String Bean", result);
    }

    @Test
    void nestedStringBean_2() {
        String result = beanRegistry.getBean("nestedStringBean-2");
        assertEquals("Nested String Bean", result);
    }

//    @Test
//    void overNestedStringBean() {
//        String result = beanRegistry.getBean("overNestedStringBean");
//        System.out.println(result);
//        assertEquals("Nested String Bean", result);
//    }

}
