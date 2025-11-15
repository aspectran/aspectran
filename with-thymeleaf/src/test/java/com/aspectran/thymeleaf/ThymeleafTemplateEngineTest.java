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
package com.aspectran.thymeleaf;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.ResourceAppendRule;
import com.aspectran.core.context.rule.params.AspectranParameters;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ThymeleafTemplateEngineTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        AspectranConfig aspectranConfig = new AspectranConfig();
        AspectranParameters parameters = aspectranConfig.touchContextConfig().touchAspectranParameters();
        parameters.setDefaultTemplateEngineBean("thymeleaf");
        parameters.addRule(new ResourceAppendRule("config/thymeleaf-test-config.xml"));

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
    void testEcho2() {
        Translet translet = aspectran.translate("echo-2");
        assertEquals("123,456,789.00", translet.getWrittenResponse());
    }

    @Test
    void echoEnvironmentProperties() {
        Translet translet = aspectran.translate("echoEnvironmentProperties");
        assertEquals("PROPERTY1", translet.getWrittenResponse());
    }

    @Test
    void testText() {
        ParameterMap params = new ParameterMap();
        params.setParameter("name", "tester");
        params.setParameter("email", "tester@aspectran.com");

        String result1 = aspectran.render("template-1", params.extractAsMap());
        String result2 = aspectran.translate("translet-1", params).getWrittenResponse();
        String result3 = aspectran.translate("translet-2", params).getWrittenResponse();

        //System.out.println(result1);
        //System.out.println(result2);
        //System.out.println(result3);

        assertEquals(result1, result2);
        assertEquals(result1, result3);
    }

    @Test
    void testHtml() {
        List<Map<String, Object>> allProducts = new ArrayList<>();
        allProducts.add(Map.of("name", "Apple", "price", 100.99));
        allProducts.add(Map.of("name", "Banana", "price", 200.99));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("allProducts", allProducts);

        String result = aspectran.translate("test-1.html", attributes).getWrittenResponse();
        System.out.println(result);
    }

}
