/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.context.env;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>Created: 2017. 12. 3.</p>
 */
public class ContextEnvironmentTest {

    @Test
    public void testEvaluateAsString() throws ActivityContextBuilderException {
        ItemRule itemRule1 = new ItemRule();
        itemRule1.setName("item1");
        itemRule1.setValue("${param1}, ${param2:Tomato}, @{attr1}, @{attr2:Melon}");

        ItemRuleMap itemRuleMap = new ItemRuleMap();
        itemRuleMap.putItemRule(itemRule1);

        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setActiveProfiles("profile-1", "profile-2", "profile-3");
        builder.setPropertyItemRuleMap(itemRuleMap);
        ActivityContext context = builder.build();
        Environment environment = context.getEnvironment();

        assertTrue(environment.acceptsProfiles("profile-1", "profile-2"));
        assertTrue(environment.acceptsProfiles("profile-2", "!profile-3"));
        assertFalse(environment.acceptsProfiles("!profile-2", "!profile-3"));

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "Strawberry");

        Activity activity = new InstantActivity(context, parameterMap, attributes);
        assertEquals("Apple, Tomato, Strawberry, Melon", environment.getProperty("item1", activity).toString());
    }

}