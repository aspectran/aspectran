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

public class AspectranContextConfig extends AbstractParameters {

    public static final ParameterDefinition base;
    public static final ParameterDefinition root;
    public static final ParameterDefinition encoding;
    public static final ParameterDefinition resources;
    public static final ParameterDefinition profiles;
    public static final ParameterDefinition hybridLoad;
    public static final ParameterDefinition autoReload;
    public static final ParameterDefinition parameters;

    private final static ParameterDefinition[] parameterDefinitions;

    static {
        base = new ParameterDefinition("base", ParameterValueType.STRING);
        root = new ParameterDefinition("root", ParameterValueType.STRING);
        encoding = new ParameterDefinition("encoding", ParameterValueType.STRING);
        resources = new ParameterDefinition("resources", ParameterValueType.STRING, true);
        profiles = new ParameterDefinition("profiles", AspectranContextProfilesConfig.class);
        hybridLoad = new ParameterDefinition("hybridLoad", ParameterValueType.BOOLEAN);
        autoReload = new ParameterDefinition("autoReload", AspectranContextAutoReloadConfig.class);
        parameters = new ParameterDefinition("parameters", ParameterValueType.PARAMETERS);

        parameterDefinitions = new ParameterDefinition[] {
            base,
            root,
            encoding,
            resources,
            profiles,
            hybridLoad,
            autoReload,
            parameters
        };
    }

    public AspectranContextConfig() {
        super(parameterDefinitions);
    }

    public AspectranContextConfig(String text) {
        super(parameterDefinitions, text);
    }

}
