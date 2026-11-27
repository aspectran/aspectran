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
package com.aspectran.core.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ContextProfilesConfig;
import com.aspectran.core.context.env.Environment;
import com.aspectran.test.TestCoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying field-level profile fallback from parent context to child context.
 */
class ChildContextProfilesFallbackTest {

    private final List<TestCoreService> services = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (int i = services.size() - 1; i >= 0; i--) {
            try {
                services.get(i).stop();
            } catch (Exception ignored) {
            }
        }
        services.clear();
    }

    @Test
    void testFallbackWhenChildHasNoProfiles() throws Exception {
        AspectranConfig parentConfig = new AspectranConfig();
        ContextConfig parentContext = parentConfig.touchContextConfig();
        parentContext.setName("parent");
        ContextProfilesConfig parentProfiles = parentContext.touchProfilesConfig();
        parentProfiles.setBaseProfiles(new String[]{"base1"});
        parentProfiles.setDefaultProfiles(new String[]{"dev"});

        TestCoreService parentService = TestCoreService.build(parentConfig);
        services.add(parentService);

        AspectranConfig childConfig = new AspectranConfig();
        ContextConfig childContext = childConfig.touchContextConfig();
        childContext.setName("child");
        // No profiles specified in childContext

        TestCoreService childService = TestCoreService.build(childConfig, parentService);
        services.add(childService);

        Environment childEnv = childService.getActivityContext().getEnvironment();
        assertTrue(childEnv.acceptsProfiles("base1"));
        assertTrue(childEnv.acceptsProfiles("dev"));
        assertFalse(childEnv.acceptsProfiles("prod"));
    }

    @Test
    void testFallbackWhenChildHasOnlyBaseProfile() throws Exception {
        AspectranConfig parentConfig = new AspectranConfig();
        ContextConfig parentContext = parentConfig.touchContextConfig();
        parentContext.setName("parent");
        ContextProfilesConfig parentProfiles = parentContext.touchProfilesConfig();
        parentProfiles.setBaseProfiles(new String[]{"parent-base"});
        parentProfiles.setDefaultProfiles(new String[]{"dev"});

        TestCoreService parentService = TestCoreService.build(parentConfig);
        services.add(parentService);

        AspectranConfig childConfig = new AspectranConfig();
        ContextConfig childContext = childConfig.touchContextConfig();
        childContext.setName("child");
        ContextProfilesConfig childProfiles = childContext.touchProfilesConfig();
        childProfiles.setBaseProfiles(new String[]{"child-base"});
        // child specifies base profile only, default profile is omitted

        TestCoreService childService = TestCoreService.build(childConfig, parentService);
        services.add(childService);

        Environment childEnv = childService.getActivityContext().getEnvironment();
        assertTrue(childEnv.acceptsProfiles("child-base"));
        assertFalse(childEnv.acceptsProfiles("parent-base"));
        // dev is inherited from parent's defaultProfiles
        assertTrue(childEnv.acceptsProfiles("dev"));
    }

    @Test
    void testOverrideWhenChildHasOwnDefaultProfile() throws Exception {
        AspectranConfig parentConfig = new AspectranConfig();
        ContextConfig parentContext = parentConfig.touchContextConfig();
        parentContext.setName("parent");
        ContextProfilesConfig parentProfiles = parentContext.touchProfilesConfig();
        parentProfiles.setDefaultProfiles(new String[]{"dev"});

        TestCoreService parentService = TestCoreService.build(parentConfig);
        services.add(parentService);

        AspectranConfig childConfig = new AspectranConfig();
        ContextConfig childContext = childConfig.touchContextConfig();
        childContext.setName("child");
        ContextProfilesConfig childProfiles = childContext.touchProfilesConfig();
        childProfiles.setDefaultProfiles(new String[]{"prod"});

        TestCoreService childService = TestCoreService.build(childConfig, parentService);
        services.add(childService);

        Environment childEnv = childService.getActivityContext().getEnvironment();
        assertTrue(childEnv.acceptsProfiles("prod"));
        assertFalse(childEnv.acceptsProfiles("dev"));
    }

    @Test
    void testFallbackActiveProfiles() throws Exception {
        AspectranConfig parentConfig = new AspectranConfig();
        ContextConfig parentContext = parentConfig.touchContextConfig();
        parentContext.setName("parent");
        ContextProfilesConfig parentProfiles = parentContext.touchProfilesConfig();
        parentProfiles.setActiveProfiles(new String[]{"active1"});

        TestCoreService parentService = TestCoreService.build(parentConfig);
        services.add(parentService);

        AspectranConfig childConfig = new AspectranConfig();
        ContextConfig childContext = childConfig.touchContextConfig();
        childContext.setName("child");

        TestCoreService childService = TestCoreService.build(childConfig, parentService);
        services.add(childService);

        Environment childEnv = childService.getActivityContext().getEnvironment();
        assertTrue(childEnv.acceptsProfiles("active1"));
    }

}
