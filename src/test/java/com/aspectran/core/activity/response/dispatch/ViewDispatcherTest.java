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
package com.aspectran.core.activity.response.dispatch;

import static junit.framework.TestCase.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.builder.config.AspectranConfig;
import com.aspectran.core.context.builder.config.AspectranContextConfig;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.support.freemarker.FreeMarkerConfigurationFactoryBean;
import com.aspectran.core.support.freemarker.FreeMarkerTemplateEngine;
import com.aspectran.embedded.service.EmbeddedAspectranService;

/**
 * <p>Created: 2016. 9. 7.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ViewDispatcherTest {

    private EmbeddedAspectranService aspectranService;

    @Before
    public void ready() throws IOException, AspectranServiceException {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranContextConfig contextConfig = aspectranConfig.newAspectranContextConfig();

        AspectranParameters parameters = contextConfig.newParameters(AspectranContextConfig.parameters);
        parameters.setDefaultTemplateEngineBean("freemarker");

        BeanRule freeMarkerConfigurationBeanRule = new BeanRule();
        freeMarkerConfigurationBeanRule.setId("freeMarkerConfiguration");
        freeMarkerConfigurationBeanRule.setBeanClass(FreeMarkerConfigurationFactoryBean.class);
        parameters.addRule(freeMarkerConfigurationBeanRule);

        BeanRule freeMarkerBeanRule = new BeanRule();
        freeMarkerBeanRule.setId("freemarker");
        freeMarkerBeanRule.setBeanClass(FreeMarkerTemplateEngine.class);
        ItemRule constructorArgumentItemRule1 = freeMarkerBeanRule.newConstructorArgumentItemRule();
        constructorArgumentItemRule1.setValue(TokenParser.parse("#{freeMarkerConfiguration}"));
        parameters.addRule(freeMarkerBeanRule);

        TransletRule transletRule1 = new TransletRule();
        transletRule1.setName("test/freemarker");
        TransformRule transformRule1 = new TransformRule();
        transformRule1.setTransformType(TransformType.TEXT);
        TemplateRule templateRule1 = new TemplateRule();
        templateRule1.setTemplateSource("${param1} ${param2}");
        transformRule1.setTemplateRule(templateRule1);
        transletRule1.applyResponseRule(transformRule1);
        parameters.addRule(transletRule1);

        AspectranParameters aspectran1 = new AspectranParameters();
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
        parameters.addRule(aspectran1);

        aspectranService = EmbeddedAspectranService.create(aspectranConfig);
    }

    @After
    public void finish() {
        if (aspectranService != null) {
            aspectranService.shutdown();
        }
    }

    @Test
    public void testFreemarkerViewDispatcher() throws AspectranServiceException, IOException {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello");
        params.setParameter("param2", "world");

        Translet translet = aspectranService.translet("test/freemarker", params);
        String result = translet.getResponseAdapter().getWriter().toString();

        assertEquals("hello world", result);
        //System.out.println(result);
    }

    @Test
    public void testEcho() throws AspectranServiceException, IOException {
        ParameterMap params = new ParameterMap();
        params.setParameter("param1", "hello2");
        params.setParameter("param2", "world2");

        Translet translet = aspectranService.translet("test/appended/echo", params);
        String result = translet.getResponseAdapter().getWriter().toString();

        assertEquals("hello2 world2", result);
        //System.out.println(result);
    }

//    @Test
//    public void test2() throws AspectranServiceException, IOException {
//        Translet translet = aspectranService.translet("attr-test");
//        System.out.println(translet.getResponseAdapter().getWriter().toString());
//    }
//
//    @Test
//    public void includeTest() throws AspectranServiceException, IOException {
//        Translet translet = aspectranService.translet("include-test");
//        System.out.println(translet.getResponseAdapter().getWriter().toString());
//    }

}