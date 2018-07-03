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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class PointcutParameters extends AbstractParameters {

    public static final ParameterDefinition type;
    public static final ParameterDefinition plus;
    public static final ParameterDefinition minus;
    public static final ParameterDefinition include;
    public static final ParameterDefinition exclude;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        type = new ParameterDefinition("type", ParameterValueType.STRING);
        plus = new ParameterDefinition("+", ParameterValueType.STRING, true, true);
        minus = new ParameterDefinition("-", ParameterValueType.STRING, true, true);
        include = new ParameterDefinition("include", PointcutQualifierParameters.class, true, true);
        exclude = new ParameterDefinition("exclude", PointcutQualifierParameters.class, true, true);

        parameterDefinitions = new ParameterDefinition[] {
                type,
                plus,
                minus,
                include,
                exclude
        };
    }

    public PointcutParameters() {
        super(parameterDefinitions);
    }

    public void addIncludePattern(String pattern) {
        putValue(plus, pattern);
    }

    public void addExcludePattern(String pattern) {
        putValue(minus, pattern);
    }

}
