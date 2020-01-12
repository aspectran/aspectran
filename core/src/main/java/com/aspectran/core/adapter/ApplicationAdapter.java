/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

/**
 * The Interface ApplicationAdapter.
 *
 * @since 2011. 3. 13.
 */
public interface ApplicationAdapter {

    /**
     * Returns the class loader used by the current application.
     *
     * @return the class loader
     */
    ClassLoader getClassLoader();

    /**
     * Returns the base path that the current application is mapped to.
     *
     * @return the application base path
     */
    String getBasePath();

    /**
     * Returns to convert the given file path with the real file path.
     *
     * @param filePath the specified file path
     * @return the real file path
     * @throws IOException if an I/O error has occurred
     */
    String toRealPath(String filePath) throws IOException;

    /**
     * Returns to convert the given file path with the real file path.
     *
     * @param filePath the specified file path
     * @return the real file path
     * @throws IOException if an I/O error has occurred
     */
    File toRealPathAsFile(String filePath) throws IOException;

    /**
     * Gets the attribute.
     *
     * @param <T> the generic type
     * @param name the name
     * @return the attribute
     */
    <T> T getAttribute(String name);

    /**
     * Sets the attribute.
     *
     * @param name the name
     * @param value the value
     */
    void setAttribute(String name, Object value);

    /**
     * Gets the attribute names.
     *
     * @return the attribute names
     */
    Enumeration<String> getAttributeNames();

    /**
     * Removes the attribute.
     *
     * @param name the name
     */
    void removeAttribute(String name);

}
