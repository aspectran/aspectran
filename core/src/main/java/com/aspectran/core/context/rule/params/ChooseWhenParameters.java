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
import com.aspectran.core.util.apon.ParameterValueType;

public class ChooseWhenParameters extends AbstractParameters {

    public static final ParameterDefinition caseNo;
    public static final ParameterDefinition test;
    public static final ParameterDefinition transform;
    public static final ParameterDefinition dispatch;
    public static final ParameterDefinition redirect;
    public static final ParameterDefinition forward;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        caseNo = new ParameterDefinition("caseNo", ParameterValueType.INT);
        test = new ParameterDefinition("test", ParameterValueType.STRING);
        transform = new ParameterDefinition("transform", TransformParameters.class);
        dispatch = new ParameterDefinition("dispatch", DispatchParameters.class);
        redirect = new ParameterDefinition("redirect", RedirectParameters.class);
        forward = new ParameterDefinition("forward", ForwardParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                caseNo,
                test,
                transform,
                dispatch,
                redirect,
                forward
        };
    }

    public ChooseWhenParameters() {
        super(parameterDefinitions);
    }

}
