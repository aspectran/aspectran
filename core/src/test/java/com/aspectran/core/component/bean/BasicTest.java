/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.util.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.setActiveProfiles("dev", "debug");
        context = builder.build("/config/bean/basic-test-config.xml");
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void test1() {
        assertEquals("String Bean", context.getBeanRegistry().getBean("stringBean"));

        Map map = context.getBeanRegistry().getBean("mapBean");
        assertEquals("{item2=value2, item1=value1}", map.toString());

        List list = context.getBeanRegistry().getBean("listBean");
        assertEquals("[value1, value2]", list.toString());
    }

    @Test
    void test2() {
        assertEquals("Nested String Bean", context.getBeanRegistry().getBean("nestedStringBean"));
    }

    @Test
    void test3() {
        assertThrows(IllegalRuleException.class, () -> {
//            context.getBeanRegistry().getBean("nestedInnerStringBean");
        });
    }

}
