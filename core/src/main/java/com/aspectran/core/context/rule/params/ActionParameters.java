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

public class ActionParameters extends AbstractParameters {

    public static final ParameterDefinition caseNo;

    public static final ParameterDefinition id;

    public static final ParameterDefinition bean;
    public static final ParameterDefinition method;
    public static final ParameterDefinition hidden;

    public static final ParameterDefinition arguments;
    public static final ParameterDefinition properties;

    public static final ParameterDefinition include;
    public static final ParameterDefinition parameters;
    public static final ParameterDefinition attributes;

    public static final ParameterDefinition echo;

    public static final ParameterDefinition headers;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        caseNo = new ParameterDefinition("caseNo", ValueType.INT);
        id = new ParameterDefinition("id", ValueType.STRING);
        bean = new ParameterDefinition("bean", ValueType.STRING);
        method = new ParameterDefinition("method", ValueType.STRING);
        hidden = new ParameterDefinition("hidden", ValueType.BOOLEAN);
        arguments = new ParameterDefinition("arguments", ItemHolderParameters.class, true, true);
        properties = new ParameterDefinition("properties", ItemHolderParameters.class, true, true);
        include = new ParameterDefinition("include", ValueType.STRING);
        parameters = new ParameterDefinition("parameters", ItemHolderParameters.class, true, true);
        attributes = new ParameterDefinition("attributes", ItemHolderParameters.class, true, true);
        echo = new ParameterDefinition("echo", ItemHolderParameters.class);
        headers = new ParameterDefinition("headers", ItemHolderParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                caseNo,
                id,
                bean,
                method,
                hidden,
                arguments,
                properties,
                include,
                parameters,
                attributes,
                echo,
                headers
        };
    }

    public ActionParameters() {
        super(parameterDefinitions);
    }

}
