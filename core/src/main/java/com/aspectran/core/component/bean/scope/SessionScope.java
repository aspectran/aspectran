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

import com.aspectran.core.activity.Activity;
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
 * Session scope implementation storing beans for the lifetime of a user session.
 * <p>
 * Maintains a read/write lock for concurrent access and destroys contained
 * beans when the scope is unbound from a session via {@link SessionBindingListener}.
 * Includes matching logic so that lookups work across equivalent bean rules
 * (e.g., across JVMs/nodes).
 * </p>
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
        // destroys all contained beans when unbound from a session
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
    public void putBeanInstance(Activity activity, BeanRule beanRule, BeanInstance beanInstance) {
        BeanRule matchingBeanRule = findMatchingBeanRule(beanRule);
        if (matchingBeanRule == null) {
            matchingBeanRule = beanRule;
        }
        super.putBeanInstance(activity, matchingBeanRule, beanInstance);
    }

    @Override
    public BeanRule getBeanRuleByInstance(Object bean) {
        throw new UnsupportedOperationException("Not available in session scope; Because it may be created in another JVM");
    }

    @Override
    public boolean containsBeanRule(BeanRule beanRule) {
        return (findMatchingBeanRule(beanRule) != null);
    }

    @Nullable
    private BeanRule findMatchingBeanRule(BeanRule beanRule) {
        for (Map.Entry<BeanRule, BeanInstance> entry : getScopedBeanInstances().entrySet()) {
            BeanRule target = entry.getKey();
            if (beanRule.getId() != null && beanRule.getId().equals(target.getId())) {
                return target;
            } else {
                String className = beanRule.getTargetBeanClassName();
                if (className != null && className.equals(target.getTargetBeanClassName())) {
                    return target;
                }
            }
        }
        return null;
    }

}
