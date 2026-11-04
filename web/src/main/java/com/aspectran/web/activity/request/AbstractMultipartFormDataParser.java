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

import com.aspectran.utils.DataSizeUtils;
import com.aspectran.utils.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Abstract base class for {@link MultipartFormDataParser} implementations,
 * providing common configuration properties such as file size limits,
 * temporary directory management, and allowed/denied file extension rules.
 *
 * <p>Created: 2026-11-04</p>
 */
public abstract class AbstractMultipartFormDataParser implements MultipartFormDataParser {

    private String tempFileDir;

    private long maxRequestSize = -1L;

    private long maxFileSize = -1L;

    private int maxInMemorySize = -1;

    private Set<String> allowedFileExtensions;

    private Set<String> deniedFileExtensions;

    protected AbstractMultipartFormDataParser() {
    }

    @Override
    public String getTempFileDir() {
        return tempFileDir;
    }

    @Override
    public void setTempFileDir(String tempFileDir) throws IOException {
        if (tempFileDir == null) {
            throw new IllegalArgumentException("tempFileDir must not be null");
        }
        File dir = new File(tempFileDir);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IOException("Given tempFileDir [" + tempFileDir + "] exists but is not a directory");
            }
        } else {
            if (!dir.mkdirs()) {
                throw new IOException("Given tempFileDir [" + tempFileDir + "] could not be created");
            }
        }
        this.tempFileDir = tempFileDir;
    }

    @Override
    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    @Override
    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public void setMaxRequestSize(String maxRequestSize) {
        this.maxRequestSize = DataSizeUtils.toMachineFriendlyByteSize(maxRequestSize);
    }

    @Override
    public long getMaxFileSize() {
        return maxFileSize;
    }

    @Override
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = DataSizeUtils.toMachineFriendlyByteSize(maxFileSize);
    }

    @Override
    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    @Override
    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public void setMaxInMemorySize(String maxInMemorySize) {
        this.maxInMemorySize = (int) DataSizeUtils.toMachineFriendlyByteSize(maxInMemorySize);
    }

    @Override
    public Set<String> getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    @Override
    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = FilenameUtils.parseFileExtensions(allowedFileExtensions);
    }

    @Override
    public void setAllowedFileExtensions(String... allowedFileExtensions) {
        this.allowedFileExtensions = FilenameUtils.parseFileExtensions(allowedFileExtensions);
    }

    @Override
    public void setAllowedFileExtensions(Collection<String> allowedFileExtensions) {
        this.allowedFileExtensions = FilenameUtils.parseFileExtensions(allowedFileExtensions);
    }

    @Override
    public Set<String> getDeniedFileExtensions() {
        return deniedFileExtensions;
    }

    @Override
    public void setDeniedFileExtensions(String deniedFileExtensions) {
        this.deniedFileExtensions = FilenameUtils.parseFileExtensions(deniedFileExtensions);
    }

    @Override
    public void setDeniedFileExtensions(String... deniedFileExtensions) {
        this.deniedFileExtensions = FilenameUtils.parseFileExtensions(deniedFileExtensions);
    }

    @Override
    public void setDeniedFileExtensions(Collection<String> deniedFileExtensions) {
        this.deniedFileExtensions = FilenameUtils.parseFileExtensions(deniedFileExtensions);
    }

    /**
     * Checks whether the given filename has an allowed file extension
     * according to the configured allowed and denied extensions.
     * @param filename the filename to check
     * @return true if the extension is valid and allowed; false otherwise
     */
    protected boolean isValidFileExtension(String filename) {
        return FilenameUtils.isValidFileExtension(filename, allowedFileExtensions, deniedFileExtensions);
    }

}
