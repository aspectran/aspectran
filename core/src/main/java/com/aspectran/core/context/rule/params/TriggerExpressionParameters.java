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

public class TriggerExpressionParameters extends AbstractParameters {

    public static final ParameterKey startDelaySeconds;
    public static final ParameterKey intervalInMilliseconds;
    public static final ParameterKey intervalInMinutes;
    public static final ParameterKey intervalInSeconds;
    public static final ParameterKey intervalInHours;
    public static final ParameterKey repeatCount;
    public static final ParameterKey repeatForever;
    public static final ParameterKey expression;

    public static final ParameterKey[] parameterKeys;

    static {
        startDelaySeconds = new ParameterKey("startDelaySeconds", ValueType.INT);
        intervalInMilliseconds = new ParameterKey("intervalInMilliseconds", ValueType.LONG);
        intervalInSeconds = new ParameterKey("intervalInSeconds", ValueType.INT);
        intervalInMinutes = new ParameterKey("intervalInMinutes", ValueType.INT);
        intervalInHours = new ParameterKey("intervalInHours", ValueType.INT);
        repeatCount = new ParameterKey("repeatCount", ValueType.INT);
        repeatForever = new ParameterKey("repeatForever", ValueType.BOOLEAN);
        expression = new ParameterKey("expression", ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                startDelaySeconds,
                intervalInMilliseconds,
                intervalInSeconds,
                intervalInMinutes,
                intervalInHours,
                repeatCount,
                repeatForever,
                expression
        };
    }

    public TriggerExpressionParameters() {
        super(parameterKeys);
    }

    public TriggerExpressionParameters(String text) throws IOException {
        this();
        readFrom(text);
    }

    public Integer getStartDelaySeconds() {
        return getInt(startDelaySeconds);
    }

    public Long getIntervalInMilliseconds() {
        return getLong(intervalInMilliseconds);
    }

    public Integer getIntervalInSeconds() {
        return getInt(intervalInSeconds);
    }

    public Integer getIntervalInMinutes() {
        return getInt(intervalInMinutes);
    }

    public Integer getIntervalInHours() {
        return getInt(intervalInHours);
    }

    public Integer getRepeatCount() {
        return getInt(repeatCount);
    }

    public Boolean getRepeatForever() {
        return getBoolean(repeatForever);
    }

    public String getExpression() {
        return getString(expression);
    }

}
