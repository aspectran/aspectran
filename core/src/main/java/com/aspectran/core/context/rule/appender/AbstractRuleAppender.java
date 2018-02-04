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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.type.AppenderFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.nodelet.NodeTracker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * The Class AbstractRuleAppender.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
abstract class AbstractRuleAppender implements RuleAppender {

    private AppenderType appenderType;

    private AppendRule appendRule;

    private AppenderFileFormatType appenderFileFormatType;

    private String[] profiles;

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
    public void setAppendRule(AppendRule appendRule) {
        this.appendRule = appendRule;

        AppenderFileFormatType appenderFileFormatType = AppenderFileFormatType.resolve(appendRule.getFormat());
        if (appenderFileFormatType != null) {
            this.appenderFileFormatType = appenderFileFormatType;
        }

        String profile = appendRule.getProfile();
        if (profile != null && !profile.isEmpty()) {
            String[] arr = StringUtils.splitCommaDelimitedString(profile);
            if (arr.length > 0) {
                this.profiles = arr;
            } else {
                this.profiles = null;
            }
        }
    }

    @Override
    public AppenderFileFormatType getAppenderFileFormatType() {
        return appenderFileFormatType;
    }

    @Override
    public void setAppenderFileFormatType(AppenderFileFormatType appenderFileFormatType) {
        this.appenderFileFormatType = appenderFileFormatType;
    }

    protected void determineAppenderFileFormatType(String resourceName) {
        if (resourceName.toLowerCase().endsWith(".apon")) {
            setAppenderFileFormatType(AppenderFileFormatType.APON);
        } else {
            setAppenderFileFormatType(AppenderFileFormatType.XML);
        }
    }

    @Override
    public String[] getProfiles() {
        return profiles;
    }

    @Override
    public void setProfiles(String[] profiles) {
        this.profiles = profiles;
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
        this.nodeTracker = nodeTracker;
    }

}
