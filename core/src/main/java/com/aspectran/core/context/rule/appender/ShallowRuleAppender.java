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

import java.io.IOException;
import java.io.InputStream;

/**
 * A concrete implementation of {@link AbstractRuleAppender} that provides a
 * shallow appending mechanism for rules. This appender does not supply any
 * specific functionality for rule processing, and its methods return
 * placeholder or null values.
 * <p>This class is used as a placeholder or for cases where no rule appending
 * behavior is required in the context.</p>
 */
public class ShallowRuleAppender extends AbstractRuleAppender {

    /**
     * Constructs an instance of AbstractRuleAppender.
     */
    public ShallowRuleAppender() {
        super(null);
    }

    @Override
    public String getQualifiedName() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

}
