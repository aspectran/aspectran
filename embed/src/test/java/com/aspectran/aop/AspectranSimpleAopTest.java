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
package com.aspectran.aop;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ExceptionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * <p>Created: 2016. 11. 5.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AspectranSimpleAopTest {

    private EmbeddedAspectran aspectran;

    @BeforeAll
    void ready() {
        String contextFile = "classpath:config/aop/simple-aop-test-context.xml";
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectranConfig.newContextConfig().setContextRules(new String[] {contextFile});
        aspectran = EmbeddedAspectran.run(aspectranConfig);
    }

    @AfterAll
    void finish() {
        if (aspectran != null) {
            aspectran.release();
        }
    }

    @Test
    void test1() {
        Translet translet = aspectran.translate("aop/test/action1");
        SampleAnnotatedAspect sampleAnnotatedAspect = translet.getAdviceBean("aspect02");
        assertEquals("foo (avoid)", sampleAnnotatedAspect.foo());
        assertEquals("value1", translet.getSetting("setting1"));
        assertEquals("value2", translet.getSetting("setting2"));
        assertEquals("value3", translet.getSetting("setting3"));
    }

    @Test
    void test2() {
        InstantActivityTestBean bean = aspectran.getBean(InstantActivityTestBean.class);
        ActivityContext context = bean.getActivityContext();
        AspectRule aspectRule = context.getAspectRuleRegistry().getAspectRule("aspect01");
        aspectRule.setDisabled(false);
        Translet translet = aspectran.translate("aop/test/action1");
        SimpleAopTestAdvice simpleAopTestAdvice = translet.getAdviceBean("aspect01");
        assertNotNull(simpleAopTestAdvice);
    }

    @Test
    void test3() {
        InstantActivityTestBean bean = aspectran.getBean(InstantActivityTestBean.class);
        ActivityContext context = bean.getActivityContext();
        AspectRule aspectRule = context.getAspectRuleRegistry().getAspectRule("aspect01");
        aspectRule.setDisabled(true);
        Translet translet = aspectran.translate("aop/test/action1");
        SimpleAopTestAdvice simpleAopTestAdvice = translet.getAdviceBean("aspect01");
        assertNull(simpleAopTestAdvice);
    }

    @Test
    void test4() {
        Exception exception = null;
        try {
            aspectran.translate("aop/test/action2");
        } catch (Exception e) {
            exception = ExceptionUtils.getRootCauseException(e);
        }
        assertInstanceOf(SimpleAopTestException.class, exception);
    }

    @Test
    void test5() {
        Translet translet = aspectran.translate("aop/test/action3-hello");
        String result = translet.getWrittenResponse();
        assertEquals("hello", result);
    }

}
