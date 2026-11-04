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
import java.util.Collection;
import java.util.Set;

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
    String getTempDir();

    /**
     * Sets the directory path used for temporary files.
     * @param tempDir the directory path used for temporary files
     * @throws IOException if the directory could not be created or accessed
     */
    void setTempDir(String tempDir) throws IOException;

    /**
     * Returns the maximum allowed size (in bytes) of the entire multipart request.
     * A value of -1 indicates no limit.
     * @return the maximum request size in bytes
     */
    long getMaxRequestSize();

    /**
     * Sets the maximum allowed size (in bytes) of the entire multipart request.
     * A value of -1 indicates no limit (the default).
     * @param maxRequestSize the maximum request size in bytes
     */
    void setMaxRequestSize(long maxRequestSize);

    /**
     * Returns the maximum allowed size (in bytes) for each individual uploaded file.
     * A value of -1 indicates no limit.
     * @return the maximum upload size per file in bytes
     */
    long getMaxFileSize();

    /**
     * Sets the maximum allowed size (in bytes) for each individual uploaded file before
     * the upload is rejected. A value of -1 indicates no limit (the default).
     * @param maxFileSize the maximum upload size per file in bytes
     * @since 3.0.0
     */
    void setMaxFileSize(long maxFileSize);

    /**
     * Returns the maximum allowed size (in bytes) before uploaded files are written to disk.
     * @return the maximum in-memory size allowed in bytes
     */
    int getMaxInMemorySize();

    /**
     * Sets the maximum allowed size (in bytes) before uploaded files are written to disk.
     * Uploaded files larger than this threshold will be written to temporary files on disk
     * rather than stored in memory.
     * @param maxInMemorySize the maximum in-memory size allowed in bytes
     */
    void setMaxInMemorySize(int maxInMemorySize);

    /**
     * Returns the set of allowed file extensions.
     * @return an unmodifiable set of allowed file extensions, or an empty set if no restrictions are configured
     */
    Set<String> getAllowedFileExtensions();

    /**
     * Sets the allowed file extensions from a delimited string (e.g. comma, semicolon, space).
     * @param allowedFileExtensions a delimited string of allowed file extensions
     */
    void setAllowedFileExtensions(String allowedFileExtensions);

    /**
     * Sets the allowed file extensions.
     * @param allowedFileExtensions an array of allowed file extensions
     */
    void setAllowedFileExtensions(String... allowedFileExtensions);

    /**
     * Sets the allowed file extensions from a collection.
     * @param allowedFileExtensions a collection of allowed file extensions
     */
    void setAllowedFileExtensions(Collection<String> allowedFileExtensions);

    /**
     * Returns the set of denied file extensions.
     * @return an unmodifiable set of denied file extensions, or an empty set if no blacklisted extensions are configured
     */
    Set<String> getDeniedFileExtensions();

    /**
     * Sets the denied file extensions from a delimited string (e.g. comma, semicolon, space).
     * @param deniedFileExtensions a delimited string of denied file extensions
     */
    void setDeniedFileExtensions(String deniedFileExtensions);

    /**
     * Sets the denied file extensions.
     * @param deniedFileExtensions an array of denied file extensions
     */
    void setDeniedFileExtensions(String... deniedFileExtensions);

    /**
     * Sets the denied file extensions from a collection.
     * @param deniedFileExtensions a collection of denied file extensions
     */
    void setDeniedFileExtensions(Collection<String> deniedFileExtensions);

    /**
     * Parses the given request, resolving its multipart elements and populating
     * parameters and file parameters into the request adapter.
     * @param requestAdapter the request adapter
     * @throws MultipartRequestParseException if multipart resolution failed
     */
    void parse(RequestAdapter requestAdapter) throws MultipartRequestParseException;

}
