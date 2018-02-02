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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.PointcutPatternRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * Test Cases for Wildcard Pointcut.
 *
 * <p>Created: 2016. 2. 29.</p>
 */
public class WildcardPointcutTest {

    @Test
    public void wildcardPointcutTest() {
        // "/translet@class:hello.Simplest^hello*World*"
        PointcutPatternRule ppr1 = PointcutPatternRule.newInstance("/translet", "class:hel*.Sim*", "hello*World*");

        // "/ga-annotated-config/*@class:hello.SimplestAction"
        PointcutPatternRule ppr2 = PointcutPatternRule.newInstance("/ga-annotated-config/*", "class:*.SimplestAction", null);

        List<PointcutPatternRule> pprList = new ArrayList<>();
        pprList.add(ppr1);
        pprList.add(ppr2);

        Pointcut wildcardPointcut = new WildcardPointcut(pprList);

        assertTrue(wildcardPointcut.matches("/ga-annotated-config/translet", null, "hello.SimplestAction", "hello World!"));
        assertTrue(wildcardPointcut.matches("/translet", null, "hello.Simplest", "hello World!"));
    }

}
