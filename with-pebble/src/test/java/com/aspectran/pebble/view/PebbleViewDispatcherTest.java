/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.pebble.view;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.pebble.PebbleEngineFactoryBean;
import com.aspectran.pebble.PebbleTemplateEngine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-03-18</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PebbleViewDispatcherTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranParameters parameters = aspectranConfig.newContextConfig().newAspectranParameters();
        parameters.setDefaultTemplateEngineBean("pebble");

        BeanRule pebbleEngineFactoryBeanRule = new BeanRule();
        pebbleEngineFactoryBeanRule.setId("pebbleEngineFactory");
        pebbleEngineFactoryBeanRule.setBeanClass(PebbleEngineFactoryBean.class);
        parameters.addRule(pebbleEngineFactoryBeanRule);

        BeanRule pebbleBeanRule = new BeanRule();
        pebbleBeanRule.setId("pebble");
        pebbleBeanRule.setBeanClass(PebbleTemplateEngine.class);
        ItemRule constructorArgumentItemRule3 = pebbleBeanRule.newConstructorArgumentItemRule();
        constructorArgumentItemRule3.setValue("#{pebbleEngineFactory}");
        parameters.addRule(pebbleBeanRule);

        // Append a child Aspectran
        AspectranParameters aspectran1 = parameters.newAspectranParameters();

        TransletRule transletRule4 = new TransletRule();
        transletRule4.setName("test/pebble");
        TransformRule transformRule4 = new TransformRule();
        transformRule4.setFormatType(FormatType.TEXT);
        TemplateRule templateRule3 = new TemplateRule();
        templateRule3.setEngineBeanId("pebble");
        templateRule3.setTemplateSource("{{ param1 }} {{ param2 }}");
        transformRule4.setTemplateRule(templateRule3);
        transletRule4.applyResponseRule(transformRule4);
        aspectran1.addRule(transletRule4);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void testPebbleTemplate() {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello");
        params.setParameter("param2", "pebble");

        Translet translet = aspectran.translate("test/pebble", params);
        String result = translet.getWrittenResponse();

        assertEquals("hello pebble", result);
    }

}
