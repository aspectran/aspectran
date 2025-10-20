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
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionBindingListener;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link Scope} implementation that stores beans for the lifetime of a user session.
 * <p>This scope is thread-safe and uses a {@link ReadWriteLock} to manage
 * concurrent access. It implements {@link SessionBindingListener} to ensure
 * that all contained beans are destroyed when the session is invalidated.
 * The bean lookup mechanism is designed to work correctly across different
 * JVMs in a clustered environment by matching bean rules based on their
 * ID or class name.</p>
 */
public class SessionScope extends AbstractScope implements SessionBindingListener {

    private static final ScopeType scopeType = ScopeType.SESSION;

    private final ReadWriteLock scopeLock = new ReentrantReadWriteLock();

    public static final String SESSION_SCOPE_ATTR_NAME = SessionScope.class.getName();

    public SessionScope() {
        super();
    }

    @Override
    public ScopeType getScopeType() {
        return scopeType;
    }

    @Override
    public ReadWriteLock getScopeLock() {
        return scopeLock;
    }

    @Override
    public void valueUnbound(Session session, String name, Object value) {
        // Destroys all contained beans when unbound from a session.
        destroy();
    }

    @Override
    public BeanInstance getBeanInstance(BeanRule beanRule) {
        BeanInstance beanInstance = super.getBeanInstance(beanRule);
        if (beanInstance == null) {
            BeanRule matchingBeanRule = findMatchingBeanRule(beanRule);
            beanInstance = super.getBeanInstance(matchingBeanRule);
        }
        return beanInstance;
    }

    @Override
    public void putBeanInstance(BeanRule beanRule, BeanInstance beanInstance) {
        BeanRule matchingBeanRule = findMatchingBeanRule(beanRule);
        if (matchingBeanRule == null) {
            matchingBeanRule = beanRule;
        }
        super.putBeanInstance(matchingBeanRule, beanInstance);
    }

    @Override
    public boolean containsBeanRule(BeanRule beanRule) {
        return (findMatchingBeanRule(beanRule) != null);
    }

    @Nullable
    private BeanRule findMatchingBeanRule(BeanRule beanRule) {
        for (Map.Entry<BeanRule, BeanInstance> entry : getScopedBeanInstances().entrySet()) {
            BeanRule target = entry.getKey();
            // If the bean rule to find has an ID, it must match the target's ID.
            if (beanRule.getId() != null) {
                if (beanRule.getId().equals(target.getId())) {
                    return target;
                }
            } else {
                // If the bean rule to find has no ID, match by class name,
                // but only against targets that also have no ID.
                if (target.getId() == null) {
                    String className = beanRule.getTargetBeanClassName();
                    if (className != null && className.equals(target.getTargetBeanClassName())) {
                        return target;
                    }
                }
            }
        }
        return null;
    }

}
