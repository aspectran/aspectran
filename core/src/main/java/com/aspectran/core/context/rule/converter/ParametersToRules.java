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
package com.aspectran.core.context.rule.converter;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanMethodActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseRuleMap;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.IncludeActionRule;
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
import com.aspectran.core.context.rule.ability.ActionRuleApplicable;
import com.aspectran.core.context.rule.ability.ResponseRuleApplicable;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.params.ActionParameters;
import com.aspectran.core.context.rule.params.AdviceActionParameters;
import com.aspectran.core.context.rule.params.AdviceParameters;
import com.aspectran.core.context.rule.params.AppendParameters;
import com.aspectran.core.context.rule.params.AspectParameters;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.params.BeanParameters;
import com.aspectran.core.context.rule.params.CallParameters;
import com.aspectran.core.context.rule.params.ChooseParameters;
import com.aspectran.core.context.rule.params.ChooseWhenParameters;
import com.aspectran.core.context.rule.params.ConstructorParameters;
import com.aspectran.core.context.rule.params.ContentParameters;
import com.aspectran.core.context.rule.params.ContentsParameters;
import com.aspectran.core.context.rule.params.DefaultSettingsParameters;
import com.aspectran.core.context.rule.params.DispatchParameters;
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
import com.aspectran.core.context.rule.params.TemplateParameters;
import com.aspectran.core.context.rule.params.TransformParameters;
import com.aspectran.core.context.rule.params.TransletParameters;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

import java.util.List;

/**
 * Converts {@code Parameters} objects to rules for context configuration.
 *
 * <p>Created: 2017. 5. 5.</p>
 */
public class ParametersToRules {

    private final ContextRuleAssistant assistant;

    public ParametersToRules(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    public void asRules(RootParameters rootParameters) throws IllegalRuleException {
        if (rootParameters == null) {
            throw new IllegalArgumentException("rootParameters must not be null");
        }

        AspectranParameters aspectranParameters = rootParameters.getParameters(RootParameters.aspectran);
        asRules(aspectranParameters);
    }

    public void asRules(AspectranParameters aspectranParameters) throws IllegalRuleException {
        if (aspectranParameters == null) {
            throw new IllegalArgumentException("aspectranParameters must not be null");
        }

        String description = aspectranParameters.getString(AspectranParameters.description);
        if (description != null) {
            assistant.getAssistantLocal().setDescription(description);
        }

        DefaultSettingsParameters defaultSettingsParameters = aspectranParameters.getParameters(AspectranParameters.settings);
        if (defaultSettingsParameters != null) {
            asDefaultSettings(defaultSettingsParameters);
        }

        Parameters typeAliasParameters = aspectranParameters.getParameters(AspectranParameters.typeAlias);
        if (typeAliasParameters != null) {
            asTypeAliasRule(typeAliasParameters);
        }

        List<EnvironmentParameters> environmentParametersList = aspectranParameters.getParametersList(AspectranParameters.environment);
        if (environmentParametersList != null) {
            for (EnvironmentParameters environmentParameters : environmentParametersList) {
                asEnvironmentRule(environmentParameters);
            }
        }

        List<AspectParameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspect);
        if (aspectParametersList != null) {
            for (AspectParameters aspectParameters : aspectParametersList) {
                asAspectRule(aspectParameters);
            }
        }

        List<BeanParameters> beanParametersList = aspectranParameters.getParametersList(AspectranParameters.bean);
        if (beanParametersList != null) {
            for (BeanParameters beanParameters : beanParametersList) {
                asBeanRule(beanParameters);
            }
        }

        List<ScheduleParameters> scheduleParametersList = aspectranParameters.getParametersList(AspectranParameters.schedule);
        if (scheduleParametersList != null) {
            for (ScheduleParameters scheduleParameters : scheduleParametersList) {
                asScheduleRule(scheduleParameters);
            }
        }

        List<TransletParameters> transletParametersList = aspectranParameters.getParametersList(AspectranParameters.translet);
        if (transletParametersList != null) {
            for (TransletParameters transletParameters : transletParametersList) {
                asTransletRule(transletParameters);
            }
        }

        List<TemplateParameters> templateParametersList = aspectranParameters.getParametersList(AspectranParameters.template);
        if (templateParametersList != null) {
            for (TemplateParameters templateParameters : templateParametersList) {
                asTemplateRule(templateParameters);
            }
        }

        List<AppendParameters> appendParametersList = aspectranParameters.getParametersList(AspectranParameters.append);
        if (appendParametersList != null) {
            for (AppendParameters appendParameters : appendParametersList) {
                asAppendRule(appendParameters);
            }
        }
    }

    private void asAppendRule(AppendParameters appendParameters) throws IllegalRuleException {
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

    private void asDefaultSettings(DefaultSettingsParameters defaultSettingsParameters) throws IllegalRuleException {
        if (defaultSettingsParameters == null) {
            return;
        }
        for (String name : defaultSettingsParameters.getParameterNameSet()) {
            assistant.putSetting(name, defaultSettingsParameters.getString(name));
        }
        assistant.applySettings();
    }

    private void asEnvironmentRule(EnvironmentParameters environmentParameters) throws IllegalRuleException {
        if (environmentParameters != null) {
            String description = environmentParameters.getString(EnvironmentParameters.description);
            String profile = StringUtils.emptyToNull(environmentParameters.getString(EnvironmentParameters.profile));
            List<ItemHolderParameters> propertyItemHolderParametersList = environmentParameters.getParametersList(EnvironmentParameters.properties);

            EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile);
            if (description != null) {
                environmentRule.setDescription(description);
            }

            if (propertyItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                    ItemRuleMap propertyItemRuleMap = asItemRuleMap(itemHolderParameters);
                    environmentRule.addPropertyItemRuleMap(propertyItemRuleMap);
                }
            }

            assistant.addEnvironmentRule(environmentRule);
        }
    }

    private void asTypeAliasRule(Parameters parameters) {
        if (parameters != null) {
            for (String alias : parameters.getParameterNameSet()) {
                assistant.addTypeAlias(alias, parameters.getString(alias));
            }
        }
    }

    private void asAspectRule(AspectParameters aspectParameters) throws IllegalRuleException {
        String description = aspectParameters.getString(AspectParameters.description);
        String id = StringUtils.emptyToNull(aspectParameters.getString(AspectParameters.id));
        String order = aspectParameters.getString(AspectParameters.order);
        Boolean isolated = aspectParameters.getBoolean(AspectParameters.isolated);
        Boolean disabled = aspectParameters.getBoolean(AspectParameters.disabled);

        AspectRule aspectRule = AspectRule.newInstance(id, order, isolated, disabled);
        if (description != null) {
            aspectRule.setDescription(description);
        }

        JoinpointParameters joinpointParameters = aspectParameters.getParameters(AspectParameters.joinpoint);
        if (joinpointParameters != null) {
            AspectRule.updateJoinpoint(aspectRule, joinpointParameters);
        }

        Parameters settingsParameters = aspectParameters.getParameters(AspectParameters.settings);
        if (settingsParameters != null) {
            SettingsAdviceRule settingsAdviceRule = SettingsAdviceRule.newInstance(aspectRule, settingsParameters);
            aspectRule.setSettingsAdviceRule(settingsAdviceRule);
        }

        AdviceParameters adviceParameters = aspectParameters.getParameters(AspectParameters.advice);
        if (adviceParameters != null) {
            String adviceBeanId = adviceParameters.getString(AdviceParameters.bean);
            if (!StringUtils.isEmpty(adviceBeanId)) {
                aspectRule.setAdviceBeanId(adviceBeanId);
            }

            AdviceActionParameters beforeAdviceParameters = adviceParameters.getParameters(AdviceParameters.beforeAdvice);
            if (beforeAdviceParameters != null) {
                ActionParameters actionParameters = beforeAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.BEFORE);
                    asActionRule(actionParameters, aspectAdviceRule);
                }
            }

            AdviceActionParameters afterAdviceParameters = adviceParameters.getParameters(AdviceParameters.afterAdvice);
            if (afterAdviceParameters != null) {
                ActionParameters actionParameters = afterAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AFTER);
                    asActionRule(actionParameters, aspectAdviceRule);
                }
            }

            AdviceActionParameters aroundAdviceParameters = adviceParameters.getParameters(AdviceParameters.aroundAdvice);
            if (aroundAdviceParameters != null) {
                ActionParameters actionParameters = aroundAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AROUND);
                    asActionRule(actionParameters, aspectAdviceRule);
                }
            }

            AdviceActionParameters finallyAdviceParameters = adviceParameters.getParameters(AdviceParameters.finallyAdvice);
            if (finallyAdviceParameters != null) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.FINALLY);
                ActionParameters actionParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.action);
                if (actionParameters != null) {
                    asActionRule(actionParameters, aspectAdviceRule);
                }
                // for thrown
                ExceptionThrownParameters etParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.thrown);
                if (etParameters != null) {
                    ExceptionThrownRule etr = asExceptionThrownRule(etParameters, aspectAdviceRule);
                    aspectAdviceRule.setExceptionThrownRule(etr);
                }
            }
        }

        ExceptionParameters exceptionParameters = aspectParameters.getParameters(AspectParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();
            exceptionRule.setDescription(exceptionParameters.getString(ExceptionParameters.description));
            List<ExceptionThrownParameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (ExceptionThrownParameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = asExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            aspectRule.setExceptionRule(exceptionRule);
        }

        assistant.resolveAdviceBeanClass(aspectRule);
        assistant.addAspectRule(aspectRule);
    }

    private void asBeanRule(BeanParameters beanParameters) throws IllegalRuleException {
        String description = beanParameters.getString(BeanParameters.description);
        String id = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.id));
        String className = StringUtils.emptyToNull(assistant.resolveAliasType(beanParameters.getString(BeanParameters.className)));
        String scan = beanParameters.getString(BeanParameters.scan);
        String mask = beanParameters.getString(BeanParameters.mask);
        String scope = beanParameters.getString(BeanParameters.scope);
        Boolean singleton = beanParameters.getBoolean(BeanParameters.singleton);
        String factoryBean = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryBean));
        String factoryMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.factoryMethod));
        String initMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.initMethod));
        String destroyMethod = StringUtils.emptyToNull(beanParameters.getString(BeanParameters.destroyMethod));
        Boolean lazyInit = beanParameters.getBoolean(BeanParameters.lazyInit);
        Boolean important = beanParameters.getBoolean(BeanParameters.important);
        ConstructorParameters constructorParameters = beanParameters.getParameters(BeanParameters.constructor);
        List<ItemHolderParameters> propertyItemHolderParametersList = beanParameters.getParametersList(BeanParameters.properties);
        FilterParameters filterParameters = beanParameters.getParameters(BeanParameters.filter);

        BeanRule beanRule;
        if (className == null && scan == null && factoryBean != null) {
            beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod, initMethod, destroyMethod, scope, singleton, lazyInit, important);
        } else {
            beanRule = BeanRule.newInstance(id, className, scan, mask, initMethod, destroyMethod, factoryMethod, scope, singleton, lazyInit, important);
        }
        if (description != null) {
            beanRule.setDescription(description);
        }
        if (filterParameters != null) {
            beanRule.setFilterParameters(filterParameters);
        }
        if (constructorParameters != null) {
            List<ItemHolderParameters> argumentItemHolderParametersList = constructorParameters.getParametersList(ConstructorParameters.arguments);
            if (argumentItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                    ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, beanRule.getConstructorArgumentItemRuleMap());
                    beanRule.setConstructorArgumentItemRuleMap(irm);
                }
            }
        }
        if (propertyItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, beanRule.getPropertyItemRuleMap());
                beanRule.setPropertyItemRuleMap(irm);
            }
        }

        assistant.resolveFactoryBeanClass(beanRule);
        assistant.addBeanRule(beanRule);
    }

    private void asScheduleRule(ScheduleParameters scheduleParameters) throws IllegalRuleException {
        String description = scheduleParameters.getString(AspectParameters.description);
        String id = StringUtils.emptyToNull(scheduleParameters.getString(AspectParameters.id));

        ScheduleRule scheduleRule = ScheduleRule.newInstance(id);
        if (description != null) {
            scheduleRule.setDescription(description);
        }

        SchedulerParameters schedulerParameters = scheduleParameters.getParameters(ScheduleParameters.scheduler);
        if (schedulerParameters != null) {
            String schedulerBeanId = schedulerParameters.getString(SchedulerParameters.bean);
            if (!StringUtils.isEmpty(schedulerBeanId)) {
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

                    translet = assistant.applyTransletNamePattern(translet);

                    ScheduledJobRule scheduledJobRule = ScheduledJobRule.newInstance(scheduleRule, translet, disabled);
                    scheduleRule.addScheduledJobRule(scheduledJobRule);
                }
            }
        }

        assistant.addScheduleRule(scheduleRule);
    }

    private void asTransletRule(TransletParameters transletParameters) throws IllegalRuleException {
        String description = transletParameters.getString(TransletParameters.description);
        String name = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.name));
        String scan = transletParameters.getString(TransletParameters.scan);
        String mask = transletParameters.getString(TransletParameters.mask);
        String method = transletParameters.getString(TransletParameters.method);

        TransletRule transletRule = TransletRule.newInstance(name, mask, scan, method);

        if (description != null) {
            transletRule.setDescription(description);
        }

        RequestParameters requestParameters = transletParameters.getParameters(TransletParameters.request);
        if (requestParameters != null) {
            RequestRule requestRule = asRequestRule(requestParameters);
            transletRule.setRequestRule(requestRule);
        }

        List<ItemHolderParameters> parameterItemHolderParametersList = transletParameters.getParametersList(TransletParameters.parameters);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                RequestRule requestRule = transletRule.touchRequestRule(false);
                irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
                requestRule.setParameterItemRuleMap(irm);
            }
        }

        List<ItemHolderParameters> attributeItemHolderParametersList = transletParameters.getParametersList(TransletParameters.attributes);
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                RequestRule requestRule = transletRule.touchRequestRule(false);
                irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
                requestRule.setAttributeItemRuleMap(irm);
            }
        }

        ContentsParameters contentsParameters = transletParameters.getParameters(TransletParameters.contents);
        if (contentsParameters != null) {
            ContentList contentList = asContentList(contentsParameters);
            transletRule.setContentList(contentList);
        }

        List<ContentParameters> contentParametersList = transletParameters.getParametersList(TransletParameters.content);
        if (contentParametersList != null) {
            ContentList contentList = new ContentList(false);
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = asActionList(contentParameters);
                contentList.addActionList(actionList);
            }
            transletRule.setContentList(contentList);
        }

        List<ResponseParameters> responseParametersList = transletParameters.getParametersList(TransletParameters.response);
        if (responseParametersList != null) {
            for (ResponseParameters responseParameters : responseParametersList) {
                ResponseRule responseRule = asResponseRule(responseParameters);
                transletRule.addResponseRule(responseRule);
            }
        }

        ExceptionParameters exceptionParameters = transletParameters.getParameters(TransletParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();
            exceptionRule.setDescription(exceptionParameters.getString(ExceptionParameters.description));
            List<ExceptionThrownParameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (ExceptionThrownParameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = asExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            transletRule.setExceptionRule(exceptionRule);
        }

        List<ActionParameters> actionParametersList = transletParameters.getParametersList(TransletParameters.action);
        if (actionParametersList != null) {
            ContentList contentList = new ContentList(false);
            ActionList actionList = new ActionList(false);
            contentList.addActionList(actionList);
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
            transletRule.setContentList(contentList);
        }

        TransformParameters transformParameters = transletParameters.getParameters(TransletParameters.transform);
        if (transformParameters != null) {
            TransformRule transformRule = asTransformRule(transformParameters);
            transletRule.applyResponseRule(transformRule);
        }

        DispatchParameters dispatchParameters = transletParameters.getParameters(TransletParameters.dispatch);
        if (dispatchParameters != null) {
            DispatchRule dispatchRule = asDispatchRule(dispatchParameters);
            transletRule.applyResponseRule(dispatchRule);
        }

        ForwardParameters forwardParameters = transletParameters.getParameters(TransletParameters.forward);
        if (forwardParameters != null) {
            ForwardRule forwardRule = asForwardRule(forwardParameters);
            transletRule.applyResponseRule(forwardRule);
        }

        RedirectParameters redirectParameters = transletParameters.getParameters(TransletParameters.redirect);
        if (redirectParameters != null) {
            RedirectRule redirectRule = asRedirectRule(redirectParameters);
            transletRule.applyResponseRule(redirectRule);
        }

        List<ChooseParameters> chooseParametersList = transletParameters.getParametersList(TransletParameters.choose);
        if (chooseParametersList != null) {
            ChooseRuleMap chooseRuleMap = transletRule.touchChooseRuleMap();
            for (ChooseParameters chooseParameters : chooseParametersList) {
                int caseNo = chooseParameters.getInt(ChooseParameters.caseNo);
                ChooseRule chooseRule = chooseRuleMap.newChooseRule(caseNo);
                List<ChooseWhenParameters> chooseWhenParametersList = chooseParameters.getParametersList(ChooseParameters.when);
                for (ChooseWhenParameters chooseWhenParameters : chooseWhenParametersList) {
                    caseNo = chooseWhenParameters.getInt(ChooseWhenParameters.caseNo);
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule(caseNo);
                    chooseWhenRule.setExpression(chooseWhenParameters.getString(ChooseWhenParameters.test));

                    transformParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.transform);
                    if (transformParameters != null) {
                        TransformRule transformRule = asTransformRule(transformParameters);
                        chooseWhenRule.applyResponseRule(transformRule);
                    }

                    dispatchParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.dispatch);
                    if (dispatchParameters != null) {
                        DispatchRule dispatchRule = asDispatchRule(dispatchParameters);
                        chooseWhenRule.applyResponseRule(dispatchRule);
                    }

                    redirectParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.redirect);
                    if (redirectParameters != null) {
                        RedirectRule redirectRule = asRedirectRule(redirectParameters);
                        chooseWhenRule.applyResponseRule(redirectRule);
                    }

                    forwardParameters = chooseWhenParameters.getParameters(ChooseWhenParameters.forward);
                    if (forwardParameters != null) {
                        ForwardRule forwardRule = asForwardRule(forwardParameters);
                        chooseWhenRule.applyResponseRule(forwardRule);
                    }
                }

                ChooseWhenParameters caseOtherwiseParameters = chooseParameters.getParameters(ChooseParameters.otherwise);
                if (caseOtherwiseParameters != null) {
                    caseNo = caseOtherwiseParameters.getInt(ChooseWhenParameters.caseNo);
                    ChooseWhenRule chooseWhenRule = chooseRule.newChooseWhenRule(caseNo);
                    transformParameters = caseOtherwiseParameters.getParameters(ChooseWhenParameters.transform);
                    if (transformParameters != null) {
                        TransformRule transformRule = asTransformRule(transformParameters);
                        chooseWhenRule.applyResponseRule(transformRule);
                    }

                    dispatchParameters = caseOtherwiseParameters.getParameters(ChooseWhenParameters.dispatch);
                    if (dispatchParameters != null) {
                        DispatchRule dispatchRule = asDispatchRule(dispatchParameters);
                        chooseWhenRule.applyResponseRule(dispatchRule);
                    }

                    forwardParameters = caseOtherwiseParameters.getParameters(ChooseWhenParameters.forward);
                    if (forwardParameters != null) {
                        ForwardRule forwardRule = asForwardRule(forwardParameters);
                        chooseWhenRule.applyResponseRule(forwardRule);
                    }

                    redirectParameters = caseOtherwiseParameters.getParameters(ChooseWhenParameters.redirect);
                    if (redirectParameters != null) {
                        RedirectRule redirectRule = asRedirectRule(redirectParameters);
                        chooseWhenRule.applyResponseRule(redirectRule);
                    }
                }
            }

            for (ChooseRule chooseRule : chooseRuleMap.values()) {
                if (chooseRule.getChooseWhenRuleMap() != null) {
                   for (ChooseWhenRule chooseWhenRule : chooseRule.getChooseWhenRuleMap().values()) {
                       if (transletRule.getContentList() != null) {
                           for (ActionList actionList : transletRule.getContentList()) {
                               checkActionList(chooseRule, chooseWhenRule, actionList);
                           }
                       }
                       if (transletRule.getResponseRule() != null) {
                           Response response = transletRule.getResponseRule().getResponse();
                           if (response != null && response.getActionList() != null) {
                               checkActionList(chooseRule, chooseWhenRule, response.getActionList());
                           }
                       }
                   }
                }
            }
        }

        assistant.addTransletRule(transletRule);
    }

    private void checkActionList(ChooseRule chooseRule, ChooseWhenRule chooseWhenRule, ActionList actionList) throws IllegalRuleException {
        Executable prev = null;
        for (Executable action : actionList) {
            if (action.getCaseNo() == chooseWhenRule.getCaseNo()) {
                if (prev != null) {
                    prev.setLastInChooseWhen(false);
                }
                action.setLastInChooseWhen(true);
                prev = action;
            } else {
                prev = null;
                if (action.getCaseNo() > 0 && chooseRule.getChooseWhenRule(action.getCaseNo()) == null) {
                    throw new IllegalRuleException("No matching case number: " + action.getCaseNo());
                }
            }
        }
    }

    private RequestRule asRequestRule(RequestParameters requestParameters) throws IllegalRuleException {
        String allowedMethod = requestParameters.getString(RequestParameters.method);
        String encoding = requestParameters.getString(RequestParameters.encoding);
        List<ItemHolderParameters> parameterItemHolderParametersList = requestParameters.getParametersList(RequestParameters.parameters);
        List<ItemHolderParameters> attributeItemHolderParametersList = requestParameters.getParametersList(RequestParameters.attributes);

        RequestRule requestRule = RequestRule.newInstance(allowedMethod, encoding);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, requestRule.getParameterItemRuleMap());
                requestRule.setParameterItemRuleMap(irm);
            }
        }
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, requestRule.getAttributeItemRuleMap());
                requestRule.setAttributeItemRuleMap(irm);
            }
        }
        return requestRule;
    }

    private ResponseRule asResponseRule(ResponseParameters responseParameters) throws IllegalRuleException {
        String name = responseParameters.getString(ResponseParameters.name);
        String encoding = responseParameters.getString(ResponseParameters.encoding);

        ResponseRule responseRule = ResponseRule.newInstance(name, encoding);

        TransformParameters transformParameters = responseParameters.getParameters(ResponseParameters.transform);
        if (transformParameters != null) {
            responseRule.applyResponseRule(asTransformRule(transformParameters));
        }

        DispatchParameters dispatchParameters = responseParameters.getParameters(ResponseParameters.dispatch);
        if (dispatchParameters != null) {
            responseRule.applyResponseRule(asDispatchRule(dispatchParameters));
        }

        ForwardParameters forwardParameters = responseParameters.getParameters(ResponseParameters.forward);
        if (forwardParameters != null) {
            responseRule.applyResponseRule(asForwardRule(forwardParameters));
        }

        RedirectParameters redirectParameters = responseParameters.getParameters(ResponseParameters.redirect);
        if (redirectParameters != null) {
            responseRule.applyResponseRule(asRedirectRule(redirectParameters));
        }

        return responseRule;
    }

    private ContentList asContentList(ContentsParameters contentsParameters) throws IllegalRuleException {
        String name = contentsParameters.getString(ContentsParameters.name);
        List<ContentParameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.content);

        ContentList contentList = ContentList.newInstance(name);
        if (contentParametersList != null) {
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = asActionList(contentParameters);
                contentList.addActionList(actionList);
            }
        }
        return contentList;
    }

    private ActionList asActionList(ContentParameters contentParameters) throws IllegalRuleException {
        String name = contentParameters.getString(ContentParameters.name);
        List<ActionParameters> actionParametersList = contentParameters.getParametersList(ContentParameters.action);

        ActionList actionList = ActionList.newInstance(name);
        if (actionParametersList != null) {
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
        }
        return actionList;
    }

    private void asActionRule(ActionParameters actionParameters, ActionRuleApplicable actionRuleApplicable) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
        String method = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.method));
        String include = actionParameters.getString(ActionParameters.include);
        ItemHolderParameters echoItemHolderParameters = actionParameters.getParameters(ActionParameters.echo);
        ItemHolderParameters headerItemHolderParameters = actionParameters.getParameters(ActionParameters.headers);
        Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);
        Integer caseNo = actionParameters.getInt(ActionParameters.caseNo);
        Executable action = null;

        if (include != null) {
            include = assistant.applyTransletNamePattern(include);
            IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, method, hidden);
            List<ItemHolderParameters> parameterItemHolderParametersList = actionParameters.getParametersList(ActionParameters.parameters);
            if (parameterItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                    ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, includeActionRule.getParameterItemRuleMap());
                    includeActionRule.setParameterItemRuleMap(irm);
                }
            }
            List<ItemHolderParameters> attributeItemHolderParametersList = actionParameters.getParametersList(ActionParameters.attributes);
            if (attributeItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                    ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, includeActionRule.getAttributeItemRuleMap());
                    includeActionRule.setAttributeItemRuleMap(irm);
                }
            }
            action = actionRuleApplicable.applyActionRule(includeActionRule);
        } else if (method != null) {
            String beanIdOrClass = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.bean));
            BeanMethodActionRule beanMethodActionRule = BeanMethodActionRule.newInstance(id, beanIdOrClass, method, hidden);
            List<ItemHolderParameters> argumentItemHolderParametersList = actionParameters.getParametersList(ActionParameters.arguments);
            if (argumentItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : argumentItemHolderParametersList) {
                    ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, beanMethodActionRule.getArgumentItemRuleMap());
                    beanMethodActionRule.setArgumentItemRuleMap(irm);
                }
            }
            List<ItemHolderParameters> propertyItemHolderParametersList = actionParameters.getParametersList(ActionParameters.properties);
            if (propertyItemHolderParametersList != null) {
                for (ItemHolderParameters itemHolderParameters : propertyItemHolderParametersList) {
                    ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                    irm = assistant.profiling(irm, beanMethodActionRule.getPropertyItemRuleMap());
                    beanMethodActionRule.setPropertyItemRuleMap(irm);
                }
            }
            assistant.resolveActionBeanClass(beanMethodActionRule);
            action = actionRuleApplicable.applyActionRule(beanMethodActionRule);
        } else if (echoItemHolderParameters != null) {
            EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
            ItemRuleMap attributeItemRuleMap = asItemRuleMap(echoItemHolderParameters);
            echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
            action = actionRuleApplicable.applyActionRule(echoActionRule);
        } else if (headerItemHolderParameters != null) {
            HeaderActionRule headerActionRule = HeaderActionRule.newInstance(id, hidden);
            ItemRuleMap headerItemRuleMap = asItemRuleMap(headerItemHolderParameters);
            headerActionRule.setHeaderItemRuleMap(headerItemRuleMap);
            action = actionRuleApplicable.applyActionRule(headerActionRule);
        }
        if (action != null && caseNo != null) {
            action.setCaseNo(caseNo);
        }
    }

    private ExceptionThrownRule asExceptionThrownRule(ExceptionThrownParameters exceptionThrownParameters, AspectAdviceRule aspectAdviceRule)
            throws IllegalRuleException {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule(aspectAdviceRule);

        String[] exceptionTypes = exceptionThrownParameters.getStringArray(ExceptionThrownParameters.type);
        exceptionThrownRule.setExceptionTypes(exceptionTypes);

        ActionParameters actionParameters = exceptionThrownParameters.getParameters(ExceptionThrownParameters.action);
        if (actionParameters != null) {
            asActionRule(actionParameters, exceptionThrownRule);
        }

        List<TransformParameters> transformParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.transform);
        if (transformParametersList != null && !transformParametersList.isEmpty()) {
            asTransformRule(transformParametersList, exceptionThrownRule);
        }

        List<DispatchParameters> dispatchParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.dispatch);
        if (dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
            asDispatchRule(dispatchParametersList, exceptionThrownRule);
        }

        List<RedirectParameters> redirectParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.redirect);
        if (redirectParametersList != null && !redirectParametersList.isEmpty()) {
            asRedirectRule(redirectParametersList, exceptionThrownRule);
        }

        return exceptionThrownRule;
    }

    private void asTransformRule(List<TransformParameters> transformParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (TransformParameters transformParameters : transformParametersList) {
            TransformRule transformRule = asTransformRule(transformParameters);
            responseRuleApplicable.applyResponseRule(transformRule);
        }
    }

    private TransformRule asTransformRule(TransformParameters transformParameters) throws IllegalRuleException {
        String transformType = transformParameters.getString(TransformParameters.type);
        String contentType = transformParameters.getString(TransformParameters.contentType);
        String encoding = transformParameters.getString(TransformParameters.encoding);
        List<ActionParameters> actionParametersList = transformParameters.getParametersList(TransformParameters.action);
        Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
        Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
        TemplateParameters templateParameters = transformParameters.getParameters(TransformParameters.template);
        CallParameters callParameters = transformParameters.getParameters(TransformParameters.call);

        TransformRule transformRule = TransformRule.newInstance(transformType, contentType, encoding, defaultResponse, pretty);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList(false);
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
            transformRule.setActionList(actionList);
        }
        if (callParameters != null) {
            String templateId = StringUtils.emptyToNull(callParameters.getString(CallParameters.template));
            if (templateId !=null) {
                TransformRule.updateTemplateId(transformRule, templateId);
            }
        }
        if (templateParameters != null) {
            String engine = templateParameters.getString(TemplateParameters.engine);
            String name = templateParameters.getString(TemplateParameters.name);
            String file = templateParameters.getString(TemplateParameters.file);
            String resource = templateParameters.getString(TemplateParameters.resource);
            String url = templateParameters.getString(TemplateParameters.url);
            String content = templateParameters.getString(TemplateParameters.content);
            String style = templateParameters.getString(TemplateParameters.style);
            String encoding2 = templateParameters.getString(TemplateParameters.encoding);
            Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);

            TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(engine, name, file, resource, url, content, style, encoding2, noCache);
            transformRule.setTemplateRule(templateRule);
            assistant.resolveBeanClass(templateRule.getTemplateTokens());
        }
        return transformRule;
    }

    private void asDispatchRule(List<DispatchParameters> dispatchParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (DispatchParameters dispatchParameters : dispatchParametersList) {
            DispatchRule dispatchRule = asDispatchRule(dispatchParameters);
            responseRuleApplicable.applyResponseRule(dispatchRule);
        }
    }

    private DispatchRule asDispatchRule(DispatchParameters dispatchParameters) throws IllegalRuleException {
        String name = dispatchParameters.getString(DispatchParameters.name);
        String dispatcherName = dispatchParameters.getString(DispatchParameters.dispatcher);
        String contentType = dispatchParameters.getString(DispatchParameters.contentType);
        String encoding = dispatchParameters.getString(DispatchParameters.encoding);
        List<ActionParameters> actionParametersList = dispatchParameters.getParametersList(DispatchParameters.action);
        Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);

        DispatchRule dispatchRule = DispatchRule.newInstance(name, dispatcherName, contentType, encoding, defaultResponse);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList(false);
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
            dispatchRule.setActionList(actionList);
        }
        return dispatchRule;
    }

    private ForwardRule asForwardRule(ForwardParameters forwardParameters) throws IllegalRuleException {
        String contentType = forwardParameters.getString(ForwardParameters.contentType);
        String translet = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.translet));
        String method = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.method));
        List<ItemHolderParameters> attributeItemHolderParametersList = forwardParameters.getParameters(ForwardParameters.attributes);
        List<ActionParameters> actionParametersList = forwardParameters.getParametersList(ForwardParameters.action);
        Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);

        translet = assistant.applyTransletNamePattern(translet);

        ForwardRule forwardRule = ForwardRule.newInstance(contentType, translet, method, defaultResponse);
        if (attributeItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : attributeItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, forwardRule.getAttributeItemRuleMap());
                forwardRule.setAttributeItemRuleMap(irm);
            }
        }
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList(false);
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
            forwardRule.setActionList(actionList);
        }
        return forwardRule;
    }

    private void asRedirectRule(List<RedirectParameters> redirectParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (RedirectParameters redirectParameters : redirectParametersList) {
            RedirectRule redirectRule = asRedirectRule(redirectParameters);
            responseRuleApplicable.applyResponseRule(redirectRule);
        }
    }

    private RedirectRule asRedirectRule(RedirectParameters redirectParameters) throws IllegalRuleException {
        String contentType = redirectParameters.getString(RedirectParameters.contentType);
        String path = redirectParameters.getString(RedirectParameters.path);
        List<ItemHolderParameters> parameterItemHolderParametersList = redirectParameters.getParametersList(RedirectParameters.parameters);
        String encoding = redirectParameters.getString(RedirectParameters.encoding);
        Boolean excludeNullParameters = redirectParameters.getBoolean(RedirectParameters.excludeNullParameters);
        Boolean excludeEmptyParameters = redirectParameters.getBoolean(RedirectParameters.excludeEmptyParameters);
        Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
        List<ActionParameters> actionParametersList = redirectParameters.getParametersList(RedirectParameters.action);

        RedirectRule redirectRule = RedirectRule.newInstance(contentType, path, encoding, excludeNullParameters, excludeEmptyParameters, defaultResponse);
        if (parameterItemHolderParametersList != null) {
            for (ItemHolderParameters itemHolderParameters : parameterItemHolderParametersList) {
                ItemRuleMap irm = asItemRuleMap(itemHolderParameters);
                irm = assistant.profiling(irm, redirectRule.getParameterItemRuleMap());
                redirectRule.setParameterItemRuleMap(irm);
            }
        }
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList(false);
            for (ActionParameters actionParameters : actionParametersList) {
                asActionRule(actionParameters, actionList);
            }
            redirectRule.setActionList(actionList);
        }
        return redirectRule;
    }

    private ItemRuleMap asItemRuleMap(ItemHolderParameters itemHolderParameters) throws IllegalRuleException {
        String profile = itemHolderParameters.getProfile();
        List<ItemParameters> itemParametersList = itemHolderParameters.getItemParametersList();
        ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(itemParametersList);
        if (itemRuleMap != null) {
            itemRuleMap.setProfile(profile);
            for (ItemRule itemRule : itemRuleMap.values()) {
                assistant.resolveBeanClass(itemRule);
            }
        }
        return itemRuleMap;
    }

    private void asTemplateRule(TemplateParameters templateParameters) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.id));
        String engine = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.engine));
        String name = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.name));
        String file = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.file));
        String resource = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.resource));
        String url = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.url));
        String content = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.content));
        String style = StringUtils.emptyToNull(templateParameters.getString(TemplateParameters.style));
        String encoding = templateParameters.getString(TemplateParameters.encoding);
        Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);

        TemplateRule templateRule = TemplateRule.newInstance(id, engine, name, file, resource, url, content, style, encoding, noCache);
        assistant.addTemplateRule(templateRule);
    }
    
}