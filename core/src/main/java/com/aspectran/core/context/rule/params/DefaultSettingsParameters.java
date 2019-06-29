/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ValueType;

public class DefaultSettingsParameters extends AbstractParameters {

    public static final ParameterDefinition transletNamePattern;
    public static final ParameterDefinition transletNamePrefix;
    public static final ParameterDefinition transletNameSuffix;
    public static final ParameterDefinition beanProxifier;
    public static final ParameterDefinition pointcutPatternVerifiable;
    public static final ParameterDefinition defaultTemplateEngineBean;
    public static final ParameterDefinition defaultSchedulerBean;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        transletNamePattern = new ParameterDefinition(DefaultSettingType.TRANSLET_NAME_PATTERN.toString(), ValueType.STRING);
        transletNamePrefix = new ParameterDefinition(DefaultSettingType.TRANSLET_NAME_PREFIX.toString(), ValueType.STRING);
        transletNameSuffix = new ParameterDefinition(DefaultSettingType.TRANSLET_NAME_SUFFIX.toString(), ValueType.STRING);
        beanProxifier = new ParameterDefinition(DefaultSettingType.BEAN_PROXIFIER.toString(), ValueType.STRING);
        pointcutPatternVerifiable = new ParameterDefinition(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE.toString(), ValueType.BOOLEAN);
        defaultTemplateEngineBean = new ParameterDefinition(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN.toString(), ValueType.STRING);
        defaultSchedulerBean = new ParameterDefinition(DefaultSettingType.DEFAULT_SCHEDULER_BEAN.toString(), ValueType.STRING);

        parameterDefinitions = new ParameterDefinition[] {
                transletNamePattern,
                transletNamePrefix,
                transletNameSuffix,
                beanProxifier,
                pointcutPatternVerifiable,
                defaultTemplateEngineBean,
                defaultSchedulerBean
        };
    }

    public DefaultSettingsParameters() {
        super(parameterDefinitions);
    }

}
