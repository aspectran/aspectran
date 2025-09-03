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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.type.AppendableFileFormatType;

/**
 * A specific {@link AppendRule} for importing rules from a classpath resource.
 * 
 * <p>Created: 2017. 05. 06.</p>
 */
public class ResourceAppendRule extends AppendRule {

    /**
     * Instantiates a new ResourceAppendRule.
     * @param resource the classpath resource path
     */
    public ResourceAppendRule(String resource) {
        this(resource, null, null);
    }

    /**
     * Instantiates a new ResourceAppendRule.
     * @param resource the classpath resource path
     * @param profile the profile expression
     */
    public ResourceAppendRule(String resource, String profile) {
        this(resource, null, profile);
    }

    /**
     * Instantiates a new ResourceAppendRule.
     * @param resource the classpath resource path
     * @param format the file format
     */
    public ResourceAppendRule(String resource, AppendableFileFormatType format) {
        this(resource, format, null);
    }

    /**
     * Instantiates a new ResourceAppendRule.
     * @param resource the classpath resource path
     * @param format the file format
     * @param profile the profile expression
     */
    public ResourceAppendRule(String resource, AppendableFileFormatType format, String profile) {
        setResource(resource);
        setFormat(format);
        setProfile(profile);
    }

}
