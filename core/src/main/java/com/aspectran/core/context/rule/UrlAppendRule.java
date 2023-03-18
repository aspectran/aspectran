/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.AppendableFileFormatType;

/**
 * Defines a rule to append a URL with defined rules.
 * 
 * <p>Created: 2017. 05. 06.</p>
 */
public class UrlAppendRule extends AppendRule {

    public UrlAppendRule(String url) {
        this(url, null, null);
    }

    public UrlAppendRule(String url, String profile) {
        this(url, null, profile);
    }

    public UrlAppendRule(String url, AppendableFileFormatType format) {
        this(url, format, null);
    }

    public UrlAppendRule(String url, AppendableFileFormatType format, String profile) {
        setUrl(url);
        setFormat(format);
        setProfile(profile);
    }

}
