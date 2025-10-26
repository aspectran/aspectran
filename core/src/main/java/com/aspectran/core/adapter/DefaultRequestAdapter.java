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

import com.aspectran.core.context.rule.type.MethodType;

/**
 * A generic, concrete implementation of {@link AbstractRequestAdapter}.
 *
 * <p>This adapter serves as a default, mutable {@link RequestAdapter}
 * suitable for internal use, testing, or for environments that do not have a
 * specific request-response model. It provides constructors to set the request
 * method and the underlying adaptee object.
 * </p>
 *
 * @author Juho Jeong
 * @since 2016. 2. 13.
 */
public class DefaultRequestAdapter extends AbstractRequestAdapter {

    /**
     * Creates a new {@code DefaultRequestAdapter} with no request method or adaptee.
     */
    public DefaultRequestAdapter() {
        this(null, null);
    }

    /**
     * Creates a new {@code DefaultRequestAdapter} with the specified request method.
     * @param requestMethod the request method
     */
    public DefaultRequestAdapter(MethodType requestMethod) {
        this(requestMethod, null);
    }

    /**
     * Creates a new {@code DefaultRequestAdapter} with the specified request method
     * and adaptee object.
     * @param requestMethod the request method
     * @param adaptee the native object to adapt
     */
    public DefaultRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

}
