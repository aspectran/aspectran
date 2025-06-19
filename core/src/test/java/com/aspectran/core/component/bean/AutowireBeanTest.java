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

import com.aspectran.core.component.template.TemplateRenderer;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test case for beans.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutowireBeanTest {

    private ActivityContextBuilder builder;

    private BeanRegistry beanRegistry;

    private TemplateRenderer templateRenderer;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.setActiveProfiles("dev", "local");
        builder.setBasePackages("com.aspectran.core.component.bean");
        ActivityContext context = builder.build("/config/bean/autowire-bean-test-config.xml", "/config/bean/test-properties.xml");
        beanRegistry = context.getBeanRegistry();
        templateRenderer = context.getTemplateRenderer();
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testProperties() {
        beanRegistry.getBean("properties");
        String property1 = templateRenderer.render("property-1");
        String property2 = templateRenderer.render("property-2");
        String property3 = templateRenderer.render("property-3");
        String property4 = templateRenderer.render("property-4");
        assertEquals("DEV-Property-1", property1);
        assertEquals("DEV-Property-2", property2);
        assertEquals("DEV-Property-3", property3);
        assertEquals("DEV-Property-1 / Property-2 / Property-3", property4);
    }

    @Test
    void testConstructorAutowire() {
        TestConstructorAutowireBean bean = beanRegistry.getBean("bean.TestConstructorAutowireBean");
        assertEquals("Property-1", bean.bean1.getProperty1());
        assertEquals("Property-1", bean.bean2.getProperty1());
    }

    @Test
    void testConstructorAutowire2() {
        TestConstructorAutowireBean2 bean = beanRegistry.getBean("bean.TestConstructorAutowireBean2");
        assertEquals("Property-1", bean.bean1.getProperty1());
        assertEquals("Property-1", bean.bean2.getProperty1());
    }

    @Test
    void testConstructorAutowire3() {
        TestConstructorAutowireBean3 bean = beanRegistry.getBean("bean.TestConstructorAutowireBean3");
        assertEquals("Property-1", bean.bean1.getBean1().getProperty1());
    }

    @Test
    void testFieldValueAutowire() {
        TestFieldValueAutowireBean bean = beanRegistry.getBean("bean.TestFieldValueAutowireBean");
        assertEquals("Property-1", bean.getProperty1());
        assertEquals("Property-2", bean.getProperty2());
        assertEquals("Property-3", bean.getProperty3());
        assertEquals("property-4", bean.getProperty4());
    }

    @Test
    void testFieldValueExpression() {
        TestFieldValueAutowireBean bean = beanRegistry.getBean("bean.TestFieldValueAutowireBean");
        assertEquals("property5", bean.getProperty5());
        assertEquals(30, bean.getProperty6());
        assertEquals("Property-1/Property-2/Property-3", bean.getProperty7());
    }

    @Test
    void testFieldAutowire() {
        TestFieldAutowireBean bean = beanRegistry.getBean("bean.TestFieldAutowireBean");
        assertEquals("Property-1", bean.getBean1().getProperty1());
        assertEquals("Property-2", bean.getBean1().getProperty2());
        assertEquals("Property-3", bean.getBean1().getProperty3());
        assertEquals("property-4", bean.getBean1().getProperty4());
        assertEquals("Property-1", bean.getBean2().getProperty1());
        assertEquals("Property-2", bean.getBean2().getProperty2());
        assertEquals("Property-3", bean.getBean2().getProperty3());
        assertEquals("property-4", bean.getBean2().getProperty4());
    }

    @Test
    void testMethodAutowire() {
        TestMethodAutowireBean bean = beanRegistry.getBean("bean.TestMethodAutowireBean");
        assertEquals("Property-1", bean.getBean1().getProperty1());
        assertEquals("Property-2", bean.getBean1().getProperty2());
        assertEquals("Property-3", bean.getBean1().getProperty3());
        assertEquals("property-4", bean.getBean1().getProperty4());
        assertEquals(223, bean.getNumber());
        assertNull(bean.getBean2()); // Undefined bean
    }

}
