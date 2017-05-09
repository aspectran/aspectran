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
package com.aspectran.core.context.rule.converter;

import java.util.List;

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
import com.aspectran.core.context.rule.params.ForwardParameters;
import com.aspectran.core.context.rule.params.ItemHolderParameters;
import com.aspectran.core.context.rule.params.ItemParameters;
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
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

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

    private void convertAsEnvironmentRule(EnvironmentParameters environmentParameters) {
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

    private void convertAsAspectRule(AspectParameters aspectParameters) {
        String description = aspectParameters.getString(AspectParameters.description);
        String id = StringUtils.emptyToNull(aspectParameters.getString(AspectParameters.id));
        String order = aspectParameters.getString(AspectParameters.order);
        Boolean isolated = aspectParameters.getBoolean(AspectParameters.isolated);

        AspectRule aspectRule = AspectRule.newInstance(id, order, isolated);
        if (description != null) {
            aspectRule.setDescription(description);
        }

        Parameters joinpointParameters = aspectParameters.getParameters(AspectParameters.jointpoint);
        if (joinpointParameters != null) {
            AspectRule.updateJoinpoint(aspectRule, joinpointParameters);
        }

        Parameters settingsParameters = aspectParameters.getParameters(AspectParameters.settings);
        if (settingsParameters != null) {
            SettingsAdviceRule settingsAdviceRule = SettingsAdviceRule.newInstance(aspectRule, settingsParameters);
            aspectRule.setSettingsAdviceRule(settingsAdviceRule);
        }

        Parameters adviceParameters = aspectParameters.getParameters(AspectParameters.advice);
        if (adviceParameters != null) {
            String adviceBeanId = adviceParameters.getString(AdviceParameters.bean);
            if (!StringUtils.isEmpty(adviceBeanId)) {
                aspectRule.setAdviceBeanId(adviceBeanId);
                assistant.resolveAdviceBeanClass(adviceBeanId, aspectRule);
            }

            Parameters beforeAdviceParameters = adviceParameters.getParameters(AdviceParameters.beforeAdvice);
            if (beforeAdviceParameters != null) {
                Parameters actionParameters = beforeAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.BEFORE);
                convertAsActionRule(actionParameters, aspectAdviceRule);
                aspectRule.addAspectAdviceRule(aspectAdviceRule);
            }

            Parameters afterAdviceParameters = adviceParameters.getParameters(AdviceParameters.afterAdvice);
            if (afterAdviceParameters != null) {
                Parameters actionParameters = afterAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AFTER);
                convertAsActionRule(actionParameters, aspectAdviceRule);
                aspectRule.addAspectAdviceRule(aspectAdviceRule);
            }

            Parameters aroundAdviceParameters = adviceParameters.getParameters(AdviceParameters.aroundAdvice);
            if (aroundAdviceParameters != null) {
                Parameters actionParameters = aroundAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.AROUND);
                convertAsActionRule(actionParameters, aspectAdviceRule);
                aspectRule.addAspectAdviceRule(aspectAdviceRule);
            }

            Parameters finallyAdviceParameters = adviceParameters.getParameters(AdviceParameters.finallyAdvice);
            if (finallyAdviceParameters != null) {
                Parameters actionParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.action);
                AspectAdviceRule aspectAdviceRule = AspectAdviceRule.newInstance(aspectRule, AspectAdviceType.FINALLY);
                convertAsActionRule(actionParameters, aspectAdviceRule);
                aspectRule.addAspectAdviceRule(aspectAdviceRule);
                // for thrown
                Parameters etParameters = finallyAdviceParameters.getParameters(AdviceActionParameters.thrown);
                ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters);
                aspectAdviceRule.setExceptionThrownRule(etr);
            }
        }

        Parameters exceptionParameters = aspectParameters.getParameters(AspectParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = ExceptionRule.newInstance();
            exceptionRule.setDescription(exceptionParameters.getString(ExceptionParameters.description));
            List<Parameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (Parameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            aspectRule.setExceptionRule(exceptionRule);
        }

        assistant.addAspectRule(aspectRule);
    }

    private void convertAsBeanRule(Parameters beanParameters) throws ClassNotFoundException {
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
        Parameters filterParameters = beanParameters.getParameters(BeanParameters.filter);

        BeanRule beanRule;

        if (className == null && scan == null && factoryBean != null) {
            beanRule = BeanRule.newOfferedFactoryBeanInstance(id, factoryBean, factoryMethod, initMethod, destroyMethod, scope, singleton, lazyInit, important);
            assistant.resolveFactoryBeanClass(factoryBean, beanRule);
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
            Parameters constructorArgumentItemHolderParameters = constructorParameters.getParameters(ConstructorParameters.arguments);
            if (constructorArgumentItemHolderParameters != null) {
                ItemRuleMap constructorArgumentItemRuleMap = convertAsItemRuleMap(constructorArgumentItemHolderParameters);
                beanRule.setConstructorArgumentItemRuleMap(constructorArgumentItemRuleMap);
            }
        }

        if (propertyItemHolderParameters != null) {
            ItemRuleMap propertyItemRuleMap = convertAsItemRuleMap(propertyItemHolderParameters);
            beanRule.setPropertyItemRuleMap(propertyItemRuleMap);
        }

        assistant.addBeanRule(beanRule);
    }

    private void convertAsScheduleRule(Parameters scheduleParameters) {
        String description = scheduleParameters.getString(AspectParameters.description);
        String id = StringUtils.emptyToNull(scheduleParameters.getString(AspectParameters.id));

        ScheduleRule scheduleRule = ScheduleRule.newInstance(id);
        if (description != null) {
            scheduleRule.setDescription(description);
        }

        Parameters schedulerParameters = scheduleParameters.getParameters(ScheduleParameters.scheduler);
        if (schedulerParameters != null) {
            String schedulerBeanId = schedulerParameters.getString(SchedulerParameters.bean);
            if (!StringUtils.isEmpty(schedulerBeanId)) {
                scheduleRule.setSchedulerBeanId(schedulerBeanId);
            }
            Parameters triggerParameters = schedulerParameters.getParameters(SchedulerParameters.trigger);
            if (triggerParameters != null) {
                ScheduleRule.updateTrigger(scheduleRule, triggerParameters);
            }

            List<Parameters> jobParametersList = scheduleParameters.getParametersList(ScheduleParameters.job);
            if (jobParametersList != null) {
                for (Parameters jobParameters : jobParametersList) {
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

    private void convertAsTransletRule(Parameters transletParameters) {
        String description = transletParameters.getString(TransletParameters.description);
        String name = StringUtils.emptyToNull(transletParameters.getString(TransletParameters.name));
        String scan = transletParameters.getString(TransletParameters.scan);
        String mask = transletParameters.getString(TransletParameters.mask);
        String method = transletParameters.getString(TransletParameters.method);

        TransletRule transletRule = TransletRule.newInstance(name, mask, scan, method);

        if (description != null) {
            transletRule.setDescription(description);
        }

        Parameters requestParamters = transletParameters.getParameters(TransletParameters.request);
        if (requestParamters != null) {
            RequestRule requestRule = convertAsRequestRule(requestParamters);
            transletRule.setRequestRule(requestRule);
        }

        Parameters contentsParameters = transletParameters.getParameters(TransletParameters.contents);
        if (contentsParameters != null) {
            ContentList contentList = convertAsContentList(contentsParameters);
            transletRule.setContentList(contentList);
        }

        List<Parameters> contentParametersList = transletParameters.getParametersList(TransletParameters.content);
        if (contentParametersList != null && !contentParametersList.isEmpty()) {
            ContentList contentList = transletRule.touchContentList();
            for (Parameters contentParamters : contentParametersList) {
                ActionList actionList = convertAsActionList(contentParamters);
                contentList.addActionList(actionList);
            }
        }

        List<Parameters> responseParametersList = transletParameters.getParametersList(TransletParameters.response);
        if (responseParametersList != null) {
            for (Parameters responseParamters : responseParametersList) {
                ResponseRule responseRule = convertAsResponseRule(responseParamters);
                transletRule.addResponseRule(responseRule);
            }
        }

        Parameters exceptionParameters = transletParameters.getParameters(TransletParameters.exception);
        if (exceptionParameters != null) {
            ExceptionRule exceptionRule = new ExceptionRule();
            exceptionRule.setDescription(exceptionParameters.getString(ExceptionParameters.description));
            List<Parameters> etParametersList = exceptionParameters.getParametersList(ExceptionParameters.thrown);
            if (etParametersList != null) {
                for (Parameters etParameters : etParametersList) {
                    ExceptionThrownRule etr = convertAsExceptionThrownRule(etParameters);
                    exceptionRule.putExceptionThrownRule(etr);
                }
            }
            transletRule.setExceptionRule(exceptionRule);
        }

        List<Parameters> actionParametersList = transletParameters.getParametersList(TransletParameters.action);
        if (actionParametersList != null) {
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, transletRule);
            }
        }

        Parameters transformParameters = transletParameters.getParameters(TransletParameters.transform);
        if (transformParameters != null) {
            TransformRule tr = convertAsTransformRule(transformParameters);
            transletRule.applyResponseRule(tr);
        }

        Parameters dispatchParameters = transletParameters.getParameters(TransletParameters.dispatch);
        if (dispatchParameters != null) {
            DispatchResponseRule drr = convertAsDispatchResponseRule(dispatchParameters);
            transletRule.applyResponseRule(drr);
        }

        Parameters redirectParameters = transletParameters.getParameters(TransletParameters.redirect);
        if (redirectParameters != null) {
            RedirectResponseRule rrr = convertAsRedirectResponseRule(redirectParameters);
            transletRule.applyResponseRule(rrr);
        }

        Parameters forwardParameters = transletParameters.getParameters(TransletParameters.forward);
        if (forwardParameters != null) {
            ForwardResponseRule frr = convertAsForwardResponseRule(forwardParameters);
            transletRule.applyResponseRule(frr);
        }

        assistant.addTransletRule(transletRule);
    }

    private RequestRule convertAsRequestRule(Parameters requestParameters) {
        String allowedMethod = requestParameters.getString(RequestParameters.method);
        String characterEncoding = requestParameters.getString(RequestParameters.characterEncoding);
        ItemHolderParameters parameterItemHolderParameters = requestParameters.getParameters(RequestParameters.parameters);
        ItemHolderParameters attributeItemHolderParameters = requestParameters.getParameters(RequestParameters.attributes);

        RequestRule requestRule = RequestRule.newInstance(allowedMethod, characterEncoding);

        if (parameterItemHolderParameters != null) {
            ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parameterItemHolderParameters);
            requestRule.setParameterItemRuleMap(parameterItemRuleMap);
        }

        if (attributeItemHolderParameters != null) {
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributeItemHolderParameters);
            requestRule.setAttributeItemRuleMap(attributeItemRuleMap);
        }

        return requestRule;
    }

    private ResponseRule convertAsResponseRule(Parameters responseParameters) {
        String name = responseParameters.getString(ResponseParameters.name);
        String characterEncoding = responseParameters.getString(ResponseParameters.characterEncoding);

        ResponseRule responseRule = ResponseRule.newInstance(name, characterEncoding);

        Parameters transformParameters = responseParameters.getParameters(ResponseParameters.transform);
        if (transformParameters != null) {
            responseRule.applyResponseRule(convertAsTransformRule(transformParameters));
        }

        Parameters dispatchParameters = responseParameters.getParameters(ResponseParameters.dispatch);
        if (dispatchParameters != null) {
            responseRule.applyResponseRule(convertAsDispatchResponseRule(dispatchParameters));
        }

        Parameters redirectParameters = responseParameters.getParameters(ResponseParameters.redirect);
        if (redirectParameters != null) {
            responseRule.applyResponseRule(convertAsRedirectResponseRule(redirectParameters));
        }

        Parameters forwardParameters = responseParameters.getParameters(ResponseParameters.forward);
        if (forwardParameters != null) {
            responseRule.applyResponseRule(convertAsForwardResponseRule(forwardParameters));
        }

        return responseRule;
    }

    private ContentList convertAsContentList(Parameters contentsParameters) {
        String name = contentsParameters.getString(ContentsParameters.name);
        Boolean omittable = contentsParameters.getBoolean(ContentsParameters.omittable);
        List<Parameters> contentParametersList = contentsParameters.getParametersList(ContentsParameters.content);

        ContentList contentList = ContentList.newInstance(name, omittable);

        if (contentParametersList != null) {
            for (Parameters contentParamters : contentParametersList) {
                ActionList actionList = convertAsActionList(contentParamters);
                contentList.addActionList(actionList);
            }
        }

        return contentList;
    }

    private ActionList convertAsActionList(Parameters contentParameters) {
        String name = contentParameters.getString(ContentParameters.name);
        Boolean omittable = contentParameters.getBoolean(ContentParameters.omittable);
        Boolean hidden = contentParameters.getBoolean(ContentParameters.hidden);
        List<Parameters> actionParametersList = contentParameters.getParametersList(ContentParameters.action);

        ActionList actionList = ActionList.newInstance(name, omittable, hidden);

        if (actionParametersList != null) {
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
        }

        return actionList;
    }

    private void convertAsActionRule(Parameters actionParameters, ActionRuleApplicable actionRuleApplicable) {
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
            if (beanIdOrClass != null) {
                assistant.resolveActionBeanClass(beanIdOrClass, beanActionRule);
            }
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

    private ExceptionThrownRule convertAsExceptionThrownRule(Parameters exceptionThrownParameters) {
        ExceptionThrownRule exceptionThrownRule = new ExceptionThrownRule();

        String[] exceptionTypes = exceptionThrownParameters.getStringArray(ExceptionThrownParameters.type);
        exceptionThrownRule.setExceptionTypes(exceptionTypes);

        Parameters actionParameters = exceptionThrownParameters.getParameters(ExceptionThrownParameters.action);
        if (actionParameters != null) {
            convertAsActionRule(actionParameters, exceptionThrownRule);
        }

        List<Parameters> transformParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.transform);
        if (transformParametersList != null && !transformParametersList.isEmpty()) {
            convertAsTransformRule(transformParametersList, exceptionThrownRule);
        }

        List<Parameters> dispatchParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.dispatch);
        if (dispatchParametersList != null && !dispatchParametersList.isEmpty()) {
            convertAsDispatchResponseRule(dispatchParametersList, exceptionThrownRule);
        }

        List<Parameters> redirectParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.redirect);
        if (redirectParametersList != null && !redirectParametersList.isEmpty()) {
            convertAsRedirectResponseRule(redirectParametersList, exceptionThrownRule);
        }

        List<Parameters> forwardParametersList = exceptionThrownParameters.getParametersList(ExceptionThrownParameters.forward);
        if (forwardParametersList != null && !forwardParametersList.isEmpty()) {
            convertAsForwardResponseRule(forwardParametersList, exceptionThrownRule);
        }

        return exceptionThrownRule;
    }

    private void convertAsTransformRule(List<Parameters> transformParametersList, ResponseRuleApplicable responseRuleApplicable) {
        for (Parameters transformParameters : transformParametersList) {
            TransformRule tr = convertAsTransformRule(transformParameters);
            responseRuleApplicable.applyResponseRule(tr);
        }
    }

    private TransformRule convertAsTransformRule(Parameters transformParameters) {
        String transformType = transformParameters.getString(TransformParameters.type);
        String contentType = transformParameters.getString(TransformParameters.contentType);
        String characterEncoding = transformParameters.getString(TransformParameters.characterEncoding);
        List<Parameters> actionParametersList = transformParameters.getParametersList(TransformParameters.action);
        Boolean defaultResponse = transformParameters.getBoolean(TransformParameters.defaultResponse);
        Boolean pretty = transformParameters.getBoolean(TransformParameters.pretty);
        Parameters templateParameters = transformParameters.getParameters(TransformParameters.template);
        Parameters callParameters = transformParameters.getParameters(TransformParameters.call);

        TransformRule tr = TransformRule.newInstance(transformType, contentType, characterEncoding, defaultResponse, pretty);

        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            tr.setActionList(actionList);
        }

        if (callParameters != null) {
            String templateId = StringUtils.emptyToNull(callParameters.getString(CallParameters.template));
            if (templateId != null) {
                tr.setTemplateId(templateId);
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
            String encoding = templateParameters.getString(TemplateParameters.encoding);
            Boolean noCache = templateParameters.getBoolean(TemplateParameters.noCache);

            TemplateRule templateRule = TemplateRule.newInstanceForBuiltin(engine, name, file, resource, url, content, style, encoding, noCache);
            tr.setTemplateRule(templateRule);

            assistant.resolveBeanClass(templateRule.getTemplateTokens());
        }

        return tr;
    }

    private void convertAsDispatchResponseRule(List<Parameters> dispatchParametersList, ResponseRuleApplicable responseRuleApplicable) {
        for (Parameters dispatchParameters : dispatchParametersList) {
            DispatchResponseRule drr = convertAsDispatchResponseRule(dispatchParameters);
            responseRuleApplicable.applyResponseRule(drr);
        }
    }

    private DispatchResponseRule convertAsDispatchResponseRule(Parameters dispatchParameters) {
        String name = dispatchParameters.getString(DispatchParameters.name);
        String dispatcher = dispatchParameters.getString(DispatchParameters.dispatcher);
        String contentType = dispatchParameters.getString(DispatchParameters.contentType);
        String characterEncoding = dispatchParameters.getString(DispatchParameters.characterEncoding);
        List<Parameters> actionParametersList = dispatchParameters.getParametersList(DispatchParameters.action);
        Boolean defaultResponse = dispatchParameters.getBoolean(DispatchParameters.defaultResponse);

        DispatchResponseRule drr = DispatchResponseRule.newInstance(name, dispatcher, contentType, characterEncoding, defaultResponse);

        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            drr.setActionList(actionList);
        }

        return drr;
    }

    private void convertAsRedirectResponseRule(List<Parameters> redirectParametersList, ResponseRuleApplicable responseRuleApplicable) {
        for (Parameters redirectParameters : redirectParametersList) {
            RedirectResponseRule rrr = convertAsRedirectResponseRule(redirectParameters);
            responseRuleApplicable.applyResponseRule(rrr);
        }
    }

    private RedirectResponseRule convertAsRedirectResponseRule(Parameters redirectParameters) {
        String contentType = redirectParameters.getString(RedirectParameters.contentType);
        String target = redirectParameters.getString(RedirectParameters.target);
        ItemHolderParameters parameterItemHolderParametersList = redirectParameters.getParameters(RedirectParameters.parameters);
        String characterEncoding = redirectParameters.getString(RedirectParameters.characterEncoding);
        Boolean excludeNullParameter = redirectParameters.getBoolean(RedirectParameters.excludeNullParameter);
        Boolean excludeEmptyParameter = redirectParameters.getBoolean(RedirectParameters.excludeEmptyParameter);
        Boolean defaultResponse = redirectParameters.getBoolean(RedirectParameters.defaultResponse);
        List<Parameters> actionParametersList = redirectParameters.getParametersList(RedirectParameters.action);

        RedirectResponseRule rrr = RedirectResponseRule.newInstance(contentType, target, characterEncoding, excludeNullParameter, excludeEmptyParameter, defaultResponse);

        if (parameterItemHolderParametersList != null) {
            ItemRuleMap parameterItemRuleMap = convertAsItemRuleMap(parameterItemHolderParametersList);
            rrr.setParameterItemRuleMap(parameterItemRuleMap);
        }

        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            rrr.setActionList(actionList);
        }

        return rrr;
    }

    private void convertAsForwardResponseRule(List<Parameters> forwardParametersList, ResponseRuleApplicable responseRuleApplicable) {
        for (Parameters forwardParameters : forwardParametersList) {
            ForwardResponseRule frr = convertAsForwardResponseRule(forwardParameters);
            responseRuleApplicable.applyResponseRule(frr);
        }
    }

    private ForwardResponseRule convertAsForwardResponseRule(Parameters forwardParameters) {
        String contentType = forwardParameters.getString(ForwardParameters.contentType);
        String translet = StringUtils.emptyToNull(forwardParameters.getString(ForwardParameters.translet));
        ItemHolderParameters attributeItemHolderParametersList = forwardParameters.getParameters(ForwardParameters.attributes);
        List<Parameters> actionParametersList = forwardParameters.getParametersList(ForwardParameters.action);
        Boolean defaultResponse = forwardParameters.getBoolean(ForwardParameters.defaultResponse);

        translet = assistant.applyTransletNamePattern(translet);

        ForwardResponseRule frr = ForwardResponseRule.newInstance(contentType, translet, defaultResponse);

        if (attributeItemHolderParametersList != null) {
            ItemRuleMap attributeItemRuleMap = convertAsItemRuleMap(attributeItemHolderParametersList);
            frr.setAttributeItemRuleMap(attributeItemRuleMap);
        }

        if (actionParametersList != null && !actionParametersList.isEmpty()) {
            ActionList actionList = new ActionList();
            for (Parameters actionParameters : actionParametersList) {
                convertAsActionRule(actionParameters, actionList);
            }
            frr.setActionList(actionList);
        }

        return frr;
    }

    private ItemRuleMap convertAsItemRuleMap(Parameters itemHolderParameters) {
        List<ItemParameters> itemParametersList = itemHolderParameters.getParametersList(ItemHolderParameters.item);
        ItemRuleMap itemRuleMap = ItemRule.toItemRuleMap(itemParametersList);

        if (itemRuleMap != null) {
            for (ItemRule itemRule : itemRuleMap.values()) {
                assistant.resolveBeanClass(itemRule);
            }
        }

        return itemRuleMap;
    }

    private void convertAsTemplateRule(Parameters templateParameters) {
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
