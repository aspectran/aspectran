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

import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for bean depends-on attribute.
 *
 * <p>Created: 2026. 04. 24</p>
 */
class DependsOnBeanTest {

    static final List<String> lifecycleLog = new ArrayList<>();

    @Test
    void testDependsOn() throws Exception {
        lifecycleLog.clear();
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setBasePath(new java.io.File("target/test-classes").getCanonicalPath());
        builder.build("/config/bean/depends-on-test.xml");

        builder.destroy();

        List<String> expected = List.of(
                "init:beanC",
                "init:beanB",
                "init:beanA",
                "destroy:beanA",
                "destroy:beanB",
                "destroy:beanC"
        );
        assertEquals(lifecycleLog, expected);
    }

    @Test
    void testCircularDependsOn() throws Exception {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setBasePath(new java.io.File("target/test-classes").getCanonicalPath());
        assertThrows(Exception.class, () -> {
            builder.build("/config/bean/circular-depends-on-test.xml");
        });
    }

    public static class TestBean {
        private final String name;
        public TestBean(String name) {
            this.name = name;
        }
        public void init() {
            lifecycleLog.add("init:" + name);
        }
        public void destroy() {
            lifecycleLog.add("destroy:" + name);
        }
    }

}
