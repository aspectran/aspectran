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
package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.VariableParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class AspectranParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition settings;
    public static final ParameterDefinition environments;
    public static final ParameterDefinition typeAlias;
    public static final ParameterDefinition aspects;
    public static final ParameterDefinition beans;
    public static final ParameterDefinition schedules;
    public static final ParameterDefinition translets;
    public static final ParameterDefinition templates;
    public static final ParameterDefinition imports;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        description = new ParameterDefinition("description", ParameterValueType.TEXT);
        settings = new ParameterDefinition("settings", DefaultSettingsParameters.class);
        environments = new ParameterDefinition("environment", EnvironmentParameters.class, true, true);
        typeAlias = new ParameterDefinition("typeAlias", VariableParameters.class);
        aspects = new ParameterDefinition("aspect", AspectParameters.class, true, true);
        beans = new ParameterDefinition("bean", BeanParameters.class, true, true);
        schedules = new ParameterDefinition("schedule", ScheduleParameters.class, true, true);
        translets = new ParameterDefinition("translet", TransletParameters.class, true, true);
        templates = new ParameterDefinition("template", TemplateParameters.class, true, true);
        imports = new ParameterDefinition("import", ImportParameters.class, true, true);

        parameterDefinitions = new ParameterDefinition[] {
            description,
            settings,
            environments,
            typeAlias,
            aspects,
            beans,
            schedules,
            translets,
            templates,
            imports
        };
    }

    public AspectranParameters() {
        super(parameterDefinitions);
    }

    public AspectranParameters(String text) {
        super(parameterDefinitions, text);
    }

}
