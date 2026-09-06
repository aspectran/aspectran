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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.RequestAdapter;

import java.io.IOException;

/**
 * Strategy interface for parsing multipart form data requests (e.g. file uploads).
 * <p>Implementations handle parsing {@code multipart/form-data} HTTP requests and populating
 * the {@link RequestAdapter} with regular parameters and
 * {@link com.aspectran.core.activity.request.FileParameter}s.</p>
 */
public interface MultipartFormDataParser {

    /**
     * Returns the directory path used for temporary files.
     * @return the directory path used for temporary files
     */
    String getTempFileDir();

    /**
     * Sets the directory path used for temporary files.
     * @param tempFileDir the directory path used for temporary files
     * @throws IOException if the directory could not be created or accessed
     */
    void setTempFileDir(String tempFileDir) throws IOException;

    /**
     * Sets the maximum allowed size (in bytes) of the entire multipart request.
     * A value of -1 indicates no limit (the default).
     * @param maxRequestSize the maximum request size in bytes
     */
    void setMaxRequestSize(long maxRequestSize);

    /**
     * Sets the maximum allowed size (in bytes) for each individual uploaded file before
     * the upload is rejected. A value of -1 indicates no limit (the default).
     * @param maxFileSize the maximum upload size per file in bytes
     * @since 3.0.0
     */
    void setMaxFileSize(long maxFileSize);

    /**
     * Sets the maximum allowed size (in bytes) before uploaded files are written to disk.
     * Uploaded files larger than this threshold will be written to temporary files on disk
     * rather than stored in memory.
     * @param maxInMemorySize the maximum in-memory size allowed in bytes
     */
    void setMaxInMemorySize(int maxInMemorySize);

    /**
     * Sets the comma-separated list of allowed file extensions.
     * @param allowedFileExtensions a comma-separated string of allowed file extensions
     */
    void setAllowedFileExtensions(String allowedFileExtensions);

    /**
     * Sets the comma-separated list of denied file extensions.
     * @param deniedFileExtensions a comma-separated string of denied file extensions
     */
    void setDeniedFileExtensions(String deniedFileExtensions);

    /**
     * Parses the given request, resolving its multipart elements and populating
     * parameters and file parameters into the request adapter.
     * @param requestAdapter the request adapter
     * @throws MultipartRequestParseException if multipart resolution failed
     */
    void parse(RequestAdapter requestAdapter) throws MultipartRequestParseException;

}
