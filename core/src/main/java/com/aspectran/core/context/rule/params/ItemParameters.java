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

public class ItemParameters extends AbstractParameters {

    public static final ParameterDefinition type;
    public static final ParameterDefinition name;
    public static final ParameterDefinition value;
    public static final ParameterDefinition valueType;
    public static final ParameterDefinition tokenize;
    public static final ParameterDefinition mandatory;
    public static final ParameterDefinition secret;
    public static final ParameterDefinition call;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        type = new ParameterDefinition("type", ValueType.STRING);
        name = new ParameterDefinition("name", ValueType.STRING);
        value = new ParameterDefinition("value", ValueType.VARIABLE);
        valueType = new ParameterDefinition("valueType", ValueType.STRING);
        tokenize = new ParameterDefinition("tokenize", ValueType.BOOLEAN);
        mandatory = new ParameterDefinition("mandatory", ValueType.BOOLEAN);
        secret = new ParameterDefinition("secret", ValueType.BOOLEAN);
        call = new ParameterDefinition("call", CallParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                type,
                name,
                value,
                valueType,
                tokenize,
                mandatory,
                secret,
                call
        };
    }

    public ItemParameters() {
        super(parameterDefinitions);
    }

}
