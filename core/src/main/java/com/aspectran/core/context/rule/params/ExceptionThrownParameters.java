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

public class ExceptionThrownParameters extends AbstractParameters {

    public static final ParameterDefinition type;
    public static final ParameterDefinition action;
    public static final ParameterDefinition transform;
    public static final ParameterDefinition dispatch;
    public static final ParameterDefinition forward;
    public static final ParameterDefinition redirect;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        type = new ParameterDefinition("type", ParameterValueType.STRING, true, true);
        action = new ParameterDefinition("action", ActionParameters.class);
        transform = new ParameterDefinition("transform", TransformParameters.class, true, true);
        dispatch = new ParameterDefinition("dispatch", DispatchParameters.class, true, true);
        forward = new ParameterDefinition("forward", ForwardParameters.class, true, true);
        redirect = new ParameterDefinition("redirect", RedirectParameters.class, true, true);

        parameterDefinitions = new ParameterDefinition[] {
                type,
                action,
                transform,
                dispatch,
                forward,
                redirect
        };
    }

    public ExceptionThrownParameters() {
        super(parameterDefinitions);
    }

}
