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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for dispatching Views.
 *
 * <p>Created: 2016. 9. 7.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewDispatcherTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranParameters parameters = aspectranConfig
                .newContextConfig()
                .newAspectranParameters();

        TransletRule transletRule1 = new TransletRule();
        transletRule1.setName("test/appended/echo");

        TransformRule transformRule1 = new TransformRule();
        transformRule1.setFormatType(FormatType.TEXT);

        TemplateRule templateRule1 = new TemplateRule();
        templateRule1.setEngineBeanId("token");
        templateRule1.setTemplateSource("${param1} ${param2}");

        transformRule1.setTemplateRule(templateRule1);
        transletRule1.putResponseRule(transformRule1);

        parameters.addRule(transletRule1);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testEcho() {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello2");
        params.setParameter("param2", "world2");

        Translet translet = aspectran.translate("test/appended/echo", params);
        String result = translet.getWrittenResponse();

        assertEquals("hello2 world2", result);
        //System.out.println(result);
    }

}
