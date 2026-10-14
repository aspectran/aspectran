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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ClassUtils;

/**
 * Represents an error page mapping for a Jetty web application.
 *
 * <p>Created: 2024-04-25</p>
 */
public class JettyErrorPage {

    private final String location;

    private final Integer errorCode;

    private final Integer toErrorCode;

    private final Class<? extends Throwable> exceptionType;

    /**
     * Constructs a {@code JettyErrorPage} instance for handling errors caused by a specific exception type.
     * @param location the path to the error page that should be displayed
     * @param exceptionType the fully qualified name of the exception class to map to the error page
     * @throws ClassNotFoundException if the specified exception class cannot be found
     */
    public JettyErrorPage(String location, String exceptionType) throws ClassNotFoundException {
        this(location, ClassUtils.loadClass(exceptionType));
    }

    /**
     * Constructs a {@code JettyErrorPage} instance for handling errors caused by a specific exception type.
     * @param location the path to the error page that should be displayed
     * @param exceptionType the exception class to map to the error page
     */
    public JettyErrorPage(String location, Class<? extends Throwable> exceptionType) {
        this.location = location;
        this.errorCode = null;
        this.toErrorCode = null;
        this.exceptionType = exceptionType;
    }

    /**
     * Constructs a {@code JettyErrorPage} instance for handling errors based on a specific HTTP status code.
     * @param location the path to the error page that should be displayed
     * @param errorCode the HTTP status code to map to the error page
     */
    public JettyErrorPage(String location, int errorCode) {
        this.location = location;
        this.errorCode = errorCode;
        this.toErrorCode = null;
        this.exceptionType = null;
    }

    /**
     * Constructs a {@code JettyErrorPage} instance for handling errors based on a range of HTTP status codes.
     * @param location the path to the error page that should be displayed
     * @param fromErrorCode the starting HTTP status code to map to the error page
     * @param toErrorCode the ending HTTP status code to map to the error page
     */
    public JettyErrorPage(String location, int fromErrorCode, int toErrorCode) {
        this.location = location;
        this.errorCode = fromErrorCode;
        this.toErrorCode = toErrorCode;
        this.exceptionType = null;
    }

    /**
     * Returns the path to the error page that should be displayed.
     * @return the path to the error page
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the HTTP status code to map to the error page.
     * @return the HTTP status code
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the ending HTTP status code to map to the error page.
     * @return the ending HTTP status code
     */
    public Integer getToErrorCode() {
        return toErrorCode;
    }

    /**
     * Returns the type of exception to map to the error page.
     * @return the type of exception
     */
    public Class<? extends Throwable> getExceptionType() {
        return exceptionType;
    }

}
