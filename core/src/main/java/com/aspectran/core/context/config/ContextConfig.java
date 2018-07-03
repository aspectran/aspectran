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
package com.aspectran.core.context.config;

import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class ContextConfig extends AbstractParameters {

    public static final ParameterDefinition base;
    public static final ParameterDefinition root;
    public static final ParameterDefinition encoding;
    public static final ParameterDefinition resources;
    public static final ParameterDefinition profiles;
    public static final ParameterDefinition hybridLoad;
    public static final ParameterDefinition autoReload;
    public static final ParameterDefinition singleton;
    public static final ParameterDefinition parameters;

    private final static ParameterDefinition[] parameterDefinitions;

    static {
        base = new ParameterDefinition("base", ParameterValueType.STRING);
        root = new ParameterDefinition("root", ParameterValueType.STRING);
        encoding = new ParameterDefinition("encoding", ParameterValueType.STRING);
        resources = new ParameterDefinition("resources", ParameterValueType.STRING, true);
        profiles = new ParameterDefinition("profiles", ContextProfilesConfig.class);
        hybridLoad = new ParameterDefinition("hybridLoad", ParameterValueType.BOOLEAN);
        autoReload = new ParameterDefinition("autoReload", ContextAutoReloadConfig.class);
        singleton = new ParameterDefinition("singleton", ParameterValueType.BOOLEAN);
        parameters = new ParameterDefinition("parameters", AspectranParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                base,
                root,
                encoding,
                resources,
                profiles,
                hybridLoad,
                autoReload,
                singleton,
                parameters
        };
    }

    public ContextConfig() {
        super(parameterDefinitions);
    }

}
