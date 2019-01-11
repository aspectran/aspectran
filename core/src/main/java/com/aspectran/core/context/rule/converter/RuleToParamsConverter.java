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
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.Response;
import com.aspectran.core.activity.response.ResponseMap;
import com.aspectran.core.activity.response.dispatch.DispatchResponse;
import com.aspectran.core.activity.response.transform.TransformResponse;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanMethodActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.ScheduleJobRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.appender.RuleAppender;
import com.aspectran.core.context.rule.assistant.AssistantLocal;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.assistant.DefaultSettings;
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
import com.aspectran.core.context.rule.params.ForwardParameters;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
import com.aspectran.core.context.rule.params.JoinpointParameters;
import com.aspectran.core.context.rule.params.RedirectParameters;
import com.aspectran.core.context.rule.params.RequestParameters;
import com.aspectran.core.context.rule.params.ResponseParameters;
import com.aspectran.core.context.rule.params.RootParameters;
import com.aspectran.core.context.rule.params.ScheduleJobParameters;
import com.aspectran.core.context.rule.params.ScheduleParameters;
import com.aspectran.core.context.rule.params.SchedulerParameters;
import com.aspectran.core.context.rule.params.TemplateParameters;
import com.aspectran.core.context.rule.params.TransformParameters;
import com.aspectran.core.context.rule.params.TransletParameters;
import com.aspectran.core.context.rule.params.TriggerParameters;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.ContentStyleType;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ResponseType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.apon.Parameter;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.apon.VariableParameters;

import java.util.List;
import java.util.Map;

/**
 * Converts rules for context configuration into {@code Parameters} objects.
 *
 * <p>Created: 2017. 5. 5.</p>
 */
public class RuleToParamsConverter {

    private final ContextRuleAssistant assistant;

    public RuleToParamsConverter(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    public RootParameters toRootParameters() {
        RootParameters rootParameters = new RootParameters();
        rootParameters.putValue(RootParameters.aspectran, toAspectranParameters());
        return rootParameters;
    }

    public AspectranParameters toAspectranParameters() {
        AspectranParameters aspectranParameters = new AspectranParameters();

        AssistantLocal assistantLocal = assistant.getAssistantLocal();
        aspectranParameters.putValueNonNull(AspectranParameters.description, assistantLocal.getDescription());

        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        if (defaultSettings != null) {
            DefaultSettingsParameters settingParameters = aspectranParameters.newParameters(AspectranParameters.settings);
            if (defaultSettings.getTransletNamePattern() != null) {
                settingParameters.putValue(DefaultSettingsParameters.transletNamePattern, defaultSettings.getTransletNamePattern());
            } else {
                settingParameters.putValueNonNull(DefaultSettingsParameters.transletNamePrefix, defaultSettings.getTransletNamePrefix());
                settingParameters.putValueNonNull(DefaultSettingsParameters.transletNameSuffix, defaultSettings.getTransletNameSuffix());
            }
            settingParameters.putValueNonNull(DefaultSettingsParameters.beanProxifier, defaultSettings.getBeanProxifier());
            settingParameters.putValueNonNull(DefaultSettingsParameters.pointcutPatternVerifiable, defaultSettings.getPointcutPatternVerifiable());
            settingParameters.putValueNonNull(DefaultSettingsParameters.defaultTemplateEngineBean, defaultSettings.getDefaultTemplateEngineBean());
            settingParameters.putValueNonNull(DefaultSettingsParameters.defaultSchedulerBean, defaultSettings.getDefaultSchedulerBean());
        }

        List<EnvironmentRule> environmentRules = assistant.getEnvironmentRules();
        if (!environmentRules.isEmpty()) {
            for (EnvironmentRule environmentRule : environmentRules) {
                EnvironmentParameters p = toEnvironmentParameters(environmentRule);
                aspectranParameters.putValue(AspectranParameters.environment, p);
            }
        }

        Map<String, String> typeAliases = assistant.getTypeAliases();
        if (!typeAliases.isEmpty()) {
            Parameters typeAliasParameters = aspectranParameters.newParameters(AspectranParameters.typeAlias);
            for (Map.Entry<String, String> entry : typeAliases.entrySet()) {
                typeAliasParameters.putValue(entry.getKey(), entry.getValue());
            }
        }

        for (AspectRule aspectRule : assistant.getAspectRules()) {
            AspectParameters p = toAspectParameters(aspectRule);
            aspectranParameters.putValue(AspectranParameters.aspect, p);
        }

        for (BeanRule beanRule : assistant.getBeanRules()) {
            BeanParameters p = toBeanParameters(beanRule);
            aspectranParameters.putValue(AspectranParameters.bean, p);
        }

        for (ScheduleRule scheduleRule : assistant.getScheduleRules()) {
            ScheduleParameters p = toScheduleParameters(scheduleRule);
            aspectranParameters.putValue(AspectranParameters.schedule, p);
        }

        for (TransletRule transletRule : assistant.getTransletRules()) {
            TransletParameters p = toTransletParameters(transletRule);
            aspectranParameters.putValue(AspectranParameters.translet, p);
        }

        for (TemplateRule templateRule : assistant.getTemplateRules()) {
            TemplateParameters p = toTemplateParameters(templateRule);
            aspectranParameters.putValue(AspectranParameters.template, p);
        }

        List<RuleAppender> pendingList = assistant.getRuleAppendHandler().getPendingList();
        if (pendingList != null) {
            for (RuleAppender appender : pendingList) {
                aspectranParameters.putValue(AspectranParameters.append, toAppendParameters(appender));
            }
        }

        return aspectranParameters;
    }

    private AppendParameters toAppendParameters(RuleAppender appender) {
        AppendRule appendRule = appender.getAppendRule();
        if (appendRule == null) {
            throw new IllegalArgumentException("Every appender except Root Appender requires an AppendRule");
        }
        return toAppendParameters(appendRule);
    }

    public static AppendParameters toAppendParameters(AppendRule appendRule) {
        AppendParameters appendParameters = new AppendParameters();
        appendParameters.putValueNonNull(AppendParameters.file, appendRule.getFile());
        appendParameters.putValueNonNull(AppendParameters.resource, appendRule.getResource());
        appendParameters.putValueNonNull(AppendParameters.url, appendRule.getUrl());
        appendParameters.putValueNonNull(AppendParameters.format, appendRule.getFormat());
        appendParameters.putValueNonNull(AppendParameters.profile, appendRule.getProfile());
        appendParameters.putValueNonNull(AppendParameters.aspectran, appendRule.getAspectranParameters());
        return appendParameters;
    }

    public static EnvironmentParameters toEnvironmentParameters(EnvironmentRule environmentRule) {
        EnvironmentParameters environmentParameters = new EnvironmentParameters();
        environmentParameters.putValueNonNull(EnvironmentParameters.description, environmentRule.getDescription());
        environmentParameters.putValueNonNull(EnvironmentParameters.profile, environmentRule.getProfile());
        if (environmentRule.getPropertyItemRuleMap() != null) {
            ItemHolderParameters itemHolderParameters = toItemHolderParameters(environmentRule.getPropertyItemRuleMap());
            environmentParameters.putValue(EnvironmentParameters.properties, itemHolderParameters);
        }
        return environmentParameters;
    }

    public static AspectParameters toAspectParameters(AspectRule aspectRule) {
        AspectParameters aspectParameters = new AspectParameters();
        aspectParameters.putValueNonNull(AspectParameters.description, aspectRule.getDescription());
        aspectParameters.putValueNonNull(AspectParameters.id, aspectRule.getId());
        if (aspectRule.getOrder() != Integer.MAX_VALUE) {
            aspectParameters.putValueNonNull(AspectParameters.order, aspectRule.getOrder());
        }
        aspectParameters.putValueNonNull(AspectParameters.isolated, aspectRule.getIsolated());

        if (aspectRule.getJoinpointRule() != null) {
            JoinpointParameters joinpointParameters = aspectRule.getJoinpointRule().getJoinpointParameters();
            if (joinpointParameters != null) {
                joinpointParameters.putValueNonNull(JoinpointParameters.target, aspectRule.getJoinpointTargetType());
                aspectParameters.putValue(AspectParameters.joinpoint, joinpointParameters);
            }
        }

        if (aspectRule.getSettingsAdviceRule() != null) {
            Map<String, Object> settings = aspectRule.getSettingsAdviceRule().getSettings();
            if (settings != null) {
                Parameters settingsParameters = aspectParameters.newParameters(AspectParameters.settings);
                for (Map.Entry<String, Object> entry : settings.entrySet()) {
                    settingsParameters.putValue(entry.getKey(), entry.getValue());
                }
            }
        }

        if (aspectRule.getAspectAdviceRuleList() != null) {
            AdviceParameters adviceParameters = aspectParameters.newParameters(AspectParameters.advice);
            adviceParameters.putValue(AdviceParameters.bean, aspectRule.getAdviceBeanId());
            for (AspectAdviceRule aspectAdviceRule : aspectRule.getAspectAdviceRuleList()) {
                if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.BEFORE) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.beforeAdvice);
                    if (aspectAdviceRule.getActionType() == ActionType.BEAN_METHOD) {
                        BeanMethodActionRule beanMethodActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(beanMethodActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.ECHO) {
                        EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(echoActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.HEADER) {
                        HeaderActionRule headerActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(headerActionRule));
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AFTER) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.afterAdvice);
                    if (aspectAdviceRule.getActionType() == ActionType.BEAN_METHOD) {
                        BeanMethodActionRule beanMethodActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(beanMethodActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.ECHO) {
                        EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(echoActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.HEADER) {
                        HeaderActionRule headerActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(headerActionRule));
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.AROUND) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.aroundAdvice);
                    if (aspectAdviceRule.getActionType() == ActionType.BEAN_METHOD) {
                        BeanMethodActionRule beanMethodActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(beanMethodActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.ECHO) {
                        EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(echoActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.HEADER) {
                        HeaderActionRule headerActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(headerActionRule));
                    }
                } else if (aspectAdviceRule.getAspectAdviceType() == AspectAdviceType.FINALLY) {
                    AdviceActionParameters adviceActionParameters = adviceParameters.newParameters(AdviceParameters.finallyAdvice);
                    if (aspectAdviceRule.getExceptionThrownRule() != null) {
                        adviceActionParameters.putValue(AdviceActionParameters.thrown, toExceptionThrownParameters(aspectAdviceRule.getExceptionThrownRule()));
                    }
                    if (aspectAdviceRule.getActionType() == ActionType.BEAN_METHOD) {
                        BeanMethodActionRule beanMethodActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(beanMethodActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.ECHO) {
                        EchoActionRule echoActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(echoActionRule));
                    } else if (aspectAdviceRule.getActionType() == ActionType.HEADER) {
                        HeaderActionRule headerActionRule = aspectAdviceRule.getExecutableAction().getActionRule();
                        adviceActionParameters.putValue(AdviceActionParameters.action, toActionParameters(headerActionRule));
                    }
                }
            }
        }

        ExceptionRule exceptionRule = aspectRule.getExceptionRule();
        if (exceptionRule != null) {
            ExceptionParameters exceptionParameters = aspectParameters.touchParameters(AspectParameters.exception);
            exceptionParameters.putValueNonNull(ExceptionParameters.description, exceptionRule.getDescription());
            for (ExceptionThrownRule etr : exceptionRule) {
                exceptionParameters.putValue(ExceptionParameters.thrown, toExceptionThrownParameters(etr));
            }
        }

        return aspectParameters;
    }

    public static BeanParameters toBeanParameters(BeanRule beanRule) {
        BeanParameters beanParameters = new BeanParameters();
        beanParameters.putValueNonNull(BeanParameters.description, beanRule.getDescription());
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
            ConstructorParameters constructorParameters = beanParameters.newParameters(BeanParameters.constructor);
            toItemHolderParameters(constructorArgumentItemRuleMap, constructorParameters, ConstructorParameters.arguments);
        }

        ItemRuleMap propertyItemRuleMap = beanRule.getPropertyItemRuleMap();
        if (propertyItemRuleMap != null) {
            toItemHolderParameters(propertyItemRuleMap, beanParameters, BeanParameters.properties);
        }

        return beanParameters;
    }

    public static ScheduleParameters toScheduleParameters(ScheduleRule scheduleRule) {
        ScheduleParameters scheduleParameters = new ScheduleParameters();
        scheduleParameters.putValueNonNull(ScheduleParameters.description, scheduleRule.getDescription());
        scheduleParameters.putValueNonNull(ScheduleParameters.id, scheduleRule.getId());

        SchedulerParameters schedulerParameters = scheduleParameters.newParameters(ScheduleParameters.scheduler);
        schedulerParameters.putValueNonNull(SchedulerParameters.bean, scheduleRule.getSchedulerBeanId());

        TriggerParameters triggerParameters = scheduleRule.getTriggerParameters();
        if (triggerParameters != null && scheduleRule.getTriggerType() != null) {
            triggerParameters.putValueNonNull(TriggerParameters.type, scheduleRule.getTriggerType().toString());
            schedulerParameters.putValue(SchedulerParameters.trigger, scheduleRule.getTriggerParameters());
        }

        List<ScheduleJobRule> scheduleJobRuleList = scheduleRule.getScheduleJobRuleList();
        if (scheduleJobRuleList != null) {
            for (ScheduleJobRule scheduleJobRule : scheduleJobRuleList) {
                ScheduleJobParameters jobParameters = scheduleParameters.newParameters(ScheduleParameters.job);
                jobParameters.putValue(ScheduleJobParameters.translet, scheduleJobRule.getTransletName());
                if (scheduleJobRule.getRequestMethod() != null) {
                    jobParameters.putValue(ScheduleJobParameters.method, scheduleJobRule.getRequestMethod().toString());
                }
                jobParameters.putValueNonNull(ScheduleJobParameters.disabled, scheduleJobRule.getDisabled());
            }
        }

        return scheduleParameters;
    }

    public static TransletParameters toTransletParameters(TransletRule transletRule) {
        TransletParameters transletParameters = new TransletParameters();
        transletParameters.putValueNonNull(TransletParameters.description, transletRule.getDescription());
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
                    toActionList(actionList, contentParameters, ContentParameters.action);
                }
            } else {
                for (ActionList actionList : contentList) {
                    if (actionList.isExplicit()) {
                        ContentParameters contentParameters = transletParameters.newParameters(TransletParameters.content);
                        contentParameters.putValueNonNull(ContentParameters.name, actionList.getName());
                        toActionList(actionList, contentParameters, ContentParameters.action);
                    } else {
                        toActionList(actionList, transletParameters, TransletParameters.action);
                    }
                }
            }
        }

        if (transletRule.getChooseRuleMap() != null) {
            for (ChooseRule chooseRule : transletRule.getChooseRuleMap().values()) {
                ChooseParameters chooseParameters = transletParameters.newParameters(TransletParameters.choose);
                chooseParameters.putValue(ChooseParameters.caseNo, chooseRule.getCaseNo());
                if (chooseRule.getChooseWhenRuleMap() != null) {
                    for (ChooseWhenRule chooseWhenRule : chooseRule.getChooseWhenRuleMap().values()) {
                        ChooseWhenParameters chooseWhenParameters;
                        if (chooseWhenRule.getExpression() != null) {
                            chooseWhenParameters = chooseParameters.newParameters(ChooseParameters.when);
                        } else {
                            chooseWhenParameters = chooseParameters.newParameters(ChooseParameters.otherwise);
                        }
                        chooseWhenParameters.putValue(ChooseWhenParameters.caseNo, chooseWhenRule.getCaseNo());
                        chooseWhenParameters.putValueNonNull(ChooseWhenParameters.test, chooseWhenRule.getExpression());
                        if (chooseWhenRule.getResponse() != null) {
                            Response response = chooseWhenRule.getResponse();
                            if (response.getResponseType() == ResponseType.TRANSFORM) {
                                TransformResponse transformResponse = (TransformResponse)response;
                                chooseWhenParameters.putValue(ChooseWhenParameters.transform, toTransformParameters(transformResponse.getTransformRule()));
                            } else if (response.getResponseType() == ResponseType.DISPATCH) {
                                DispatchResponse dispatchResponse = (DispatchResponse)response;
                                chooseWhenParameters.putValue(ChooseWhenParameters.dispatch, toDispatchParameters(dispatchResponse.getDispatchRule()));
                            } else if (response.getResponseType() == ResponseType.FORWARD) {
                                ForwardResponse forwardResponse = (ForwardResponse)response;
                                chooseWhenParameters.putValue(ChooseWhenParameters.forward, toForwardParameters(forwardResponse.getForwardRule()));
                            } else if (response.getResponseType() == ResponseType.REDIRECT) {
                                RedirectResponse redirectResponse = (RedirectResponse)response;
                                chooseWhenParameters.putValue(ChooseWhenParameters.redirect, toRedirectParameters(redirectResponse.getRedirectRule()));
                            }
                        }
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

        ExceptionRule exceptionRule = transletRule.getExceptionRule();
        if (exceptionRule != null) {
            ExceptionParameters exceptionParameters = transletParameters.touchParameters(TransletParameters.exception);
            exceptionParameters.putValueNonNull(ExceptionParameters.description, exceptionRule.getDescription());
            for (ExceptionThrownRule etr : exceptionRule) {
                exceptionParameters.putValue(ExceptionParameters.thrown, toExceptionThrownParameters(etr));
            }
        }

        return transletParameters;
    }

    public static ExceptionThrownParameters toExceptionThrownParameters(ExceptionThrownRule exceptionThrownRule) {
        ExceptionThrownParameters etParameters = new ExceptionThrownParameters();
        if (exceptionThrownRule.getExceptionTypes() != null) {
            for (String exceptionType : exceptionThrownRule.getExceptionTypes()) {
                etParameters.putValue(ExceptionThrownParameters.type, exceptionType);
            }
        }

        if (exceptionThrownRule.getActionType() == ActionType.BEAN_METHOD) {
            BeanMethodActionRule beanMethodActionRule = exceptionThrownRule.getAction().getActionRule();
            etParameters.putValue(ExceptionThrownParameters.action, toActionParameters(beanMethodActionRule));
        } else if (exceptionThrownRule.getActionType() == ActionType.ECHO) {
            EchoActionRule echoActionRule = exceptionThrownRule.getAction().getActionRule();
            etParameters.putValue(ExceptionThrownParameters.action, toActionParameters(echoActionRule));
        } else if (exceptionThrownRule.getActionType() == ActionType.HEADER) {
            HeaderActionRule headerActionRule = exceptionThrownRule.getAction().getActionRule();
            etParameters.putValue(ExceptionThrownParameters.action, toActionParameters(headerActionRule));
        }

        ResponseMap responseMap = exceptionThrownRule.getResponseMap();
        for (Response response : responseMap) {
            if (response.getResponseType() == ResponseType.TRANSFORM) {
                TransformResponse transformResponse = (TransformResponse)response;
                etParameters.putValue(ExceptionThrownParameters.transform, toTransformParameters(transformResponse.getTransformRule()));
            } else if (response.getResponseType() == ResponseType.DISPATCH) {
                DispatchResponse dispatchResponse = (DispatchResponse)response;
                etParameters.putValue(ExceptionThrownParameters.dispatch, toDispatchParameters(dispatchResponse.getDispatchRule()));
            } else if (response.getResponseType() == ResponseType.FORWARD) {
                ForwardResponse forwardResponse = (ForwardResponse)response;
                etParameters.putValue(ExceptionThrownParameters.forward, toForwardParameters(forwardResponse.getForwardRule()));
            } else if (response.getResponseType() == ResponseType.REDIRECT) {
                RedirectResponse redirectResponse = (RedirectResponse)response;
                etParameters.putValue(ExceptionThrownParameters.redirect, toRedirectParameters(redirectResponse.getRedirectRule()));
            }
        }

        return etParameters;
    }

    public static ResponseParameters toResponseParameters(ResponseRule responseRule) {
        ResponseParameters responseParameters = new ResponseParameters();
        responseParameters.putValueNonNull(ResponseParameters.name, responseRule.getName());
        responseParameters.putValueNonNull(ResponseParameters.encoding, responseRule.getEncoding());

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

        return responseParameters;
    }

    public static TransformParameters toTransformParameters(TransformRule transformRule) {
        TransformParameters transformParameters = new TransformParameters();

        if (transformRule.getTransformType() != null) {
            transformParameters.putValue(TransformParameters.type, transformRule.getTransformType().toString());
        }

        transformParameters.putValueNonNull(TransformParameters.contentType, transformRule.getContentType());
        transformParameters.putValueNonNull(TransformParameters.encoding, transformRule.getEncoding());
        transformParameters.putValueNonNull(TransformParameters.defaultResponse, transformRule.getDefaultResponse());
        transformParameters.putValueNonNull(TransformParameters.pretty, transformRule.getPretty());

        ActionList actionList = transformRule.getActionList();
        if (actionList != null) {
            toActionList(actionList, transformParameters, TransformParameters.action);
        }

        if (transformRule.getTemplateId() != null) {
            CallParameters callParameters = transformParameters.newParameters(TransformParameters.call);
            callParameters.putValue(CallParameters.template, transformRule.getTemplateId());
            transformParameters.putValue(TransformParameters.call, callParameters);
        }
        if (transformRule.getTemplateRule() != null) {
            transformParameters.putValue(TransformParameters.template, toTemplateParameters(transformRule.getTemplateRule()));
        }

        return transformParameters;
    }

    public static DispatchParameters toDispatchParameters(DispatchRule dispatchRule) {
        DispatchParameters dispatchParameters = new DispatchParameters();
        dispatchParameters.putValueNonNull(DispatchParameters.name, dispatchRule.getName());
        dispatchParameters.putValueNonNull(DispatchParameters.dispatcher, dispatchRule.getDispatcherName());
        dispatchParameters.putValueNonNull(DispatchParameters.contentType, dispatchRule.getContentType());
        dispatchParameters.putValueNonNull(DispatchParameters.encoding, dispatchRule.getEncoding());
        dispatchParameters.putValueNonNull(DispatchParameters.defaultResponse, dispatchRule.getDefaultResponse());

        ActionList actionList = dispatchRule.getActionList();
        if (actionList != null) {
            toActionList(actionList, dispatchParameters, DispatchParameters.action);
        }

        return dispatchParameters;
    }

    public static ForwardParameters toForwardParameters(ForwardRule forwardRule) {
        ForwardParameters forwardParameters = new ForwardParameters();
        forwardParameters.putValueNonNull(ForwardParameters.contentType, forwardRule.getContentType());
        forwardParameters.putValueNonNull(ForwardParameters.translet, forwardRule.getTransletName());
        forwardParameters.putValueNonNull(ForwardParameters.defaultResponse, forwardRule.getDefaultResponse());

        ItemRuleMap attributeItemRuleMap = forwardRule.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null) {
            toItemHolderParameters(attributeItemRuleMap, forwardParameters, ForwardParameters.attributes);
        }

        ActionList actionList = forwardRule.getActionList();
        if (actionList != null) {
            toActionList(actionList, forwardParameters, ForwardParameters.action);
        }

        return forwardParameters;
    }

    public static RedirectParameters toRedirectParameters(RedirectRule redirectRule) {
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

        ActionList actionList = redirectRule.getActionList();
        if (actionList != null) {
            toActionList(actionList, redirectParameters, RedirectParameters.action);
        }

        return redirectParameters;
    }

    public static TemplateParameters toTemplateParameters(TemplateRule templateRule) {
        TemplateParameters templateParameters = new TemplateParameters();
        templateParameters.putValueNonNull(TemplateParameters.id, templateRule.getId());
        templateParameters.putValueNonNull(TemplateParameters.engine, templateRule.getEngine());
        templateParameters.putValueNonNull(TemplateParameters.name, templateRule.getName());
        templateParameters.putValueNonNull(TemplateParameters.file, templateRule.getFile());
        templateParameters.putValueNonNull(TemplateParameters.resource, templateRule.getResource());
        templateParameters.putValueNonNull(TemplateParameters.url, templateRule.getUrl());
        if (templateRule.getContent() != null) {
            ContentStyleType contentStyleType = templateRule.getContentStyle();
            if (contentStyleType == ContentStyleType.APON) {
                String content = ContentStyleType.styling(templateRule.getContent(), contentStyleType);
                templateParameters.putValue(TemplateParameters.content, content);
            } else {
                templateParameters.putValue(TemplateParameters.content, templateRule.getContent());
                templateParameters.putValueNonNull(TemplateParameters.style, contentStyleType);
            }
        } else {
            templateParameters.putValueNonNull(TemplateParameters.content, templateRule.getTemplateSource());
            templateParameters.putValueNonNull(TemplateParameters.style, templateRule.getContentStyle());
        }
        templateParameters.putValueNonNull(TemplateParameters.encoding, templateRule.getEncoding());
        templateParameters.putValueNonNull(TemplateParameters.noCache, templateRule.getNoCache());

        return templateParameters;
    }

    private static void toActionList(ActionList actionList, Parameters parameters, ParameterDefinition actionParam) {
        for (Executable action : actionList) {
            ActionParameters actionParameters = null;
            if (action.getActionType() == ActionType.BEAN_METHOD) {
                BeanMethodActionRule beanMethodActionRule = action.getActionRule();
                actionParameters = toActionParameters(beanMethodActionRule);
            } else if (action.getActionType() == ActionType.INCLUDE) {
                IncludeActionRule includeActionRule = action.getActionRule();
                actionParameters = toActionParameters(includeActionRule);
            } else if (action.getActionType() == ActionType.ECHO) {
                EchoActionRule echoActionRule = action.getActionRule();
                actionParameters = toActionParameters(echoActionRule);
            } else if (action.getActionType() == ActionType.HEADER) {
                HeaderActionRule headerActionRule = action.getActionRule();
                actionParameters = toActionParameters(headerActionRule);
            }
            if (actionParameters != null) {
                if (action.getCaseNo() > 0) {
                    actionParameters.putValue(ActionParameters.caseNo, action.getCaseNo());
                }
                parameters.putValue(actionParam, actionParameters);
            }
        }
    }

    public static ActionParameters toActionParameters(BeanMethodActionRule beanMethodActionRule) {
        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, beanMethodActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.bean, beanMethodActionRule.getBeanId());
        actionParameters.putValueNonNull(ActionParameters.method, beanMethodActionRule.getMethodName());
        actionParameters.putValueNonNull(ActionParameters.hidden, beanMethodActionRule.getHidden());

        ItemRuleMap propertyItemRuleMap = beanMethodActionRule.getPropertyItemRuleMap();
        if (propertyItemRuleMap != null) {
            toItemHolderParameters(propertyItemRuleMap, actionParameters, ActionParameters.properties);
        }

        ItemRuleMap argumentItemRuleMap = beanMethodActionRule.getArgumentItemRuleMap();
        if (argumentItemRuleMap != null) {
            toItemHolderParameters(argumentItemRuleMap, actionParameters, ActionParameters.arguments);
        }

        return actionParameters;
    }

    public static ActionParameters toActionParameters(IncludeActionRule includeActionRule) {
        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, includeActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.include, includeActionRule.getTransletName());
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

    public static ActionParameters toActionParameters(EchoActionRule echoActionRule) {
        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, echoActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.hidden, echoActionRule.getHidden());

        ItemRuleMap attributeItemRuleMap = echoActionRule.getAttributeItemRuleMap();
        if (attributeItemRuleMap != null) {
            actionParameters.putValue(ActionParameters.echo, toItemHolderParameters(attributeItemRuleMap));
        }

        return actionParameters;
    }

    public static ActionParameters toActionParameters(HeaderActionRule headerActionRule) {
        ActionParameters actionParameters = new ActionParameters();
        actionParameters.putValueNonNull(ActionParameters.id, headerActionRule.getActionId());
        actionParameters.putValueNonNull(ActionParameters.hidden, headerActionRule.getHidden());

        ItemRuleMap headerItemRuleMap = headerActionRule.getHeaderItemRuleMap();
        if (headerItemRuleMap != null) {
            actionParameters.putValue(ActionParameters.headers, toItemHolderParameters(headerItemRuleMap));
        }

        return actionParameters;
    }

    public static ItemHolderParameters toItemHolderParameters(ItemRuleMap itemRuleMap) {
        ItemHolderParameters itemHolderParameters = new ItemHolderParameters();
        if (itemRuleMap.getProfile() != null) {
            itemHolderParameters.putValue(ItemHolderParameters.profile, itemRuleMap.getProfile());
        }
        for (ItemRule itemRule : itemRuleMap.values()) {
            itemHolderParameters.putValue(ItemHolderParameters.item, toItemParameters(itemRule));
        }
        return itemHolderParameters;
    }

    private static void toItemHolderParameters(ItemRuleMap itemRuleMap, Parameters parameters, ParameterDefinition parameterDefinition) {
        if (itemRuleMap.getCandidates() != null) {
            for (ItemRuleMap irm : itemRuleMap.getCandidates()) {
                parameters.putValue(parameterDefinition, toItemHolderParameters(irm));
            }
        } else {
            parameters.putValue(parameterDefinition, toItemHolderParameters(itemRuleMap));
        }
    }

    public static ItemParameters toItemParameters(ItemRule itemRule) {
        ItemParameters itemParameters = new ItemParameters();
        if (itemRule.getType() != null && itemRule.getType() != ItemType.SINGLE) {
            itemParameters.putValue(ItemParameters.type, itemRule.getType().toString());
        }
        if (!itemRule.isAutoNamed()) {
            itemParameters.putValue(ItemParameters.name, itemRule.getName());
        }
        if (itemRule.getValueType() != null) {
            itemParameters.putValue(ItemParameters.valueType, itemRule.getValueType().toString());
        }

        itemParameters.putValueNonNull(ItemParameters.defaultValue, itemRule.getDefaultValue());
        itemParameters.putValueNonNull(ItemParameters.tokenize, itemRule.getTokenize());
        itemParameters.putValueNonNull(ItemParameters.mandatory, itemRule.getMandatory());
        itemParameters.putValueNonNull(ItemParameters.security, itemRule.getSecurity());

        if (itemRule.getType() == ItemType.SINGLE) {
            Object o = determineItemValue(itemRule.getValueType(), itemRule.getValue());
            itemParameters.putValueNonNull(ItemParameters.value, o);
        } else if (itemRule.isListableType()) {
            List<String> valueList = itemRule.getValueList();
            if (valueList != null && !valueList.isEmpty()) {
                Parameter p = itemParameters.getParameter(ItemParameters.value);
                p.arraylize();
                for (String value : valueList) {
                    Object o = determineItemValue(itemRule.getValueType(), value);
                    p.putValue(o);
                }
            }
        } else if (itemRule.isMappableType()) {
            Map<String, String> valueMap = itemRule.getValueMap();
            if (valueMap != null) {
                Parameters p = itemParameters.newParameters(ItemParameters.value);
                for (Map.Entry<String, String> entry : valueMap.entrySet()) {
                    Object o = determineItemValue(itemRule.getValueType(), entry.getValue());
                    p.putValue(entry.getKey(), o);
                }
            }
        }

        return itemParameters;
    }

    private static Object determineItemValue(ItemValueType valueType, String value) {
        if (value == null) {
            return null;
        }
        if (valueType == ItemValueType.PARAMETERS) {
            return new VariableParameters(value);
        } else {
            return value;
        }
    }

}
