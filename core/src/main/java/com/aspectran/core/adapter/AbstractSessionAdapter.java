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
package com.aspectran.core.adapter;

import com.aspectran.core.component.bean.scope.SessionScope;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Abstract Class for session object adapter.
 *
 * @since 2011. 3. 13.
 */
public abstract class AbstractSessionAdapter implements SessionAdapter {

    private final Object adaptee;

    private volatile SessionScope sessionScope;

    /**
     * Instantiates a new AbstractSessionAdapter.
     *
     * @param adaptee the adaptee object
     */
    public AbstractSessionAdapter(Object adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    @Override
    public SessionScope getSessionScope() {
        if (this.sessionScope == null) {
            this.sessionScope = getAttribute(SESSION_SCOPE_ATTRIBUTE_NAME);
            if (this.sessionScope == null) {
                this.sessionScope = new SessionScope();
                setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, this.sessionScope);
            }
        }
        return this.sessionScope;
    }

    /**
     * Creates a new session scope.
     */
    private void newSessionScope() {
        this.sessionScope = new SessionScope();
        setAttribute(SESSION_SCOPE_ATTRIBUTE_NAME, this.sessionScope);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("adaptee", adaptee);
        return tsb.toString();
    }

}
