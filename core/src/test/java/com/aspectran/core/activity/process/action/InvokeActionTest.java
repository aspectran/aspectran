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

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test case for {@link InvokeAction}.
 */
@AspectranTest(
    rules = "/config/activity/invoke-action-test-config.xml"
)
class InvokeActionTest {

    @Test
    void testInvokePreResolvedMethodWithoutArgs(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.perform(() -> {
            Method method = TestService.class.getMethod("sayHello");

            InvokeActionRule rule = new InvokeActionRule();
            rule.setBeanClass(TestService.class);
            rule.setMethod(method);

            InvokeAction action = new InvokeAction(rule);
            Object result = action.execute(activity);
            assertEquals("hello", result);
            return null;
        });
    }

    @Test
    void testInvokePreResolvedMethodWithTranslet(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("msg", "world");
        activity.setParameterMap(parameterMap);

        activity.perform(() -> {
            Method method = TestService.class.getMethod("echoTranslet", Translet.class);

            InvokeActionRule rule = new InvokeActionRule();
            rule.setBeanClass(TestService.class);
            rule.setMethod(method);
            rule.setRequiresTranslet(true);

            InvokeAction action = new InvokeAction(rule);
            Object result = action.execute(activity);
            assertEquals("echo:world", result);
            return null;
        });
    }

    @Test
    void testInvokeDynamicMethodByNameWithTranslet(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("msg", "dynamic");
        activity.setParameterMap(parameterMap);

        activity.perform(() -> {
            InvokeActionRule rule = new InvokeActionRule();
            rule.setBeanClass(TestService.class);
            rule.setMethodName("echoTranslet");

            InvokeAction action = new InvokeAction(rule);
            Object result = action.execute(activity);
            assertEquals("echo:dynamic", result);
            return null;
        });
    }

    @Test
    void testInvokeDynamicMethodByNameWithArguments(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);

        activity.perform(() -> {
            ItemRule itemRule = new ItemRule();
            itemRule.setName("greeting");
            itemRule.setType(ItemType.SINGLE);
            itemRule.setValue("hi");

            ItemRuleMap argumentItemRuleMap = new ItemRuleMap();
            argumentItemRuleMap.putItemRule(itemRule);

            InvokeActionRule rule = new InvokeActionRule();
            rule.setBeanClass(TestService.class);
            rule.setMethodName("greet");
            rule.setArgumentItemRuleMap(argumentItemRuleMap);

            InvokeAction action = new InvokeAction(rule);
            Object result = action.execute(activity);
            assertEquals("greeting:hi", result);
            return null;
        });
    }

    public static class TestService {

        public String sayHello() {
            return "hello";
        }

        public String echoTranslet(Translet translet) {
            return "echo:" + translet.getParameter("msg");
        }

        public String greet(String greeting) {
            return "greeting:" + greeting;
        }

    }

}
