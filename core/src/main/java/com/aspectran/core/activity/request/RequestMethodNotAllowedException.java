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
package com.aspectran.core.activity.request;

import com.aspectran.core.context.rule.type.MethodType;

import java.io.Serial;

/**
 * Exception thrown when a specific request method is not allowed.
 */
public class RequestMethodNotAllowedException extends RequestException {

    @Serial
    private static final long serialVersionUID = 4068498460127610368L;

    private final MethodType requestMethod;

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     * @param requestMethod the request method
     */
    public RequestMethodNotAllowedException(MethodType requestMethod) {
        this(requestMethod, "Request method '" + requestMethod + "' not allowed");
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     * @param requestMethod the request method
     * @param msg the detail message
     */
    public RequestMethodNotAllowedException(MethodType requestMethod, String msg) {
        super(msg);
        this.requestMethod = requestMethod;
    }

    /**
     * Gets the request method type.
     * @return the request method type
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

}
