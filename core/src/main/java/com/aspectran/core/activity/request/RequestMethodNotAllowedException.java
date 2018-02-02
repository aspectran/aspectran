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
package com.aspectran.core.activity.request;

import com.aspectran.core.context.rule.type.MethodType;

/**
 * Exception thrown when a request handler does not allow a specific request method.
 */
public class RequestMethodNotAllowedException extends RequestException {

    /** @serial */
    private static final long serialVersionUID = 4068498460127610368L;

    private MethodType requestMethod;

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     */
    public RequestMethodNotAllowedException() {
        super();
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     *
     * @param msg a message to associate with the exception
     */
    public RequestMethodNotAllowedException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     *
     * @param cause the real cause of the exception
     */
    public RequestMethodNotAllowedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     *
     * @param msg the detail message
     * @param cause the real cause of the exception
     */
    public RequestMethodNotAllowedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     *
     * @param requestMethod the request method
     * @param msg the detail message
     */
    public RequestMethodNotAllowedException(MethodType requestMethod, String msg) {
        super(msg);
        this.requestMethod = requestMethod;
    }

    /**
     * Instantiates a new RequestMethodNotAllowedException.
     *
     * @param requestMethod the request method
     */
    public RequestMethodNotAllowedException(MethodType requestMethod) {
        this(requestMethod, "Request method '" + requestMethod + "' not allowed");
    }

    /**
     * Gets the request method type.
     *
     * @return the request method type
     */
    public MethodType getRequestMethod() {
        return requestMethod;
    }

}
