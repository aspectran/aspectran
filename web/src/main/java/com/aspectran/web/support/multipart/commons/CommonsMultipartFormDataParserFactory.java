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
package com.aspectran.web.support.multipart.commons;

import com.aspectran.utils.DataSizeUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;

import java.io.IOException;

/**
 * A factory for creating and configuring {@link CommonsMultipartFormDataParser} instances.
 * <p>This class provides a centralized way to set properties such as file size limits
 * and temporary directories for the multipart parser.
 *
 * @since 2.0.0
 */
public class CommonsMultipartFormDataParserFactory {

    private String tempFileDir;

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Instantiates a new Commons multipart request wrapper parser.
     */
    public CommonsMultipartFormDataParserFactory() {
    }

    /**
     * Gets the directory path used to temporarily files.
     * @return the directory path used for temporary files
     */
    public String getTempFileDir() {
        return tempFileDir;
    }

    /**
     * Sets the temporary directory where uploaded files will be stored.
     * @param tempFileDir the path to the temporary directory
     */
    public void setTempFileDir(String tempFileDir) {
        this.tempFileDir = tempFileDir;
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
     * Sets the maximum size of a file that will be stored in memory, in bytes.
     * Files larger than this threshold will be written to disk.
     * @param maxInMemorySize the maximum memory size in bytes
     */
    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    /**
     * Sets the maximum size of a file that will be stored in memory using a
     * human-readable format (e.g., "128KB", "1MB").
     * @param maxInMemorySize the maximum memory size in a human-readable format
     */
    public void setMaxInMemorySize(String maxInMemorySize) {
        this.maxInMemorySize = (int)DataSizeUtils.toMachineFriendlyByteSize(maxInMemorySize);
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
     * If a temporary directory is not set, it defaults to the system's temporary directory.
     * @return a new, configured {@code MultipartFormDataParser} instance
     * @throws IOException if the temporary directory cannot be accessed or created
     */
    public MultipartFormDataParser createMultipartFormDataParser() throws IOException {
        MultipartFormDataParser parser = new CommonsMultipartFormDataParser();
        if (tempFileDir != null) {
            parser.setTempFileDir(tempFileDir);
        } else {
            parser.setTempFileDir(SystemUtils.getJavaIoTmpDir());
        }
        if (maxRequestSize > -1L) {
            parser.setMaxRequestSize(maxRequestSize);
        }
        if (maxFileSize > -1L) {
            parser.setMaxFileSize(maxFileSize);
        }
        if (maxInMemorySize > -1) {
            parser.setMaxInMemorySize(maxInMemorySize);
        }
        parser.setAllowedFileExtensions(allowedFileExtensions);
        parser.setDeniedFileExtensions(deniedFileExtensions);
        return parser;
    }

}
