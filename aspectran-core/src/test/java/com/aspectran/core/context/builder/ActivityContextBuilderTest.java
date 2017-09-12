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
package com.aspectran.core.context.builder;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;

/**
 * Test case for building ActivityContext.
 *
 * <p>Created: 2016. 3. 26.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ActivityContextBuilderTest {

    private File baseDir;

    private ActivityContextBuilder activityContextBuilder;

    @Before
    public void ready() throws IOException {
        baseDir = new File("./target/test-classes");

        System.out.println("----------------------------------------");
        System.out.println(" Test case for building ActivityContext");
        System.out.println("----------------------------------------");

        activityContextBuilder = new HybridActivityContextBuilder();
        activityContextBuilder.setBasePath(baseDir.getCanonicalPath());
        activityContextBuilder.setHybridLoad(true);
        activityContextBuilder.setActiveProfiles("dev", "local");
    }

    @Test
    public void test1HybridLoading() throws ActivityContextBuilderException {
        File apon1 = new File(baseDir, "config/test-config.xml.apon");
        File apon2 = new File(baseDir, "config/scheduler-config.xml.apon");

        apon1.delete();
        apon2.delete();

        System.out.println("================ load ===============");

        activityContextBuilder.build("/config/test-config.xml");
        activityContextBuilder.destroy();

        System.out.println("=============== reload ==============");

        activityContextBuilder.build();
        activityContextBuilder.destroy();
    }

    @After
    public void finish() {
    }

}