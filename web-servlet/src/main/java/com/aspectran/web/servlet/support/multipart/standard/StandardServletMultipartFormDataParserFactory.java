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
package com.aspectran.web.servlet.support.multipart.standard;

import com.aspectran.utils.DataSizeUtils;
import com.aspectran.utils.SystemUtils;
import com.aspectran.web.activity.request.MultipartFormDataParser;

import java.io.IOException;

/**
 * A factory for creating and configuring {@link StandardServletMultipartFormDataParser} instances.
 *
 * @since 9.7.0
 */
public class StandardServletMultipartFormDataParserFactory {

    private String tempFileDir;

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private String allowedFileExtensions;

    private String deniedFileExtensions;

    /**
     * Constructs a new StandardServletMultipartFormDataParserFactory.
     */
    public StandardServletMultipartFormDataParserFactory() {
    }

    public String getTempFileDir() {
        return tempFileDir;
    }

    public void setTempFileDir(String tempFileDir) {
        this.tempFileDir = tempFileDir;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = DataSizeUtils.toMachineFriendlyByteSize(maxRequestSize);
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = DataSizeUtils.toMachineFriendlyByteSize(maxFileSize);
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public void setMaxInMemorySize(String maxInMemorySize) {
        this.maxInMemorySize = (int) DataSizeUtils.toMachineFriendlyByteSize(maxInMemorySize);
    }

    public String getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    public String getDeniedFileExtensions() {
        return deniedFileExtensions;
    }

    public void setDeniedFileExtensions(String deniedFileExtensions) {
        this.deniedFileExtensions = deniedFileExtensions;
    }

    /**
     * Creates and configures a new {@link StandardServletMultipartFormDataParser} instance.
     * @return a new, configured {@code StandardServletMultipartFormDataParser} instance
     * @throws IOException if the temporary directory cannot be accessed or created
     */
    public MultipartFormDataParser createMultipartFormDataParser() throws IOException {
        StandardServletMultipartFormDataParser parser = new StandardServletMultipartFormDataParser();
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
