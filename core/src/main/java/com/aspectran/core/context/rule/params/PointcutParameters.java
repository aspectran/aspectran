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

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.AponParseException;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class PointcutParameters extends AbstractParameters {

    public static final ParameterKey type;
    public static final ParameterKey plus;
    public static final ParameterKey minus;
    public static final ParameterKey include;
    public static final ParameterKey exclude;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING); // "wildcard" or "regexp"
        plus = new ParameterKey("+", ValueType.STRING, true, true);
        minus = new ParameterKey("-", ValueType.STRING, true, true);
        include = new ParameterKey("include", PointcutQualifierParameters.class, true, true);
        exclude = new ParameterKey("exclude", PointcutQualifierParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                type,
                plus,
                minus,
                include,
                exclude
        };
    }

    public PointcutParameters() {
        super(parameterKeys);
    }

    public PointcutParameters(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    public void addIncludePattern(String pattern) {
        putValue(plus, pattern);
    }

    public void addExcludePattern(String pattern) {
        putValue(minus, pattern);
    }

}
