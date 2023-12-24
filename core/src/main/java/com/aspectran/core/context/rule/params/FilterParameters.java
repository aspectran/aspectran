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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

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

    public FilterParameters setExcludePattern(String[] excludePatterns) {
        removeValue(exclude);
        putValue(exclude, excludePatterns);
        return this;
    }

    public FilterParameters addExcludePattern(String excludePattern) {
        putValue(exclude, excludePattern);
        return this;
    }

}
