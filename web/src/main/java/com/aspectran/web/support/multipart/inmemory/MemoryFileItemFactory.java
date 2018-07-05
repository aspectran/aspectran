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
package com.aspectran.web.support.multipart.inmemory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

/**
 * The class is an implementation of the {@link org.apache.commons.fileupload.FileItemFactory} interface.
 *
 * <p>Created: 2018. 7. 5.</p>
 */
public class MemoryFileItemFactory implements FileItemFactory {

    /** Threshold file size in Memory is set to max file upload size (default is max integer value). */
    private int sizeThreshold = Integer.MAX_VALUE;

    /**
     * Create a new {@link MemoryFileItem} instance from the supplied parameters and the local factory configuration.
     *
     * @param fieldName The name of the form field.
     * @param contentType The content type of the form field.
     * @param isFormField <code>true</code> if this is a plain form field; <code>false</code> otherwise.
     * @param fileName The name of the uploaded file, if any, as supplied by the browser or other client.
     * @return The newly created file item.
     */
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new MemoryFileItem(fieldName, contentType, isFormField, fileName, sizeThreshold);
    }

    /**
     * Sets the size threshold to store the data in memory, beyond that MemoryFileItem will throw error,
     * as file handling is not supported in GAE.
     *
     * @param sizeThreshold The size threshold, in bytes.
     *
     * @see #getSizeThreshold()
     */
    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    /**
     * Returns the size threshold beyond which files are written directly to disk.
     * The default value is Integer.MAX_VALUE bytes (2 GB approx).
     *
     * @return The size threshold, in bytes.
     * @see #setSizeThreshold(int)
     */
    public int getSizeThreshold() {
        return sizeThreshold;
    }

}