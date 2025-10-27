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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.embed.sample.anno.ThirdResult;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ExceptionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.newContextConfig()
                .setContextRules(new String[] {"classpath:config/anno/annotated-configuration-test-context.xml"})
                .addBasePackage("com.aspectran.embed.sample.anno");

        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void firstTest() {
        ThirdResult thirdResult = aspectran.getBean("thirdResult");
        assertEquals("This is a second bean.", thirdResult.getMessage());
    }

    @Test
    void testInvokeMethod_1() {
        aspectran.translate("/action-1");
    }

    @Test
    void testInvokeMethod_2() {
        aspectran.translate("/action-2");
    }

    @Test
    void testInvokeMethod_3() {
        aspectran.translate("/action-3");
    }

    @Test
    void testInvokeMethod_4() {
        aspectran.translate("/action-4");
    }

    @Test
    void testInvokeMethod_5() {
        aspectran.translate("/action-5");
    }

    @Test
    void testInvokeMethod_6() {
        ParameterMap params = new ParameterMap();
        params.setParameterValues("param1", new String[] { "v1", "v2", "v3" });
        aspectran.translate("/action-6", params);
    }

    @Test
    void testInvokeMethod_7() {
        ParameterMap params = new ParameterMap();
        params.setParameterValues("param3", new String[] { "AA", "BB", "CC" });
        aspectran.translate("/action-7", params);
    }

    @Test
    void testInvokeMethod_8() {
        ParameterMap params = new ParameterMap();
        params.setParameterValues("param1", new String[] { "1", "2", "3" });
        aspectran.translate("/action-8", params);
    }

    @Test
    void testInvokeMethod_9() {
        ParameterMap params = new ParameterMap();
        params.setParameter("string", "Apple");
        params.setParameterValues("strings", new String[] { "Orange", "Grape", "Melon" });

        params.setParameter("character", "A");
        params.setParameterValues("characters", new String[] { "A", "B", "C" });
        params.setParameter("pcharacter", "A");
        params.setParameterValues("pcharacters", new String[] { "A", "B", "C" });

        params.setParameter("abyte", "1");
        params.setParameterValues("bytes", new String[] { "1", "2", "3" });
        params.setParameter("pbyte", "1");
        params.setParameterValues("pbytes", new String[] { "1", "2", "3" });

        params.setParameter("ashort", "1");
        params.setParameterValues("shorts", new String[] { "1", "2", "3" });
        params.setParameter("pshort", "1");
        params.setParameterValues("pshorts", new String[] { "1", "2", "3" });

        params.setParameter("integer", "1");
        params.setParameterValues("integers", new String[] { "1", "2", "3" });
        params.setParameter("pinteger", "1");
        params.setParameterValues("pintegers", new String[] { "1", "2", "3" });

        params.setParameter("along", "1");
        params.setParameterValues("longs", new String[] { "1", "2", "3" });
        params.setParameter("plong", "1");
        params.setParameterValues("plongs", new String[] { "1", "2", "3" });

        params.setParameter("afloat", "1");
        params.setParameterValues("floats", new String[] { "1", "2", "3" });
        params.setParameter("pfloat", "1");
        params.setParameterValues("pfloats", new String[] { "1", "2", "3" });

        params.setParameter("adouble", "1");
        params.setParameterValues("doubles", new String[] { "1", "2", "3" });
        params.setParameter("pdouble", "1");
        params.setParameterValues("pdoubles", new String[] { "1", "2", "3" });

        params.setParameter("bigInteger", "1");
        params.setParameterValues("bigIntegers", new String[] { "1", "2", "3" });
        params.setParameter("bigDecimal", "1");
        params.setParameterValues("bigDecimals", new String[] { "1", "2", "3" });

        params.setParameter("date", "2019-02-17");

        aspectran.translate("/action-9", params);
    }

    @Test
    void testInvokeMethod_10() {
        ParameterMap params = new ParameterMap();
        params.setParameter("one", "1");
        params.setParameter("two", "2");
        params.setParameter("three", "3");
        params.setParameterValues("four", new String[] {"1", "2", "3", "4"});
        aspectran.translate("/action-10", params);
    }

    @Test
    void testInvokeMethod_11() {
        ParameterMap params = new ParameterMap();
        params.setParameterValues("list", new String[] {"1", "2", "3"});
        aspectran.translate("/action-11", params);
    }

    @Test
    void testInvokeMethod_12() {
        String body = "p1(int): 1234\np2(int): 5678";
        aspectran.translate("/action-12", body);
    }

    @Test
    void testInvokeMethod_13() {
        ParameterMap params = new ParameterMap();
        params.setParameter("one", "1");
        params.setParameter("two", "2");
        Translet translet = aspectran.translate("/action-13", params);
        String result = translet.getWrittenResponse();
        assertEquals("12", result);
    }

    @Test
    void forward() {
        aspectran.translate("/forward");
    }

    @Test
    void requiredParam() {
        Exception e = assertThrows(CoreServiceException.class, () -> aspectran.translate("/requiredParam"));
        assertEquals("Missing required parameter 'param1'", ExceptionUtils.getRootCauseSimpleMessage(e));
    }

}
