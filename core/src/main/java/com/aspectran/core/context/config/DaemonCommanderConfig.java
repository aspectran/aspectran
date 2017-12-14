/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

/**
 * <p>Created: 2017. 12. 12.</p>
 *
 * @since 5.1.0
 */
public class DaemonCommanderConfig extends AbstractParameters {

    public static final ParameterDefinition pollingInterval;
    public static final ParameterDefinition inbound;
    public static final ParameterDefinition output;
    public static final ParameterDefinition processed;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        pollingInterval = new ParameterDefinition("pollingInterval", ParameterValueType.LONG);
        inbound = new ParameterDefinition("inbound", ParameterValueType.STRING);
        output = new ParameterDefinition("output", ParameterValueType.STRING);
        processed = new ParameterDefinition("processed", ParameterValueType.STRING);

        parameterDefinitions = new ParameterDefinition[] {
                pollingInterval,
                inbound,
                output,
                processed
        };
    }

    public DaemonCommanderConfig() {
        super(parameterDefinitions);
    }

}
