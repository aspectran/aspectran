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
package com.aspectran.core.context.rule;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class SettingsAdviceRule.
 */
public class SettingsAdviceRule {

    private final AspectRule aspectRule;

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
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Setting name can not be null");
        }
        if (settings == null) {
            settings = new LinkedHashMap<>();
        }
        settings.put(name, value);
    }

    public static SettingsAdviceRule newInstance(AspectRule aspectRule) {
        return new SettingsAdviceRule(aspectRule);
    }

    public static SettingsAdviceRule newInstance(AspectRule aspectRule, String text)
            throws IOException {
        if (StringUtils.hasText(text)) {
            Parameters settingsParameters = new VariableParameters(text);
            return newInstance(aspectRule, settingsParameters);
        } else {
            return newInstance(aspectRule, (Parameters)null);
        }
    }

    public static SettingsAdviceRule newInstance(AspectRule aspectRule, Parameters settingsParameters) {
        SettingsAdviceRule sar = new SettingsAdviceRule(aspectRule);
        updateSettingsAdviceRule(sar, settingsParameters);
        return sar;
    }

    public static void updateSettingsAdviceRule(SettingsAdviceRule sar, String text) throws IllegalRuleException {
        if (StringUtils.hasText(text)) {
            Parameters settingsParameters;
            try {
                settingsParameters = new VariableParameters(text);
            } catch (IOException e) {
                throw new IllegalRuleException("Settings parameter can not be parsed", e);
            }
            updateSettingsAdviceRule(sar, settingsParameters);
        }
    }

    public static void updateSettingsAdviceRule(SettingsAdviceRule sar, Parameters settingsParameters) {
        if (settingsParameters != null) {
            Set<String> parametersNames = settingsParameters.getParameterNameSet();
            if (parametersNames != null) {
                for (String name : parametersNames) {
                    sar.putSetting(name, settingsParameters.getString(name));
                }
            }
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (aspectRule != null) {
            tsb.append("aspectId", aspectRule.getId());
        }
        tsb.append("settings", settings);
        return tsb.toString();
    }

}
