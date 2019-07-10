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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityContextBuilderTest {

    private ActivityContextBuilder builder;

    @BeforeAll
    void ready() throws IOException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        System.out.println(baseDir.getCanonicalPath());
        System.out.println(" --- Test case for building ActivityContext --- ");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
    }

    @AfterAll
    void finish() {
        builder.destroy();
    }

    @Test
    void testHybridLoading() throws ActivityContextBuilderException {
        System.out.println("================ load ===============");

        builder.setActiveProfiles("dev", "debug");
        ActivityContext context1 = builder.build("/config/sample/test-config.xml");
        String result = context1.getTemplateRenderer().render("echo1");
        //System.out.println(result);
        assertEquals("ECHO-1", result);

        String devProp1 = context1.getEnvironment().getProperty("prop-1", context1.getDefaultActivity());
        String devProp2 = context1.getEnvironment().getProperty("prop-2", context1.getDefaultActivity());
        assertEquals("dev-debug-1", devProp1);
        assertEquals("dev-debug-2", devProp2);

        builder.destroy();

//        System.out.println("=============== reload ==============");
//
//        builder.setActiveProfiles("local");
//        ActivityContext context2 = builder.build("/config/sample/test-config.xml.apon");
//        String result2 = context2.getTemplateRenderer().render("echo2");
//        //System.out.println(result2);
//        assertEquals("ECHO-2", result2);
//
//        String localProp1 = context2.getEnvironment().getProperty("prop-1", context1.getDefaultActivity());
//        String localProp2 = context2.getEnvironment().getProperty("prop-2", context1.getDefaultActivity());
//        assertEquals("local-!debug-1", localProp1);
//        assertEquals("local-!debug-2", localProp2);
//
//        builder.destroy();

        //System.out.println(devProp1);
        //System.out.println(devProp2);
        //System.out.println(localProp1);
        //System.out.println(localProp2);
    }

}
