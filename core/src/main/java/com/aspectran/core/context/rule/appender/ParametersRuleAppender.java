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

import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.core.context.rule.type.AppenderType;
import com.aspectran.utils.ToStringBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * A {@link RuleAppender} for appending rules from {@link com.aspectran.core.context.rule.params.AspectranParameters}.
 *
 * <p>Created: 2017. 05. 09.</p>
 */
public class ParametersRuleAppender extends AbstractRuleAppender {

    /**
     * Instantiates a new ParametersRuleAppender.
     */
    public ParametersRuleAppender() {
        super(AppenderType.PARAMETERS);
        setAppendableFileFormatType(AppendableFileFormatType.APON);
    }

    @Override
    public String getQualifiedName() {
        return null;
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader(String encoding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("type", getAppenderType());
        tsb.append("format", getAppendableFileFormatType());
        tsb.append("profile", getProfiles());
        return tsb.toString();
    }

}
