/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context;

import static junit.framework.TestCase.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.adapter.BasicApplicationAdapter;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.loader.ActivityContextLoader;
import com.aspectran.core.context.loader.HybridActivityContextLoader;
import com.aspectran.core.context.loader.resource.InvalidResourceException;
import com.aspectran.core.context.template.TemplateProcessor;

import test.call.NumericBean;
import test.call.TotalBean;

/**
 * Test cases that call beans and templates.
 *
 * <p>Created: 2017. 3. 20.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CallTest {

    private File base = base = new File("./target/test-classes");

    private ActivityContext context;

    @Before
    public void ready() throws IOException, InvalidResourceException, ActivityContextBuilderException {
        BasicApplicationAdapter applicationAdapter = new BasicApplicationAdapter();
        applicationAdapter.setBasePath(base.getCanonicalPath());
        ActivityContextLoader activityContextLoader = new HybridActivityContextLoader(applicationAdapter);
        activityContextLoader.setHybridLoad(false);

        ActivityContext context = activityContextLoader.load("/config/call/call-test-config.xml");
        context.initialize();

        this.context = context;
    }

    @Test
    public void testBeanCall() throws InvalidResourceException, ActivityContextBuilderException {
        TotalBean totalBean = context.getBeanRegistry().getBean("totalBean");
        int count = 1;
        for (NumericBean o : totalBean.getNumerics()) {
            assertEquals(count++, o.getNumber());
            //System.out.println(o.getNumber() + " : " + o);
        }
    }

    @Test
    public void testTemplateCall() throws InvalidResourceException, ActivityContextBuilderException {
        TemplateProcessor templateProcessor = context.getTemplateProcessor();
        String result1 = templateProcessor.process("template-2");

        System.out.println("-------------------------------");
        System.out.println(" Test cases for template calls");
        System.out.println("-------------------------------");

        assertEquals("TEMPATE-1", result1);
        System.out.println(result1);

        String result2 = templateProcessor.process("template-4");

        assertEquals("TEMPATE-3", result2);
        System.out.println(result2);

        String result4 = templateProcessor.process("aponStyle");
        System.out.println(" === aponStyle ===");
        System.out.println(result4);

        String result5 = templateProcessor.process("compactStyle");
        System.out.println(" === compactStyle ===");
        System.out.println(result5);

        String result6 = templateProcessor.process("compressedStyle");
        System.out.println(" === compressedStyle ===");
        System.out.println(result6);

    }

    @After
    public void finish() {
        context.destroy();
    }

}