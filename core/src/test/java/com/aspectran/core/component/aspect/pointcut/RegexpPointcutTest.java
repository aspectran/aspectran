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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Cases for Regular Expression Pointcut.
 *
 * <p>Created: 2026. 9. 17.</p>
 */
class RegexpPointcutTest {

    @Test
    void regexpPointcutBasicTest() {
        PointcutPatternRule ppr = PointcutPatternRule.newInstance(
                "^/api/v[1-9]/.*",
                "^(user|order)Service$",
                "^(create|update|delete).*"
        );

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr);

        Pointcut regexpPointcut = new RegexpPointcut(pprList);

        assertTrue(regexpPointcut.matches("/api/v1/users", "userService", null, "createUser"));
        assertTrue(regexpPointcut.matches("/api/v2/orders", "orderService", null, "deleteOrder"));
        assertTrue(regexpPointcut.matches("/api/v9/items", "orderService", null, "updateStatus"));

        assertFalse(regexpPointcut.matches("/api/v0/users", "userService", null, "createUser"));
        assertFalse(regexpPointcut.matches("/api/v1/users", "itemService", null, "createUser"));
        assertFalse(regexpPointcut.matches("/api/v1/users", "userService", null, "getUser"));
    }

    @Test
    void regexpPointcutWithClassDirectiveTest() {
        PointcutPatternRule ppr = PointcutPatternRule.newInstance(
                "^/service/.*",
                "class:^com\\.aspectran\\.core\\.component\\.aspect\\..*Test$",
                "^test.*"
        );

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr);

        Pointcut regexpPointcut = new RegexpPointcut(pprList);

        assertTrue(regexpPointcut.matches("/service/test", "anyBeanId",
                "com.aspectran.core.component.aspect.RegexpPointcutTest", "testMethod"));
        assertFalse(regexpPointcut.matches("/service/test", "anyBeanId",
                "com.aspectran.core.component.bean.OtherTest", "testMethod"));
        assertFalse(regexpPointcut.matches("/service/test", "anyBeanId",
                "com.aspectran.core.component.aspect.RegexpPointcutTest", "runMethod"));
    }

    @Test
    void regexpPointcutWithExcludeTest() {
        PointcutPatternRule includeRule = PointcutPatternRule.newInstance(
                "^/api/v1/.*",
                "^userService$",
                "^.*"
        );
        PointcutPatternRule excludeRule = PointcutPatternRule.newInstance(
                "^/api/v1/public/.*",
                null,
                null
        );

        List<PointcutPatternRule> excludeList = new ArrayList<>();
        excludeList.add(excludeRule);
        includeRule.setExcludePatternRuleList(excludeList);

        List<PointcutPatternRule> includeList = new ArrayList<>();
        includeList.add(includeRule);

        Pointcut regexpPointcut = new RegexpPointcut(includeList);

        assertTrue(regexpPointcut.matches("/api/v1/private/profile", "userService", null, "getProfile"));
        assertFalse(regexpPointcut.matches("/api/v1/public/login", "userService", null, "login"));
    }

    @Test
    void regexpPointcutFactoryTest() throws IllegalRuleException {
        PointcutRule pointcutRule = PointcutRule.newInstance("regexp");
        PointcutPatternRule ppr = PointcutPatternRule.newInstance(
                "^/translet/.*",
                "class:^com\\.aspectran\\..*",
                "^execute.*"
        );
        pointcutRule.addPointcutPatternRule(ppr);

        Pointcut pointcut = PointcutFactory.createPointcut(pointcutRule);
        assertInstanceOf(RegexpPointcut.class, pointcut);

        assertTrue(pointcut.matches("/translet/order", null, "com.aspectran.core.SampleClass", "executeOrder"));
        assertFalse(pointcut.matches("/other/order", null, "com.aspectran.core.SampleClass", "executeOrder"));
        assertFalse(pointcut.matches("/translet/order", null, "org.other.SampleClass", "executeOrder"));
        assertFalse(pointcut.matches("/translet/order", null, "com.aspectran.core.SampleClass", "cancelOrder"));
    }

}
