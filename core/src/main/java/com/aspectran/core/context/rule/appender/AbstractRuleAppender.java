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
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeTracker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Abstract base class for {@link RuleAppender} implementations.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
abstract class AbstractRuleAppender implements RuleAppender {

    private final AppenderType appenderType;

    private AppendRule appendRule;

    private AppendableFileFormatType appendableFileFormatType;

    private Profiles profiles;

    private long lastModified;

    private NodeTracker nodeTracker;

    AbstractRuleAppender(AppenderType appenderType) {
        this.appenderType = appenderType;
    }

    @Override
    public AppenderType getAppenderType() {
        return appenderType;
    }

    @Override
    public AppendRule getAppendRule() {
        return appendRule;
    }

    @Override
    public void setAppendRule(@NonNull AppendRule appendRule) {
        this.appendRule = appendRule;
        // If the rule does not have a format, it follows its own determined format.
        if (appendRule.getFormat() != null) {
            this.appendableFileFormatType = appendRule.getFormat();
        }
        setProfiles(appendRule.getProfiles());
    }

    @Override
    public AppendableFileFormatType getAppendableFileFormatType() {
        return appendableFileFormatType;
    }

    @Override
    public void setAppendableFileFormatType(AppendableFileFormatType appendableFileFormatType) {
        this.appendableFileFormatType = appendableFileFormatType;
    }

    /**
     * Determines the appended file format type based on the resource name.
     * @param resourceName the name of the resource
     */
    protected void determineAppendedFileFormatType(String resourceName) {
        if (resourceName != null && resourceName.toLowerCase().endsWith(".apon")) {
            setAppendableFileFormatType(AppendableFileFormatType.APON);
        } else {
            setAppendableFileFormatType(AppendableFileFormatType.XML);
        }
    }

    @Override
    public Profiles getProfiles() {
        return profiles;
    }

    private void setProfiles(Profiles profiles) {
        this.profiles = profiles;
    }

    @Override
    public void setProfile(String profile) {
        setProfiles(profile != null ? Profiles.of(profile) : null);
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Reader getReader() throws IOException {
        return getReader(null);
    }

    @Override
    public Reader getReader(String encoding) throws IOException {
        if (encoding != null) {
            return new InputStreamReader(getInputStream(), encoding);
        } else {
            return new InputStreamReader(getInputStream());
        }
    }

    @Override
    public NodeTracker getNodeTracker() {
        return nodeTracker;
    }

    @Override
    public void setNodeTracker(NodeTracker nodeTracker) {
        Assert.notNull(nodeTracker, "NodeTracker must not be null");
        this.nodeTracker = nodeTracker;
        String qualifiedName = getQualifiedName();
        if (qualifiedName != null) {
            nodeTracker.setResource(qualifiedName);
        }
    }

}
