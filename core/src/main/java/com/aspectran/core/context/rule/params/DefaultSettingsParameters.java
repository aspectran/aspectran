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
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class DefaultSettingsParameters extends AbstractParameters {

    public static final ParameterKey transletNamePattern;
    public static final ParameterKey transletNamePrefix;
    public static final ParameterKey transletNameSuffix;
    public static final ParameterKey beanProxifier;
    public static final ParameterKey pointcutPatternVerifiable;
    public static final ParameterKey defaultTemplateEngineBean;
    public static final ParameterKey defaultSchedulerBean;

    private static final ParameterKey[] parameterKeys;

    static {
        transletNamePattern = new ParameterKey(DefaultSettingType.TRANSLET_NAME_PATTERN.toString(), ValueType.STRING);
        transletNamePrefix = new ParameterKey(DefaultSettingType.TRANSLET_NAME_PREFIX.toString(), ValueType.STRING);
        transletNameSuffix = new ParameterKey(DefaultSettingType.TRANSLET_NAME_SUFFIX.toString(), ValueType.STRING);
        beanProxifier = new ParameterKey(DefaultSettingType.BEAN_PROXIFIER.toString(), ValueType.STRING);
        pointcutPatternVerifiable = new ParameterKey(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE.toString(), ValueType.BOOLEAN);
        defaultTemplateEngineBean = new ParameterKey(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN.toString(), ValueType.STRING);
        defaultSchedulerBean = new ParameterKey(DefaultSettingType.DEFAULT_SCHEDULER_BEAN.toString(), ValueType.STRING);

        parameterKeys = new ParameterKey[] {
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
        super(parameterKeys);
    }

}
