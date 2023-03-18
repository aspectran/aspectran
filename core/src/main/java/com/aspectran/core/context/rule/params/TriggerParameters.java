/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

public class TriggerParameters extends AbstractParameters {

    public static final ParameterKey type;
    public static final ParameterKey expression;

    private static final ParameterKey[] parameterKeys;

    static {
        type = new ParameterKey("type", ValueType.STRING);
        expression = new ParameterKey("expression", new String[] {"trigger"}, TriggerExpressionParameters.class);

        parameterKeys = new ParameterKey[] {
                type,
                expression
        };
    }

    public TriggerParameters() {
        super(parameterKeys);
    }

}
