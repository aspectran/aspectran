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

import com.aspectran.core.activity.request.AbstractRequest;
import com.aspectran.core.component.bean.scope.RequestScope;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.security.Principal;

/**
 * Base implementation of {@link RequestAdapter} that holds a reference to the
 * underlying request "adaptee" and provides lazy creation of a {@link RequestScope}.
 *
 * @since 2011. 3. 13.
*/
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

    /**
     * The underlying request object being adapted (framework-specific).
     */
    protected final Object adaptee;

    /**
     * Lazily created per-request scope container.
     */
    private RequestScope requestScope;

    /**
     * Create a new AbstractRequestAdapter.
     * @param requestMethod the request method; may be {@code null} if unknown
     * @param adaptee the native request object being adapted; may be {@code null}
     */
    public AbstractRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod);
        this.adaptee = adaptee;
    }

    /**
     * Return the underlying request adaptee.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    /**
     * Whether a {@link RequestScope} has already been associated with this adapter.
     */
    @Override
    public boolean hasRequestScope() {
        return (requestScope == null);
    }

    /**
     * Obtain the {@link RequestScope}, creating it if necessary.
     */
    @Override
    @NonNull
    public RequestScope getRequestScope() {
        if (requestScope == null) {
            requestScope = new RequestScope();
        }
        return requestScope;
    }

    /**
     * Return the current authenticated user {@link Principal}, if available.
     * Default implementation returns {@code null}.
     */
    @Override
    public Principal getPrincipal() {
        return null;
    }

}
