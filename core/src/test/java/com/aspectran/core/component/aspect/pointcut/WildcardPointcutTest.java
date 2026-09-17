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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test Cases for Wildcard Pointcut.
 *
 * <p>Created: 2016. 2. 29.</p>
 */
class WildcardPointcutTest {

    @Test
    void wildcardPointcutTest1() {
        // "/translet@class:hello.Simplest^hello*World*"
        PointcutPatternRule ppr1 = PointcutPatternRule.newInstance("/translet", "class:hel*.Sim*", "hello*World*");
        // "/ga-annotated-config/*@class:hello.SimplestActivity"
        PointcutPatternRule ppr2 = PointcutPatternRule.newInstance("/ga-annotated-config/*", "class:*.SimplestActivity", null);

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr1);
        pprList.add(ppr2);

        Pointcut wildcardPointcut = new WildcardPointcut(pprList);

        assertTrue(wildcardPointcut.matches("/ga-annotated-config/translet", null, "hello.SimplestActivity", "hello World!"));
        assertTrue(wildcardPointcut.matches("/translet", null, "hello.Simplest", "hello World!"));
    }

    @Test
    void wildcardPointcutTest2() {
        // "/translet@classname^*"
        PointcutPatternRule ppr1 = PointcutPatternRule.newInstance("/translet", "class:name", "*");

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr1);

        Pointcut wildcardPointcut = new WildcardPointcut(pprList);

        assertTrue(wildcardPointcut.matches("/translet", null, "name", null));
    }

    @Test
    void wildcardPointcutTest3() {
        // "@classname"
        PointcutPatternRule ppr1 = PointcutPatternRule.newInstance(null, "class:name", null);

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr1);

        Pointcut wildcardPointcut = new WildcardPointcut(pprList);

        assertTrue(wildcardPointcut.matches("/translet", "id", "name", null));
    }

    @Test
    void wildcardPointcutTest4() {
        // "@classname"
        PointcutPatternRule ppr1 = PointcutPatternRule.newInstance(null, "id", null);

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr1);

        Pointcut wildcardPointcut = new WildcardPointcut(pprList);

        assertTrue(wildcardPointcut.matches("/translet", "id", "name", null));
    }

    @Test
    void pipelinePatternTest() throws IllegalRuleException {
        PointcutRule pointcutRule = PointcutRule.newInstance(new String[] {
                "+: /user/**|/order/**@userService|orderService^get*|find*"
        });
        Pointcut wildcardPointcut = PointcutFactory.createPointcut(pointcutRule);

        assertTrue(wildcardPointcut.matches("/user/profile", "userService", null, "getUser"));
        assertTrue(wildcardPointcut.matches("/order/view", "orderService", null, "findOrder"));
        assertTrue(wildcardPointcut.matches("/user/list", "orderService", null, "getOrders"));
        assertTrue(wildcardPointcut.matches("/order/detail", "userService", null, "findUser"));

        assertFalse(wildcardPointcut.matches("/item/list", "userService", null, "getUser"));
        assertFalse(wildcardPointcut.matches("/user/profile", "itemService", null, "getUser"));
        assertFalse(wildcardPointcut.matches("/user/profile", "userService", null, "deleteUser"));
    }

    @Test
    void methodOnlyPatternTest() throws IllegalRuleException {
        PointcutRule pointcutRule = PointcutRule.newInstance(new String[] {
                "+: @^get*"
        });
        Pointcut wildcardPointcut = PointcutFactory.createPointcut(pointcutRule);

        assertTrue(wildcardPointcut.matches("/any/translet", "anyBean", "any.Class", "getName"));
        assertTrue(wildcardPointcut.matches(null, null, null, "getUser"));
        assertFalse(wildcardPointcut.matches("/any/translet", "anyBean", "any.Class", "setName"));
    }

    @Test
    void transletAndMethodPatternTest() throws IllegalRuleException {
        PointcutRule pointcutRule = PointcutRule.newInstance(new String[] {
                "+: /api/**@^get*|find*"
        });
        Pointcut wildcardPointcut = PointcutFactory.createPointcut(pointcutRule);

        assertTrue(wildcardPointcut.matches("/api/v1/users", "userService", "com.example.UserService", "getUser"));
        assertTrue(wildcardPointcut.matches("/api/v1/orders", null, null, "findOrder"));
        assertFalse(wildcardPointcut.matches("/admin/users", "userService", "com.example.UserService", "getUser"));
        assertFalse(wildcardPointcut.matches("/api/v1/users", "userService", "com.example.UserService", "deleteUser"));
    }

    @Test
    void pipelineWithClassDirectiveTest() throws IllegalRuleException {
        PointcutRule pointcutRule = PointcutRule.newInstance(new String[] {
                "+: /api/**@class:com.example.*Service|**.OtherService^get*|find*"
        });
        Pointcut wildcardPointcut = PointcutFactory.createPointcut(pointcutRule);

        assertTrue(wildcardPointcut.matches("/api/v1/users", "anyBeanId", "com.example.UserService", "getUsers"));
        assertTrue(wildcardPointcut.matches("/api/v1/orders", "anyBeanId", "org.sample.OtherService", "findOrders"));
        assertFalse(wildcardPointcut.matches("/api/v1/users", "anyBeanId", "com.other.SampleService", "getUsers"));
        assertFalse(wildcardPointcut.matches("/api/v1/users", "anyBeanId", "com.example.UserService", "updateUsers"));
    }

}
