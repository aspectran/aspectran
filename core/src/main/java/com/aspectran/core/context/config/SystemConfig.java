/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

/**
 * @since 6.6.4
 */
public class SystemConfig extends AbstractParameters {

    private static final ParameterKey properties;

    private static final ParameterKey[] parameterKeys;

    static {
        properties = new ParameterKey("properties", VariableParameters.class);

        parameterKeys = new ParameterKey[] {
                properties
        };
    }

    public SystemConfig() {
        super(parameterKeys);
    }

    public String[] getPropertyKeys() {
        Parameters properties = getParameters(SystemConfig.properties);
        if (properties != null) {
            return properties.getParameterNames();
        } else {
            return null;
        }
    }

    public String getProperty(String key) {
        Parameters properties = getParameters(SystemConfig.properties);
        if (properties != null) {
            return properties.getString(key);
        } else {
            return null;
        }
    }

    public void setProperty(String key, String value) {
        Parameters properties = touchParameters(SystemConfig.properties);
        properties.putValue(key, value);
    }

}
