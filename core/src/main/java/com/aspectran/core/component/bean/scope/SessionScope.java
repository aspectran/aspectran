/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.component.bean.BeanInstance;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.bean.NoUniqueBeanException;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionBindingListener;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class SessionScope.
 *
 * @since 2011. 3. 12.
 */
public class SessionScope extends AbstractScope implements SessionBindingListener, Serializable {

    private static final Log log = LogFactory.getLog(SessionScope.class);

    public static final String SESSION_SCOPE_ATTRIBUTE_NAME = SessionScope.class.getName();

    public static final String SESSION_SCOPE_BEAN_ATTR_SUFFIX = "@" + SessionScope.class.getSimpleName();

    private static final long serialVersionUID = -6385922726768971308L;

    private static final ScopeType scopeType = ScopeType.SESSION;

    private transient final ReadWriteLock scopeLock = new ReentrantReadWriteLock();

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
    public void putBeanInstance(BeanRule beanRule, BeanInstance beanInstance) {
        super.putBeanInstance(beanRule, beanInstance);
    }

    public static SessionScope restore(SessionAdapter sessionAdapter, BeanRuleRegistry beanRuleRegistry) {
        SessionScope sessionScope = sessionAdapter.getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
        if (sessionScope == null) {
            sessionScope = sessionAdapter.newSessionScope();
            ReadWriteLock scopeLock = sessionScope.getScopeLock();
            scopeLock.writeLock().lock();
            try {
                sessionAdapter.setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, sessionScope);
                for (Enumeration<String> names = sessionAdapter.getAttributeNames(); names.hasMoreElements();) {
                    String name = names.nextElement();
                    if (name.endsWith(SessionScope.SESSION_SCOPE_BEAN_ATTR_SUFFIX)) {
                        String beanName = name.substring(0, name.length() - SessionScope.SESSION_SCOPE_BEAN_ATTR_SUFFIX.length());
                        BeanRule[] beanRules;
                        try {
                            beanRules = beanRuleRegistry.getBeanRules(beanName);
                        } catch (ClassNotFoundException e) {
                            log.warn("Failed to restore the bean to session scope", e);
                            continue;
                        }
                        if (beanRules == null) {
                            log.warn("No bean named '" + beanName + "' available");
                        } else if (beanRules.length > 1) {
                            log.warn("No qualifying bean of type '" + beanName +
                                    "' is defined: expected single matching bean but found " +
                                    beanRules.length +
                                    ": [" + NoUniqueBeanException.getBeanDescriptions(beanRules) + "]");
                        } else {
                            sessionScope.putBeanInstance(beanRules[0], new BeanInstance(sessionAdapter.getAttribute(name)));
                        }
                    }
                }
            } finally {
                scopeLock.writeLock().unlock();
            }
        }
        return sessionScope;
    }

}
