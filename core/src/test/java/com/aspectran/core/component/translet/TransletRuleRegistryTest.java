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
package com.aspectran.core.component.translet;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases for TransletRuleRegistry.
 */
class TransletRuleRegistryTest {

    private TransletRuleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TransletRuleRegistry(null, getClass().getClassLoader());
    }

    @NonNull
    private TransletRule createRule(String name, MethodType... methods) {
        TransletRule rule = new TransletRule();
        rule.setName(name);
        if (methods != null && methods.length > 0) {
            rule.setAllowedMethods(methods);
        }
        return rule;
    }

    @Test
    void testSimpleGetPost() throws IllegalRuleException {
        TransletRule getRule = createRule("/test/get", MethodType.GET);
        TransletRule postRule = createRule("/test/post", MethodType.POST);
        registry.addTransletRule(getRule);
        registry.addTransletRule(postRule);

        assertEquals(getRule, registry.getTransletRule("/test/get", MethodType.GET));
        assertEquals(postRule, registry.getTransletRule("/test/post", MethodType.POST));
        assertNull(registry.getTransletRule("/test/get", MethodType.POST));
    }

    @Test
    void testWildcardMatching() throws IllegalRuleException {
        TransletRule wildcardRule = createRule("/test/*", MethodType.GET);
        registry.addTransletRule(wildcardRule);

        assertEquals(wildcardRule, registry.getTransletRule("/test/anything", MethodType.GET));
        assertEquals(wildcardRule, registry.getTransletRule("/test/1234", MethodType.GET));
        assertNull(registry.getTransletRule("/test", MethodType.GET));
    }

    @Test
    void testPathVariableMatching() throws IllegalRuleException {
        TransletRule pathRule = createRule("/test/${id}", MethodType.GET);
        registry.addTransletRule(pathRule);

        TransletRule found = registry.getTransletRule("/test/123", MethodType.GET);
        assertNotNull(found);
        assertEquals(pathRule, found);
        assertNotNull(found.getNameTokens());
    }

    @Test
    void testRulePriority() throws IllegalRuleException {
        TransletRule generalRule = createRule("/test/*", MethodType.GET);
        TransletRule specificRule = createRule("/test/specific", MethodType.GET);

        registry.addTransletRule(generalRule);
        registry.addTransletRule(specificRule);

        assertEquals(specificRule, registry.getTransletRule("/test/specific", MethodType.GET));
        assertEquals(generalRule, registry.getTransletRule("/test/other", MethodType.GET));
    }

    /**
     * This is the most important test to verify the intended fallback behavior.
     * A POST request should fall back to a method-less (implicit GET) rule,
     * but NOT to an explicit GET rule.
     */
    @Test
    void testFallbackLogic() throws IllegalRuleException {
        // Rule A: Method-less (implicit GET)
        TransletRule ruleA = createRule("/fallback/test");
        registry.addTransletRule(ruleA);

        // Rule B: Explicit GET
        TransletRule ruleB = createRule("/fallback/explicit", MethodType.GET);
        registry.addTransletRule(ruleB);

        // 1. POST should fall back to Rule A
        TransletRule foundForA = registry.getTransletRule("/fallback/test", MethodType.POST);
        assertNotNull(foundForA);
        assertEquals(ruleA, foundForA, "POST should fall back to a method-less rule");

        // 2. POST should NOT fall back to Rule B
        TransletRule foundForB = registry.getTransletRule("/fallback/explicit", MethodType.POST);
        assertNull(foundForB, "POST should NOT fall back to an explicit GET rule");

        // 3. GET should find both rules correctly
        assertEquals(ruleA, registry.getTransletRule("/fallback/test", MethodType.GET));
        assertEquals(ruleB, registry.getTransletRule("/fallback/explicit", MethodType.GET));
    }

}
