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
package com.aspectran.core.context.rule.converter;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.process.ContentList;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.EchoActionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ExceptionRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.HeadingActionRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.IncludeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.RequestRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.ScheduleJobRule;
import com.aspectran.core.context.rule.ScheduleRule;
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
import com.aspectran.core.context.rule.params.ScheduleJobParameters;
import com.aspectran.core.context.rule.params.ScheduleParameters;
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
public class ParamsToRuleConverter {

    private final ContextRuleAssistant assistant;

    public ParamsToRuleConverter(ContextRuleAssistant assistant) {
        this.assistant = assistant;
    }

    public void convertAsRule(RootParameters rootParameters) throws Exception {
        AspectranParameters aspectranParameters = rootParameters.getParameters(RootParameters.aspectran);
        convertAsRule(aspectranParameters);
    }

    public void convertAsRule(AspectranParameters aspectranParameters) throws Exception {
        String description = aspectranParameters.getString(AspectranParameters.description);
        if (description != null) {
            assistant.getAssistantLocal().setDescription(description);
        }

        DefaultSettingsParameters defaultSettingsParameters = aspectranParameters.getParameters(AspectranParameters.settings);
        if (defaultSettingsParameters != null) {
            convertAsDefaultSettings(defaultSettingsParameters);
        }

        List<EnvironmentParameters> environmentParametersList = aspectranParameters.getParametersList(AspectranParameters.environment);
        if (environmentParametersList != null) {
            for (EnvironmentParameters environmentParameters : environmentParametersList) {
                convertAsEnvironmentRule(environmentParameters);
            }
        }

        Parameters typeAliasParameters = aspectranParameters.getParameters(AspectranParameters.typeAlias);
        if (typeAliasParameters != null) {
            convertAsTypeAlias(typeAliasParameters);
        }

        List<AspectParameters> aspectParametersList = aspectranParameters.getParametersList(AspectranParameters.aspect);
        if (aspectParametersList != null) {
            for (AspectParameters aspectParameters : aspectParametersList) {
                convertAsAspectRule(aspectParameters);
            }
        }

        List<BeanParameters> beanParametersList = aspectranParameters.getParametersList(AspectranParameters.bean);
        if (beanParametersList != null) {
            for (BeanParameters beanParameters : beanParametersList) {
                convertAsBeanRule(beanParameters);
            }
        }

        List<ScheduleParameters> scheduleParametersList = aspectranParameters.getParametersList(AspectranParameters.schedule);
        if (scheduleParametersList != null) {
            for (ScheduleParameters scheduleParameters : scheduleParametersList) {
                convertAsScheduleRule(scheduleParameters);
            }
        }

        List<TransletParameters> transletParametersList = aspectranParameters.getParametersList(AspectranParameters.translet);
        if (transletParametersList != null) {
            for (TransletParameters transletParameters : transletParametersList) {
                convertAsTransletRule(transletParameters);
            }
        }

        List<TemplateParameters> templateParametersList = aspectranParameters.getParametersList(AspectranParameters.template);
        if (templateParametersList != null) {
            for (TemplateParameters templateParameters : templateParametersList) {
                convertAsTemplateRule(templateParameters);
            }
        }

        List<AppendParameters> appendParametersList = aspectranParameters.getParametersList(AspectranParameters.append);
        if (appendParametersList != null) {
            for (AppendParameters appendParameters : appendParametersList) {
                convertAsPendingAppender(appendParameters);
            }
        }
    }

    private void convertAsPendingAppender(AppendParameters appendParameters) throws Exception {
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

    private void convertAsDefaultSettings(DefaultSettingsParameters defaultSettingsParameters) throws ClassNotFoundException {
        if (defaultSettingsParameters == null) {
            return;
        }
        for (String name : defaultSettingsParameters.getParameterNameSet()) {
            assistant.putSetting(name, defaultSettingsParameters.getString(name));
        }
        assistant.applySettings();
    }

    private void convertAsEnvironmentRule(EnvironmentParameters environmentParameters) throws IllegalRuleException {
        if (environmentParameters != null) {
            String profile = StringUtils.emptyToNull(environmentParameters.getString(EnvironmentParameters.profile));
            ItemHolderParameters propertyItemHolderParameters = environmentParameters.getParameters(EnvironmentParameters.properties);
            ItemRuleMap propertyItemRuleMap = null;
            if (propertyItemHolderParameters != null) {
                propertyItemRuleMap = convertAsItemRuleMap(propertyItemHolderParameters);
            }
            EnvironmentRule environmentRule = EnvironmentRule.newInstance(profile, propertyItemRuleMap);
            assistant.addEnvironmentRule(environmentRule);
        }
    }

    private void convertAsTypeAlias(Parameters parameters) {
        if (parameters != null) {
            for (String alias : parameters.getParameterNameSet()) {
                assistant.addTypeAlias(alias, parameters.getString(alias));
            }
        }
    }

    private void convertAsAspectRule(AspectParameters aspectParameters) throws IllegalRuleException {
        String description = aspectParameters.getString(AspectParameters.description);
        String id = StringUtils.emptyToNull(aspectParameters.getString(AspectParameters.id));
        String order = aspectParameters.getString(AspectParameters.order);
        Boolean isolated = aspectParameters.getBoolean(AspectParameters.isolated);

        AspectRule aspectRule = AspectRule.newInstance(id, order, isolated);
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
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.BEFORE);
                convertAsActionRule(actionParameters, aspectAdviceRule);
            }

            AdviceActionParameters afterAdviceParameters = adviceParameters.getParameters(AdviceParameters.afterAdvice);
            if (afterAdviceParameters != null) {
                ActionParameters actionParameters = afterAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AFTER);
                convertAsActionRule(actionParameters, aspectAdviceRule);
            }

            AdviceActionParameters aroundAdviceParameters = adviceParameters.getParameters(AdviceParameters.aroundAdvice);
            if (aroundAdviceParameters != null) {
                ActionParameters actionParameters = aroundAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AROUND);
                convertAsActionRule(actionParameters, aspectAdviceRule);
            }

            AdviceActionParameters finallyAdviceParameters = adviceParameters.getParameters(AdviceParameters.finallyAdvice);
            if (finallyAdviceParameters != null) {
                ActionParameters actionParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.FINALLY);
                convertAsActionRule(actionParameters, aspectAdviceRule);
                // for thrown
                ExceptionThrownParameters etParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.thrown);
                ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters, aspectAdviceRule);
                aspectAdviceRule.setExceptionThrownRule(etr);
            }
        }

        ExceptionParameters exceptionParameters = aspectParameters.getParameters(AspectParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();
            exceptionRule.setDescription(exceptionParameters.getString(ExceptionParameters.description));
            List<ExceptionThrownParameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (ExceptionThrownParameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            aspectRule.setExceptionRule(exceptionRule);
        }

        assistant.resolveAdviceBeanClass(aspectRule);
        assistant.addAspectRule(aspectRule);
    }

    private void convertAsBeanRule(BeanParameters beanParameters) throws IllegalRuleException {
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
        ItemHolderParameters propertyItemHolderParameters = beanParameters.getParameters(BeanParameters.properties);
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
            ItemHolderParameters constructorArgumentItemHolderParameters = constructorParameters.getParameters(ConstructorParameters.arguments);
            if (constructorArgumentItemHolderParameters != null) {
                ItemRuleMap constructorArgumentItemRuleMap = convertAsItemRuleMap(constructorArgumentItemHolderParameters);
                beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
            }
        }
        if (propertyItemHolderParameters != null) {
            ItemRuleMap propertyItemRuleMap = convertAsItemRuleMap(propertyItemHolderParameters);
            beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
        }

        assistant.resolveFactoryBeanClass(beanRule);
        assistant.addBeanRule(beanRule);
    }

    private void convertAsScheduleRule(ScheduleParameters scheduleParameters) throws IllegalRuleException {
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

            List<ScheduleJobParameters> jobParametersList = scheduleParameters.getParametersList(ScheduleParameters.job);
            if (jobParametersList != null) {
                for (ScheduleJobParameters jobParameters : jobParametersList) {
                    String translet = StringUtils.emptyToNull(jobParameters.getString(ScheduleJobParameters.translet));
                    String method = StringUtils.emptyToNull(jobParameters.getString(ScheduleJobParameters.method));
                    Boolean disabled = jobParameters.getBoolean(ScheduleJobParameters.disabled);

                    translet = assistant.applyTransletNamePattern(translet);

                    ScheduleJobRule scheduleJobRule = ScheduleJobRule.newInstance(scheduleRule, translet, method, disabled);
                    scheduleRule.addScheduleJobRule(scheduleJobRule);
                }
            }
        }

        assistant.addScheduleRule(scheduleRule);
    }

    private void convertAsTransletRule(TransletParameters transletParameters) throws IllegalRuleException {
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
            RequestRule requestRule = convertAsRequestRule(requestParameters);
            transletRule.setRequestRule(requestRule);
        }

        ItemHolderParameters parametersItemHolderParameters = transletParameters.getParameters(TransletParameters.parameters);
        if (parametersItemHolderParameters != null) {
            RequestRule requestRule = transletRule.getRequestRule();
            if (requestRule == null) {
                requestRule = RequestRule.newInstance(true);
                transletRule.setRequestRule(requestRule);
            }
            ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parametersItemHolderParameters);
            requestRule.setParameterItemRuleMap(parameterItemRuleMap);
        }

        ItemHolderParameters attributesItemHolderParameters = transletParameters.getParameters(TransletParameters.attributes);
        if (attributesItemHolderParameters != null) {
            RequestRule requestRule = transletRule.getRequestRule();
            if (requestRule == null) {
                requestRule = RequestRule.newInstance(true);
                transletRule.setRequestRule(requestRule);
            }
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributesItemHolderParameters);
            requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
        }

        ContentsParameters contentsParameters = transletParameters.getParameters(TransletParameters.contents);
        if (contentsParameters != null) {
            ContentList contentList = convertAsContentList(contentsParameters);
            transletRule.setContentList(contentList);
        }

        List<ContentParameters> contentParametersList = transletParameters.getParametersList(TransletParameters.content);
        if (contentParametersList != null && !contentParametersList.isEmpty()) {
            ContentList contentList = transletRule.touchContentList();
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = convertAsActionList(contentParameters);
                contentList.addActionList(actionList);
            }
        }

        List<ResponseParameters> responseParametersList = transletParameters.getParametersList(TransletParameters.response);
        if (responseParametersList != null) {
            for (ResponseParameters responseParameters : responseParametersList) {
                ResponseRule responseRule = convertAsResponseRule(responseParameters);
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
                    ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters, null);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            transletRule.setExceptionRule(exceptionRule);
        }

        List<ActionParameters> actionParametersList = transletParameters.getParametersList(TransletParameters.action);
        if (actionParametersList != null) {
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, transletRule);
            }
        }

        TransformParameters transformParameters = transletParameters.getParameters(TransletParameters.transform);
        if (transformParameters != null) {
            TransformRule tr = convertAsTransformRule(transformParameters);
            transletRule.applyResponseRule(tr);
        }

        DispatchParameters dispatchParameters = transletParameters.getParameters(TransletParameters.dispatch);
        if (dispatchParameters != null) {
            DispatchResponseRule drr = convertAsDispatchResponseRule(dispatchParameters);
            transletRule.applyResponseRule(drr);
        }

        RedirectParameters redirectParameters = transletParameters.getParameters(TransletParameters.redirect);
        if (redirectParameters != null) {
            RedirectResponseRule rrr = convertAsRedirectResponseRule(redirectParameters);
            transletRule.applyResponseRule(rrr);
        }

        ForwardParameters forwardParameters = transletParameters.getParameters(TransletParameters.forward);
        if (forwardParameters != null) {
            ForwardResponseRule frr = convertAsForwardResponseRule(forwardParameters);
            transletRule.applyResponseRule(frr);
        }

        assistant.addTransletRule(transletRule);
    }

    private RequestRule convertAsRequestRule(RequestParameters requestParameters) throws IllegalRuleException {
        String allowedMethod = requestParameters.getString(RequestParameters.method);
        String encoding = requestParameters.getString(RequestParameters.encoding);
        ItemHolderParameters parametersItemHolderParameters = requestParameters.getParameters(RequestParameters.parameters);
        ItemHolderParameters attributesItemHolderParameters = requestParameters.getParameters(RequestParameters.attributes);

        RequestRule requestRule = RequestRule.newInstance(allowedMethod, encoding);
        if (parametersItemHolderParameters != null) {
            ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parametersItemHolderParameters);
            requestRule.setParameterItemRuleMap(parameterItemRuleMap);
        }
        if (attributesItemHolderParameters != null) {
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributesItemHolderParameters);
            requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
        }
        return requestRule;
    }

    private ResponseRule convertAsResponseRule(ResponseParameters responseParameters) throws IllegalRuleException {
        String name = responseParameters.getString(ResponseParameters.name);
        String encoding = responseParameters.getString(ResponseParameters.encoding);

        ResponseRule responseRule = ResponseRule.newInstance(name, encoding);

        TransformParameters transformParameters = responseParameters.getParameters(ResponseParameters.transform);
        if (transformParameters != null) {
            responseRule.applyResponseRule(convertAsTransformRule(transformParameters));
        }

        DispatchParameters dispatchParameters = responseParameters.getParameters(ResponseParameters.dispatch);
        if (dispatchParameters != null) {
            responseRule.applyResponseRule(convertAsDispatchResponseRule(dispatchParameters));
        }

        RedirectParameters redirectParameters = responseParameters.getParameters(ResponseParameters.redirect);
        if (redirectParameters != null) {
            responseRule.applyResponseRule(convertAsRedirectResponseRule(redirectParameters));
        }

        ForwardParameters forwardParameters = responseParameters.getParameters(ResponseParameters.forward);
        if (forwardParameters != null) {
            responseRule.applyResponseRule(convertAsForwardResponseRule(forwardParameters));
        }

        return responseRule;
    }

    private ContentList convertAsContentList(ContentsParameters contentsParameters) throws IllegalRuleException {
        String name = contentsParameters.getString(ContentsParameters.name);
        Boolean omittable = contentsParameters.getBoolean(ContentsParameters.omittable);
        List<ContentParameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.content);

        ContentList contentList = ContentList.newInstance(name, omittable);
        if (contentParametersList != null) {
            for (ContentParameters contentParameters : contentParametersList) {
                ActionList actionList = convertAsActionList(contentParameters);
                contentList.addActionList(actionList);
            }
        }
        return contentList;
    }

    private ActionList convertAsActionList(ContentParameters contentParameters) throws IllegalRuleException {
        String name = contentParameters.getString(ContentParameters.name);
        Boolean omittable = contentParameters.getBoolean(ContentParameters.omittable);
        Boolean hidden = contentParameters.getBoolean(ContentParameters.hidden);
        List<ActionParameters> actionParametersList = contentParameters.getParametersList(ContentParameters.action);

        ActionList actionList = ActionList.newInstance(name, omittable, hidden);
        if (actionParametersList != null) {
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
        }
        return actionList;
    }

    private void convertAsActionRule(ActionParameters actionParameters, ActionRuleApplicable actionRuleApplicable) throws IllegalRuleException {
        String id = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.id));
        String methodName = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.methodName));
        String include = actionParameters.getString(ActionParameters.include);
        ItemHolderParameters echoItemHolderParameters = actionParameters.getParameters(ActionParameters.echo);
        ItemHolderParameters headersItemHolderParameters = actionParameters.getParameters(ActionParameters.headers);
        Boolean hidden = actionParameters.getBoolean(ActionParameters.hidden);

        if (methodName != null) {
            String beanIdOrClass = StringUtils.emptyToNull(actionParameters.getString(ActionParameters.bean));
            ItemHolderParameters argumentItemHolderParameters = actionParameters.getParameters(ActionParameters.arguments);
            ItemHolderParameters propertyItemHolderParameters = actionParameters.getParameters(ActionParameters.properties);
            BeanActionRule beanActionRule = BeanActionRule.newInstance(id, beanIdOrClass, methodName, hidden);
            if (argumentItemHolderParameters != null) {
                ItemRuleMap argumentItemRuleMap = convertAsItemRuleMap(argumentItemHolderParameters);
                beanActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
            }
            if (propertyItemHolderParameters != null) {
                ItemRuleMap propertyItemRuleMap = convertAsItemRuleMap(propertyItemHolderParameters);
                beanActionRule.setPropertyItemRuleMap(propertyItemRuleMap);
            }
            assistant.resolveActionBeanClass(beanActionRule);
            actionRuleApplicable.applyActionRule(beanActionRule);
        } else if (include != null) {
            include = assistant.applyTransletNamePattern(include);
            IncludeActionRule includeActionRule = IncludeActionRule.newInstance(id, include, hidden);
            ItemHolderParameters parameterItemHolderParameters = actionParameters.getParameters(ActionParameters.parameters);
            if (parameterItemHolderParameters != null) {
                ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parameterItemHolderParameters);
                includeActionRule.setParameterItemRuleMap(parameterItemRuleMap);
            }
            ItemHolderParameters attributeItemHolderParameters = actionParameters.getParameters(ActionParameters.attributes);
            if (attributeItemHolderParameters != null) {
                ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributeItemHolderParameters);
                includeActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
            }
            actionRuleApplicable.applyActionRule(includeActionRule);
        } else if (echoItemHolderParameters != null) {
            EchoActionRule echoActionRule = EchoActionRule.newInstance(id, hidden);
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(echoItemHolderParameters);
            echoActionRule.setAttributeItemRuleMap(attributeItemRuleMap);
            actionRuleApplicable.applyActionRule(echoActionRule);
        } else if (headersItemHolderParameters != null) {
            HeadingActionRule headingActionRule = HeadingActionRule.newInstance(id, hidden);
            ItemRuleMap headerItemRuleMap = convertAsItemRuleMap(headersItemHolderParameters);
            headingActionRule.setHeaderItemRuleMap(headerItemRuleMap);
            actionRuleApplicable.applyActionRule(headingActionRule);
        }
    }

    private ExceptionThrownRule convertAsExceptionThrownRule(ExceptionThrownParameters exceptionThrownParameters, AspectAdviceRule aspectAdviceRule)
            throws IllegalRuleException {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule(aspectAdviceRule);

        String[] exceptionTypes = exceptionThrownParameters.getStringArray(ExceptionThrownParameters.type);
        exceptionThrownRule.setExceptionTypes(exceptionTypes);

        ActionParameters actionParameters = exceptionThrownParameters.getParameters(ExceptionThrownParameters.action);
        if (actionParameters != null) {
            convertAsActionRule(actionParameters, exceptionThrownRule);
        }

        List<TransformParameters> transformParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.transform);
        if (transformParametersList != null && !transformParametersList.isEmpty()) {
            convertAsTransformRule(transformParametersList, exceptionThrownRule);
        }

        List<DispatchParameters> dispatchParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.dispatch);
        if (dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
            convertAsDispatchResponseRule(dispatchParametersList, exceptionThrownRule);
        }

        List<RedirectParameters> redirectParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.redirect);
        if (redirectParametersList != null && !redirectParametersList.isEmpty()) {
            convertAsRedirectResponseRule(redirectParametersList, exceptionThrownRule);
        }

        List<ForwardParameters> forwardParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.forward);
        if (forwardParametersList != null && !forwardParametersList.isEmpty()) {
            convertAsForwardResponseRule(forwardParametersList, exceptionThrownRule);
        }

        return exceptionThrownRule;
    }

    private void convertAsTransformRule(List<TransformParameters> transformParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (TransformParameters transformParameters : transformParametersList) {
            TransformRule tr = convertAsTransformRule(transformParameters);
            responseRuleApplicable.applyResponseRule(tr);
        }
    }

    private TransformRule convertAsTransformRule(TransformParameters transformParameters) throws IllegalRuleException {
        String transformType = transformParameters.getString(TransformParameters.type);
        String contentType = transformParameters.getString(TransformParameters.contentType);
        String encoding = transformParameters.getString(TransformParameters.encoding);
        List<ActionParameters> actionParametersList = transformParameters.getParametersList(TransformParameters.action);
        Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
        Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
        TemplateParameters templateParameters = transformParameters.getParameters(TransformParameters.template);
        CallParameters callParameters = transformParameters.getParameters(TransformParameters.call);

        TransformRule tr = TransformRule.newInstance(transformType, contentType, encoding, defaultResponse, pretty);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            tr.setActionList(actionList);
        }
        if (callParameters != null) {
            String templateId = StringUtils.emptyToNull(callParameters.getString(CallParameters.template));
            TransformRule.updateTemplateId(tr, templateId);
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
            tr.setTemplateRule(templateRule);
            assistant.resolveBeanClass(templateRule.getTemplateTokens());
        }
        return tr;
    }

    private void convertAsDispatchResponseRule(List<DispatchParameters> dispatchParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (DispatchParameters dispatchParameters : dispatchParametersList) {
            DispatchResponseRule drr = convertAsDispatchResponseRule(dispatchParameters);
            responseRuleApplicable.applyResponseRule(drr);
        }
    }

    private DispatchResponseRule convertAsDispatchResponseRule(DispatchParameters dispatchParameters) throws IllegalRuleException {
        String name = dispatchParameters.getString(DispatchParameters.name);
        String dispatcher = dispatchParameters.getString(DispatchParameters.dispatcher);
        String contentType = dispatchParameters.getString(DispatchParameters.contentType);
        String encoding = dispatchParameters.getString(DispatchParameters.encoding);
        List<ActionParameters> actionParametersList = dispatchParameters.getParametersList(DispatchParameters.action);
        Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);

        DispatchResponseRule drr = DispatchResponseRule.newInstance(name, dispatcher, contentType, encoding, defaultResponse);
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            drr.setActionList(actionList);
        }
        return drr;
    }

    private void convertAsRedirectResponseRule(List<RedirectParameters> redirectParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (RedirectParameters redirectParameters : redirectParametersList) {
            RedirectResponseRule rrr = convertAsRedirectResponseRule(redirectParameters);
            responseRuleApplicable.applyResponseRule(rrr);
        }
    }

    private RedirectResponseRule convertAsRedirectResponseRule(RedirectParameters redirectParameters) throws IllegalRuleException {
        String contentType = redirectParameters.getString(RedirectParameters.contentType);
        String path = redirectParameters.getString(RedirectParameters.path);
        ItemHolderParameters parameterItemHolderParametersList = redirectParameters.getParameters(RedirectParameters.parameters);
        String encoding = redirectParameters.getString(RedirectParameters.encoding);
        Boolean excludeNullParameter = redirectParameters.getBoolean(RedirectParameters.excludeNullParameter);
        Boolean excludeEmptyParameter = redirectParameters.getBoolean(RedirectParameters.excludeEmptyParameter);
        Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
        List<ActionParameters> actionParametersList = redirectParameters.getParametersList(RedirectParameters.action);

        RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, path, encoding, excludeNullParameter, excludeEmptyParameter, defaultResponse);
        if (parameterItemHolderParametersList != null) {
            ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parameterItemHolderParametersList);
            rrr.setParameterItemRuleMap(parameterItemRuleMap);
        }
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            rrr.setActionList(actionList);
        }
        return rrr;
    }

    private void convertAsForwardResponseRule(List<ForwardParameters> forwardParametersList, ResponseRuleApplicable responseRuleApplicable)
            throws IllegalRuleException {
        for (ForwardParameters forwardParameters : forwardParametersList) {
            ForwardResponseRule frr = convertAsForwardResponseRule(forwardParameters);
            responseRuleApplicable.applyResponseRule(frr);
        }
    }

    private ForwardResponseRule convertAsForwardResponseRule(ForwardParameters forwardParameters) throws IllegalRuleException {
        String contentType = forwardParameters.getString(ForwardParameters.contentType);
        String translet = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.translet));
        ItemHolderParameters attributeItemHolderParametersList = forwardParameters.getParameters(ForwardParameters.attributes);
        List<ActionParameters> actionParametersList = forwardParameters.getParametersList(ForwardParameters.action);
        Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);

        translet = assistant.applyTransletNamePattern(translet);

        ForwardResponseRule frr = ForwardResponseRule.newInstance(contentType, translet, defaultResponse);
        if (attributeItemHolderParametersList != null) {
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributeItemHolderParametersList);
            frr.setAttributeItemRuleMap(attributeItemRuleMap);
        }
        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (ActionParameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            frr.setActionList(actionList);
        }
        return frr;
    }

    private ItemRuleMap convertAsItemRuleMap(ItemHolderParameters itemHolderParameters) throws IllegalRuleException {
        List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList(ItemHolderParameters.item);
        ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(itemParametersList);
        if (itemRuleMap != null) {
            for (ItemRule itemRule : itemRuleMap.values()) {
                assistant.resolveBeanClass(itemRule);
            }
        }
        return itemRuleMap;
    }

    private void convertAsTemplateRule(TemplateParameters templateParameters) throws IllegalRuleException {
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
