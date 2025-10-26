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
 * Abstract base implementation of the {@link RequestAdapter} interface.
 *
 * <p>This class extends {@link AbstractRequest} and holds a reference to the
 * underlying native request object (the "adaptee"). It also provides lazy
 * creation and management of a {@link RequestScope} for request-scoped beans.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

    /**
     * The underlying, framework-specific request object.
     */
    protected final Object adaptee;

    /**
     * The lazily-created container for request-scoped beans.
     */
    private RequestScope requestScope;

    /**
     * Creates a new {@code AbstractRequestAdapter}.
     * @param requestMethod the request method (e.g., GET, POST), may be {@code null}
     * @param adaptee the native, framework-specific request object to adapt, may be {@code null}
     */
    public AbstractRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod);
        this.adaptee = adaptee;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    @Override
    public boolean hasRequestScope() {
        return (requestScope != null);
    }

    @Override
    @NonNull
    public RequestScope getRequestScope() {
        if (requestScope == null) {
            requestScope = new RequestScope();
        }
        return requestScope;
    }

    /**
     * {@inheritDoc}
     * <p>This default implementation always returns {@code null}.
     * Subclasses should override this method to provide the actual principal.
     */
    @Override
    public Principal getPrincipal() {
        return null;
    }

}
