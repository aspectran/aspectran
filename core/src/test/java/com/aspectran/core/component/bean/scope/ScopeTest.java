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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for bean scope implementations.
 *
 * <p>Created: 2026. 10. 05</p>
 */
class ScopeTest {

    private static final List<String> destroyLog = new ArrayList<>();

    @BeforeEach
    void setUp() {
        destroyLog.clear();
    }

    @Test
    void testRequestScope() {
        RequestScope scope = new RequestScope();
        assertEquals(ScopeType.REQUEST, scope.getScopeType());
        assertNull(scope.getScopeLock());

        BeanRule rule = new BeanRule();
        rule.setId("testBean");
        SampleBean bean = new SampleBean("testBean");
        BeanInstance instance = BeanInstance.forProduct(bean);

        scope.putBeanInstance(rule, instance);
        assertTrue(scope.containsBeanRule(rule));
        assertTrue(scope.hasInstance(bean));
        assertSame(instance, scope.getBeanInstance(rule));
        assertSame(rule, scope.getBeanRuleByInstance(bean));
    }

    @Test
    void testSingletonScopeBasic() {
        SingletonScope scope = new SingletonScope();
        assertEquals(ScopeType.SINGLETON, scope.getScopeType());
        assertNotNull(scope.getScopeLock());

        BeanRule rule = new BeanRule();
        rule.setId("singletonBean");
        SampleBean bean = new SampleBean("singletonBean");
        BeanInstance instance = BeanInstance.forProduct(bean);

        scope.putBeanInstance(rule, instance);
        assertTrue(scope.containsBeanRule(rule));
        assertTrue(scope.hasInstance(bean));
    }

    @Test
    void testSingleBeanDestroySuccess() throws Exception {
        SingletonScope scope = new SingletonScope();
        BeanRule rule = new BeanRule();
        rule.setId("bean1");
        SampleBean bean = new SampleBean("bean1");
        BeanInstance instance = BeanInstance.forProduct(bean);

        scope.putBeanInstance(rule, instance);
        assertTrue(scope.hasInstance(bean));

        scope.destroy(bean);
        assertFalse(scope.hasInstance(bean));
        assertNull(scope.getBeanInstance(rule));
        assertEquals(List.of("destroyed:bean1"), destroyLog);
    }

    @Test
    void testSingleBeanDestroyFailureStillRemovesFromScope() {
        SingletonScope scope = new SingletonScope();
        BeanRule rule = new BeanRule();
        rule.setId("failingBean");
        FailingBean bean = new FailingBean("failingBean");
        BeanInstance instance = BeanInstance.forProduct(bean);

        scope.putBeanInstance(rule, instance);
        assertTrue(scope.hasInstance(bean));

        assertThrows(RuntimeException.class, () -> scope.destroy(bean));
        // Even if destroy fails with exception, the bean must be removed from the scope
        assertFalse(scope.hasInstance(bean));
        assertNull(scope.getBeanInstance(rule));
    }

    @Test
    void testDestroyAllWithReverseOrderAndLazyDestroy() {
        SingletonScope scope = new SingletonScope();

        // Bean 1 (non-lazy)
        BeanRule rule1 = new BeanRule();
        rule1.setId("bean1");
        SampleBean bean1 = new SampleBean("bean1");
        scope.putBeanInstance(rule1, BeanInstance.forProduct(bean1));

        // Bean 2 (lazy-destroy)
        BeanRule rule2 = new BeanRule();
        rule2.setId("lazyBean2");
        rule2.setLazyDestroy(true);
        SampleBean bean2 = new SampleBean("lazyBean2");
        scope.putBeanInstance(rule2, BeanInstance.forProduct(bean2));

        // Bean 3 (non-lazy)
        BeanRule rule3 = new BeanRule();
        rule3.setId("bean3");
        SampleBean bean3 = new SampleBean("bean3");
        scope.putBeanInstance(rule3, BeanInstance.forProduct(bean3));

        // Bean 4 (lazy-destroy)
        BeanRule rule4 = new BeanRule();
        rule4.setId("lazyBean4");
        rule4.setLazyDestroy(true);
        SampleBean bean4 = new SampleBean("lazyBean4");
        scope.putBeanInstance(rule4, BeanInstance.forProduct(bean4));

        scope.destroy();

        // Expected:
        // Step 1: non-lazy destroyed in reverse creation order -> bean3, bean1
        // Step 2: lazy destroyed in reverse creation order -> lazyBean4, lazyBean2
        List<String> expected = List.of(
                "destroyed:bean3",
                "destroyed:bean1",
                "destroyed:lazyBean4",
                "destroyed:lazyBean2"
        );
        assertEquals(expected, destroyLog);

        assertFalse(scope.hasInstance(bean1));
        assertFalse(scope.hasInstance(bean2));
        assertFalse(scope.hasInstance(bean3));
        assertFalse(scope.hasInstance(bean4));
    }

    @Test
    void testDestroyAllContinuesOnError() {
        SingletonScope scope = new SingletonScope();

        BeanRule rule1 = new BeanRule();
        rule1.setId("bean1");
        SampleBean bean1 = new SampleBean("bean1");
        scope.putBeanInstance(rule1, BeanInstance.forProduct(bean1));

        BeanRule rule2 = new BeanRule();
        rule2.setId("failingBean");
        FailingBean bean2 = new FailingBean("failingBean");
        scope.putBeanInstance(rule2, BeanInstance.forProduct(bean2));

        BeanRule rule3 = new BeanRule();
        rule3.setId("bean3");
        SampleBean bean3 = new SampleBean("bean3");
        scope.putBeanInstance(rule3, BeanInstance.forProduct(bean3));

        // Should not throw, logs error and continues
        scope.destroy();

        List<String> expected = List.of(
                "destroyed:bean3",
                "destroyed:bean1"
        );
        assertEquals(expected, destroyLog);
        assertFalse(scope.hasInstance(bean1));
        assertFalse(scope.hasInstance(bean2));
        assertFalse(scope.hasInstance(bean3));
    }

    @Test
    void testSessionScopeMatchingRule() {
        SessionScope scope = new SessionScope();
        assertEquals(ScopeType.SESSION, scope.getScopeType());
        assertNotNull(scope.getScopeLock());

        // Rule with ID
        BeanRule ruleWithId = new BeanRule();
        ruleWithId.setId("sessionBean");
        SampleBean bean1 = new SampleBean("sessionBean");
        BeanInstance instance1 = BeanInstance.forProduct(bean1);
        scope.putBeanInstance(ruleWithId, instance1);

        // Another rule instance with the same ID (e.g. from different classloader / deserialized)
        BeanRule duplicateRuleWithId = new BeanRule();
        duplicateRuleWithId.setId("sessionBean");
        assertTrue(scope.containsBeanRule(duplicateRuleWithId));
        assertSame(instance1, scope.getBeanInstance(duplicateRuleWithId));

        // Rule without ID (matched by class name)
        BeanRule ruleWithoutId = new BeanRule();
        ruleWithoutId.setBeanClass(SampleBean.class);
        SampleBean bean2 = new SampleBean("noIdBean");
        BeanInstance instance2 = BeanInstance.forProduct(bean2);
        scope.putBeanInstance(ruleWithoutId, instance2);

        BeanRule duplicateRuleWithoutId = new BeanRule();
        duplicateRuleWithoutId.setBeanClass(SampleBean.class);
        assertTrue(scope.containsBeanRule(duplicateRuleWithoutId));
        assertSame(instance2, scope.getBeanInstance(duplicateRuleWithoutId));
    }

    @Test
    void testSessionScopeValueUnbound() {
        SessionScope scope = new SessionScope();
        BeanRule rule = new BeanRule();
        rule.setId("sessionBean");
        SampleBean bean = new SampleBean("sessionBean");
        scope.putBeanInstance(rule, BeanInstance.forProduct(bean));

        assertTrue(scope.hasInstance(bean));
        scope.valueUnbound(null, "test", scope);

        assertFalse(scope.hasInstance(bean));
        assertEquals(List.of("destroyed:sessionBean"), destroyLog);
    }

    private static class SampleBean implements DisposableBean {
        private final String name;

        SampleBean(String name) {
            this.name = name;
        }

        @Override
        public void destroy() {
            destroyLog.add("destroyed:" + name);
        }
    }

    private static class FailingBean implements DisposableBean {
        private final String name;

        FailingBean(String name) {
            this.name = name;
        }

        @Override
        public void destroy() {
            throw new RuntimeException("Forced destroy failure for " + name);
        }
    }

}
