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

    /**
     * Returns the appender type.
     * @return the appender type
     */
    AppenderType getAppenderType();

    /**
     * Returns the append rule.
     * @return the append rule
     */
    AppendRule getAppendRule();

    /**
     * Sets the append rule.
     * @param appendRule the append rule
     */
    void setAppendRule(AppendRule appendRule);

    /**
     * Returns the appendable file format type.
     * @return the appendable file format type
     */
    AppendableFileFormatType getAppendableFileFormatType();

    /**
     * Sets the appendable file format type.
     * @param appendableFileFormatType the appendable file format type
     */
    void setAppendableFileFormatType(AppendableFileFormatType appendableFileFormatType);

    /**
     * Returns the profiles.
     * @return the profiles
     */
    Profiles getProfiles();

    /**
     * Sets the profile.
     * @param profile the profile
     */
    void setProfile(String profile);

    /**
     * Returns the qualified name of the resource.
     * @return the qualified name
     */
    String getQualifiedName();

    /**
     * Returns the last modified time of the resource.
     * @return the last modified time
     */
    long getLastModified();

    /**
     * Sets the last modified time of the resource.
     * @param lastModified the last modified time
     */
    void setLastModified(long lastModified);

    /**
     * Returns an input stream for reading the rules.
     * @return the input stream
     * @throws IOException if an I/O error has occurred
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns a reader for reading the rules.
     * @return the reader
     * @throws IOException if an I/O error has occurred
     */
    Reader getReader() throws IOException;

    /**
     * Returns a reader for reading the rules with the specified encoding.
     * @param encoding the encoding
     * @return the reader
     * @throws IOException if an I/O error has occurred
     */
    Reader getReader(String encoding) throws IOException;

    /**
     * Returns the node tracker.
     * @return the node tracker
     */
    NodeTracker getNodeTracker();

    /**
     * Sets the node tracker.
     * @param nodeTracker the node tracker
     */
    void setNodeTracker(NodeTracker nodeTracker);

}
