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
package com.aspectran.core.adapter;

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.utils.ToStringBuilder;

import static com.aspectran.core.component.bean.scope.SessionScope.SESSION_SCOPE_ATTR_NAME;

/**
 * Abstract base implementation of {@link SessionAdapter}.
 * <p>This class holds a reference to the underlying native session object (the "adaptee")
 * and provides lazy management of a {@link SessionScope} instance. The session scope is
 * stored as an attribute in the native session.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public abstract class AbstractSessionAdapter implements SessionAdapter {

    /**
     * The underlying, framework-specific session object.
     */
    private final Object adaptee;

    /**
     * The lazily-initialized {@link SessionScope}, cached on this adapter after first access.
     */
    private volatile SessionScope sessionScope;

    /**
     * Creates a new {@code AbstractSessionAdapter}.
     * @param adaptee the native, framework-specific session object to adapt, may be {@code null}
     */
    public AbstractSessionAdapter(Object adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation lazily loads the {@link SessionScope} from a session
     * attribute. If the scope does not exist and {@code create} is true, it uses
     * the {@link #createSessionScope()} factory method to instantiate a new one.
     * </p>
     */
    @Override
    public SessionScope getSessionScope(boolean create) {
        if (sessionScope == null) {
            SessionScope loaded = getAttribute(SESSION_SCOPE_ATTR_NAME);
            if (loaded != null) {
                sessionScope = loaded;
            } else if (create) {
                sessionScope = createSessionScope();
                setAttribute(SESSION_SCOPE_ATTR_NAME, sessionScope);
            }
        }
        return sessionScope;
    }

    /**
     * Factory method to create a new {@link SessionScope} instance.
     * <p>The default implementation returns a plain {@link SessionScope}.
     * Subclasses can override this to provide a custom scope implementation.
     * @return a new {@code SessionScope}
     */
    protected SessionScope createSessionScope() {
        return new SessionScope();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("adaptee", adaptee);
        return tsb.toString();
    }

}
