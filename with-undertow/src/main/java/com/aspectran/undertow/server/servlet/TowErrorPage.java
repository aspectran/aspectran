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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ClassUtils;
import io.undertow.servlet.api.ErrorPage;

/**
 * Represents an error page that can be added to a deployment.
 *
 * <p>Created: 2024-04-25</p>
 */
public class TowErrorPage extends ErrorPage {

    /**
     * Creates a new error page with the specified location and exception type name.
     * @param location the location of the error page
     * @param exceptionType the exception type name
     * @throws ClassNotFoundException if the class is not found
     */
    public TowErrorPage(String location, String exceptionType) throws ClassNotFoundException {
        this(location, ClassUtils.loadClass(exceptionType));
    }

    /**
     * Creates a new error page with the specified location and exception type.
     * @param location the location of the error page
     * @param exceptionType the exception type
     */
    public TowErrorPage(String location, Class<? extends Throwable> exceptionType) {
        super(location, exceptionType);
    }

    /**
     * Creates a new error page with the specified location and error code.
     * @param location the location of the error page
     * @param errorCode the error code
     */
    public TowErrorPage(String location, int errorCode) {
        super(location, errorCode);
    }

    /**
     * Creates a new default error page with the specified location.
     * @param location the location of the error page
     */
    public TowErrorPage(String location) {
        super(location);
    }

}
