/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.builder.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class AspectranContextSessionConfig extends AbstractParameters {

    public static final ParameterDefinition timeout;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        timeout = new ParameterDefinition("timeout", ParameterValueType.INT);

        parameterDefinitions = new ParameterDefinition[] {
            timeout
        };
    }

    public AspectranContextSessionConfig() {
        super(parameterDefinitions);
    }

    public AspectranContextSessionConfig(String text) {
        super(parameterDefinitions, text);
    }

}
