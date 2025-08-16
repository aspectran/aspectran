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
 * Base implementation of {@link SessionAdapter} that stores a reference to the underlying
 * session "adaptee" and lazily manages a {@link SessionScope} within the session attributes.
 *
 * @since 2011. 3. 13.
 */
public abstract class AbstractSessionAdapter implements SessionAdapter {

    /**
     * The underlying session object being adapted (framework-specific).
     */
    private final Object adaptee;

    /**
     * Lazily initialized {@link SessionScope} cached on this adapter and stored in the session.
     */
    private volatile SessionScope sessionScope;

    /**
     * Create a new AbstractSessionAdapter.
     * @param adaptee the native session object being adapted; may be {@code null}
     */
    public AbstractSessionAdapter(Object adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * Return the underlying session adaptee.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    /**
     * Load or create a {@link SessionScope} and store it in the session attributes as needed.
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
