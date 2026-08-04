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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.TypeAliasRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.AppendParameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test case for scoped overriding in {@code <append>} elements.
 */
class ScopedOverrideTest {

    public static class TestBean {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Test
    void testSuccessfulScopedOverride() throws Exception {
        HybridActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setDebugMode(true);
        ActivityContext context = builder.build("classpath:com/aspectran/core/context/rule/appender/scoped-override-valid.xml");
        assertNotNull(context);

        TestBean bean = context.getBeanRegistry().getBean("testBean");
        assertNotNull(bean);
        assertEquals("Overridden Message", bean.getMessage());

        String appTitle = context.getEnvironment().getProperty("app.title");
        assertEquals("Overridden Title", appTitle);

        TransletRule transletRule = context.getTransletRuleRegistry().getTransletRule("/hello");
        assertNotNull(transletRule);

        builder.destroy();
    }

    @Test
    void testStrictOverrideFailureOnNonExistentBean() {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        assertThrows(Exception.class, () ->
            builder.build("classpath:com/aspectran/core/context/rule/appender/scoped-override-invalid-bean.xml")
        );
    }

    @Test
    void testStrictOverrideFailureOnNonExistentProperty() {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        assertThrows(Exception.class, () ->
            builder.build("classpath:com/aspectran/core/context/rule/appender/scoped-override-invalid-prop.xml")
        );
    }

    @Test
    void testStrictOverrideFailureOnNonExistentTranslet() {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        assertThrows(Exception.class, () ->
            builder.build("classpath:com/aspectran/core/context/rule/appender/scoped-override-invalid-translet.xml")
        );
    }

    @Test
    void testStrictOverrideFailureOnNonExistentTypeAlias() {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        assertThrows(Exception.class, () ->
            builder.build("classpath:com/aspectran/core/context/rule/appender/scoped-override-invalid-typealias.xml")
        );
    }

    @Test
    void testAponScopedOverrideConversion() throws Exception {
        AppendRule appendRule = AppendRule.newInstance("com/aspectran/core/context/rule/appender/scoped-override-target.xml", null, null, null, null);
        appendRule.addChildRule(new TypeAliasRule("myAlias", "java.lang.StringBuilder"));

        EnvironmentRule envRule = EnvironmentRule.newInstance(null);
        ItemRuleMap irm = new ItemRuleMap();
        ItemRule itemRule = new ItemRule();
        itemRule.setName("app.title");
        itemRule.setValue("Overridden Title");
        irm.putItemRule(itemRule);
        envRule.setPropertyItemRuleMap(irm);
        appendRule.addChildRule(envRule);

        AppendParameters appendParameters = RulesToParameters.toAppendParameters(appendRule);
        assertNotNull(appendParameters);
        assertNotNull(appendParameters.getParameters(com.aspectran.core.context.rule.params.AppendParameters.aspectran));
    }

}
