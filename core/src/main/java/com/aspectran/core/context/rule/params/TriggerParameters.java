/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.ValueType;

import java.io.IOException;

public class TriggerParameters extends AbstractParameters {

    public static final ParameterDefinition type;
    public static final ParameterDefinition expression;
    public static final ParameterDefinition startDelaySeconds;
    public static final ParameterDefinition intervalInMilliseconds;
    public static final ParameterDefinition intervalInMinutes;
    public static final ParameterDefinition intervalInSeconds;
    public static final ParameterDefinition intervalInHours;
    public static final ParameterDefinition repeatCount;
    public static final ParameterDefinition repeatForever;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        type = new ParameterDefinition("type", ValueType.STRING);
        expression = new ParameterDefinition("expression", ValueType.STRING);
        startDelaySeconds = new ParameterDefinition("startDelaySeconds", ValueType.INT);
        intervalInMilliseconds = new ParameterDefinition("intervalInMilliseconds", ValueType.LONG);
        intervalInMinutes = new ParameterDefinition("intervalInMinutes", ValueType.INT);
        intervalInSeconds = new ParameterDefinition("intervalInSeconds", ValueType.INT);
        intervalInHours = new ParameterDefinition("intervalInHours", ValueType.INT);
        repeatCount = new ParameterDefinition("repeatCount", ValueType.INT);
        repeatForever = new ParameterDefinition("repeatForever", ValueType.BOOLEAN);

        parameterDefinitions = new ParameterDefinition[] {
                type,
                expression,
                startDelaySeconds,
                intervalInMilliseconds,
                intervalInMinutes,
                intervalInSeconds,
                intervalInHours,
                repeatCount,
                repeatForever
        };
    }

    public TriggerParameters() {
        super(parameterDefinitions);
    }

    public TriggerParameters(String text) throws IOException {
        this();
        readFrom(text);
    }

}
