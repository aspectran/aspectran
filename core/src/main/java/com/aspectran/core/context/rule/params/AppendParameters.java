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

public class AppendParameters extends AbstractParameters {

    public static final ParameterDefinition file;
    public static final ParameterDefinition resource;
    public static final ParameterDefinition url;
    public static final ParameterDefinition format;
    public static final ParameterDefinition profile;
    public static final ParameterDefinition aspectran;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        file = new ParameterDefinition("file", ParameterValueType.STRING);
        resource = new ParameterDefinition("resource", ParameterValueType.STRING);
        url = new ParameterDefinition("url", ParameterValueType.STRING);
        format = new ParameterDefinition("format", ParameterValueType.STRING);
        profile = new ParameterDefinition("profile", ParameterValueType.STRING);
        aspectran = new ParameterDefinition("aspectran", AspectranParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                file,
                resource,
                url,
                format,
                profile,
                aspectran
        };
    }

    public AppendParameters() {
        super(parameterDefinitions);
    }

}
