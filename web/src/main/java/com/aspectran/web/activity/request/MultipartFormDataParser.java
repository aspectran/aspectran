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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.RequestAdapter;

/**
 * Multi-part form data parser.
 */
public interface MultipartFormDataParser {

    /**
     * Returns the directory path used for temporary files.
     *
     * @return the directory path used for temporary files
     */
    String getTempDirectoryPath();

    /**
     * Sets the directory path used to temporarily files.
     *
     * @param tempDirectoryPath the directory path used for temporary files
     */
    void setTempDirectoryPath(String tempDirectoryPath);

    /**
     * Sets the maximum length of HTTP GET Request
     * -1 indicates no limit (the default).
     *
     * @param maxRequestSize the maximum length of HTTP GET Request
     * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
     */
    void setMaxRequestSize(long maxRequestSize);

    /**
     * Set the maximum allowed size (in bytes) for each individual file before
     * an upload gets rejected. -1 indicates no limit (the default).
     *
     * @param maxFileSize the maximum upload size per file
     * @since 3.0.0
     * @see org.apache.commons.fileupload.FileUploadBase#setFileSizeMax
     */
    void setMaxFileSize(long maxFileSize);

    /**
     * Set the maximum allowed size (in bytes) before uploads are written to disk.
     * Uploaded files will still be received past this amount, but they will not be
     * stored in memory. Default is 10240, according to Commons FileUpload.
     *
     * @param maxInMemorySize the maximum in memory size allowed
     * @see org.apache.commons.fileupload.disk.DiskFileItemFactory#setSizeThreshold
     */
    void setMaxInMemorySize(int maxInMemorySize);

    /**
     * Sets the allowed file extensions.
     *
     * @param allowedFileExtensions the allowed file extensions
     */
    void setAllowedFileExtensions(String allowedFileExtensions);

    /**
     * Sets the denied file extensions.
     *
     * @param deniedFileExtensions the denied file extensions
     */
    void setDeniedFileExtensions(String deniedFileExtensions);

    /**
     * Parse the given servlet request, resolving its multipart elements.
     *
     * @param requestAdapter the request adapter
     * @throws MultipartRequestParseException if multipart resolution failed
     */
    void parse(RequestAdapter requestAdapter);

}
