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
 * Defines the contract for loading and appending rule configuration from various sources
 * such as the file system, classpath, or network URLs.
 * <p>Implementations of this interface provide standardized access to rule resources
 * through input streams or readers, allowing the framework to aggregate multiple
 * configuration sources.</p>
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public interface RuleAppender {

    /**
     * Returns the type of this rule appender.
     * @return the {@link AppenderType} representing the source type of this appender
     */
    AppenderType getAppenderType();

    /**
     * Returns the rule that describes the resource to be appended.
     * @return the {@link AppendRule} instance
     */
    AppendRule getAppendRule();

    /**
     * Sets the rule that describes the resource to be appended.
     * @param appendRule the {@link AppendRule} instance
     */
    void setAppendRule(AppendRule appendRule);

    /**
     * Returns the format of the rule configuration file to be appended.
     * @return the {@link AppendableFileFormatType}
     */
    AppendableFileFormatType getAppendableFileFormatType();

    /**
     * Sets the format of the rule configuration file to be appended.
     * @param appendableFileFormatType the {@link AppendableFileFormatType}
     */
    void setAppendableFileFormatType(AppendableFileFormatType appendableFileFormatType);

    /**
     * Returns the active profiles for which the rules are applicable.
     * @return the {@link Profiles}
     */
    Profiles getProfiles();

    /**
     * Sets a profile that determines whether the rule resource should be applied.
     * @param profile the profile name
     */
    void setProfile(String profile);

    /**
     * Returns the unique, qualified name of the rule resource.
     * @return the qualified name
     */
    String getQualifiedName();

    /**
     * Returns the last modified time of the rule resource.
     * @return the last modified time in milliseconds
     */
    long getLastModified();

    /**
     * Sets the last modified time of the rule resource.
     * @param lastModified the last modified time in milliseconds
     */
    void setLastModified(long lastModified);

    /**
     * Returns an input stream for reading the rule configuration data.
     * @return an {@link InputStream} to access the rule resource
     * @throws IOException if an I/O error occurs while opening the stream
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns a reader for reading the rule configuration data.
     * @return a {@link Reader} to access the rule resource
     * @throws IOException if an I/O error occurs while opening the reader
     */
    Reader getReader() throws IOException;

    /**
     * Returns a reader for reading the rule configuration data using the specified encoding.
     * @param encoding the character encoding to use
     * @return a {@link Reader} with the specified encoding
     * @throws IOException if an I/O error occurs while opening the reader
     */
    Reader getReader(String encoding) throws IOException;

    /**
     * Returns the {@link NodeTracker} used to track the location of the rules during parsing.
     * @return the node tracker instance
     */
    NodeTracker getNodeTracker();

    /**
     * Sets the {@link NodeTracker} used to track the location of the rules during parsing.
     * @param nodeTracker the node tracker instance
     */
    void setNodeTracker(NodeTracker nodeTracker);

}
