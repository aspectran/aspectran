/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionBindingListener;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class SessionScope.
 *
 * @since 2011. 3. 12.
 */
public class SessionScope extends AbstractScope implements SessionBindingListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionScope.class);

    private static final String SESSION_SCOPED_BEAN_INSTANCES_ATTR_NAME =
            SessionScope.class.getName() + ".BEAN_INSTANCES";

    private static final ScopeType scopeType = ScopeType.SESSION;

    private final ReadWriteLock scopeLock = new ReentrantReadWriteLock();

    private final Map<String, BeanInstance> sessionScopedBeanInstances = new HashMap<>();

    /**
     * Instantiates a new Session scope.
     */
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
        destroy();
    }

    @Override
    public void putBeanInstance(Activity activity, BeanRule beanRule, BeanInstance beanInstance) {
        super.putBeanInstance(activity, beanRule, beanInstance);

        if (beanRule.getId() != null) {
            sessionScopedBeanInstances.put(beanRule.getId(), beanInstance);
        } else {
            String className = BeanRule.CLASS_DIRECTIVE_PREFIX + beanRule.getTargetBeanClassName();
            sessionScopedBeanInstances.put(className, beanInstance);
        }

        SessionAdapter sessionAdapter = activity.getSessionAdapter();
        if (sessionAdapter != null) {
            saveSessionScopedBeanInstances(sessionAdapter, sessionScopedBeanInstances);
        }
    }

    public static SessionScope restore(Activity activity, BeanRuleRegistry beanRuleRegistry) {
        SessionAdapter sessionAdapter = activity.getSessionAdapter();
        if (sessionAdapter == null) {
            return null;
        }

        SessionScope sessionScope = sessionAdapter.getSessionScope(true);
        ReadWriteLock scopeLock = sessionScope.getScopeLock();
        scopeLock.writeLock().lock();
        try {
            Map<String, BeanInstance> map = loadSessionScopedBeanInstances(sessionAdapter);
            if (map != null) {
                for (Map.Entry<String, BeanInstance> entry : map.entrySet()) {
                    String name = entry.getKey();
                    BeanInstance value = entry.getValue();
                    if (value != null) {
                        BeanRule[] beanRules;
                        try {
                            beanRules = beanRuleRegistry.getBeanRules(name);
                            if (beanRules == null) {
                                logger.warn("No bean named '" + name + "' available");
                            } else if (beanRules.length > 1) {
                                logger.warn("No qualifying bean of type '" + name +
                                        "' is defined: expected single matching bean but found " +
                                        beanRules.length +
                                        ": [" + NoUniqueBeanException.getBeanDescriptions(beanRules) + "]");
                            } else {
                                sessionScope.putBeanInstance(activity, beanRules[0], value);
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to restore the bean to session scope", e);
                        }
                    }
                }
            }
        } finally {
            scopeLock.writeLock().unlock();
        }
        return sessionScope;
    }

    private static Map<String, BeanInstance> loadSessionScopedBeanInstances(SessionAdapter sessionAdapter) {
        return sessionAdapter.getAttribute(SESSION_SCOPED_BEAN_INSTANCES_ATTR_NAME);
    }

    private static void saveSessionScopedBeanInstances(SessionAdapter sessionAdapter, Map<String, BeanInstance> map) {
        sessionAdapter.setAttribute(SESSION_SCOPED_BEAN_INSTANCES_ATTR_NAME, map);
    }

}
