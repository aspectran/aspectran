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
package com.aspectran.core.context.loader.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class AspectranWebConfig extends AbstractParameters {

    public static final ParameterDefinition exposals;
    public static final ParameterDefinition uriDecoding;
    public static final ParameterDefinition defaultServletName;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        exposals = new ParameterDefinition("exposals", ParameterValueType.STRING, true);
        uriDecoding = new ParameterDefinition("uriDecoding", ParameterValueType.STRING);
        defaultServletName = new ParameterDefinition("defaultServletName", ParameterValueType.STRING);

        parameterDefinitions = new ParameterDefinition[] {
            exposals,
            uriDecoding,
            defaultServletName
        };
    }

    public AspectranWebConfig() {
        super(parameterDefinitions);
    }

    public AspectranWebConfig(String text) {
        super(parameterDefinitions, text);
    }

}
