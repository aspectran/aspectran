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
package com.aspectran.freemarker;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.rule.ResourceAppendRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FreeMarkerTemplateEngineTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        AspectranParameters parameters = contextConfig.touchAspectranParameters();
        parameters.setDefaultTemplateEngineBean("freemarker");
        parameters.addRule(new ResourceAppendRule("config/freemarker-test-config.xml"));

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        aspectran.destroy();
    }

    @Test
    void testEcho1() {
        Translet translet = aspectran.translate("echo-1");
        assertEquals("1234567890", translet.getWrittenResponse());
    }

    @Test
    void testSelectStatement() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("select-template", params.extractAsMap());
        Translet translet = aspectran.translate("select-translet", params);

        assertEquals(result1, translet.getWrittenResponse());
    }

    @Test
    void testUpdateStatement() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("update-template", params.extractAsMap());
        String result2 = aspectran.translate("update-translet-1", params).getWrittenResponse();
        String result3 = aspectran.translate("update-translet-2", params).getWrittenResponse();

        assertEquals(result1, result2);
        assertEquals(result1, result3);
    }

}
