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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.params.SettingParameters;
import com.aspectran.core.context.rule.params.SettingsParameters;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A specialized advice rule for injecting configuration settings into a bean.
 * This allows aspects to provide configuration to other components.
 *
 * <p>Created: 2016. 03. 26.</p>
 */
public class SettingsAdviceRule {

    private final AspectRule aspectRule;

    private Map<String, Object> settings;

    /**
     * Instantiates a new SettingsAdviceRule.
     * @param aspectRule the parent aspect rule
     */
    public SettingsAdviceRule(AspectRule aspectRule) {
        if (aspectRule == null) {
            throw new IllegalArgumentException("aspectRule must not be null");
        }
        this.aspectRule = aspectRule;
    }

    /**
     * Gets the ID of the parent aspect.
     * @return the aspect ID
     */
    public String getAspectId() {
        return aspectRule.getId();
    }

    /**
     * Gets the parent aspect rule.
     * @return the aspect rule
     */
    public AspectRule getAspectRule() {
        return aspectRule;
    }

    /**
     * Gets the map of settings.
     * @return the settings map
     */
    public Map<String, Object> getSettings() {
        return settings;
    }

    /**
     * Sets the map of settings.
     * @param settings the settings map
     */
    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    /**
     * Gets a specific setting by name.
     * @param name the name of the setting
     * @param <T> the type of the setting value
     * @return the setting value
     */
    @SuppressWarnings("unchecked")
    public <T> T getSetting(String name) {
        return (T)settings.get(name);
    }

    /**
     * Adds or updates a setting.
     * @param name the name of the setting
     * @param value the value of the setting
     */
    public void putSetting(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Setting name can not be null");
        }
        if (settings == null) {
            settings = new LinkedHashMap<>();
        }
        settings.put(name, value);
    }

    /**
     * Creates a new instance of SettingsAdviceRule.
     * @param aspectRule the parent aspect rule
     * @return a new SettingsAdviceRule instance
     */
    @NonNull
    public static SettingsAdviceRule newInstance(AspectRule aspectRule) {
        return new SettingsAdviceRule(aspectRule);
    }

    /**
     * Creates a new instance of SettingsAdviceRule from parameters.
     * @param aspectRule the parent aspect rule
     * @param settingsParameters the settings parameters
     * @return a new SettingsAdviceRule instance
     */
    @NonNull
    public static SettingsAdviceRule newInstance(AspectRule aspectRule, SettingsParameters settingsParameters) {
        SettingsAdviceRule sar = new SettingsAdviceRule(aspectRule);
        updateSettingsAdviceRule(sar, settingsParameters);
        return sar;
    }

    /**
     * Updates a SettingsAdviceRule from an APON string.
     * @param sar the SettingsAdviceRule to update
     * @param apon the APON string containing settings
     * @throws IllegalRuleException if the APON string is invalid
     */
    public static void updateSettingsAdviceRule(SettingsAdviceRule sar, String apon) throws IllegalRuleException {
        if (StringUtils.hasText(apon)) {
            SettingsParameters settingsParameters = new SettingsParameters();
            try {
                Parameters parameters = new VariableParameters(apon);
                for (String name : parameters.getParameterNames()) {
                    settingsParameters.putSetting(name, parameters.getValue(name));
                }
            } catch (IOException e) {
                throw new IllegalRuleException("Settings parameter can not be parsed", e);
            }
            updateSettingsAdviceRule(sar, settingsParameters);
        }
    }

    private static void updateSettingsAdviceRule(SettingsAdviceRule sar, SettingsParameters settingsParameters) {
        if (settingsParameters != null) {
            List<SettingParameters> settingParametersList = settingsParameters.getParametersList(SettingsParameters.setting);
            if (settingParametersList != null) {
                for (SettingParameters settingParameters : settingParametersList) {
                    sar.putSetting(settingParameters.getName(), settingParameters.getValue());
                }
            }
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (settings != null) {
            tsb.append("settings", settings.keySet());
        }
        return tsb.toString();
    }

}
