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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ClassUtils;

/**
 * <p>Created: 2024-04-25</p>
 */
public class JettyErrorPage {

    private final String location;

    private final Integer errorCode;

    private final Integer toErrorCode;

    private final Class<? extends Throwable> exceptionType;

    public JettyErrorPage(String location, String exceptionType) throws ClassNotFoundException {
        this(location, ClassUtils.loadClass(exceptionType));
    }

    public JettyErrorPage(String location, Class<? extends Throwable> exceptionType) {
        this.location = location;
        this.errorCode = null;
        this.toErrorCode = null;
        this.exceptionType = exceptionType;
    }

    public JettyErrorPage(String location, int errorCode) {
        this.location = location;
        this.errorCode = errorCode;
        this.toErrorCode = null;
        this.exceptionType = null;
    }

    public JettyErrorPage(String location, int fromErrorCode, int toErrorCode) {
        this.location = location;
        this.errorCode = fromErrorCode;
        this.toErrorCode = toErrorCode;
        this.exceptionType = null;
    }

    public String getLocation() {
        return location;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public Integer getToErrorCode() {
        return toErrorCode;
    }

    public Class<? extends Throwable> getExceptionType() {
        return exceptionType;
    }

}
