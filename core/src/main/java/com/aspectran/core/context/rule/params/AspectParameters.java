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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Represents the parameters for an aspect rule.
 */
public class AspectParameters extends DefaultParameters {

    public static final ParameterKey description;
    public static final ParameterKey id;
    public static final ParameterKey order;
    public static final ParameterKey isolated;
    public static final ParameterKey disabled;
    public static final ParameterKey joinpoint;
    public static final ParameterKey settings;
    public static final ParameterKey advice;
    public static final ParameterKey exception;

    private static final ParameterKey[] parameterKeys;

    static {
        description = new ParameterKey("description", DescriptionParameters.class, true, true);
        id = new ParameterKey("id", ValueType.STRING);
        order = new ParameterKey("order", ValueType.INT);
        isolated = new ParameterKey("isolated", ValueType.BOOLEAN);
        disabled = new ParameterKey("disabled", ValueType.BOOLEAN);
        joinpoint = new ParameterKey("joinpoint", JoinpointParameters.class);
        settings = new ParameterKey("settings", SettingsParameters.class);
        advice = new ParameterKey("advice", AdviceParameters.class);
        exception = new ParameterKey("exception", ExceptionParameters.class);

        parameterKeys = new ParameterKey[] {
                description,
                id,
                order,
                isolated,
                disabled,
                joinpoint,
                settings,
                advice,
                exception
        };
    }

    public AspectParameters() {
        super(parameterKeys);
    }

}
