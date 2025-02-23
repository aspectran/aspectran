/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.core.activity.ActivityPerformException;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.core.context.rule.ItemRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created: 2017. 12. 3.</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityEnvironmentTest {

    private ActivityContextBuilder builder;

    private ActivityContext context;

    private Environment environment;

    @BeforeAll
    void ready() throws ActivityContextBuilderException {
        ItemRule itemRule1 = new ItemRule();
        itemRule1.setName("property1");
        itemRule1.setValue("${param1}, ${param2:Tomato}, @{attr1}, @{attr2:Melon}");

        builder = new HybridActivityContextBuilder();
        builder.setActiveProfiles("profile-1", "profile-2", "profile-3");
        builder.putPropertyItemRule(itemRule1);
        context = builder.build();
        environment = context.getEnvironment();
    }

    @AfterAll
    void finish() {
        if (builder != null) {
            builder.destroy();
        }
    }

    @Test
    void testEnvironmentProperties() throws ActivityPerformException {
        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "Strawberry");

        InstantActivity activity = new InstantActivity(context);
        activity.setParameterMap(parameterMap);
        activity.setAttributeMap(attributes);
        activity.perform(() -> {
            assertEquals("Apple, Tomato, Strawberry, Melon",
                    environment.getProperty("property1", activity).toString());
            return null;
        });
    }

    @Test
    void testAcceptsProfiles() throws ActivityPerformException {
        assertTrue(environment.acceptsProfiles("profile-1", "profile-2"));
        assertTrue(environment.acceptsProfiles("profile-2", "!profile-3"));
        assertFalse(environment.acceptsProfiles("!profile-2", "!profile-3"));

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "Strawberry");

        InstantActivity activity = new InstantActivity(context);
        activity.setParameterMap(parameterMap);
        activity.setAttributeMap(attributes);
        activity.perform(() -> {
            assertEquals("Apple, Tomato, Strawberry, Melon",
                    environment.getProperty("property1", activity).toString());
            return null;
        });
    }

    @Test
    void testMatchesProfiles() {
        assertTrue(environment.matchesProfiles("(profile-1, profile-2)"));
        assertFalse(environment.matchesProfiles("(profile-1, profile-x)"));
        assertTrue(environment.matchesProfiles("[profile-1, profile-z]"));
        assertTrue(environment.matchesProfiles("(profile-1, [profile-2, profile-z])"));
    }

}
