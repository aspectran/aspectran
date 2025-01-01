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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.PointcutPatternRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

}
