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

import com.aspectran.core.AboutMe;
import com.aspectran.core.component.template.TemplateRenderer;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.sample.call.OrderedBean;
import com.aspectran.core.sample.call.TotalBean;
import com.aspectran.utils.apon.AponFormat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for calling beans and templates.
 *
 * <p>Created: 2017. 3. 20.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanCallTest {

    private final File baseDir = new File("./target/test-classes");

    private HybridActivityContextBuilder activityContextBuilder;

    private BeanRegistry beanRegistry;

    private TemplateRenderer templateRenderer;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        activityContextBuilder = new HybridActivityContextBuilder();
        activityContextBuilder.setBasePath(baseDir.getCanonicalPath());
        activityContextBuilder.setDebugMode(true);

        ActivityContext context = activityContextBuilder.build("/config/bean/call/bean-call-test-config.xml");
        beanRegistry = context.getBeanRegistry();
        templateRenderer = context.getTemplateRenderer();
    }

    @AfterAll
    void finish() {
        if (activityContextBuilder != null) {
            activityContextBuilder.destroy();
        }
    }

    @Test
    void testBeanCall() {
        TotalBean totalBean = beanRegistry.getBean("totalBean");
        int count1 = 1;
        for (OrderedBean o : totalBean.getOrderedBeans1()) {
            assertEquals(count1++, o.getOrder());
        }
        int count2 = 1;
        for (OrderedBean o : totalBean.getOrderedBeans2()) {
            assertEquals(count2++, o.getOrder());
        }
    }

    @Test
    void testTemplateCall() {
        String result1 = templateRenderer.render("template-2");
        assertEquals("TEMPLATE-1", result1);

        String result2 = templateRenderer.render("template-4");
        assertEquals("TEMPLATE-3", result2);

        String result4 = templateRenderer.render("aponStyle");
        assertEquals("line-1\nline-2\nline-3".replace("\n", AponFormat.SYSTEM_NEW_LINE), result4);

        String result5 = templateRenderer.render("compactStyle");
        assertEquals("line-1\nline-2\nline-3".replace("\n", AponFormat.SYSTEM_NEW_LINE), result5);

        String result6 = templateRenderer.render("compressedStyle");
        assertEquals("line-1line-2line-3", result6);

        String result7 = templateRenderer.render("aspectranVersion");
        assertEquals(AboutMe.getVersion(), result7);
    }

}
