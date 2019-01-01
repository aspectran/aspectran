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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.sample.anno.ThirdResult;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for Annotated Configuration.
 *
 * <p>Created: 2016. 9. 7.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnnotatedConfigurationTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        String appConfigRootFile = "classpath:config/anno/annotated-configuration-test-config.xml";
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.updateAppConfigRootFile(appConfigRootFile);
        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void firstTest() {
        ThirdResult thirdResult = aspectran.getActivityContext().getBeanRegistry().getBean("thirdResult");
        assertEquals(thirdResult.getMessage(), "This is a second bean.");
    }

}