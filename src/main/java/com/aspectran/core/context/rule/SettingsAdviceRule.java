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
package com.aspectran.core.context.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.VariableParameters;
import com.aspectran.core.util.apon.Parameters;

/**
 * The Class SettingsAdviceRule.
 */
public class SettingsAdviceRule {

    private final AspectRule aspectRule;

    private final AspectAdviceType aspectAdviceType = AspectAdviceType.SETTINGS;

    private Map<String, Object> settings;

    public SettingsAdviceRule(AspectRule aspectRule) {
        this.aspectRule = aspectRule;
    }

    public String getAspectId() {
        return aspectRule.getId();
    }

    public AspectRule getAspectRule() {
        return aspectRule;
    }

    public AspectAdviceType getAspectAdviceType() {
        return aspectAdviceType;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSetting(String name) {
        return (T)settings.get(name);
    }

    public void putSetting(String name, Object value) {
        if (settings == null) {
            settings = new HashMap<String, Object>(5);
        }
        settings.put(name, value);
    }

    public static SettingsAdviceRule newInstance(AspectRule aspectRule, String text) {
        if (StringUtils.hasText(text)) {
            Parameters settingsParameters = new VariableParameters(text);
            return newInstance(aspectRule, settingsParameters);
        } else {
            return newInstance(aspectRule, (Parameters)null);
        }
    }

    public static SettingsAdviceRule newInstance(AspectRule aspectRule, Parameters settingsParameters) {
        SettingsAdviceRule sar = new SettingsAdviceRule(aspectRule);
        if (settingsParameters != null) {
            Set<String> parametersNames = settingsParameters.getParameterNameSet();
            if (parametersNames != null) {
                for (String name : parametersNames) {
                    sar.putSetting(name, settingsParameters.getString(name));
                }
            }
        }

        return sar;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (aspectRule != null) {
            tsb.append("aspectId", aspectRule.getId());
        }
        tsb.append("aspectAdviceType", aspectAdviceType);
        tsb.append("settings", settings);
        return tsb.toString();
    }

}
