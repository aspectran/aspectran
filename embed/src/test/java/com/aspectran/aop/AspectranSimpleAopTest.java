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
package com.aspectran.aop;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AspectranSimpleAopTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    public void ready() {
        String appConfigRootFile = "classpath:config/aop/simple-aop-test-config.xml";
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.updateAppConfigRootFile(appConfigRootFile);
        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    public void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    public void test1() {
        Translet translet = aspectran.translate("aop/test/action1");
        SampleAnnotatedAspect sampleAnnotatedAspect = translet.getAspectAdviceBean("aspect02");
        assertEquals(sampleAnnotatedAspect.foo(), "foo");
        assertEquals(translet.getSetting("setting1"), "value1");
        assertEquals(translet.getSetting("setting2"), "value2");
        assertEquals(translet.getSetting("setting3"), "value3");
    }

    @Test
    public void test2() {
        aspectran.translate("aop/test/action2");
    }

}
