/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

public class ScheduleParameters extends AbstractParameters {

    public static final ParameterKey description;
    public static final ParameterKey id;
    public static final ParameterKey scheduler;
    public static final ParameterKey job;

    private static final ParameterKey[] parameterKeys;

    static {
        description = new ParameterKey("description", DescriptionParameters.class, true, true);
        id = new ParameterKey("id", ValueType.STRING);
        scheduler = new ParameterKey("scheduler", SchedulerParameters.class);
        job = new ParameterKey("job", ScheduledJobParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                description,
                id,
                scheduler,
                job
        };
    }

    public ScheduleParameters() {
        super(parameterKeys);
    }

}
