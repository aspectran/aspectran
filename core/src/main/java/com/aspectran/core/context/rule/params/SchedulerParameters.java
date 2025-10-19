/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Represents the parameters for a scheduler within a schedule rule.
 */
public class SchedulerParameters extends AbstractParameters {

    public static final ParameterKey bean;
    public static final ParameterKey trigger;

    private static final ParameterKey[] parameterKeys;

    static {
        bean = new ParameterKey("bean", ValueType.STRING);
        trigger = new ParameterKey("trigger", TriggerParameters.class);

        parameterKeys = new ParameterKey[] {
                bean,
                trigger
        };
    }

    public SchedulerParameters() {
        super(parameterKeys);
    }

}
