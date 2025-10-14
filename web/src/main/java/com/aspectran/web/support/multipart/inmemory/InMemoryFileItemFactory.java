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
package com.aspectran.web.support.multipart.inmemory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;

/**
 * A factory for creating {@link InMemoryFileItem} instances.
 * <p>This implementation creates file items that are stored entirely in memory.
 */
public class InMemoryFileItemFactory implements FileItemFactory {

    /** The maximum size of a file that can be stored in memory. */
    private int sizeThreshold = Integer.MAX_VALUE;

    /**
     * Creates a new {@link InMemoryFileItem} instance.
     * @param fieldName   the name of the form field
     * @param contentType the content type of the form field
     * @param isFormField {@code true} if this is a plain form field; {@code false} otherwise
     * @param fileName    the name of the uploaded file, if any
     * @return the newly created file item
     */
    @Override
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new InMemoryFileItem(fieldName, contentType, isFormField, fileName, sizeThreshold);
    }

    /**
     * Sets the maximum size threshold, in bytes. If an uploaded file exceeds this
     * threshold, an {@link org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException}
     * will be thrown.
     * @param sizeThreshold the size threshold, in bytes
     */
    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    /**
     * Returns the maximum size threshold, in bytes.
     * @return the size threshold, in bytes
     */
    public int getSizeThreshold() {
        return sizeThreshold;
    }

}
