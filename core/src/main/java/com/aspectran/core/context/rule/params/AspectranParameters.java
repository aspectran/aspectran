/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RuleToParamsConverter;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

public class AspectranParameters extends AbstractParameters {

    public static final ParameterDefinition description;
    public static final ParameterDefinition settings;
    public static final ParameterDefinition environment;
    public static final ParameterDefinition typeAlias;
    public static final ParameterDefinition aspect;
    public static final ParameterDefinition bean;
    public static final ParameterDefinition schedule;
    public static final ParameterDefinition translet;
    public static final ParameterDefinition template;
    public static final ParameterDefinition append;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        description = new ParameterDefinition("description", ParameterValueType.TEXT);
        settings = new ParameterDefinition("settings", DefaultSettingsParameters.class);
        typeAlias = new ParameterDefinition("typeAlias", VariableParameters.class);
        environment = new ParameterDefinition("environment", EnvironmentParameters.class, true, true);
        aspect = new ParameterDefinition("aspect", AspectParameters.class, true, true);
        bean = new ParameterDefinition("bean", BeanParameters.class, true, true);
        schedule = new ParameterDefinition("schedule", ScheduleParameters.class, true, true);
        translet = new ParameterDefinition("translet", TransletParameters.class, true, true);
        template = new ParameterDefinition("template", TemplateParameters.class, true, true);
        append = new ParameterDefinition("append", AppendParameters.class, true, true);

        parameterDefinitions = new ParameterDefinition[] {
                description,
                settings,
                typeAlias,
                environment,
                aspect,
                bean,
                schedule,
                translet,
                template,
                append
        };
    }

    public AspectranParameters() {
        super(parameterDefinitions);
    }

    public void setDescription(String desc) {
        putValue(description, desc);
    }

    public void setTransletNamePattern(String namePattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNamePattern, namePattern);
    }

    public void setTransletNamePrefix(String prefixPattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNamePrefix, prefixPattern);
    }

    public void setTransletNameSuffix(String suffixPattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNameSuffix, suffixPattern);
    }

    public void setTransletInterfaceClass(Class<? extends Translet> transletInterfaceClass) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletInterfaceClass, transletInterfaceClass.getName());
    }

    public void setTransletImplementationClass(Class<? extends CoreTranslet> transletImplementationClass) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletImplementationClass, transletImplementationClass.getName());
    }

    public void setBeanProxifier(String proxifierName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.beanProxifier, proxifierName);
    }

    public void setPointcutPatternVerifiable(boolean verifiable) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.pointcutPatternVerifiable, verifiable);
    }

    public void setDefaultTemplateEngineBean(String beanName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.defaultTemplateEngineBean, beanName);
    }

    public void setDefaultSchedulerBean(String beanName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.defaultSchedulerBean, beanName);
    }

    public void addTypeAlias(String alias, String type) {
        Parameters typeAliasParameters = touchParameters(typeAlias);
        typeAliasParameters.putValue(alias, type);
    }

    public void addRule(EnvironmentRule environmentRule) {
        putValue(environment, RuleToParamsConverter.toEnvironmentParameters(environmentRule));
    }

    public void addRule(AspectRule aspectRule) {
        putValue(aspect, RuleToParamsConverter.toAspectParameters(aspectRule));
    }

    public void addRule(BeanRule beanRule) {
        putValue(bean, RuleToParamsConverter.toBeanParameters(beanRule));
    }

    public void addRule(ScheduleRule scheduleRule) {
        putValue(schedule, RuleToParamsConverter.toScheduleParameters(scheduleRule));
    }

    public void addRule(TransletRule transletRule) {
        putValue(translet, RuleToParamsConverter.toTransletParameters(transletRule));
    }

    public void addRule(TemplateRule templateRule) {
        putValue(template, RuleToParamsConverter.toTemplateParameters(templateRule));
    }

    public void addRule(AppendRule appendRule) {
        putValue(append, RuleToParamsConverter.toAppendParameters(appendRule));
    }

    public void addRule(AspectranParameters aspectranParameters) {
        addRule(aspectranParameters, null);
    }

    public void addRule(AspectranParameters aspectranParameters, String profile) {
        AppendRule appendRule = new AppendRule();
        appendRule.setAspectranParameters(aspectranParameters);
        if (profile != null && !profile.isEmpty()) {
            appendRule.setProfile(profile);
        }
        addRule(appendRule);
    }

}
