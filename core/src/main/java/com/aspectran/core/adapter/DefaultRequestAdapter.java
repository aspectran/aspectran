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
 * The Class DefaultRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public class DefaultRequestAdapter extends AbstractRequestAdapter {

    /**
     * Instantiates a new DefaultRequestAdapter.
     */
    public DefaultRequestAdapter() {
        this(null, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter.
     * @param requestMethod the request method
     */
    public DefaultRequestAdapter(MethodType requestMethod) {
        this(requestMethod, null);
    }

    /**
     * Instantiates a new DefaultRequestAdapter.
     * @param requestMethod the request method
     * @param adaptee the adaptee object
     */
    public DefaultRequestAdapter(MethodType requestMethod, Object adaptee) {
        super(requestMethod, adaptee);
    }

}
