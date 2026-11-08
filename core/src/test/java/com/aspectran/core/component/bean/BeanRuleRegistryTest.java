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
package com.aspectran.core.component.bean;

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link BeanRuleRegistry}.
 *
 * <p>Created: 2026-11-08</p>
 */
class BeanRuleRegistryTest {

    private BeanRuleRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new BeanRuleRegistry(getClass().getClassLoader());
    }

    @Test
    void testGetBeanRules() throws IllegalRuleException {
        BeanRule rule1 = new BeanRule();
        rule1.setId("bean1");
        rule1.setBeanClass(String.class);

        BeanRule rule2 = new BeanRule();
        rule2.setId("bean2");
        rule2.setBeanClass(Integer.class);

        registry.addBeanRule(rule1);
        registry.addBeanRule(rule2);

        Collection<BeanRule> allRules = registry.getBeanRules();
        assertNotNull(allRules);
        assertEquals(2, allRules.size());
        assertTrue(allRules.contains(rule1));
        assertTrue(allRules.contains(rule2));
    }

    @Test
    void testFindBeanRulesByType() throws IllegalRuleException {
        BeanRule rule1 = new BeanRule();
        rule1.setId("stringBean");
        rule1.setBeanClass(String.class);

        BeanRule rule2 = new BeanRule();
        rule2.setId("intBean");
        rule2.setBeanClass(Integer.class);

        BeanRule rule3 = new BeanRule();
        rule3.setId("longBean");
        rule3.setBeanClass(Long.class);

        registry.addBeanRule(rule1);
        registry.addBeanRule(rule2);
        registry.addBeanRule(rule3);

        List<BeanRule> numberRules = registry.findBeanRules(Number.class);
        assertNotNull(numberRules);
        assertEquals(2, numberRules.size());
        assertTrue(numberRules.contains(rule2));
        assertTrue(numberRules.contains(rule3));
        assertFalse(numberRules.contains(rule1));
    }

    @Test
    void testFindBeanRulesByPredicate() throws IllegalRuleException {
        BeanRule rule1 = new BeanRule();
        rule1.setId("apple");
        rule1.setBeanClass(String.class);

        BeanRule rule2 = new BeanRule();
        rule2.setId("banana");
        rule2.setBeanClass(String.class);

        registry.addBeanRule(rule1);
        registry.addBeanRule(rule2);

        List<BeanRule> matched = registry.findBeanRules(rule -> "apple".equals(rule.getId()));
        assertNotNull(matched);
        assertEquals(1, matched.size());
        assertEquals("apple", matched.getFirst().getId());
    }

}
