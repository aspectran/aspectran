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
package com.aspectran.core.component.aspect;

import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test cases for {@link AdviceRuleRegistry} sorting behavior.
 *
 * <p>Created: 2026. 10. 18.</p>
 */
class AdviceRuleRegistryTest {

    @Test
    void testExceptionRuleAscendingOrder() {
        AdviceRuleRegistry registry = new AdviceRuleRegistry();

        AspectRule aspect1 = new AspectRule();
        aspect1.setId("aspect1");
        aspect1.setOrder(10);
        aspect1.putExceptionThrownRule(new ExceptionThrownRule());

        AspectRule aspect2 = new AspectRule();
        aspect2.setId("aspect2");
        aspect2.setOrder(0);
        aspect2.putExceptionThrownRule(new ExceptionThrownRule());

        AspectRule aspect3 = new AspectRule();
        aspect3.setId("aspect3");
        aspect3.setOrder(5);
        aspect3.putExceptionThrownRule(new ExceptionThrownRule());

        // Register in arbitrary order: 10, 0, 5
        registry.register(aspect1);
        registry.register(aspect2);
        registry.register(aspect3);

        List<ExceptionRule> list = registry.getExceptionRuleList();
        assertNotNull(list);
        assertEquals(3, list.size());

        // Should be ordered ascending: 0 (aspect2), 5 (aspect3), 10 (aspect1)
        assertEquals(0, list.get(0).getOrder());
        assertEquals("aspect2", list.get(0).getAspectRule().getId());

        assertEquals(5, list.get(1).getOrder());
        assertEquals("aspect3", list.get(1).getAspectRule().getId());

        assertEquals(10, list.get(2).getOrder());
        assertEquals("aspect1", list.get(2).getAspectRule().getId());
    }

    @Test
    void testMergeAndReplicateOrder() {
        AdviceRuleRegistry registry1 = new AdviceRuleRegistry();

        AspectRule aspect1 = new AspectRule();
        aspect1.setId("aspect1");
        aspect1.setOrder(10);
        aspect1.putExceptionThrownRule(new ExceptionThrownRule());
        registry1.register(aspect1);

        AdviceRuleRegistry registry2 = new AdviceRuleRegistry();

        AspectRule aspect2 = new AspectRule();
        aspect2.setId("aspect2");
        aspect2.setOrder(0);
        aspect2.putExceptionThrownRule(new ExceptionThrownRule());
        registry2.register(aspect2);

        // Merge registry2 into registry1
        registry1.merge(registry2);

        List<ExceptionRule> list = registry1.getExceptionRuleList();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(0, list.get(0).getOrder());
        assertEquals(10, list.get(1).getOrder());

        // Replicate registry1
        AdviceRuleRegistry replicated = registry1.replicate();
        List<ExceptionRule> repList = replicated.getExceptionRuleList();
        assertNotNull(repList);
        assertEquals(2, repList.size());
        assertEquals(0, repList.get(0).getOrder());
        assertEquals(10, repList.get(1).getOrder());
    }

    @Test
    void testExceptionRuleAspectValidation() {
        AspectRule aspect1 = new AspectRule();
        aspect1.setId("aspect1");

        // ExceptionRule without aspectRule -> should fail
        ExceptionRule standaloneRule = new ExceptionRule();
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                aspect1.setExceptionRule(standaloneRule)
        );

        // ExceptionRule from different aspect -> should fail
        AspectRule aspect2 = new AspectRule();
        aspect2.setId("aspect2");
        ExceptionRule aspect2Rule = new ExceptionRule(aspect2);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
                aspect1.setExceptionRule(aspect2Rule)
        );

        // Correct ExceptionRule -> should succeed
        ExceptionRule correctRule = new ExceptionRule(aspect1);
        aspect1.setExceptionRule(correctRule);
        assertEquals(aspect1, correctRule.getAspectRule());
    }

}
