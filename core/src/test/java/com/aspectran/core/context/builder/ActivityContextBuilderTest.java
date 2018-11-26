/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
public class ActivityContextBuilderTest {

    private File baseDir;

    private ActivityContextBuilder builder;

    @BeforeAll
    public void ready() throws IOException {
        //baseDir = new File("./target/test-classes");
        baseDir = ResourceUtils.getResourceAsFile("");

        System.out.println(" --- Test case for building ActivityContext --- ");

        builder = new HybridActivityContextBuilder();
        builder.setBasePath(baseDir.getCanonicalPath());
        builder.setHybridLoad(true);
        builder.setActiveProfiles("dev", "local");
    }

    @AfterAll
    public void finish() {
        builder.destroy();
    }

    @Test
    public void test1HybridLoading() throws ActivityContextBuilderException {
        File apon1 = new File(baseDir, "config/sample/test-config.xml.apon");
        File apon2 = new File(baseDir, "config/sample/scheduler-config.xml.apon");

        apon1.delete();
        apon2.delete();

        System.out.println("================ load ===============");

        ActivityContext context = builder.build("/config/sample/test-config.xml");
        String result = context.getTemplateProcessor().process("echo1");
        //System.out.println(result);
        assertEquals("ECHO-1", result);
        builder.destroy();

        System.out.println("=============== reload ==============");

        ActivityContext context2 = builder.build();
        String result2 = context2.getTemplateProcessor().process("echo2");
        //System.out.println(result2);
        assertEquals("ECHO-2", result2);
        builder.destroy();
    }

}