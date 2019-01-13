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
package com.aspectran.core.activity.response.dispatch;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.support.freemarker.FreeMarkerConfigurationFactoryBean;
import com.aspectran.core.support.freemarker.FreeMarkerTemplateEngine;
import com.aspectran.core.support.pebble.PebbleEngineFactoryBean;
import com.aspectran.core.support.pebble.PebbleTemplateEngine;
import com.aspectran.core.support.view.FreeMarkerViewDispatcher;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ContextConfig contextConfig = aspectranConfig.newContextConfig();

        AspectranParameters parameters = contextConfig.newParameters(ContextConfig.parameters);
        parameters.setDefaultTemplateEngineBean("freemarker");

        BeanRule freeMarkerConfigurationBeanRule = new BeanRule();
        freeMarkerConfigurationBeanRule.setId("freeMarkerConfiguration");
        freeMarkerConfigurationBeanRule.setBeanClass(FreeMarkerConfigurationFactoryBean.class);
        freeMarkerConfigurationBeanRule.newPropertyItemRule("templateLoaderPath").setValue("classpath:view");
        parameters.addRule(freeMarkerConfigurationBeanRule);

        BeanRule freeMarkerBeanRule = new BeanRule();
        freeMarkerBeanRule.setId("freemarker");
        freeMarkerBeanRule.setBeanClass(FreeMarkerTemplateEngine.class);
        ItemRule constructorArgumentItemRule1 = freeMarkerBeanRule.newConstructorArgumentItemRule();
        constructorArgumentItemRule1.setValue("#{freeMarkerConfiguration}");
        parameters.addRule(freeMarkerBeanRule);

        BeanRule freeMarkerViewDispatcherBeanRule = new BeanRule();
        freeMarkerViewDispatcherBeanRule.setId("freeMarkerViewDispatcher");
        freeMarkerViewDispatcherBeanRule.setBeanClass(FreeMarkerViewDispatcher.class);
        ItemRule constructorArgumentItemRule2 = freeMarkerViewDispatcherBeanRule.newConstructorArgumentItemRule();
        constructorArgumentItemRule2.setValue("#{freeMarkerConfiguration}");
        ItemRule propertyItemRule1 = freeMarkerViewDispatcherBeanRule.newPropertyItemRule("prefix");
        propertyItemRule1.setValue("freemarker/");
        ItemRule propertyItemRule2 = freeMarkerViewDispatcherBeanRule.newPropertyItemRule("suffix");
        propertyItemRule2.setValue(".ftl");
        parameters.addRule(freeMarkerViewDispatcherBeanRule);

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

        /*
        BeanRule pebbleViewDispatcherBeanRule = new BeanRule();
        pebbleViewDispatcherBeanRule.setId("pebbleViewDispatcher");
        pebbleViewDispatcherBeanRule.setBeanClass(PebbleViewDispatcher.class);
        ItemRule constructorArgumentItemRule4 = pebbleViewDispatcherBeanRule.newConstructorArgumentItemRule();
        constructorArgumentItemRule4.setValue("#{pebbleEngineFactory}");
        parameters.addRule(pebbleViewDispatcherBeanRule);
        */

        AspectRule aspectRule1 = new AspectRule();
        aspectRule1.setId("transletSettings");
        SettingsAdviceRule settingsAdviceRule1 = aspectRule1.touchSettingsAdviceRule();
        settingsAdviceRule1.putSetting("viewDispatcher", "freeMarkerViewDispatcher");
        parameters.addRule(aspectRule1);

        // Append a child Aspectran
        AspectranParameters aspectran1 = new AspectranParameters();

        TransletRule transletRule1 = new TransletRule();
        transletRule1.setName("test/freemarker");
        TransformRule transformRule1 = new TransformRule();
        transformRule1.setTransformType(TransformType.TEXT);
        TemplateRule templateRule1 = new TemplateRule();
        templateRule1.setTemplateSource("${param1} ${param2}");
        transformRule1.setTemplateRule(templateRule1);
        transletRule1.applyResponseRule(transformRule1);
        aspectran1.addRule(transletRule1);

        TransletRule transletRule2 = new TransletRule();
        transletRule2.setName("test/appended/echo");
        TransformRule transformRule2 = new TransformRule();
        transformRule2.setTransformType(TransformType.TEXT);
        TemplateRule templateRule2 = new TemplateRule();
        templateRule2.setEngineBeanId("token");
        templateRule2.setTemplateSource("${param1} ${param2}");
        transformRule2.setTemplateRule(templateRule2);
        transletRule2.applyResponseRule(transformRule2);
        aspectran1.addRule(transletRule2);

        TransletRule transletRule3 = new TransletRule();
        transletRule3.setName("test/appended/freemarker/template1");
        DispatchRule dispatchRule1 = new DispatchRule();
        dispatchRule1.setName("freemarker-template1");
        transletRule3.applyResponseRule(dispatchRule1);
        aspectran1.addRule(transletRule3);

        TransletRule transletRule4 = new TransletRule();
        transletRule4.setName("test/pebble");
        TransformRule transformRule4 = new TransformRule();
        transformRule4.setTransformType(TransformType.TEXT);
        TemplateRule templateRule3 = new TemplateRule();
        templateRule3.setEngineBeanId("pebble");
        templateRule3.setTemplateSource("{{ param1 }} {{ param2 }}");
        transformRule4.setTemplateRule(templateRule3);
        transletRule4.applyResponseRule(transformRule4);
        aspectran1.addRule(transletRule4);

        parameters.addRule(aspectran1);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void testFreemarkerViewDispatcher() {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello");
        params.setParameter("param2", "world");

        Translet translet = aspectran.translate("test/freemarker", params);
        String result = translet.toString();

        assertEquals("hello world", result);
        //System.out.println(result);
    }

    @Test
    void testEcho() {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello2");
        params.setParameter("param2", "world2");

        Translet translet = aspectran.translate("test/appended/echo", params);
        String result = translet.toString();

        assertEquals("hello2 world2", result);
        //System.out.println(result);
    }

    @Test
    void testFreemarkerTemplate1() {
        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Orange");
        fruits.add("Strawberry");

        Map<String, Object> attrs = new HashMap<>();
        attrs.put("fruits", fruits);

        Translet translet = aspectran.translate("test/appended/freemarker/template1", attrs);
        String result = translet.toString();

        System.out.println(result);
    }

    @Test
    void testPebbleTemplate() {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello");
        params.setParameter("param2", "pebble");

        Translet translet = aspectran.translate("test/pebble", params);
        String result = translet.toString();

        assertEquals("hello pebble", result);
        //System.out.println(result);
    }

}