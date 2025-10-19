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

import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.utils.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link RuleAppender} implementation that reads rules from a URL.
 *
 * <p>Created: 2008. 04. 24 AM 11:23:36</p>
 */
public class UrlRuleAppender extends AbstractRuleAppender {

    private final String ruleUrl;

    /**
     * Instantiates a new UrlRuleAppender.
     * @param ruleUrl the rule URL
     */
    public UrlRuleAppender(String ruleUrl) {
        super(AppenderType.URL);
        this.ruleUrl = ruleUrl;
        determineAppendedFileFormatType(ruleUrl);
    }

    @Override
    public String getQualifiedName() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            URL url = new URI(ruleUrl).toURL();
            URLConnection conn = url.openConnection();
            setLastModified(conn.getLastModified());
            return conn.getInputStream();
        } catch (Exception e) {
            throw new IOException("Failed to get rules from url: " + ruleUrl, e);
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("url", ruleUrl);
        tsb.append("format", getAppendableFileFormatType());
        tsb.append("profile", getProfiles());
        return tsb.toString();
    }

}
