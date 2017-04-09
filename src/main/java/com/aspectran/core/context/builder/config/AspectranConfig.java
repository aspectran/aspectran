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
import com.aspectran.core.util.apon.Parameters;

public class AspectranConfig extends AbstractParameters {

    public static final ParameterDefinition context;
    public static final ParameterDefinition scheduler;
    public static final ParameterDefinition console;
    public static final ParameterDefinition web;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        context = new ParameterDefinition("context", AspectranContextConfig.class);
        scheduler = new ParameterDefinition("scheduler", AspectranSchedulerConfig.class);
        console = new ParameterDefinition("console", AspectranConsoleConfig.class);
        web = new ParameterDefinition("web", AspectranWebConfig.class);

        parameterDefinitions = new ParameterDefinition[] {
            context,
            scheduler,
            console,
            web
        };
    }

    public AspectranConfig() {
        super(parameterDefinitions);
    }

    public AspectranConfig(String text) {
        super(parameterDefinitions, text);
    }

    public void updateRootContext(String rootContext) {
        Parameters contextParameters = touchParameters(context);
        contextParameters.putValue(AspectranContextConfig.root, rootContext);
    }

    public void updateSchedulerConfig(int startDelaySeconds, boolean waitOnShutdown, boolean startup) {
        Parameters schedulerParameters = touchParameters(scheduler);
        schedulerParameters.putValue(AspectranSchedulerConfig.startDelaySeconds, startDelaySeconds);
        schedulerParameters.putValue(AspectranSchedulerConfig.waitOnShutdown, waitOnShutdown);
        schedulerParameters.putValue(AspectranSchedulerConfig.startup, startup);
    }

}
