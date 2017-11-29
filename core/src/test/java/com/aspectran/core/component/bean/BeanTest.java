/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
import com.aspectran.core.util.ResourceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test case for beans.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BeanTest {

    private File baseDir;

    private ActivityContextBuilder builder;

    private ActivityContext context;

    private BeanRegistry beanRegistry;

    @Before
    public void ready() throws IOException, ActivityContextBuilderException {
        baseDir = ResourceUtils.getResourceAsFile("");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setHybridLoad(false);
        builder.setActiveProfiles("dev", "local");
        context = builder.build("/config/bean/bean-test-config.xml");
        beanRegistry = context.getBeanRegistry();
    }

    @Test
    public void testProperties() {
        beanRegistry.getBean("properties");
        String property1 = context.getTemplateProcessor().process("property-1");
        String property2 = context.getTemplateProcessor().process("property-2");
        String property3 = context.getTemplateProcessor().process("property-3");
        String property4 = context.getTemplateProcessor().process("property-4");
        assertEquals(property1, "DEV-This is a Property-1");
        assertEquals(property2, "DEV-This is a Property-2");
        assertEquals(property3, "DEV-This is a Property-3");
        assertEquals(property4, "DEV-This is a Property-1 / This is a Property-2 / This is a Property-3");
    }

    @Test
    public void testAutowire() {
        TestValueAnnotationBean bean1 = beanRegistry.getBean("bean.TestValueAnnotationBean");
        assertEquals(bean1.getProperty1(), "This is a Property-1");
        assertEquals(bean1.getProperty2(), "This is a Property-2");
        assertEquals(bean1.getProperty3(), "This is a Property-3");
        assertEquals(bean1.getProperty4(), "property-4");
    }

    @After
    public void finish() {
        builder.destroy();
    }

}