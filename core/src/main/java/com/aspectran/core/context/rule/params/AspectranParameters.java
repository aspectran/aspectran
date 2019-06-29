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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.ValueType;
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
        description = new ParameterDefinition("description", ValueType.TEXT);
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

    public AspectranParameters setDescription(String desc) {
        putValue(description, desc);
        return this;
    }

    public AspectranParameters setTransletNamePattern(String namePattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNamePattern, namePattern);
        return this;
    }

    public AspectranParameters setTransletNamePrefix(String prefixPattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNamePrefix, prefixPattern);
        return this;
    }

    public AspectranParameters setTransletNameSuffix(String suffixPattern) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.transletNameSuffix, suffixPattern);
        return this;
    }

    public AspectranParameters setBeanProxifier(String proxifierName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.beanProxifier, proxifierName);
        return this;
    }

    public AspectranParameters setPointcutPatternVerifiable(boolean verifiable) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.pointcutPatternVerifiable, verifiable);
        return this;
    }

    public AspectranParameters setDefaultTemplateEngineBean(String beanName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.defaultTemplateEngineBean, beanName);
        return this;
    }

    public AspectranParameters setDefaultSchedulerBean(String beanName) {
        DefaultSettingsParameters settingsParameters = touchParameters(settings);
        settingsParameters.putValue(DefaultSettingsParameters.defaultSchedulerBean, beanName);
        return this;
    }

    public AspectranParameters addTypeAlias(String alias, String type) {
        Parameters typeAliasParameters = touchParameters(typeAlias);
        typeAliasParameters.putValue(alias, type);
        return this;
    }

    public AspectranParameters addRule(EnvironmentRule environmentRule) {
        putValue(environment, RulesToParameters.toEnvironmentParameters(environmentRule));
        return this;
    }

    public EnvironmentRule newEnvironmentRule() {
        EnvironmentRule environmentRule = new EnvironmentRule();
        addRule(environmentRule);
        return environmentRule;
    }

    public AspectranParameters addRule(AspectRule aspectRule) {
        putValue(aspect, RulesToParameters.toAspectParameters(aspectRule));
        return this;
    }

    public AspectRule newAspectRule() {
        AspectRule aspectRule = new AspectRule();
        addRule(aspectRule);
        return aspectRule;
    }

    public AspectranParameters addRule(BeanRule beanRule) {
        putValue(bean, RulesToParameters.toBeanParameters(beanRule));
        return this;
    }

    public BeanRule newBeanRule() {
        BeanRule beanRule = new BeanRule();
        addRule(beanRule);
        return beanRule;
    }

    public AspectranParameters addRule(ScheduleRule scheduleRule) {
        putValue(schedule, RulesToParameters.toScheduleParameters(scheduleRule));
        return this;
    }

    public ScheduleRule newScheduleRule() {
        ScheduleRule scheduleRule = new ScheduleRule();
        addRule(scheduleRule);
        return scheduleRule;
    }

    public AspectranParameters addRule(TransletRule transletRule) {
        putValue(translet, RulesToParameters.toTransletParameters(transletRule));
        return this;
    }

    public TransletRule newTransletRule() {
        TransletRule transletRule = new TransletRule();
        addRule(transletRule);
        return transletRule;
    }

    public AspectranParameters addRule(TemplateRule templateRule) {
        putValue(template, RulesToParameters.toTemplateParameters(templateRule));
        return this;
    }

    public TemplateRule newTemplateRule() {
        TemplateRule templateRule = new TemplateRule();
        addRule(templateRule);
        return templateRule;
    }

    public AspectranParameters addRule(AppendRule appendRule) {
        putValue(append, RulesToParameters.toAppendParameters(appendRule));
        return this;
    }

    public AppendRule newAppendRule() {
        AppendRule appendRule = new AppendRule();
        addRule(appendRule);
        return appendRule;
    }

    public AspectranParameters append(AspectranParameters aspectranParameters) {
        return append(aspectranParameters, null);
    }

    public AspectranParameters append(AspectranParameters aspectranParameters, String profile) {
        AppendParameters appendParameters = new AppendParameters();
        appendParameters.putValue(AppendParameters.aspectran, aspectranParameters);
        if (profile != null && !profile.isEmpty()) {
            appendParameters.putValue(AppendParameters.profile, profile);
        }
        putValue(append, appendParameters);
        return this;
    }

    public AspectranParameters newAspectranParameters() {
        return newAspectranParameters(null);
    }

    public AspectranParameters newAspectranParameters(String profile) {
        AspectranParameters aspectranParameters = new AspectranParameters();
        append(aspectranParameters, profile);
        return aspectranParameters;
    }

}
