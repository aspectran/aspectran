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
package com.aspectran.core.context.rule.appender;

import com.aspectran.core.context.env.Profiles;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.utils.nodelet.NodeTracker;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Defines the contract for appending rules from various sources such as files,
 * classpath resources, or URLs.
 * <p>Implementations provide an {@link java.io.InputStream} for the configuration
 * data to be parsed.</p>
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public interface RuleAppender {

    AppenderType getAppenderType();

    AppendRule getAppendRule();

    void setAppendRule(AppendRule appendRule);

    AppendableFileFormatType getAppendableFileFormatType();

    void setAppendableFileFormatType(AppendableFileFormatType appendableFileFormatType);

    Profiles getProfiles();

    void setProfile(String profile);

    String getQualifiedName();

    long getLastModified();

    void setLastModified(long lastModified);

    InputStream getInputStream() throws IOException;

    Reader getReader() throws IOException;

    Reader getReader(String encoding) throws IOException;

    NodeTracker getNodeTracker();

    void setNodeTracker(NodeTracker nodeTracker);

}
