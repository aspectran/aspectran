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
package com.aspectran.core.context.builder;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ResourceUtils;
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
class HybridActivityContextBuilderTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    @BeforeAll
    void ready() throws IOException, ActivityContextBuilderException {
        File baseDir = ResourceUtils.getResourceAsFile(".");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setDebugMode(true);
        builder.setActiveProfiles("dev", "debug");
        context = builder.build("/config/sample/builder-test-config.xml");
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testEcho() {
        String result = context.getTemplateRenderer().render("echo1");
        assertEquals("ECHO-1", result);

        String result2 = context.getTemplateRenderer().render("echo2");
        assertEquals("ECHO-2", result2);

        String devProp1 = context.getEnvironment().getProperty("prop-1", context.getDefaultActivity());
        String devProp2 = context.getEnvironment().getProperty("prop-2", context.getDefaultActivity());
        assertEquals("dev-debug-1", devProp1);
        assertEquals("dev-debug-2", devProp2);
    }

}
