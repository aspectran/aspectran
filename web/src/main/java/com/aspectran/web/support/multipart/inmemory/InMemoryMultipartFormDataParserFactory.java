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

import com.aspectran.utils.DataSizeUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * A factory for creating and configuring {@link InMemoryMultipartFormDataParser} instances.
 * <p>This class provides a centralized way to set properties such as file size limits
 * for the in-memory multipart parser.
 *
 * @since 5.1.0
 */
public class InMemoryMultipartFormDataParserFactory {

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new Multipart request wrapper resolver.
     */
    public InMemoryMultipartFormDataParserFactory() {
    }

    /**
     * Gets the maximum size of the request.
     * @return the maximum size of the request
     */
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * Sets the maximum size of the entire multipart request, in bytes.
     * @param maxRequestSize the maximum request size in bytes
     */
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    /**
     * Sets the maximum size of the entire multipart request using a human-readable
     * format (e.g., "10MB", "2G").
     * @param maxRequestSize the maximum request size in a human-readable format
     */
    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = DataSizeUtils.toMachineFriendlyByteSize(maxRequestSize);
    }

    /**
     * Sets the maximum size of a single uploaded file, in bytes.
     * @param maxFileSize the maximum file size in bytes
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the maximum size of a single uploaded file using a human-readable
     * format (e.g., "10MB", "2G").
     * @param maxFileSize the maximum file size in a human-readable format
     */
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = DataSizeUtils.toMachineFriendlyByteSize(maxFileSize);
    }

    /**
     * Gets the allowed file extensions.
     * @return the allowed file extensions
     */
    public String getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    /**
     * Sets the comma-separated list of allowed file extensions.
     * @param allowedFileExtensions a string containing allowed extensions
     */
    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    /**
     * Gets the denied file extensions.
     * @return the denied file extensions
     */
    public String getDeniedFileExtensions() {
        return deniedFileExtensions;
    }

    /**
     * Sets the comma-separated list of denied file extensions.
     * @param deniedFileExtensions a string containing denied extensions
     */
    public void setDeniedFileExtensions(String deniedFileExtensions) {
        this.deniedFileExtensions = deniedFileExtensions;
    }

    /**
     * Creates and configures a new {@link MultipartFormDataParser} instance.
     * @return a new, configured {@code MultipartFormDataParser} instance
     */
    public MultipartFormDataParser createMultipartFormDataParser() {
        MultipartFormDataParser parser = new InMemoryMultipartFormDataParser();
        if (maxRequestSize > -1L) {
            parser.setMaxRequestSize(maxRequestSize);
        }
        if (maxFileSize > -1L) {
            parser.setMaxFileSize(maxFileSize);
        }
        parser.setAllowedFileExtensions(allowedFileExtensions);
        parser.setDeniedFileExtensions(deniedFileExtensions);
        return parser;
    }

}
