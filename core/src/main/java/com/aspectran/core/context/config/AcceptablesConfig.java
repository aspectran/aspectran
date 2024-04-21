/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class AcceptablesConfig extends AbstractParameters {

    private static final ParameterKey plus;
    private static final ParameterKey minus;

    private static final ParameterKey[] parameterKeys;

    static {
        plus = new ParameterKey("+", ValueType.STRING, true, true);
        minus = new ParameterKey("-", ValueType.STRING, true, true);

        parameterKeys = new ParameterKey[] {
                plus,
                minus
        };
    }

    public AcceptablesConfig() {
        super(parameterKeys);
    }

    public String[] getIncludePatterns() {
        return getStringArray(plus);
    }

    public AcceptablesConfig addIncludePattern(String pattern) {
        putValue(plus, pattern);
        return this;
    }

    public String[] getExcludePatterns() {
        return getStringArray(minus);
    }

    public AcceptablesConfig addExcludePattern(String pattern) {
        putValue(minus, pattern);
        return this;
    }

}
