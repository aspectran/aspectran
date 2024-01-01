/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.core.context.rule.type.MethodType;

/**
 * Checked exception thrown when an attempt is made to access a translet
 * that does not exist.
 * 
 * <p>Created: 2008. 01. 07 AM 3:35:55</p>
 */
public class TransletNotFoundException extends ActivityException {

    private static final long serialVersionUID = -5619283297296999361L;

    private final String transletName;

    private final MethodType requestMethod;

    /**
     * Constructor to create exception with a message.
     * @param transletName the translet name
     */
    public TransletNotFoundException(String transletName) {
        this(transletName, null);
    }

    /**
     * Constructor to create exception with a message.
     * @param transletName the translet name
     * @param requestMethod the request method
     */
    public TransletNotFoundException(String transletName, MethodType requestMethod) {
        super("No such translet map to " + makeTransletName(transletName, requestMethod));
        this.transletName = transletName;
        this.requestMethod = requestMethod;
    }

    public String getTransletName() {
        if (requestMethod == null || requestMethod == MethodType.GET) {
            return transletName;
        } else {
            return (requestMethod + " " + transletName);
        }
    }

    public MethodType getRequestMethod() {
        return requestMethod;
    }

    public MethodType getRequestMethod(MethodType defaultRequestMethod) {
        return (requestMethod != null ? requestMethod : defaultRequestMethod);
    }

    private static String makeTransletName(String transletName, MethodType requestMethod) {
        if (requestMethod != null) {
            return requestMethod + " " + transletName;
        } else {
            return transletName;
        }
    }

}
