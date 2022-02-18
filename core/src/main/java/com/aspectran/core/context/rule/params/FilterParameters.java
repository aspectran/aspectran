/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class FilterParameters extends AbstractParameters {

    public static final ParameterKey filterClass;
    public static final ParameterKey exclude;

    private static final ParameterKey[] parameterKeys;

    static {
        filterClass = new ParameterKey("class", ValueType.STRING);
        exclude = new ParameterKey("exclude", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                filterClass,
                exclude
        };
    }

    public FilterParameters() {
        super(parameterKeys);
    }

    public String getFilterClass() {
        return getString(filterClass);
    }

    public FilterParameters setFilterClass(String filterClass) {
        putValue(FilterParameters.filterClass, filterClass);
        return this;
    }

    public String[] getExcludePatterns() {
        return getStringArray(exclude);
    }

    public FilterParameters addExcludePattern(String excludePattern) {
        putValue(exclude, excludePattern);
        return this;
    }

}
