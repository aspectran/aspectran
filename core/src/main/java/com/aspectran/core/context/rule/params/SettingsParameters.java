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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;

public class SettingsParameters extends AbstractParameters {

    public static final ParameterKey setting;

    private static final ParameterKey[] parameterKeys;

    static {
        setting = new ParameterKey("setting", SettingParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                setting
        };
    }

    public SettingsParameters() {
        super(parameterKeys);
    }

    public void putSetting(String name, Object value) {
        SettingParameters settingParameters = newParameters(setting);
        settingParameters.putValue(SettingParameters.name, name);
        settingParameters.putValue(SettingParameters.value, value);
    }

}
