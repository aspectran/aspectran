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

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class WebConfig extends AbstractParameters {

    public static final ParameterDefinition uriDecoding;
    public static final ParameterDefinition defaultServletName;
    public static final ParameterDefinition exposals;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        uriDecoding = new ParameterDefinition("uriDecoding", ParameterValueType.STRING);
        defaultServletName = new ParameterDefinition("defaultServletName", ParameterValueType.STRING);
        exposals = new ParameterDefinition("exposals", ExposalsConfig.class);

        parameterDefinitions = new ParameterDefinition[] {
                uriDecoding,
                defaultServletName,
                exposals
        };
    }

    public WebConfig() {
        super(parameterDefinitions);
    }

    public ExposalsConfig newExposalsConfig() {
        return newParameters(exposals);
    }

    public ExposalsConfig touchExposalsConfig() {
        return touchParameters(exposals);
    }

    public ExposalsConfig getExposalsConfig() {
        return getParameters(exposals);
    }

    public void putExposalsConfig(ExposalsConfig exposalsConfig) {
        putValue(exposals, exposalsConfig);
    }

}
