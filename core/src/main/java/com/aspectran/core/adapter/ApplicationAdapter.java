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
import java.util.Set;

/**
 * Provides an abstraction for the application or container environment.
 * <p>
 * This interface is implemented to adapt to a specific runtime container
 * (e.g., Servlet container, command-line shell, etc.). It exposes a consistent API
 * for resolving application base paths and managing application-scoped attributes.
 * This allows the core framework to operate uniformly across different execution
 * environments.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {

    /**
     * Returns the base path of the application as a {@link Path} object.
     * @return the application's base path
     */
    Path getBasePath();

    /**
     * Returns the base path of the application as a string.
     * @return the application's base path as a string
     * @see #getBasePath()
     */
    String getBasePathString();

    /**
     * Returns the real file system path for a given virtual path.
     * The path is resolved relative to the application's base path.
     * @param path the virtual path to translate to a real path
     * @return the resolved, absolute file system path
     */
    Path getRealPath(String path);

    /**
     * Returns the application-scoped attribute with the given name.
     * @param <T> the type of the attribute
     * @param name the name of the attribute
     * @return the attribute value, or {@code null} if it does not exist
     */
    <T> T getAttribute(String name);

    /**
     * Sets an application-scoped attribute.
     * If an attribute with the same name already exists, it will be replaced.
     * @param name the name of the attribute
     * @param value the value to set for the attribute
     */
    void setAttribute(String name, Object value);

    /**
     * Returns an {@link Set} of all application-scoped attribute names.
     * @return a set of attribute names
     */
    Set<String> getAttributeNames();

    /**
     * Removes the application-scoped attribute with the given name.
     * @param name the name of the attribute to remove
     */
    void removeAttribute(String name);

}
