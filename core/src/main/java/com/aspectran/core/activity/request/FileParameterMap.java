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
package com.aspectran.core.activity.request;

import java.io.Serial;
import java.util.LinkedHashMap;

/**
 * A specialized map for managing arrays of {@link FileParameter} objects
 * associated with a request.
 * <p>
 * Each key corresponds to a parameter name in the request, and its value
 * is one or more uploaded files submitted under that field name.
 * This class simplifies handling single or multiple file uploads.
 * </p>
 *
 * <p>Created: 2008. 03. 29 PM 6:23:00</p>
 */
public class FileParameterMap extends LinkedHashMap<String, FileParameter[]> {

    @Serial
    private static final long serialVersionUID = -2589963778315184242L;

    /**
     * Returns the first file parameter associated with the given name.
     * @param name the parameter name
     * @return the first {@link FileParameter}, or {@code null} if none exist
     */
    public FileParameter getFileParameter(String name) {
        FileParameter[] fileParameters = get(name);
        return (fileParameters != null && fileParameters.length > 0 ? fileParameters[0] : null);
    }

    /**
     * Returns all file parameters associated with the given name.
     * @param name the parameter name
     * @return an array of {@link FileParameter} objects, or {@code null} if none exist
     */
    public FileParameter[] getFileParameterValues(String name) {
        return get(name);
    }

    /**
     * Associates a single {@link FileParameter} with the given name.
     * <p>If multiple files already exist under this name, they will be replaced.</p>
     * @param name the parameter name
     * @param fileParameter the file parameter to store
     */
    public void setFileParameter(String name, FileParameter fileParameter) {
        put(name, new FileParameter[] { fileParameter });
    }

    /**
     * Associates multiple {@link FileParameter} objects with the given name.
     * @param name the parameter name
     * @param fileParameters the array of file parameters to store
     */
    public void setFileParameterValues(String name, FileParameter[] fileParameters) {
        put(name, fileParameters);
    }

}
