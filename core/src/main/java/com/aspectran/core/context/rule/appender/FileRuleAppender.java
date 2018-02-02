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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.core.util.ToStringBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The Class FileRuleAppender.
 * 
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public class FileRuleAppender extends AbstractRuleAppender {

    private final String basePath;

    private final String filePath;

    public FileRuleAppender(String filePath) {
        this(null, filePath);
    }

    public FileRuleAppender(String basePath, String filePath) {
        super(AppenderType.FILE);

        this.basePath = basePath;
        this.filePath = filePath;

        determineAppenderFileFormatType(filePath);
    }

    public String getBasePath() {
        return basePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        if (basePath == null) {
            return new File(filePath);
        } else {
            return new File(basePath, filePath);
        }
    }

    public String getQualifiedName() {
        try {
            return getFile().getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public long getLastModified() {
        File file = getFile();
        return file.lastModified();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            File file = getFile();
            if (!file.isFile()) {
                throw new IOException("Is this a file?");
            }
            setLastModified(file.lastModified());
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new IOException("Failed to create input stream from rule file: " + getFile().getAbsolutePath(), e);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("appenderType", getAppenderType());
        tsb.append("basePath", basePath);
        tsb.append("filePath", filePath);
        tsb.append("format", getAppenderFileFormatType());
        tsb.append("profile", getProfiles());
        return tsb.toString();
    }

}
