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
import com.aspectran.core.util.apon.VariableParameters;

public class AspectParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition id;
    public static final ParameterDefinition order;
    public static final ParameterDefinition isolated;
    public static final ParameterDefinition joinpoint;
    public static final ParameterDefinition settings;
    public static final ParameterDefinition advice;
    public static final ParameterDefinition exception;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        description = new ParameterDefinition("description", ParameterValueType.TEXT);
        id = new ParameterDefinition("id", ParameterValueType.STRING);
        order = new ParameterDefinition("order", ParameterValueType.INT);
        isolated = new ParameterDefinition("isolated", ParameterValueType.BOOLEAN);
        joinpoint = new ParameterDefinition("joinpoint", JoinpointParameters.class);
        settings = new ParameterDefinition("settings", VariableParameters.class);
        advice = new ParameterDefinition("advice", AdviceParameters.class);
        exception = new ParameterDefinition("exception", ExceptionParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                description,
                id,
                order,
                isolated,
                joinpoint,
                settings,
                advice,
                exception
        };
    }

    public AspectParameters() {
        super(parameterDefinitions);
    }

}
