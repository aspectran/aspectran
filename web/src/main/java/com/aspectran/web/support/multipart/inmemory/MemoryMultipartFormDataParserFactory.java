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

import com.aspectran.core.util.StringUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;

/**
 * The Class MemoryMultipartFormDataParserFactory.
 *
 * @since 5.1.0
 */
public class MemoryMultipartFormDataParserFactory {

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new Multipart request wrapper resolver.
     */
    public MemoryMultipartFormDataParserFactory() {
    }

    /**
     * Gets the maximum size of the request.
     *
     * @return the maximum size of the request
     */
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    /**
     * Sets the maximum size of the request.
     *
     * @param maxRequestSize the maximum size of the request
     */
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    /**
     * Sets the maximum size of the request.
     *
     * @param maxRequestSize the maximum size of the request
     */
    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = StringUtils.convertToMachineFriendlyByteSize(maxRequestSize);
    }

    /**
     * Sets the maximum size of the file.
     *
     * @param maxFileSize the maximum size of the file
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Sets the maximum size of the file.
     *
     * @param maxFileSize the maximum size of the file
     */
    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = StringUtils.convertToMachineFriendlyByteSize(maxFileSize);
    }

    /**
     * Gets the allowed file extensions.
     *
     * @return the allowed file extensions
     */
    public String getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    /**
     * Sets the allowed file extensions.
     *
     * @param allowedFileExtensions the allowed file extensions
     */
    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    /**
     * Gets the denied file extensions.
     *
     * @return the denied file extensions
     */
    public String getDeniedFileExtensions() {
        return deniedFileExtensions;
    }

    /**
     * Sets the denied file extensions.
     *
     * @param deniedFileExtensions the denied file extensions
     */
    public void setDeniedFileExtensions(String deniedFileExtensions) {
        this.deniedFileExtensions = deniedFileExtensions;
    }

    /**
     * Creates a new MultipartFormDataParser object.
     *
     * @return the multipart form data parser
     */
    public MultipartFormDataParser createMultipartFormDataParser() {
        MultipartFormDataParser parser = new MemoryMultipartFormDataParser();
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
