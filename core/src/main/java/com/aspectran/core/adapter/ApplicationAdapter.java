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

import java.nio.file.Path;
import java.util.Enumeration;

/**
 * The Interface ApplicationAdapter.
 *
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {

    /**
     * Returns the base path that the current application is mapped to.
     * @return the base path for the current application
     */
    Path getBasePath();

    /**
     * Returns the base path that the current application is mapped to.
     * @return the base path for the current application
     */
    String getBasePathString();

    /**
     * Returns the real file system path for a given virtual path relative
     * to the base path of the current application.
     * @param path the virtual path to be translated to a real path
     * @return the real path
     */
    Path getRealPath(String path);

    /**
     * Returns the value for an attribute with the given name.
     * @param <T> the value type
     * @param name the name of the attribute
     * @return the value for the attribute
     */
    <T> T getAttribute(String name);

    /**
     * Sets the value for the attribute of the given name,
     * replacing an existing value (if any).
     * @param name the name of the attribute
     * @param value the value for the attribute
     */
    void setAttribute(String name, Object value);

    /**
     * Returns an {@link Enumeration} containing the names
     * of the attributes available to this application.
     * @return the attribute names
     */
    Enumeration<String> getAttributeNames();

    /**
     * Removes an attribute set with the given name.
     * @param name the name of the attribute to be removed
     */
    void removeAttribute(String name);

}
