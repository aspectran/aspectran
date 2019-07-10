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
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

import java.io.IOException;

public class TriggerParameters extends AbstractParameters {

    public static final ParameterKey type;
    public static final ParameterKey expression;
    public static final ParameterKey startDelaySeconds;
    public static final ParameterKey intervalInMilliseconds;
    public static final ParameterKey intervalInMinutes;
    public static final ParameterKey intervalInSeconds;
    public static final ParameterKey intervalInHours;
    public static final ParameterKey repeatCount;
    public static final ParameterKey repeatForever;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING);
        expression = new ParameterKey("expression", ValueType.STRING);
        startDelaySeconds = new ParameterKey("startDelaySeconds", ValueType.INT);
        intervalInMilliseconds = new ParameterKey("intervalInMilliseconds", ValueType.LONG);
        intervalInMinutes = new ParameterKey("intervalInMinutes", ValueType.INT);
        intervalInSeconds = new ParameterKey("intervalInSeconds", ValueType.INT);
        intervalInHours = new ParameterKey("intervalInHours", ValueType.INT);
        repeatCount = new ParameterKey("repeatCount", ValueType.INT);
        repeatForever = new ParameterKey("repeatForever", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
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
        super(parameterKeys);
    }

    public TriggerParameters(String text) throws IOException {
        this();
        readFrom(text);
    }

}
