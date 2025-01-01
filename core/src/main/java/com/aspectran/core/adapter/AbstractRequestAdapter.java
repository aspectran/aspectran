/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * The Class AbstractRequestAdapter.
  *
 * @since 2011. 3. 13.
*/
public abstract class AbstractRequestAdapter extends AbstractRequest implements RequestAdapter {

    protected final Object adaptee;

    private RequestScope requestScope;

    /**
     * Instantiates a new AbstractRequestAdapter.
     * @param requestMethod the request method
     * @param adaptee the adaptee object
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
        return (requestScope == null);
    }

    @Override
    @NonNull
    public RequestScope getRequestScope() {
        if (requestScope == null) {
            requestScope = new RequestScope();
        }
        return requestScope;
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

}
