/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import java.io.IOException;
import java.io.InputStream;

/**
 * The Class ResourceRuleAppender.
 * 
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public class ResourceRuleAppender extends AbstractRuleAppender {

    private final ClassLoader classLoader;

    private final String resource;

    public ResourceRuleAppender(ClassLoader classLoader, String resource) {
        super(AppenderType.RESOURCE);

        this.classLoader = classLoader;
        this.resource = resource;

        setLastModified(System.currentTimeMillis());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream(resource);
        if (inputStream == null) {
            throw new IOException("Failed to append rule resource: " + resource);
        }
        return inputStream;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("appenderType", getAppenderType());
        tsb.append("resource", resource);
        tsb.append("format", getAppenderFileFormatType());
        tsb.append("profile", getProfiles());
        return tsb.toString();
    }

}
