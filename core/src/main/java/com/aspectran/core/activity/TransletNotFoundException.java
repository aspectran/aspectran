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
package com.aspectran.core.activity;

import com.aspectran.core.context.rule.type.MethodType;

import java.io.Serial;

/**
 * Exception thrown when a request is made for a translet that does not exist.
 * This typically occurs when no matching {@link com.aspectran.core.context.rule.TransletRule}
 * can be found in the {@link com.aspectran.core.component.translet.TransletRuleRegistry}.
 *
 * @since 2008. 01. 07
 */
public class TransletNotFoundException extends ActivityException {

    @Serial
    private static final long serialVersionUID = -5619283297296999361L;

    private final String transletName;

    private final MethodType requestMethod;

    /**
     * Constructs a new TransletNotFoundException for a given request name, assuming a GET request method.
     * @param requestName the name of the requested translet
     */
    public TransletNotFoundException(String requestName) {
        this(requestName, null);
    }

    /**
     * Constructs a new TransletNotFoundException for a given request name and request method.
     * @param requestName the name of the requested translet
     * @param requestMethod the request method used
     */
    public TransletNotFoundException(String requestName, MethodType requestMethod) {
        super("No such translet mapped to " + makeRequestName(requestName, requestMethod));
        this.transletName = requestName;
        this.requestMethod = requestMethod;
    }

    /**
     * Returns the name of the translet that was not found, prefixed with the request method if it is not GET.
     * @return the fully assembled name of the translet
     */
    public String getTransletName() {
        if (requestMethod == null || requestMethod == MethodType.GET) {
            return transletName;
        } else {
            return (requestMethod + " " + transletName);
        }
    }

    /**
     * Returns the request method that was used when the translet was requested.
     * @return the request method
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

    /**
     * Returns the request method, or a default value if no method was specified.
     * @param defaultRequestMethod the default method to return if none is set
     * @return the actual or default request method
     */
    public MethodType getRequestMethod(MethodType defaultRequestMethod) {
        return (requestMethod != null ? requestMethod : defaultRequestMethod);
    }

    /**
     * Helper method to create the exception message string.
     * @param requestName the request name
     * @param requestMethod the request method
     * @return the formatted string for the exception message
     */
    private static String makeRequestName(String requestName, MethodType requestMethod) {
        if (requestMethod != null) {
            return requestMethod + " " + requestName;
        } else {
            return requestName;
        }
    }

}
