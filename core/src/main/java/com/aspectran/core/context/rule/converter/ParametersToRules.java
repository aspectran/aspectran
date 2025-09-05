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
package com.aspectran.core.context.rule.converter;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.AdviceRule;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.core.context.rule.ability.HasResponseRules;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.params.ActionParameters;
import com.aspectran.core.context.rule.params.AdviceActionParameters;
import com.aspectran.core.context.rule.params.AdviceParameters;
import com.aspectran.core.context.rule.params.AppendParameters;
import com.aspectran.core.context.rule.params.AspectParameters;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.BeanParameters;
import com.aspectran.core.context.rule.params.ChooseWhenParameters;
import com.aspectran.core.context.rule.params.ContentParameters;
import com.aspectran.core.context.rule.params.ContentsParameters;
import com.aspectran.core.context.rule.params.DescriptionParameters;
import com.aspectran.core.context.rule.params.DispatchParameters;
import com.aspectran.core.context.rule.params.EntryParameters;
import com.aspectran.core.context.rule.params.EnvironmentParameters;
import com.aspectran.core.context.rule.params.ExceptionParameters;
import com.aspectran.core.context.rule.params.ExceptionThrownParameters;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.params.ForwardParameters;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.context.rule.params.JoinpointParameters;
import com.aspectran.core.context.rule.params.RedirectParameters;
import com.aspectran.core.context.rule.params.RequestParameters;
import com.aspectran.core.context.rule.params.ResponseParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.params.ScheduleParameters;
import com.aspectran.core.context.rule.params.ScheduledJobParameters;
import com.aspectran.core.context.rule.params.SchedulerParameters;
import com.aspectran.core.context.rule.params.SettingParameters;
import com.aspectran.core.context.rule.params.SettingsParameters;
import com.aspectran.core.context.rule.params.TemplateParameters;
import com.aspectran.core.context.rule.params.TransformParameters;
import com.aspectran.core.context.rule.params.TransletParameters;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.params.TypeAliasParameters;
import com.aspectran.core.context.rule.params.TypeAliasesParameters;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.List;

/**
 * Converts {@code Parameters} objects to rules for context configuration.
 *
 * <p>Created: 2017. 5. 5.</p>
 */
public class ParametersToRules {

    private final ActivityRuleAssistant assistant;

    public ParametersToRules(ActivityRuleAssistant assistant) {
        this.assistant = assistant;
    }

    public void toRules(RootParameters rootParameters) throws IllegalRuleException {
        if (rootParameters == null) {
            throw new IllegalArgumentException("rootParameters must not be null");
        }

        AspectranParameters aspectranParameters = rootParameters.getParameters(RootParameters.aspectran);
        toRules(aspectranParameters);
    }

    public void toRules(AspectranParameters aspectranParameters) throws IllegalRuleException {
        if (aspectranParameters == null) {
            throw new IllegalArgumentException("aspectranParameters must not be null");
        }

        List<DescriptionParameters> descriptionParametersList = aspectranParameters.getParametersList(AspectranParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, assistant.getAssistantLocal().getDescriptionRule());
                assistant.getAssistantLocal().setDescriptionRule(descriptionRule);
            }
        }

        SettingsParameters settingsParameters = aspectranParameters.getParameters(AspectranParameters.settings);
        if (settingsParameters != null) {
            toDefaultSettings(settingsParameters);
        }

        TypeAliasesParameters typeAliasesParameters = aspectranParameters.getParameters(AspectranParameters.typeAliases);
        if (typeAliasesParameters != null) {
            toTypeAliasesRule(typeAliasesParameters);
        }

        List<EnvironmentParameters> environmentParametersList = aspectranParameters.getParametersList(AspectranParameters.environment);
        if (environmentParametersList != null) {
            for (EnvironmentParameters environmentParameters : environmentParametersList) {
                toEnvironmentRule(environmentParameters);
            }
        }

        List<AspectParameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspect);
        if (aspectParametersList != null) {
            for (AspectParameters aspectParameters : aspectParametersList) {
                toAspectRule(aspectParameters);
            }
        }

        List<BeanParameters> beanParametersList = aspectranParameters.getParametersList(AspectranParameters.bean);
        if (beanParametersList != null) {
            for (BeanParameters beanParameters : beanParametersList) {
                toBeanRule(beanParameters);
            }
        }

        List<ScheduleParameters> scheduleParametersList = aspectranParameters.getParametersList(AspectranParameters.schedule);
        if (scheduleParametersList != null) {
            for (ScheduleParameters scheduleParameters : scheduleParametersList) {
                toScheduleRule(scheduleParameters);
            }
        }

        List<TransletParameters> transletParametersList = aspectranParameters.getParametersList(AspectranParameters.translet);
        if (transletParametersList != null) {
            for (TransletParameters transletParameters : transletParametersList) {
                toTransletRule(transletParameters);
            }
        }

        List<TemplateParameters> templateParametersList = aspectranParameters.getParametersList(AspectranParameters.template);
        if (templateParametersList != null) {
            for (TemplateParameters templateParameters : templateParametersList) {
                toTemplateRule(templateParameters);
            }
        }

        List<AppendParameters> appendParametersList = aspectranParameters.getParametersList(AspectranParameters.append);
        if (appendParametersList != null) {
            for (AppendParameters appendParameters : appendParametersList) {
                toAppendRule(appendParameters);
            }
        }
    }

    @NonNull
    private DescriptionRule toDescriptionRule(@NonNull DescriptionParameters descriptionParameters)
            throws IllegalRuleException {
        String profile = descriptionParameters.getString(DescriptionParameters.profile);
        String style = descriptionParameters.getString(DescriptionParameters.style);
        String content = descriptionParameters.getString(DescriptionParameters.content);
        DescriptionRule descriptionRule = DescriptionRule.newInstance(profile, style);
        descriptionRule.setContent(content);
        return descriptionRule;
    }

    private void toAppendRule(AppendParameters appendParameters) throws IllegalRuleException {
        RuleAppendHandler appendHandler = assistant.getRuleAppendHandler();
        if (appendHandler != null) {
            AspectranParameters aspectran = appendParameters.getParameters(AppendParameters.aspectran);
            String profile = appendParameters.getString(AppendParameters.profile);
            if (aspectran != null) {
                AppendRule appendRule = AppendRule.newInstance(aspectran, profile);
                appendHandler.pending(appendRule);
            } else {
                String file = appendParameters.getString(AppendParameters.file);
                String resource = appendParameters.getString(AppendParameters.resource);
                String url = appendParameters.getString(AppendParameters.url);
                String format = appendParameters.getString(AppendParameters.format);
                AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                appendHandler.pending(appendRule);
            }
        }
    }

    private void toDefaultSettings(SettingsParameters settingsParameters) throws IllegalRuleException {
        if (settingsParameters != null) {
            List<SettingParameters> settingParametersList = settingsParameters.getParametersList(SettingsParameters.setting);
            if (settingParametersList != null) {
                for (SettingParameters settingParameters : settingParametersList) {
                    assistant.putSetting(settingParameters.getName(), settingParameters.getValueAsString());
                }
                assistant.applySettings();
            }
        }
    }

    private void toTypeAliasesRule(TypeAliasesParameters typeAliasesParameters) {
        if (typeAliasesParameters != null) {
            List<TypeAliasParameters> typeAliasParametersList = typeAliasesParameters.getParametersList(TypeAliasesParameters.typeAlias);
            if (typeAliasParametersList != null) {
                for (TypeAliasParameters typeAliasParameters : typeAliasParametersList) {
                    String alias = typeAliasParameters.getString(TypeAliasParameters.alias);
                    String type = typeAliasParameters.getString(TypeAliasParameters.type);
                    assistant.addTypeAlias(alias, type);
                }
            }
        }
    }

    private void toEnvironmentRule(EnvironmentParameters environmentParameters) throws IllegalRuleException {
        if (environmentParameters != null) {
            String profile = environmentParameters.getString(EnvironmentParameters.profile);

            EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile);

            List<DescriptionParameters> descriptionParametersList = environmentParameters.getParametersList(EnvironmentParameters.description);
            if (descriptionParametersList != null) {
                for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                    DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                    descriptionRule = assistant.profiling(descriptionRule, environmentRule.getDescriptionRule());
                    environmentRule.setDescriptionRule(descriptionRule);
                }
            }

            List<ItemHolderParameters> propertyItemHolderParametersList = environmentParameters.getParametersList(EnvironmentParameters.properties);
            if (propertyItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                    ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, environmentRule.getPropertyItemRuleMap());
                    environmentRule.setPropertyItemRuleMap(irm);
                }
            }

            assistant.addEnvironmentRule(environmentRule);
        }
    }

    private void toAspectRule(@NonNull AspectParameters aspectParameters) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(aspectParameters.getString(AspectParameters.id));
        String order = aspectParameters.getString(AspectParameters.order);
        Boolean isolated = aspectParameters.getBoolean(AspectParameters.isolated);
        Boolean disabled = aspectParameters.getBoolean(AspectParameters.disabled);

        AspectRule aspectRule = AspectRule.newInstance(id, order, isolated, disabled);

        List<DescriptionParameters> descriptionParametersList = aspectParameters.getParametersList(AspectParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, aspectRule.getDescriptionRule());
                aspectRule.setDescriptionRule(descriptionRule);
            }
        }

        JoinpointParameters joinpointParameters = aspectParameters.getParameters(AspectParameters.joinpoint);
        if (joinpointParameters != null) {
            AspectRule.updateJoinpoint(aspectRule, joinpointParameters);
        }

        SettingsParameters settingsParameters = aspectParameters.getParameters(AspectParameters.settings);
        if (settingsParameters != null) {
            SettingsAdviceRule settingsAdviceRule = SettingsAdviceRule.newInstance(aspectRule, settingsParameters);
            aspectRule.setSettingsAdviceRule(settingsAdviceRule);
        }

        AdviceParameters adviceParameters = aspectParameters.getParameters(AspectParameters.advice);
        if (adviceParameters != null) {
            String adviceBeanId = adviceParameters.getString(AdviceParameters.bean);
            if (StringUtils.hasLength(adviceBeanId)) {
                aspectRule.setAdviceBeanId(adviceBeanId);
            }

            AdviceActionParameters beforeAdviceParameters = adviceParameters.getParameters(AdviceParameters.beforeAdvice);
            if (beforeAdviceParameters != null) {
                ActionParameters actionParameters = beforeAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AdviceRule adviceRule = aspectRule.newBeforeAdviceRule();
                    toActionRule(actionParameters, adviceRule);
                }
            }

            AdviceActionParameters afterAdviceParameters = adviceParameters.getParameters(AdviceParameters.afterAdvice);
            if (afterAdviceParameters != null) {
                ActionParameters actionParameters = afterAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AdviceRule adviceRule = aspectRule.newAfterAdviceRule();
                    toActionRule(actionParameters, adviceRule);
                }
            }

            AdviceActionParameters aroundAdviceParameters = adviceParameters.getParameters(AdviceParameters.aroundAdvice);
            if (aroundAdviceParameters != null) {
                ActionParameters actionParameters = aroundAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AdviceRule adviceRule = aspectRule.newAroundAdviceRule();
                    toActionRule(actionParameters, adviceRule);
                }
            }

            AdviceActionParameters finallyAdviceParameters = adviceParameters.getParameters(AdviceParameters.finallyAdvice);
            if (finallyAdviceParameters != null) {
                AdviceRule adviceRule = aspectRule.newFinallyAdviceRule();
                ActionParameters actionParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    toActionRule(actionParameters, adviceRule);
                }
                ExceptionThrownParameters etParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.thrown);
                if (etParameters != null) {
                    ExceptionThrownRule etr = toExceptionThrownRule(etParameters, adviceRule);
                    adviceRule.setExceptionThrownRule(etr);
                }
            }
        }

        ExceptionParameters exceptionParameters = aspectParameters.getParameters(AspectParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();

            List<DescriptionParameters> descriptionParametersList2 = exceptionParameters.getParametersList(ExceptionParameters.description);
            if (descriptionParametersList2 != null) {
                for (DescriptionParameters descriptionParameters : descriptionParametersList2) {
                    DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                    descriptionRule = assistant.profiling(descriptionRule, exceptionRule.getDescriptionRule());
                    exceptionRule.setDescriptionRule(descriptionRule);
                }
            }

            List<ExceptionThrownParameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (ExceptionThrownParameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = toExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            aspectRule.setExceptionRule(exceptionRule);
        }

        assistant.resolveAdviceBeanClass(aspectRule);
        assistant.addAspectRule(aspectRule);
    }

    private void toBeanRule(@NonNull BeanParameters beanParameters) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.id));
        String className = StringUtils.emptyToNull(assistant.resolveAliasType(beanParameters.getString(BeanParameters.className)));
        String scan = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.scan));
        String mask = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.mask));
        String scope = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.scope));
        Boolean singleton = beanParameters.getBoolean(BeanParameters.singleton);
        String factoryBean = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryBean));
        String factoryMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryMethod));
        String initMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.initMethod));
        String destroyMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.destroyMethod));
        Boolean lazyInit = beanParameters.getBoolean(BeanParameters.lazyInit);
        Boolean lazyDestroy = beanParameters.getBoolean(BeanParameters.lazyDestroy);
        Boolean important = beanParameters.getBoolean(BeanParameters.important);

        BeanRule beanRule;
        if (className == null && scan == null && factoryBean != null) {
            beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod,
                initMethod, destroyMethod, scope, singleton, lazyInit, lazyDestroy, important);
        } else {
            beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod,
                factoryMethod, scope, singleton, lazyInit, lazyDestroy, important);
        }

        List<DescriptionParameters> descriptionParametersList = beanParameters.getParametersList(BeanParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, beanRule.getDescriptionRule());
                beanRule.setDescriptionRule(descriptionRule);
            }
        }

        FilterParameters filterParameters = beanParameters.getParameters(BeanParameters.filter);
            if (filterParameters != null && (filterParameters.hasFilterClass() || filterParameters.hasPatterns())) {
            beanRule.setFilterParameters(filterParameters);
        }

        List<ItemHolderParameters> argumentItemHolderParametersList = beanParameters.getParametersList(BeanParameters.arguments);
        if (argumentItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, beanRule.getArgumentItemRuleMap());
                beanRule.setArgumentItemRuleMap(irm);
            }
        }

        List<ItemHolderParameters> propertyItemHolderParametersList = beanParameters.getParametersList(BeanParameters.properties);
        if (propertyItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
                beanRule.setPropertyItemRuleMap(irm);
            }
        }

        assistant.resolveBeanClass(beanRule);
        assistant.resolveFactoryBeanClass(beanRule);
        assistant.addBeanRule(beanRule);
    }

    private BeanRule toInnerBeanRule(@NonNull BeanParameters beanParameters) throws IllegalRuleException {
        String className = StringUtils.emptyToNull(assistant.resolveAliasType(beanParameters.getString(BeanParameters.className)));
        String factoryBean = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryBean));
        String factoryMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryMethod));
        String initMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.initMethod));
        String destroyMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.destroyMethod));

        BeanRule beanRule;
        if (className == null && factoryBean != null) {
            beanRule = BeanRule.newInnerOfferedFactoryBeanRule(factoryBean, factoryMethod, initMethod, destroyMethod);
        } else {
            beanRule = BeanRule.newInnerBeanRule(className, initMethod, destroyMethod, factoryMethod);
        }

        List<DescriptionParameters> descriptionParametersList = beanParameters.getParametersList(BeanParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, beanRule.getDescriptionRule());
                beanRule.setDescriptionRule(descriptionRule);
            }
        }

        List<ItemHolderParameters> argumentItemHolderParametersList = beanParameters.getParametersList(BeanParameters.arguments);
        if (argumentItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, beanRule.getArgumentItemRuleMap());
                beanRule.setArgumentItemRuleMap(irm);
            }
        }

        List<ItemHolderParameters> propertyItemHolderParametersList = beanParameters.getParametersList(BeanParameters.properties);
        if (propertyItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
                beanRule.setPropertyItemRuleMap(irm);
            }
        }

        assistant.resolveBeanClass(beanRule);
        assistant.resolveFactoryBeanClass(beanRule);
        assistant.addInnerBeanRule(beanRule);

        return beanRule;
    }

    private void toScheduleRule(@NonNull ScheduleParameters scheduleParameters) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(scheduleParameters.getString(AspectParameters.id));

        ScheduleRule scheduleRule = ScheduleRule.newInstance(id);

        List<DescriptionParameters> descriptionParametersList = scheduleParameters.getParametersList(ScheduleParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, scheduleRule.getDescriptionRule());
                scheduleRule.setDescriptionRule(descriptionRule);
            }
        }

        SchedulerParameters schedulerParameters = scheduleParameters.getParameters(ScheduleParameters.scheduler);
        if (schedulerParameters != null) {
            String schedulerBeanId = schedulerParameters.getString(SchedulerParameters.bean);
            if (StringUtils.hasLength(schedulerBeanId)) {
                scheduleRule.setSchedulerBeanId(schedulerBeanId);
            }
            TriggerParameters triggerParameters = schedulerParameters.getParameters(SchedulerParameters.trigger);
            if (triggerParameters != null) {
                ScheduleRule.updateTrigger(scheduleRule, triggerParameters);
            }

            List<ScheduledJobParameters> jobParametersList = scheduleParameters.getParametersList(ScheduleParameters.job);
            if (jobParametersList != null) {
                for (ScheduledJobParameters jobParameters : jobParametersList) {
                    String translet = StringUtils.emptyToNull(jobParameters.getString(ScheduledJobParameters.translet));
                    Boolean disabled = jobParameters.getBoolean(ScheduledJobParameters.disabled);
                    ScheduledJobRule scheduledJobRule = ScheduledJobRule.newInstance(scheduleRule, translet, disabled);
                    scheduleRule.addScheduledJobRule(scheduledJobRule);
                }
            }
        }

        assistant.addScheduleRule(scheduleRule);
    }

    private void toTransletRule(@NonNull TransletParameters transletParameters) throws IllegalRuleException {
        String name = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.name));
        String scan = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.scan));
        String mask = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.mask));
        String method = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.method));
        Boolean async = transletParameters.getBoolean(TransletParameters.async);
        Long timeout = transletParameters.getLong(TransletParameters.timeout);

        TransletRule transletRule = TransletRule.newInstance(name, mask, scan, method, async, timeout);

        List<DescriptionParameters> descriptionParametersList = transletParameters.getParametersList(TransletParameters.description);
        if (descriptionParametersList != null) {
            for (DescriptionParameters descriptionParameters : descriptionParametersList) {
                DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                descriptionRule = assistant.profiling(descriptionRule, transletRule.getDescriptionRule());
                transletRule.setDescriptionRule(descriptionRule);
            }
        }

        RequestParameters requestParameters = transletParameters.getParameters(TransletParameters.request);
        if (requestParameters != null) {
            toRequestRule(requestParameters, transletRule);
        }

        List<ItemHolderParameters> parameterItemHolderParametersList = transletParameters.getParametersList(TransletParameters.parameters);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                RequestRule requestRule = transletRule.touchRequestRule(false);
                irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
                requestRule.setParameterItemRuleMap(irm);
            }
        }

        List<ItemHolderParameters> attributeItemHolderParametersList = transletParameters.getParametersList(TransletParameters.attributes);
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                RequestRule requestRule = transletRule.touchRequestRule(false);
                irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
                requestRule.setAttributeItemRuleMap(irm);
            }
        }

        List<ActionParameters> actionParametersList = transletParameters.getParametersList(TransletParameters.action);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ContentList contentList = new ContentList(false);
            ActionList actionList = new ActionList(false);
            contentList.addActionList(actionList);
            for (ActionParameters actionParameters : actionParametersList) {
                toActionRule(actionParameters, actionList);
            }
            transletRule.setContentList(contentList);
        }

        TransformParameters transformParameters = transletParameters.getParameters(TransletParameters.transform);
        if (transformParameters != null) {
            toTransformRule(transformParameters, transletRule);
        }

        DispatchParameters dispatchParameters = transletParameters.getParameters(TransletParameters.dispatch);
        if (dispatchParameters != null) {
            toDispatchRule(dispatchParameters, transletRule);
        }

        ForwardParameters forwardParameters = transletParameters.getParameters(TransletParameters.forward);
        if (forwardParameters != null) {
            toForwardRule(forwardParameters, transletRule);
        }

        RedirectParameters redirectParameters = transletParameters.getParameters(TransletParameters.redirect);
        if (redirectParameters != null) {
            toRedirectRule(redirectParameters, transletRule);
        }

        ContentsParameters contentsParameters = transletParameters.getParameters(TransletParameters.contents);
        if (contentsParameters != null) {
            ContentList contentList = toContentList(contentsParameters);
            transletRule.setContentList(contentList);
        }

        List<ContentParameters> contentParametersList = transletParameters.getParametersList(TransletParameters.content);
        if (contentParametersList != null) {
            ContentList contentList = new ContentList(false);
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = toActionList(contentParameters);
                contentList.addActionList(actionList);
            }
            transletRule.setContentList(contentList);
        }

        List<ResponseParameters> responseParametersList = transletParameters.getParametersList(TransletParameters.response);
        if (responseParametersList != null) {
            for (ResponseParameters responseParameters : responseParametersList) {
                toResponseRule(responseParameters, transletRule);
            }
        }

        ExceptionParameters exceptionParameters = transletParameters.getParameters(TransletParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();

            List<DescriptionParameters> descriptionParametersList2 = exceptionParameters.getParametersList(ExceptionParameters.description);
            if (descriptionParametersList2 != null) {
                for (DescriptionParameters descriptionParameters : descriptionParametersList2) {
                    DescriptionRule descriptionRule = toDescriptionRule(descriptionParameters);
                    descriptionRule = assistant.profiling(descriptionRule, exceptionRule.getDescriptionRule());
                    exceptionRule.setDescriptionRule(descriptionRule);
                }
            }

            List<ExceptionThrownParameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (ExceptionThrownParameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = toExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }

            transletRule.setExceptionRule(exceptionRule);
        }

        assistant.addTransletRule(transletRule);
    }

    private void toRequestRule(@NonNull RequestParameters requestParameters, TransletRule transletRule)
            throws IllegalRuleException {
        String method = requestParameters.getString(RequestParameters.method);
        String encoding = requestParameters.getString(RequestParameters.encoding);

        RequestRule requestRule = RequestRule.newInstance(method, encoding);

        List<ItemHolderParameters> parameterItemHolderParametersList = requestParameters.getParametersList(RequestParameters.parameters);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
                requestRule.setParameterItemRuleMap(irm);
            }
        }

        List<ItemHolderParameters> attributeItemHolderParametersList = requestParameters.getParametersList(RequestParameters.attributes);
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
                requestRule.setAttributeItemRuleMap(irm);
            }
        }

        transletRule.setRequestRule(requestRule);
    }

    private void toResponseRule(@NonNull ResponseParameters responseParameters, TransletRule transletRule)
            throws IllegalRuleException {
        String name = responseParameters.getString(ResponseParameters.name);
        String encoding = responseParameters.getString(ResponseParameters.encoding);

        ResponseRule responseRule = ResponseRule.newInstance(name, encoding);

        List<ActionParameters> actionParametersList = responseParameters.getParametersList(ResponseParameters.action);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList(false);
            for (ActionParameters actionParameters : actionParametersList) {
                toActionRule(actionParameters, actionList);
            }
            responseRule.setActionList(actionList);
        }

        TransformParameters transformParameters = responseParameters.getParameters(ResponseParameters.transform);
        if (transformParameters != null) {
            toTransformRule(transformParameters, responseRule);
        }

        DispatchParameters dispatchParameters = responseParameters.getParameters(ResponseParameters.dispatch);
        if (dispatchParameters != null) {
            toDispatchRule(dispatchParameters, responseRule);
        }

        ForwardParameters forwardParameters = responseParameters.getParameters(ResponseParameters.forward);
        if (forwardParameters != null) {
            toForwardRule(forwardParameters, responseRule);
        }

        RedirectParameters redirectParameters = responseParameters.getParameters(ResponseParameters.redirect);
        if (redirectParameters != null) {
            toRedirectRule(redirectParameters, responseRule);
        }

        transletRule.addResponseRule(responseRule);
    }

    @NonNull
    private ContentList toContentList(@NonNull ContentsParameters contentsParameters) throws IllegalRuleException {
        String name = contentsParameters.getString(ContentsParameters.name);
        ContentList contentList = ContentList.newInstance(name);
        List<ContentParameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.content);
        if (contentParametersList != null) {
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = toActionList(contentParameters);
                contentList.addActionList(actionList);
            }
        }
        return contentList;
    }

    @NonNull
    private ActionList toActionList(@NonNull ContentParameters contentParameters) throws IllegalRuleException {
        String name = contentParameters.getString(ContentParameters.name);
        ActionList actionList = ActionList.newInstance(name);
        List<ActionParameters> actionParametersList = contentParameters.getParametersList(ContentParameters.action);
        if (actionParametersList != null) {
            for (ActionParameters actionParameters : actionParametersList) {
                toActionRule(actionParameters, actionList);
            }
        }
        return actionList;
    }

    private void toActionRule(@NonNull ActionParameters actionParameters, HasActionRules actionRuleApplicable)
            throws IllegalRuleException {
        String actualName = actionParameters.getActualName();
        if (actualName == null) {
            throw new IllegalRuleException("No actual name");
        }

        switch (actualName) {
            case "action": {
                String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
                String beanIdOrClass = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.bean));
                String method = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.method));
                Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(id, beanIdOrClass, method, hidden);
                List<ItemHolderParameters> argumentItemHolderParametersList = actionParameters.getParametersList(ActionParameters.arguments);
                if (argumentItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, invokeActionRule.getArgumentItemRuleMap());
                        invokeActionRule.setArgumentItemRuleMap(irm);
                    }
                }
                List<ItemHolderParameters> propertyItemHolderParametersList = actionParameters.getParametersList(ActionParameters.properties);
                if (propertyItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, invokeActionRule.getPropertyItemRuleMap());
                        invokeActionRule.setPropertyItemRuleMap(irm);
                    }
                }
                assistant.resolveActionBeanClass(invokeActionRule);
                actionRuleApplicable.putActionRule(invokeActionRule);
                break;
            }
            case "invoke": {
                String method = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.method));
                Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
                InvokeActionRule invokeActionRule = InvokeActionRule.newInstance(method, hidden);
                List<ItemHolderParameters> argumentItemHolderParametersList = actionParameters.getParametersList(ActionParameters.arguments);
                if (argumentItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, invokeActionRule.getArgumentItemRuleMap());
                        invokeActionRule.setArgumentItemRuleMap(irm);
                    }
                }
                List<ItemHolderParameters> propertyItemHolderParametersList = actionParameters.getParametersList(ActionParameters.properties);
                if (propertyItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, invokeActionRule.getPropertyItemRuleMap());
                        invokeActionRule.setPropertyItemRuleMap(irm);
                    }
                }
                assistant.resolveActionBeanClass(invokeActionRule);
                actionRuleApplicable.putActionRule(invokeActionRule);
                break;
            }
            case "echo": {
                String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
                Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
                List<ItemParameters> itemParametersList = actionParameters.getParametersList(ActionParameters.item);
                EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
                ItemRuleMap attributeItemRuleMap = toItemRuleMap(null, itemParametersList);
                echoActionRule.setEchoItemRuleMap(attributeItemRuleMap);
                actionRuleApplicable.putActionRule(echoActionRule);
                break;
            }
            case "headers": {
                String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
                Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
                List<ItemParameters> itemParametersList = actionParameters.getParametersList(ActionParameters.item);
                HeaderActionRule headerActionRule = HeaderActionRule.newInstance(id, hidden);
                ItemRuleMap headerItemRuleMap = toItemRuleMap(null, itemParametersList);
                headerActionRule.setHeaderItemRuleMap(headerItemRuleMap);
                actionRuleApplicable.putActionRule(headerActionRule);
                break;
            }
            case "include": {
                String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
                String translet = actionParameters.getString(ActionParameters.translet);
                translet = assistant.applyTransletNamePattern(translet);
                String method = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.method));
                Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
                IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, translet, method, hidden);
                List<ItemHolderParameters> parameterItemHolderParametersList = actionParameters.getParametersList(ActionParameters.parameters);
                if (parameterItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, includeActionRule.getParameterItemRuleMap());
                        includeActionRule.setParameterItemRuleMap(irm);
                    }
                }
                List<ItemHolderParameters> attributeItemHolderParametersList = actionParameters.getParametersList(ActionParameters.attributes);
                if (attributeItemHolderParametersList != null) {
                    for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                        ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                        irm = assistant.profiling(irm, includeActionRule.getAttributeItemRuleMap());
                        includeActionRule.setAttributeItemRuleMap(irm);
                    }
                }
                actionRuleApplicable.putActionRule(includeActionRule);
                break;
            }
            case "choose": {
                toChooseRule(actionParameters, actionRuleApplicable);
                break;
            }
            default:
                throw new IllegalRuleException("Illegal actual name: " + actualName);
        }
    }

    private void toChooseRule(@NonNull ActionParameters actionParameters, HasActionRules actionRuleApplicable)
            throws IllegalRuleException {
        List<ChooseWhenParameters> chooseWhenParametersList = actionParameters.getParametersList(ActionParameters.when);
        ChooseWhenParameters chooseOtherwiseParameters = actionParameters.getParameters(ActionParameters.otherwise);

        if (chooseWhenParametersList != null && !chooseWhenParametersList.isEmpty() ||
                chooseOtherwiseParameters != null) {
            ChooseRule chooseRule = ChooseRule.newInstance();
            if (chooseWhenParametersList != null) {
                for (ChooseWhenParameters chooseWhenParameters : chooseWhenParametersList) {
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                    toChooseWhenRule(chooseWhenParameters, chooseWhenRule);
                }
            }
            if (chooseOtherwiseParameters != null) {
                ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule();
                toChooseWhenRule(chooseOtherwiseParameters, chooseWhenRule);
            }
            actionRuleApplicable.putActionRule(chooseRule);
        }
    }

    private void toChooseWhenRule(@NonNull ChooseWhenParameters chooseWhenParameters,
                                  @NonNull ChooseWhenRule chooseWhenRule)
            throws IllegalRuleException {
        String expression = StringUtils.emptyToNull(chooseWhenParameters.getString(ChooseWhenParameters.test));
        chooseWhenRule.setExpression(expression);

        List<ActionParameters> whenActionParametersList = chooseWhenParameters.getParametersList(ChooseWhenParameters.action);
        if (whenActionParametersList != null && !whenActionParametersList.isEmpty()) {
            for (ActionParameters actionParameters : whenActionParametersList) {
                toActionRule(actionParameters, chooseWhenRule);
            }
        }

        TransformParameters transformParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.transform);
        if (transformParameters != null) {
            toTransformRule(transformParameters, chooseWhenRule);
        }

        DispatchParameters dispatchParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.dispatch);
        if (dispatchParameters != null) {
            toDispatchRule(dispatchParameters, chooseWhenRule);
        }

        ForwardParameters forwardParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.forward);
        if (forwardParameters != null) {
            toForwardRule(forwardParameters, chooseWhenRule);
        }

        RedirectParameters redirectParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.redirect);
        if (redirectParameters != null) {
            toRedirectRule(redirectParameters, chooseWhenRule);
        }
    }

    @NonNull
    private ExceptionThrownRule toExceptionThrownRule(@NonNull ExceptionThrownParameters exceptionThrownParameters,
                                                      AdviceRule adviceRule)
            throws IllegalRuleException {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule(adviceRule);

        String[] exceptionTypes = exceptionThrownParameters.getStringArray(ExceptionThrownParameters.type);
        exceptionThrownRule.setExceptionTypes(exceptionTypes);

        ActionParameters actionParameters = exceptionThrownParameters.getParameters(ExceptionThrownParameters.action);
        if (actionParameters != null) {
            toActionRule(actionParameters, exceptionThrownRule);
        }

        List<TransformParameters> transformParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.transform);
        if (transformParametersList != null && !transformParametersList.isEmpty()) {
            toTransformRule(transformParametersList, exceptionThrownRule);
        }

        List<DispatchParameters> dispatchParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.dispatch);
        if (dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
            toDispatchRule(dispatchParametersList, exceptionThrownRule);
        }

        List<RedirectParameters> redirectParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.redirect);
        if (redirectParametersList != null && !redirectParametersList.isEmpty()) {
            toRedirectRule(redirectParametersList, exceptionThrownRule);
        }

        return exceptionThrownRule;
    }

    private void toTransformRule(@NonNull List<TransformParameters> transformParametersList,
                                 HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        for (TransformParameters transformParameters : transformParametersList) {
            toTransformRule(transformParameters, responseRuleApplicable);
        }
    }

    private void toTransformRule(@NonNull TransformParameters transformParameters,
                                 HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        String format = transformParameters.getString(TransformParameters.format);
        String contentType = transformParameters.getString(TransformParameters.contentType);
        String encoding = transformParameters.getString(TransformParameters.encoding);
        Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
        Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);

        TransformRule transformRule = TransformRule.newInstance(format, contentType, encoding, defaultResponse, pretty);

        TemplateParameters templateParameters = transformParameters.getParameters(TransformParameters.template);
        if (templateParameters != null) {
            String id = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.id));
            String engine = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.engine));
            String name = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.name));
            String file = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.file));
            String resource = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.resource));
            String url = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.url));
            String style = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.style));
            String content = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.content));
            String contentType2 = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.contentType));
            String encoding2 = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.encoding));
            Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);

            TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(id, engine, name, file, resource, url, style, content, contentType2, encoding2, noCache);
            transformRule.setTemplateRule(templateRule);
            assistant.resolveBeanClass(templateRule.getTemplateTokens());
        }

        responseRuleApplicable.putResponseRule(transformRule);
    }

    private void toDispatchRule(@NonNull List<DispatchParameters> dispatchParametersList,
                                HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        for (DispatchParameters dispatchParameters : dispatchParametersList) {
            toDispatchRule(dispatchParameters, responseRuleApplicable);
        }
    }

    private void toDispatchRule(@NonNull DispatchParameters dispatchParameters,
                                @NonNull HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        String name = dispatchParameters.getString(DispatchParameters.name);
        String dispatcher = dispatchParameters.getString(DispatchParameters.dispatcher);
        String contentType = dispatchParameters.getString(DispatchParameters.contentType);
        String encoding = dispatchParameters.getString(DispatchParameters.encoding);
        Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);

        DispatchRule dispatchRule = DispatchRule.newInstance(name, dispatcher, contentType, encoding, defaultResponse);
        responseRuleApplicable.putResponseRule(dispatchRule);
    }

    private void toForwardRule(@NonNull ForwardParameters forwardParameters,
                               @NonNull HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        String contentType = forwardParameters.getString(ForwardParameters.contentType);
        String translet = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.translet));
        String method = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.method));
        Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);

        translet = assistant.applyTransletNamePattern(translet);

        ForwardRule forwardRule = ForwardRule.newInstance(contentType, translet, method, defaultResponse);

        List<ItemHolderParameters> attributeItemHolderParametersList = forwardParameters.getParameters(ForwardParameters.attributes);
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, forwardRule.getAttributeItemRuleMap());
                forwardRule.setAttributeItemRuleMap(irm);
            }
        }

        responseRuleApplicable.putResponseRule(forwardRule);
    }

    private void toRedirectRule(@NonNull List<RedirectParameters> redirectParametersList,
                                @NonNull HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        for (RedirectParameters redirectParameters : redirectParametersList) {
            toRedirectRule(redirectParameters, responseRuleApplicable);
        }
    }

    private void toRedirectRule(@NonNull RedirectParameters redirectParameters,
                                @NonNull HasResponseRules responseRuleApplicable)
            throws IllegalRuleException {
        String contentType = redirectParameters.getString(RedirectParameters.contentType);
        String path = redirectParameters.getString(RedirectParameters.path);
        String encoding = redirectParameters.getString(RedirectParameters.encoding);
        Boolean excludeNullParameters = redirectParameters.getBoolean(RedirectParameters.excludeNullParameters);
        Boolean excludeEmptyParameters = redirectParameters.getBoolean(RedirectParameters.excludeEmptyParameters);
        Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);

        RedirectRule redirectRule = RedirectRule.newInstance(contentType, path, encoding, excludeNullParameters, excludeEmptyParameters, defaultResponse);

        List<ItemHolderParameters> parameterItemHolderParametersList = redirectParameters.getParametersList(RedirectParameters.parameters);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = toItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, redirectRule.getParameterItemRuleMap());
                redirectRule.setParameterItemRuleMap(irm);
            }
        }

        responseRuleApplicable.putResponseRule(redirectRule);
        assistant.resolveBeanClass(redirectRule.getPathTokens());
    }

    private void toTemplateRule(@NonNull TemplateParameters templateParameters) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.id));
        String engine = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.engine));
        String name = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.name));
        String file = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.file));
        String resource = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.resource));
        String url = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.url));
        String style = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.style));
        String content = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.content));
        String contentType = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.contentType));
        String encoding = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.encoding));
        Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);

        TemplateRule templateRule = TemplateRule.newInstance(id, engine, name, file, resource, url, style, content, contentType, encoding, noCache);
        assistant.addTemplateRule(templateRule);
    }

    private ItemRuleMap toItemRuleMap(@NonNull ItemHolderParameters itemHolderParameters) throws IllegalRuleException {
        String profile = itemHolderParameters.getProfile();
        List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
        return toItemRuleMap(profile, itemParametersList);
    }

    private ItemRuleMap toItemRuleMap(String profile, List<ItemParameters> itemParametersList) throws IllegalRuleException {
        ItemRuleMap itemRuleMap = toItemRuleMap(itemParametersList);
        if (itemRuleMap != null) {
            itemRuleMap.setProfile(profile);
            for (ItemRule itemRule : itemRuleMap.values()) {
                assistant.resolveBeanClass(itemRule);
            }
        }
        return itemRuleMap;
    }

    private ItemRuleMap toItemRuleMap(List<ItemParameters> itemParametersList) throws IllegalRuleException {
        if (itemParametersList == null || itemParametersList.isEmpty()) {
            return null;
        }
        ItemRuleMap itemRuleMap = new ItemRuleMap();
        for (ItemParameters parameters : itemParametersList) {
            itemRuleMap.putItemRule(toItemRule(parameters));
        }
        return itemRuleMap;
    }

    @NonNull
    private ItemRule toItemRule(@NonNull ItemParameters itemParameters) throws IllegalRuleException {
        String type = itemParameters.getString(ItemParameters.type);
        String name = itemParameters.getString(ItemParameters.name);
        String valueType = itemParameters.getString(ItemParameters.valueType);
        Boolean tokenize = itemParameters.getBoolean(ItemParameters.tokenize);
        Boolean mandatory = itemParameters.getBoolean(ItemParameters.mandatory);
        Boolean secret = itemParameters.getBoolean(ItemParameters.secret);

        ItemRule itemRule = ItemRule.newInstance(type, name, valueType, tokenize, mandatory, secret);
        if (itemRule.getValue() == null && itemParameters.hasValue(ItemParameters.bean)) {
            itemRule.setValueType(ItemValueType.BEAN);
        }

        if (itemRule.isListableType()) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                List<BeanParameters> beanParametersList = itemParameters.getParametersList(ItemParameters.bean);
                if (beanParametersList != null) {
                    for (BeanParameters beanParameters : beanParametersList) {
                        BeanRule beanRule = toInnerBeanRule(beanParameters);
                        itemRule.addBeanRule(beanRule);
                    }
                }
            } else {
                List<String> stringList = itemParameters.getStringList(ItemParameters.value);
                if (stringList != null) {
                    for (String value : stringList) {
                        itemRule.addValue(value);
                    }
                }
            }
        } else if (itemRule.isMappableType()) {
            List<EntryParameters> entryParametersList = itemParameters.getParametersList(ItemParameters.entry);
            if (entryParametersList != null) {
                if (itemRule.getValueType() == ItemValueType.BEAN) {
                    for (EntryParameters parameters : entryParametersList) {
                        if (parameters != null) {
                            String entryName = parameters.getString(EntryParameters.name);
                            List<BeanParameters> beanParametersList = itemParameters.getParametersList(ItemParameters.bean);
                            if (beanParametersList != null && !beanParametersList.isEmpty()) {
                                BeanRule beanRule = toInnerBeanRule(beanParametersList.getFirst());
                                itemRule.putBeanRule(entryName, beanRule);
                            } else {
                                itemRule.putBeanRule(entryName, null);
                            }
                        }
                    }
                } else {
                    for (EntryParameters parameters : entryParametersList) {
                        if (parameters != null) {
                            String entryName = parameters.getString(EntryParameters.name);
                            String entryValue = parameters.getString(EntryParameters.value);
                            itemRule.putValue(entryName, entryValue);
                        }
                    }
                }
            }
        } else {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                List<BeanParameters> beanParametersList = itemParameters.getParametersList(ItemParameters.bean);
                if (beanParametersList != null && !beanParametersList.isEmpty()) {
                    BeanRule beanRule = toInnerBeanRule(beanParametersList.getFirst());
                    itemRule.setBeanRule(beanRule);
                }
            } else {
                List<String> stringList = itemParameters.getStringList(ItemParameters.value);
                if (stringList != null && !stringList.isEmpty()) {
                    itemRule.setValue(stringList.getFirst());
                }
            }
        }

        return itemRule;
    }

}
