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
package com.aspectran.core.context.rule.converter;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
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
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
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
import com.aspectran.core.context.rule.params.SettingsParameters;
import com.aspectran.core.context.rule.params.TemplateParameters;
import com.aspectran.core.context.rule.params.TransformParameters;
import com.aspectran.core.context.rule.params.TransletParameters;
import com.aspectran.core.context.rule.params.TriggerExpressionParameters;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.params.TypeAliasesParameters;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.core.util.TextStyler;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.Parameter;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.VariableParameters;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Converts rules for context configuration into {@code Parameters} objects.
 *
 * <p>Created: 2017. 5. 5.</p>
 */
public class RulesToParameters {

    private RulesToParameters() {
    }

    @NonNull
    public static RootParameters toRootParameters(ActivityRuleAssistant assistant) {
        RootParameters rootParameters = new RootParameters();
        rootParameters.putValue(RootParameters.aspectran, toAspectranParameters(assistant));
        return rootParameters;
    }

    @NonNull
    private static AspectranParameters toAspectranParameters(@NonNull ActivityRuleAssistant assistant) {
        AspectranParameters aspectranParameters = new AspectranParameters();

        AssistantLocal assistantLocal = assistant.getAssistantLocal();
        if (assistantLocal.getDescriptionRule() != null) {
            toDescriptionParameters(assistantLocal.getDescriptionRule(), aspectranParameters, AspectranParameters.description);
        }

        SettingsParameters settingsParameters = toDefaultSettings(assistantLocal);
        aspectranParameters.putValueNonNull(AspectranParameters.settings, settingsParameters);

        Map<String, String> typeAliases = assistant.getTypeAliases();
        if (!typeAliases.isEmpty()) {
            TypeAliasesParameters typeAliasesParameters = aspectranParameters.newParameters(AspectranParameters.typeAliases);
            for (Map.Entry<String, String> entry : typeAliases.entrySet()) {
                typeAliasesParameters.putTypeAlias(entry.getKey(), entry.getValue());
            }
        }

        List<EnvironmentRule> environmentRules = assistant.getEnvironmentRules();
        if (!environmentRules.isEmpty()) {
            for (EnvironmentRule environmentRule : environmentRules) {
                EnvironmentParameters ps = toEnvironmentParameters(environmentRule);
                aspectranParameters.putValue(AspectranParameters.environment, ps);
            }
        }

        for (AspectRule aspectRule : assistant.getAspectRules()) {
            AspectParameters ps = toAspectParameters(aspectRule);
            aspectranParameters.putValue(AspectranParameters.aspect, ps);
        }

        for (BeanRule beanRule : assistant.getBeanRules()) {
            BeanParameters ps = toBeanParameters(beanRule);
            aspectranParameters.putValue(AspectranParameters.bean, ps);
        }

        for (ScheduleRule scheduleRule : assistant.getScheduleRules()) {
            ScheduleParameters ps = toScheduleParameters(scheduleRule);
            aspectranParameters.putValue(AspectranParameters.schedule, ps);
        }

        for (TransletRule transletRule : assistant.getTransletRules()) {
            TransletParameters ps = toTransletParameters(transletRule);
            aspectranParameters.putValue(AspectranParameters.translet, ps);
        }

        for (TemplateRule templateRule : assistant.getTemplateRules()) {
            TemplateParameters ps = toTemplateParameters(templateRule);
            aspectranParameters.putValue(AspectranParameters.template, ps);
        }

        List<RuleAppender> appenders = assistant.getRuleAppendHandler().getPendingList();
        if (appenders != null) {
            for (RuleAppender appender : appenders) {
                AppendParameters ps = toAppendParameters(appender);
                aspectranParameters.putValue(AspectranParameters.append, ps);
            }
        }

        return aspectranParameters;
    }

    public static void toDescriptionParameters(DescriptionRule descriptionRule, Parameters parameters, ParameterKey key) {
        if (descriptionRule == null) {
            throw new IllegalArgumentException("descriptionRule must not be null");
        }

        if (descriptionRule.getCandidates() != null) {
            for (DescriptionRule dr : descriptionRule.getCandidates()) {
                DescriptionParameters descriptionParameters = new DescriptionParameters(dr);
                parameters.putValue(key, descriptionParameters);
            }
        } else {
            DescriptionParameters descriptionParameters = new DescriptionParameters(descriptionRule);
            parameters.putValue(key, descriptionParameters);
        }
    }

    @Nullable
    private static SettingsParameters toDefaultSettings(@NonNull AssistantLocal assistantLocal) {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        if (defaultSettings != null) {
            SettingsParameters settingsParameters = new SettingsParameters();
            if (defaultSettings.getTransletNamePrefix() != null) {
                settingsParameters.putSetting(DefaultSettingType.TRANSLET_NAME_PREFIX.toString(), defaultSettings.getTransletNamePrefix());
            }
            if (defaultSettings.getTransletNameSuffix() != null) {
                settingsParameters.putSetting(DefaultSettingType.TRANSLET_NAME_SUFFIX.toString(), defaultSettings.getTransletNameSuffix());
            }
            if (defaultSettings.getPointcutPatternVerifiable() != null) {
                settingsParameters.putSetting(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE.toString(), defaultSettings.getPointcutPatternVerifiable());
            }
            if (defaultSettings.getDefaultTemplateEngineBean() != null) {
                settingsParameters.putSetting(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN.toString(), defaultSettings.getDefaultTemplateEngineBean());
            }
            if (defaultSettings.getDefaultSchedulerBean() != null) {
                settingsParameters.putSetting(DefaultSettingType.DEFAULT_SCHEDULER_BEAN.toString(), defaultSettings.getDefaultSchedulerBean());
            }
            return settingsParameters;
        } else {
            return null;
        }
    }

    @NonNull
    private static AppendParameters toAppendParameters(@NonNull RuleAppender appender) {
        AppendRule appendRule = appender.getAppendRule();
        if (appendRule == null) {
            throw new IllegalArgumentException("Every appender except Root Appender requires an AppendRule");
        }
        return toAppendParameters(appendRule);
    }

    @NonNull
    public static AppendParameters toAppendParameters(AppendRule appendRule) {
        if (appendRule == null) {
            throw new IllegalArgumentException("appendRule must not be null");
        }

        AppendParameters appendParameters = new AppendParameters();
        appendParameters.putValueNonNull(AppendParameters.file, appendRule.getFile());
        appendParameters.putValueNonNull(AppendParameters.resource, appendRule.getResource());
        appendParameters.putValueNonNull(AppendParameters.url, appendRule.getUrl());
        appendParameters.putValueNonNull(AppendParameters.format, appendRule.getFormat());
        appendParameters.putValueNonNull(AppendParameters.profile, appendRule.getProfile());
        appendParameters.putValueNonNull(AppendParameters.aspectran, appendRule.getAspectranParameters());
        return appendParameters;
    }

    @NonNull
    public static EnvironmentParameters toEnvironmentParameters(EnvironmentRule environmentRule) {
        if (environmentRule == null) {
            throw new IllegalArgumentException("environmentRule must not be null");
        }

        EnvironmentParameters environmentParameters = new EnvironmentParameters();
        if (environmentRule.getDescriptionRule() != null) {
            toDescriptionParameters(environmentRule.getDescriptionRule(), environmentParameters, EnvironmentParameters.description);
        }
        environmentParameters.putValueNonNull(EnvironmentParameters.profile, environmentRule.getProfile());
        if (environmentRule.getPropertyItemRuleMapList() != null) {
            for (ItemRuleMap propertyItemRuleMap : environmentRule.getPropertyItemRuleMapList()) {
                toItemHolderParameters(propertyItemRuleMap, environmentParameters, EnvironmentParameters.properties);
            }
        }
        return environmentParameters;
    }

    @NonNull
    public static AspectParameters toAspectParameters(AspectRule aspectRule) {
        if (aspectRule == null) {
            throw new IllegalArgumentException("aspectRule must not be null");
        }

        AspectParameters aspectParameters = new AspectParameters();
        if (aspectRule.getDescriptionRule() != null) {
            toDescriptionParameters(aspectRule.getDescriptionRule(), aspectParameters, AspectParameters.description);
        }
        aspectParameters.putValueNonNull(AspectParameters.id, aspectRule.getId());
        if (aspectRule.getOrder() != Integer.MAX_VALUE) {
            aspectParameters.putValueNonNull(AspectParameters.order, aspectRule.getOrder());
        }
        aspectParameters.putValueNonNull(AspectParameters.isolated, aspectRule.getIsolated());
        aspectParameters.putValueNonNull(AspectParameters.disabled, aspectRule.getDisabled());

        if (aspectRule.getJoinpointRule() != null) {
            JoinpointParameters joinpointParameters = aspectRule.getJoinpointRule().getJoinpointParameters();
            if (joinpointParameters != null) {
                joinpointParameters.putValueNonNull(JoinpointParameters.target, aspectRule.getJoinpointTargetType());
                aspectParameters.putValue(AspectParameters.joinpoint, joinpointParameters);
            } else {
                joinpointParameters = aspectParameters.newParameters(AspectParameters.joinpoint);
                joinpointParameters.putValueNonNull(JoinpointParameters.target, aspectRule.getJoinpointTargetType());
            }
        }

        if (aspectRule.getSettingsAdviceRule() != null) {
            Map<String, Object> settings = aspectRule.getSettingsAdviceRule().getSettings();
            if (settings != null) {
                SettingsParameters settingsParameters = aspectParameters.newParameters(AspectParameters.settings);
                for (Map.Entry<String, Object> entry : settings.entrySet()) {
                    settingsParameters.putSetting(entry.getKey(), entry.getValue());
                }
            }
        }

        if (aspectRule.getAspectAdviceRuleList() != null) {
            AdviceParameters adviceParameters = aspectParameters.newParameters(AspectParameters.advice);
            adviceParameters.putValueNonNull(AdviceParameters.bean, aspectRule.getAdviceBeanId());
            for (AspectAdviceRule aspectAdviceRule : aspectRule.getAspectAdviceRuleList()) {
                if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.beforeAdvice);
                    if (aspectAdviceRule.getExecutableAction() != null) {
                        toActionParameters(aspectAdviceRule.getExecutableAction(), adviceActionParameters);
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.afterAdvice);
                    if (aspectAdviceRule.getExecutableAction() != null) {
                        toActionParameters(aspectAdviceRule.getExecutableAction(), adviceActionParameters);
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.aroundAdvice);
                    if (aspectAdviceRule.getExecutableAction() != null) {
                        toActionParameters(aspectAdviceRule.getExecutableAction(), adviceActionParameters);
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.finallyAdvice);
                    if (aspectAdviceRule.getExceptionThrownRule() != null) {
                        adviceActionParameters.putValue(AdviceActionParameters.thrown,
                                toExceptionThrownParameters(aspectAdviceRule.getExceptionThrownRule()));
                    }
                    if (aspectAdviceRule.getExecutableAction() != null) {
                        toActionParameters(aspectAdviceRule.getExecutableAction(), adviceActionParameters);
                    }
                }
            }
        }

        ExceptionRule exceptionRule = aspectRule.getExceptionRule();
        if (exceptionRule != null) {
            ExceptionParameters exceptionParameters = aspectParameters.touchParameters(AspectParameters.exception);
            if (exceptionRule.getDescriptionRule() != null) {
                toDescriptionParameters(exceptionRule.getDescriptionRule(), exceptionParameters, ExceptionParameters.description);
            }
            for (ExceptionThrownRule etr : exceptionRule.getExceptionThrownRuleList()) {
                exceptionParameters.putValue(ExceptionParameters.thrown, toExceptionThrownParameters(etr));
            }
        }

        return aspectParameters;
    }

    @NonNull
    public static BeanParameters toBeanParameters(BeanRule beanRule) {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }

        BeanParameters beanParameters = new BeanParameters();
        if (beanRule.getDescriptionRule() != null) {
            toDescriptionParameters(beanRule.getDescriptionRule(), beanParameters, BeanParameters.description);
        }
        beanParameters.putValueNonNull(BeanParameters.id, beanRule.getId());
        beanParameters.putValueNonNull(BeanParameters.className, beanRule.getClassName());
        beanParameters.putValueNonNull(BeanParameters.scan, beanRule.getScanPattern());
        beanParameters.putValueNonNull(BeanParameters.mask, beanRule.getMaskPattern());
        if (beanRule.getSingleton() == Boolean.TRUE && beanRule.getScopeType() == ScopeType.SINGLETON) {
            beanParameters.putValue(BeanParameters.singleton, beanRule.getSingleton());
        } else if (beanRule.getScopeType() != null) {
            beanParameters.putValue(BeanParameters.scope, beanRule.getScopeType().toString());
        }
        beanParameters.putValueNonNull(BeanParameters.factoryBean, beanRule.getFactoryBeanId());
        beanParameters.putValueNonNull(BeanParameters.factoryMethod, beanRule.getFactoryMethodName());
        beanParameters.putValueNonNull(BeanParameters.initMethod, beanRule.getInitMethodName());
        beanParameters.putValueNonNull(BeanParameters.destroyMethod, beanRule.getDestroyMethodName());
        beanParameters.putValueNonNull(BeanParameters.lazyInit, beanRule.getLazyInit());
        beanParameters.putValueNonNull(BeanParameters.important, beanRule.getImportant());
        beanParameters.putValueNonNull(BeanParameters.filter, beanRule.getFilterParameters());

        ItemRuleMap constructorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
        if (constructorArgumentItemRuleMap != null) {
            toItemHolderParameters(constructorArgumentItemRuleMap, beanParameters, BeanParameters.arguments);
        }

        ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
        if (propertyItemRuleMap != null) {
            toItemHolderParameters(propertyItemRuleMap, beanParameters, BeanParameters.properties);
        }

        return beanParameters;
    }

    @NonNull
    public static ScheduleParameters toScheduleParameters(ScheduleRule scheduleRule) {
        return toScheduleParameters(scheduleRule, null);
    }

    @NonNull
    public static ScheduleParameters toScheduleParameters(ScheduleRule scheduleRule, ScheduledJobRule scheduledJobRule) {
        if (scheduleRule == null) {
            throw new IllegalArgumentException("scheduleRule must not be null");
        }

        ScheduleParameters scheduleParameters = new ScheduleParameters();
        if (scheduleRule.getDescriptionRule() != null) {
            toDescriptionParameters(scheduleRule.getDescriptionRule(), scheduleParameters, ScheduleParameters.description);
        }
        scheduleParameters.putValueNonNull(ScheduleParameters.id, scheduleRule.getId());

        SchedulerParameters schedulerParameters = scheduleParameters.newParameters(ScheduleParameters.scheduler);
        schedulerParameters.putValueNonNull(SchedulerParameters.bean, scheduleRule.getSchedulerBeanId());

        TriggerExpressionParameters expressionParameters = scheduleRule.getTriggerExpressionParameters();
        if (expressionParameters != null && scheduleRule.getTriggerType() != null) {
            TriggerParameters triggerParameters = schedulerParameters.newParameters(SchedulerParameters.trigger);
            triggerParameters.putValue(TriggerParameters.type, scheduleRule.getTriggerType());
            triggerParameters.putValue(TriggerParameters.expression, expressionParameters);
        }

        if (scheduledJobRule == null) {
            List<ScheduledJobRule> scheduledJobRuleList = scheduleRule.getScheduledJobRuleList();
            if (scheduledJobRuleList != null) {
                for (ScheduledJobRule jobRule : scheduledJobRuleList) {
                    scheduleParameters.putValue(ScheduleParameters.job, toScheduledJobParameters(jobRule));
                }
            }
        } else {
            scheduleParameters.putValue(ScheduleParameters.job, toScheduledJobParameters(scheduledJobRule));
        }

        return scheduleParameters;
    }

    @NonNull
    public static ScheduledJobParameters toScheduledJobParameters(ScheduledJobRule scheduledJobRule) {
        if (scheduledJobRule == null) {
            throw new IllegalArgumentException("scheduledJobRule must not be null");
        }

        ScheduledJobParameters scheduledJobParameters = new ScheduledJobParameters();
        scheduledJobParameters.putValue(ScheduledJobParameters.translet, scheduledJobRule.getTransletName());
        scheduledJobParameters.putValueNonNull(ScheduledJobParameters.disabled, scheduledJobRule.getDisabled());
        return scheduledJobParameters;
    }

    @NonNull
    public static TransletParameters toTransletParameters(TransletRule transletRule) {
        if (transletRule == null) {
            throw new IllegalArgumentException("transletRule must not be null");
        }

        TransletParameters transletParameters = new TransletParameters();
        if (transletRule.getDescriptionRule() != null) {
            toDescriptionParameters(transletRule.getDescriptionRule(), transletParameters, TransletParameters.description);
        }
        transletParameters.putValueNonNull(TransletParameters.name, transletRule.getName());
        transletParameters.putValueNonNull(TransletParameters.scan, transletRule.getScanPath());
        transletParameters.putValueNonNull(TransletParameters.mask, transletRule.getMaskPattern());
        if (transletRule.getAllowedMethods() != null) {
            transletParameters.putValue(TransletParameters.method, MethodType.stringify(transletRule.getAllowedMethods()));
        }

        RequestRule requestRule = transletRule.getRequestRule();
        if (requestRule != null) {
            if (requestRule.isExplicit()) {
                RequestParameters requestParameters = transletParameters.newParameters(TransletParameters.request);
                requestParameters.putValueNonNull(RequestParameters.method, requestRule.getAllowedMethod());
                requestParameters.putValueNonNull(RequestParameters.encoding, requestRule.getEncoding());

                ItemRuleMap parameterItemRuleMap = requestRule.getParameterItemRuleMap();
                if (parameterItemRuleMap != null) {
                    toItemHolderParameters(parameterItemRuleMap, requestParameters, RequestParameters.parameters);
                }

                ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
                if (attributeItemRuleMap != null) {
                    toItemHolderParameters(attributeItemRuleMap, requestParameters, RequestParameters.attributes);
                }
            } else {
                ItemRuleMap parameterItemRuleMap = requestRule.getParameterItemRuleMap();
                if (parameterItemRuleMap != null) {
                    toItemHolderParameters(parameterItemRuleMap, transletParameters, TransletParameters.parameters);
                }

                ItemRuleMap attributeItemRuleMap = requestRule.getAttributeItemRuleMap();
                if (attributeItemRuleMap != null) {
                    toItemHolderParameters(attributeItemRuleMap, transletParameters, TransletParameters.attributes);
                }
            }
        }

        ContentList contentList = transletRule.getContentList();
        if (contentList != null) {
            if (contentList.isExplicit()) {
                ContentsParameters contentsParameters = transletParameters.newParameters(TransletParameters.contents);
                contentsParameters.putValueNonNull(ContentsParameters.name, contentList.getName());
                for (ActionList actionList : contentList) {
                    ContentParameters contentParameters = contentsParameters.newParameters(ContentsParameters.content);
                    contentParameters.putValueNonNull(ContentParameters.name, actionList.getName());
                    toActionParameters(actionList, contentParameters);
                }
            } else {
                for (ActionList actionList : contentList) {
                    if (actionList.isExplicit()) {
                        ContentParameters contentParameters = transletParameters.newParameters(TransletParameters.content);
                        contentParameters.putValueNonNull(ContentParameters.name, actionList.getName());
                        toActionParameters(actionList, contentParameters);
                    } else {
                        toActionParameters(actionList, transletParameters);
                    }
                }
            }
        }

        List<ResponseRule> responseRuleList = transletRule.getResponseRuleList();
        if (responseRuleList != null) {
            for (ResponseRule responseRule : responseRuleList) {
                if (responseRule != null) {
                    transletParameters.putValue(TransletParameters.response, toResponseParameters(responseRule));
                }
            }
        } else {
            ResponseRule responseRule = transletRule.getResponseRule();
            if (responseRule != null) {
                if (responseRule.isExplicit()) {
                    transletParameters.putValue(TransletParameters.response, toResponseParameters(responseRule));
                } else {
                    Response response = responseRule.getResponse();
                    if (response != null) {
                        if (response.getResponseType() == ResponseType.TRANSFORM) {
                            TransformResponse transformResponse = (TransformResponse)response;
                            transletParameters.putValue(TransletParameters.transform, toTransformParameters(transformResponse.getTransformRule()));
                        } else if (response.getResponseType() == ResponseType.DISPATCH) {
                            DispatchResponse dispatchResponse = (DispatchResponse)response;
                            transletParameters.putValue(TransletParameters.dispatch, toDispatchParameters(dispatchResponse.getDispatchRule()));
                        } else if (response.getResponseType() == ResponseType.FORWARD) {
                            ForwardResponse forwardResponse = (ForwardResponse)response;
                            transletParameters.putValue(TransletParameters.forward, toForwardParameters(forwardResponse.getForwardRule()));
                        } else if (response.getResponseType() == ResponseType.REDIRECT) {
                            RedirectResponse redirectResponse = (RedirectResponse)response;
                            transletParameters.putValue(TransletParameters.redirect, toRedirectParameters(redirectResponse.getRedirectRule()));
                        }
                    }
                }
            }
        }

        ExceptionRule exceptionRule = transletRule.getExceptionRule();
        if (exceptionRule != null) {
            ExceptionParameters exceptionParameters = transletParameters.touchParameters(TransletParameters.exception);
            if (exceptionRule.getDescriptionRule() != null) {
                toDescriptionParameters(exceptionRule.getDescriptionRule(), exceptionParameters, ExceptionParameters.description);
            }
            for (ExceptionThrownRule etr : exceptionRule.getExceptionThrownRuleList()) {
                exceptionParameters.putValue(ExceptionParameters.thrown, toExceptionThrownParameters(etr));
            }
        }

        return transletParameters;
    }

    @NonNull
    public static ExceptionThrownParameters toExceptionThrownParameters(ExceptionThrownRule exceptionThrownRule) {
        if (exceptionThrownRule == null) {
            throw new IllegalArgumentException("exceptionThrownRule must not be null");
        }

        ExceptionThrownParameters exceptionThrownParameters = new ExceptionThrownParameters();
        if (exceptionThrownRule.getExceptionTypes() != null) {
            for (String exceptionType : exceptionThrownRule.getExceptionTypes()) {
                exceptionThrownParameters.putValue(ExceptionThrownParameters.type, exceptionType);
            }
        }

        if (exceptionThrownRule.getAction() != null) {
            toActionParameters(exceptionThrownRule.getAction(), exceptionThrownParameters);
        }

        ResponseMap responseMap = exceptionThrownRule.getResponseMap();
        if (responseMap != null) {
            for (Response response : responseMap) {
                if (response.getResponseType() == ResponseType.TRANSFORM) {
                    TransformResponse transformResponse = (TransformResponse) response;
                    exceptionThrownParameters.putValue(ExceptionThrownParameters.transform, toTransformParameters(transformResponse.getTransformRule()));
                } else if (response.getResponseType() == ResponseType.DISPATCH) {
                    DispatchResponse dispatchResponse = (DispatchResponse) response;
                    exceptionThrownParameters.putValue(ExceptionThrownParameters.dispatch, toDispatchParameters(dispatchResponse.getDispatchRule()));
                } else if (response.getResponseType() == ResponseType.REDIRECT) {
                    RedirectResponse redirectResponse = (RedirectResponse) response;
                    exceptionThrownParameters.putValue(ExceptionThrownParameters.redirect, toRedirectParameters(redirectResponse.getRedirectRule()));
                } else if (response.getResponseType() == ResponseType.FORWARD) {
                    throw new IllegalArgumentException("Cannot apply the forward response rule to the exception thrown rule");
                }
            }
        }

        return exceptionThrownParameters;
    }

    @NonNull
    public static ResponseParameters toResponseParameters(ResponseRule responseRule) {
        if (responseRule == null) {
            throw new IllegalArgumentException("responseRule must not be null");
        }

        ResponseParameters responseParameters = new ResponseParameters();
        responseParameters.putValueNonNull(ResponseParameters.name, responseRule.getName());
        responseParameters.putValueNonNull(ResponseParameters.encoding, responseRule.getEncoding());

        ActionList actionList = responseRule.getActionList();
        if (actionList != null) {
            toActionParameters(actionList, responseParameters);
        }

        if (responseRule.getResponse() != null) {
            if (responseRule.getResponseType() == ResponseType.TRANSFORM) {
                TransformResponse transformResponse = (TransformResponse)responseRule.getResponse();
                responseParameters.putValue(ResponseParameters.transform, toTransformParameters(transformResponse.getTransformRule()));
            } else if (responseRule.getResponseType() == ResponseType.DISPATCH) {
                DispatchResponse dispatchResponse = (DispatchResponse)responseRule.getResponse();
                responseParameters.putValue(ResponseParameters.dispatch, toDispatchParameters(dispatchResponse.getDispatchRule()));
            } else if (responseRule.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)responseRule.getResponse();
                responseParameters.putValue(ResponseParameters.forward, toForwardParameters(forwardResponse.getForwardRule()));
            } else if (responseRule.getResponseType() == ResponseType.REDIRECT) {
                RedirectResponse redirectResponse = (RedirectResponse)responseRule.getResponse();
                responseParameters.putValue(ResponseParameters.redirect, toRedirectParameters(redirectResponse.getRedirectRule()));
            }
        }

        return responseParameters;
    }

    @NonNull
    public static TransformParameters toTransformParameters(TransformRule transformRule) {
        if (transformRule == null) {
            throw new IllegalArgumentException("transformRule must not be null");
        }

        TransformParameters transformParameters = new TransformParameters();
        transformParameters.putValueNonNull(TransformParameters.format, transformRule.getFormatType());
        transformParameters.putValueNonNull(TransformParameters.contentType, transformRule.getContentType());
        transformParameters.putValueNonNull(TransformParameters.encoding, transformRule.getEncoding());
        transformParameters.putValueNonNull(TransformParameters.defaultResponse, transformRule.getDefaultResponse());
        transformParameters.putValueNonNull(TransformParameters.pretty, transformRule.getPretty());
        if (transformRule.getTemplateRule() != null) {
            transformParameters.putValue(TransformParameters.template, toTemplateParameters(transformRule.getTemplateRule()));
        }
        return transformParameters;
    }

    @NonNull
    public static DispatchParameters toDispatchParameters(DispatchRule dispatchRule) {
        if (dispatchRule == null) {
            throw new IllegalArgumentException("dispatchRule must not be null");
        }

        DispatchParameters dispatchParameters = new DispatchParameters();
        dispatchParameters.putValueNonNull(DispatchParameters.name, dispatchRule.getName());
        dispatchParameters.putValueNonNull(DispatchParameters.dispatcher, dispatchRule.getDispatcherName());
        dispatchParameters.putValueNonNull(DispatchParameters.contentType, dispatchRule.getContentType());
        dispatchParameters.putValueNonNull(DispatchParameters.encoding, dispatchRule.getEncoding());
        dispatchParameters.putValueNonNull(DispatchParameters.defaultResponse, dispatchRule.getDefaultResponse());
        return dispatchParameters;
    }

    @NonNull
    public static ForwardParameters toForwardParameters(ForwardRule forwardRule) {
        if (forwardRule == null) {
            throw new IllegalArgumentException("forwardRule must not be null");
        }

        ForwardParameters forwardParameters = new ForwardParameters();
        forwardParameters.putValueNonNull(ForwardParameters.contentType, forwardRule.getContentType());
        forwardParameters.putValueNonNull(ForwardParameters.translet, forwardRule.getTransletName());
        forwardParameters.putValueNonNull(ForwardParameters.defaultResponse, forwardRule.getDefaultResponse());

        ItemRuleMap attributeItemRuleMap = forwardRule.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null) {
            toItemHolderParameters(attributeItemRuleMap, forwardParameters, ForwardParameters.attributes);
        }

        return forwardParameters;
    }

    @NonNull
    public static RedirectParameters toRedirectParameters(RedirectRule redirectRule) {
        if (redirectRule == null) {
            throw new IllegalArgumentException("redirectRule must not be null");
        }

        RedirectParameters redirectParameters = new RedirectParameters();
        redirectParameters.putValueNonNull(RedirectParameters.contentType, redirectRule.getContentType());
        redirectParameters.putValueNonNull(RedirectParameters.path, redirectRule.getPath());
        redirectParameters.putValueNonNull(RedirectParameters.encoding, redirectRule.getEncoding());
        redirectParameters.putValueNonNull(RedirectParameters.excludeNullParameters, redirectRule.getExcludeNullParameters());
        redirectParameters.putValueNonNull(RedirectParameters.excludeEmptyParameters, redirectRule.getExcludeEmptyParameters());
        redirectParameters.putValueNonNull(RedirectParameters.defaultResponse, redirectRule.getDefaultResponse());

        ItemRuleMap parameterItemRuleMap = redirectRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null) {
            toItemHolderParameters(parameterItemRuleMap, redirectParameters, RedirectParameters.parameters);
        }

        return redirectParameters;
    }

    @NonNull
    public static TemplateParameters toTemplateParameters(TemplateRule templateRule) {
        if (templateRule == null) {
            throw new IllegalArgumentException("templateRule must not be null");
        }

        TemplateParameters templateParameters = new TemplateParameters();
        templateParameters.putValueNonNull(TemplateParameters.id, templateRule.getId());
        templateParameters.putValueNonNull(TemplateParameters.engine, templateRule.getEngine());
        if (templateRule.getFile() != null) {
            templateParameters.putValueNonNull(TemplateParameters.file, templateRule.getFile());
        } else if (templateRule.getResource() != null) {
            templateParameters.putValueNonNull(TemplateParameters.resource, templateRule.getResource());
        } else if (templateRule.getUrl() != null) {
            templateParameters.putValueNonNull(TemplateParameters.url, templateRule.getUrl());
        } else if (templateRule.getName() != null) {
            templateParameters.putValueNonNull(TemplateParameters.name, templateRule.getName());
        } else {
            if (templateRule.getContent() != null) {
                if (templateRule.getContentStyle() == TextStyleType.APON) {
                    String content = TextStyler.stripAponStyle(templateRule.getContent());
                    templateParameters.putValue(TemplateParameters.content, content);
                } else {
                    templateParameters.putValue(TemplateParameters.content, templateRule.getContent());
                }
            } else {
                templateParameters.putValueNonNull(TemplateParameters.content, templateRule.getTemplateSource());
            }
            templateParameters.putValueNonNull(TemplateParameters.style, templateRule.getContentStyle());
        }
        templateParameters.putValueNonNull(TemplateParameters.encoding, templateRule.getEncoding());
        templateParameters.putValueNonNull(TemplateParameters.noCache, templateRule.getNoCache());

        return templateParameters;
    }

    @NonNull
    private static void toActionParameters(@NonNull ActionList actionList, Parameters parameters) {
        for (Executable action : actionList) {
            toActionParameters(action, parameters);
        }
    }

    private static void toActionParameters(@NonNull Executable action, Parameters parameters) {
        if (action.getActionType() == ActionType.ACTION) {
            InvokeActionRule invokeActionRule = action.getActionRule();
            if (invokeActionRule.getBeanId() != null) {
                parameters.putValue("action", toActionParameters(invokeActionRule));
            } else {
                parameters.putValue("invoke", toActionParameters(invokeActionRule));
            }
        } else if (action.getActionType() == ActionType.ACTION_ANNOTATED) {
            AnnotatedActionRule annotatedActionRule = action.getActionRule();
            parameters.putValue("action", toActionParameters(annotatedActionRule));
        } else if (action.getActionType() == ActionType.INCLUDE) {
            IncludeActionRule includeActionRule = action.getActionRule();
            parameters.putValue("include", toActionParameters(includeActionRule));
        } else if (action.getActionType() == ActionType.ECHO) {
            EchoActionRule echoActionRule = action.getActionRule();
            parameters.putValue("echo", toActionParameters(echoActionRule));
        } else if (action.getActionType() == ActionType.HEADER) {
            HeaderActionRule headerActionRule = action.getActionRule();
            parameters.putValue("headers", toActionParameters(headerActionRule));
        } else if (action.getActionType() == ActionType.CHOOSE) {
            ChooseRule chooseRule = action.getActionRule();
            parameters.putValue("choose", toActionParameters(chooseRule));
        }
    }

    @NonNull
    public static ActionParameters toActionParameters(HeaderActionRule headerActionRule) {
        if (headerActionRule == null) {
            throw new IllegalArgumentException("headerActionRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, headerActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.hidden, headerActionRule.getHidden());

        ItemRuleMap headerItemRuleMap = headerActionRule.getHeaderItemRuleMap();
        if (headerItemRuleMap != null) {
            toItemParameters(headerItemRuleMap, actionParameters);
        }

        return actionParameters;
    }

    @NonNull
    public static ActionParameters toActionParameters(EchoActionRule echoActionRule) {
        if (echoActionRule == null) {
            throw new IllegalArgumentException("echoActionRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, echoActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.hidden, echoActionRule.getHidden());

        ItemRuleMap attributeItemRuleMap = echoActionRule.getEchoItemRuleMap();
        if (attributeItemRuleMap != null) {
            toItemParameters(attributeItemRuleMap, actionParameters);
        }

        return actionParameters;
    }

    @NonNull
    public static ActionParameters toActionParameters(InvokeActionRule invokeActionRule) {
        if (invokeActionRule == null) {
            throw new IllegalArgumentException("invokeActionRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, invokeActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.bean, invokeActionRule.getBeanId());
        actionParameters.putValueNonNull(ActionParameters.method, invokeActionRule.getMethodName());
        actionParameters.putValueNonNull(ActionParameters.hidden, invokeActionRule.getHidden());

        ItemRuleMap propertyItemRuleMap = invokeActionRule.getPropertyItemRuleMap();
        if (propertyItemRuleMap != null) {
            toItemHolderParameters(propertyItemRuleMap, actionParameters, ActionParameters.properties);
        }

        ItemRuleMap argumentItemRuleMap = invokeActionRule.getArgumentItemRuleMap();
        if (argumentItemRuleMap != null) {
            toItemHolderParameters(argumentItemRuleMap, actionParameters, ActionParameters.arguments);
        }

        return actionParameters;
    }

    @NonNull
    public static ActionParameters toActionParameters(AnnotatedActionRule annotatedActionRule) {
        if (annotatedActionRule == null) {
            throw new IllegalArgumentException("annotatedActionRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, annotatedActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.bean, annotatedActionRule.getBeanClass());
        actionParameters.putValueNonNull(ActionParameters.method, annotatedActionRule.getMethod());
        return actionParameters;
    }

    @NonNull
    public static ActionParameters toActionParameters(IncludeActionRule includeActionRule) {
        if (includeActionRule == null) {
            throw new IllegalArgumentException("includeActionRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, includeActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.translet, includeActionRule.getTransletName());
        actionParameters.putValueNonNull(ActionParameters.method, includeActionRule.getMethodType());
        actionParameters.putValueNonNull(ActionParameters.hidden, includeActionRule.getHidden());

        ItemRuleMap parameterItemRuleMap = includeActionRule.getParameterItemRuleMap();
        if (parameterItemRuleMap != null) {
            toItemHolderParameters(parameterItemRuleMap, actionParameters, ActionParameters.parameters);
        }

        ItemRuleMap attributeItemRuleMap = includeActionRule.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null) {
            toItemHolderParameters(attributeItemRuleMap, actionParameters, ActionParameters.attributes);
        }

        return actionParameters;
    }

    @NonNull
    private static ActionParameters toActionParameters(ChooseRule chooseRule) {
        if (chooseRule == null) {
            throw new IllegalArgumentException("chooseRule must not be null");
        }

        ActionParameters actionParameters = new ActionParameters();
        if (chooseRule.getChooseWhenRules() != null) {
            for (ChooseWhenRule chooseWhenRule : chooseRule.getChooseWhenRules()) {
                ChooseWhenParameters chooseWhenParameters;
                if (chooseWhenRule.getExpression() != null) {
                    chooseWhenParameters = actionParameters.newParameters(ActionParameters.when);
                } else {
                    chooseWhenParameters = actionParameters.newParameters(ActionParameters.otherwise);
                }
                chooseWhenParameters.putValueNonNull(ChooseWhenParameters.test, chooseWhenRule.getExpression());
                if (chooseWhenRule.getResponse() != null) {
                    Response response = chooseWhenRule.getResponse();
                    if (response.getResponseType() == ResponseType.TRANSFORM) {
                        TransformResponse transformResponse = (TransformResponse)response;
                        TransformParameters transformParameters = toTransformParameters(transformResponse.getTransformRule());
                        chooseWhenParameters.putValue(ChooseWhenParameters.transform, transformParameters);
                    } else if (response.getResponseType() == ResponseType.DISPATCH) {
                        DispatchResponse dispatchResponse = (DispatchResponse)response;
                        DispatchParameters dispatchParameters = toDispatchParameters(dispatchResponse.getDispatchRule());
                        chooseWhenParameters.putValue(ChooseWhenParameters.dispatch, dispatchParameters);
                    } else if (response.getResponseType() == ResponseType.FORWARD) {
                        ForwardResponse forwardResponse = (ForwardResponse)response;
                        ForwardParameters forwardParameters = toForwardParameters(forwardResponse.getForwardRule());
                        chooseWhenParameters.putValue(ChooseWhenParameters.forward, forwardParameters);
                    } else if (response.getResponseType() == ResponseType.REDIRECT) {
                        RedirectResponse redirectResponse = (RedirectResponse)response;
                        RedirectParameters redirectParameters = toRedirectParameters(redirectResponse.getRedirectRule());
                        chooseWhenParameters.putValue(ChooseWhenParameters.redirect, redirectParameters);
                    }
                }
            }
        }
        return actionParameters;
    }

    private static void toItemHolderParameters(@NonNull ItemRuleMap itemRuleMap, Parameters parameters, ParameterKey key) {
        if (itemRuleMap.getCandidates() != null) {
            for (ItemRuleMap irm : itemRuleMap.getCandidates()) {
                parameters.putValue(key, toItemHolderParameters(irm));
            }
        } else {
            parameters.putValue(key, toItemHolderParameters(itemRuleMap));
        }
    }

    @NonNull
    public static ItemHolderParameters toItemHolderParameters(ItemRuleMap itemRuleMap) {
        if (itemRuleMap == null) {
            throw new IllegalArgumentException("itemRuleMap must not be null");
        }

        ItemHolderParameters itemHolderParameters = new ItemHolderParameters();
        itemHolderParameters.putValueNonNull(ItemHolderParameters.profile, itemRuleMap.getProfile());
        for (ItemRule itemRule : itemRuleMap.values()) {
            itemHolderParameters.addItemParameters(toItemParameters(itemRule));
        }
        return itemHolderParameters;
    }

    private static void toItemParameters(ItemRuleMap itemRuleMap, ActionParameters actionParameters) {
        if (itemRuleMap == null) {
            throw new IllegalArgumentException("itemRuleMap must not be null");
        }

        for (ItemRule itemRule : itemRuleMap.values()) {
            actionParameters.putValue(ActionParameters.item, toItemParameters(itemRule));
        }
    }

    @NonNull
    public static ItemParameters toItemParameters(ItemRule itemRule) {
        if (itemRule == null) {
            throw new IllegalArgumentException("itemRule must not be null");
        }

        ItemParameters itemParameters = new ItemParameters();
        if (itemRule.getType() != null && itemRule.getType() != ItemType.SINGLE) {
            itemParameters.putValue(ItemParameters.type, itemRule.getType());
        }
        if (!itemRule.isAutoNamed()) {
            itemParameters.putValue(ItemParameters.name, itemRule.getName());
        }
        if (itemRule.getValueType() != ItemValueType.STRING && itemRule.getValueType() != ItemValueType.BEAN) {
            itemParameters.putValueNonNull(ItemParameters.valueType, itemRule.getValueType());
        }
        itemParameters.putValueNonNull(ItemParameters.tokenize, itemRule.getTokenize());
        itemParameters.putValueNonNull(ItemParameters.mandatory, itemRule.getMandatory());
        itemParameters.putValueNonNull(ItemParameters.secret, itemRule.getSecret());

        if (itemRule.getType() == ItemType.SINGLE) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                BeanRule beanRule = itemRule.getBeanRule();
                BeanParameters beanParameters = toBeanParameters(beanRule);
                itemParameters.putValueNonNull(ItemParameters.bean, beanParameters);
            } else {
                Object o = determineItemValue(itemRule.getValue(), itemRule.getValueType());
                itemParameters.putValueNonNull(ItemParameters.value, o);
            }
        } else if (itemRule.isListableType()) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                List<BeanRule> beanRuleList = itemRule.getBeanRuleList();
                if (beanRuleList != null) {
                    for (BeanRule beanRule : beanRuleList) {
                        BeanParameters beanParameters = toBeanParameters(beanRule);
                        itemParameters.putValueNonNull(ItemParameters.bean, beanParameters);
                    }
                }
            } else {
                List<String> valueList = itemRule.getValueList();
                if (valueList != null && !valueList.isEmpty()) {
                    Parameter p = itemParameters.getParameter(ItemParameters.value);
                    p.arraylize();
                    for (String value : valueList) {
                        Object o = determineItemValue(value, itemRule.getValueType());
                        p.putValue(o);
                    }
                }
            }
        } else if (itemRule.isMappableType()) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                Map<String, BeanRule> beanRuleMap = itemRule.getBeanRuleMap();
                if (beanRuleMap != null) {
                    for (Map.Entry<String, BeanRule> entry : beanRuleMap.entrySet()) {
                        EntryParameters ps = itemParameters.newParameters(ItemParameters.entry);
                        BeanParameters beanParameters = toBeanParameters(entry.getValue());
                        ps.putValue(EntryParameters.name, entry.getKey());
                        ps.putValue(EntryParameters.bean, beanParameters);
                    }
                }
            } else {
                Map<String, String> valueMap = itemRule.getValueMap();
                if (valueMap != null) {
                    for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                        EntryParameters ps = itemParameters.newParameters(ItemParameters.entry);
                        Object o = determineItemValue(entry.getValue(), itemRule.getValueType());
                        ps.putValue(EntryParameters.name, entry.getKey());
                        ps.putValue(EntryParameters.value, o);
                    }
                }
            }
        }

        return itemParameters;
    }

    private static Object determineItemValue(String value, ItemValueType valueType) {
        if (value == null) {
            return null;
        }
        if (valueType == ItemValueType.PARAMETERS) {
            try {
                return new VariableParameters(value);
            } catch (IOException e) {
                throw new RuntimeException("Parameters can not be parsed", e);
            }
        } else {
            return value;
        }
    }

}
