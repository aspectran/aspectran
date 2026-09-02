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
package com.aspectran.web.service;

import com.aspectran.core.service.CoreService;

import java.util.Set;

/**
 * The main interface for the Aspectran Web service.
 * <p>This service specializes the core Aspectran service for web environments.</p>
 *
 * @since 2.0.0
 */
public interface WebService extends CoreService {

    /**
     * Returns whether session adaptation is enabled for this web service.
     * @return {@code true} if session adaptation is enabled, {@code false} otherwise
     */
    boolean isSessionAdaptable();

    /**
     * Returns the value of the named attribute in the web application scope.
     * @param <T> the type of the attribute value
     * @param name the name of the attribute
     * @return the value of the attribute, or {@code null} if no attribute by the given name exists
     */
    <T> T getAttribute(String name);

    /**
     * Binds an object to a given attribute name in the web application scope.
     * @param name the name of the attribute
     * @param value the value to be bound
     */
    void setAttribute(String name, Object value);

    /**
     * Returns a {@link Set} of attribute names available to this web service.
     * @return a set of attribute names
     */
    Set<String> getAttributeNames();

    /**
     * Removes the attribute with the given name from the web application scope.
     * @param name the name of the attribute to remove
     */
    void removeAttribute(String name);

}
