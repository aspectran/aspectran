/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.util.thread.Locker;

import java.util.Map;

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
     *
     * @param adaptee the adaptee object
     */
    public AbstractRequestAdapter(Object adaptee) {
        super();
        this.adaptee = adaptee;
    }

    /**
     * Instantiates a new AbstractRequestAdapter.
     *
     * @param adaptee the adaptee object
     * @param parameterMap the parameter map
     */
    public AbstractRequestAdapter(Object adaptee, Map<String, String[]> parameterMap) {
        super(parameterMap);
        this.adaptee = adaptee;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdaptee() {
        return (T)adaptee;
    }

    @Override
    public RequestScope getRequestScope() {
        return getRequestScope(true);
    }

    @Override
    public RequestScope getRequestScope(boolean create) {
        try (Locker.Lock ignored = locker.lockIfNotHeld()) {
            if (requestScope == null && create) {
                requestScope = new RequestScope();
            }
            return requestScope;
        }
    }

}
