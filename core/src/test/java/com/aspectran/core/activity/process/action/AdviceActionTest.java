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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.AdviceType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for {@link AdviceAction}, {@link AnnotatedAdviceAction}, and {@link AdviceRule}.
 *
 * <p>Created: 2026-10-20</p>
 */
class AdviceActionTest {

    @Test
    void testAnnotatedAdviceActionToString() throws NoSuchMethodException {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("AccessControlAspect");
        aspectRule.setOrder(2);

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);

        Method method = SampleAdvice.class.getMethod("checkPermission", Translet.class);
        AnnotatedActionRule actionRule = new AnnotatedActionRule();
        actionRule.setMethod(method);

        AnnotatedAdviceAction adviceAction = new AnnotatedAdviceAction(adviceRule, actionRule);

        assertEquals("{type=before, aspect=AccessControlAspect, advice={method=" +
                SampleAdvice.class.getName() + ".checkPermission(Translet)}, order=2}", adviceAction.toString());
    }

    @Test
    void testAdviceActionWithBeanToString() throws NoSuchMethodException {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("xssFilter");
        aspectRule.setOrder(0);

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);

        Method method = SampleAdvice.class.getMethod("checkPermission", Translet.class);
        InvokeActionRule invokeActionRule = new InvokeActionRule();
        invokeActionRule.setBeanId("xssPreventionFilter");
        invokeActionRule.setMethod(method);

        AdviceAction adviceAction = new AdviceAction(adviceRule, invokeActionRule);

        assertEquals("{type=before, aspect=xssFilter, invoke={bean=xssPreventionFilter, method=" +
                SampleAdvice.class.getName() + ".checkPermission(Translet)}, order=0}", adviceAction.toString());
    }

    @Test
    void testAdviceActionWithAspectBeanToString() throws NoSuchMethodException {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("userAuthAspect");
        aspectRule.setAdviceBeanId("userAuthAdvice");

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);

        Method method = SampleAdvice.class.getMethod("checkPermission", Translet.class);
        InvokeActionRule invokeActionRule = new InvokeActionRule();
        invokeActionRule.setMethod(method);

        AdviceAction adviceAction = new AdviceAction(adviceRule, invokeActionRule);

        assertEquals("{type=before, aspect=userAuthAspect, method=" +
                SampleAdvice.class.getName() + ".checkPermission(Translet)}", adviceAction.toString());
    }

    @Test
    void testAdviceRuleToStringWithEchoAction() {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("webTransletSettings");

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);

        EchoActionRule echoActionRule = new EchoActionRule();
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        ItemRule itemRule = new ItemRule();
        itemRule.setName("staticCodes");
        itemRuleMap.putItemRule(itemRule);
        echoActionRule.setEchoItemRuleMap(itemRuleMap);

        Executable echoAction = adviceRule.putActionRule(echoActionRule);

        assertEquals("{type=before, aspect=webTransletSettings, echo={attributes=[staticCodes]}}",
                AdviceRule.toString(echoAction, adviceRule));
    }

    @Test
    void testAdviceRuleToStringWithHeaderAction() {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("webSecuritySettings");

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);

        HeaderActionRule headerActionRule = new HeaderActionRule();
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        ItemRule itemRule = new ItemRule();
        itemRule.setName("X-Frame-Options");
        itemRuleMap.putItemRule(itemRule);
        headerActionRule.setHeaderItemRuleMap(itemRuleMap);

        Executable headerAction = adviceRule.putActionRule(headerActionRule);

        assertEquals("{type=before, aspect=webSecuritySettings, header={headers=[X-Frame-Options]}}",
                AdviceRule.toString(headerAction, adviceRule));
    }

    @Test
    void testAdviceRuleToStringWithDescription() {
        AspectRule aspectRule = new AspectRule();
        aspectRule.setId("simpleTxAspect");
        aspectRule.setOrder(0);

        AdviceRule adviceRule = new AdviceRule(aspectRule, AdviceType.BEFORE);
        adviceRule.setDescription("new SqlSessionAdvice()");

        assertEquals("{type=before, aspect=simpleTxAspect, action=new SqlSessionAdvice(), order=0}",
                AdviceRule.toString(null, adviceRule));
    }

    public static class SampleAdvice {
        public void checkPermission(Translet translet) {
        }
    }

}
