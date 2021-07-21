/*
 * Copyright (c) 2008-2021 The Aspectran Project
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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.process.action.AnnotatedAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.activity.response.transform.CustomTransformResponse;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Around;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.AttrItem;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.CronTrigger;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.Destroy;
import com.aspectran.core.component.bean.annotation.Dispatch;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Forward;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.core.component.bean.annotation.Job;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.component.bean.annotation.ParamItem;
import com.aspectran.core.component.bean.annotation.Profile;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Redirect;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.component.bean.annotation.RequestToDelete;
import com.aspectran.core.component.bean.annotation.RequestToGet;
import com.aspectran.core.component.bean.annotation.RequestToPatch;
import com.aspectran.core.component.bean.annotation.RequestToPost;
import com.aspectran.core.component.bean.annotation.RequestToPut;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.core.component.bean.annotation.Schedule;
import com.aspectran.core.component.bean.annotation.Scope;
import com.aspectran.core.component.bean.annotation.Settings;
import com.aspectran.core.component.bean.annotation.SimpleTrigger;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.component.bean.annotation.Value;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The Class AnnotatedConfigParser.
 *
 * <p>Created: 2016. 2. 16.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(AnnotatedConfigParser.class);

    private final EnvironmentProfiles environmentProfiles;

    private final Map<String, BeanRule> idBasedBeanRuleMap;

    private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap;

    private final Map<Class<?>, BeanRule> configurableBeanRuleMap;

    private final AnnotatedConfigRelater configRelater;

    public AnnotatedConfigParser(ActivityRuleAssistant assistant, AnnotatedConfigRelater configRelater) {
        this.environmentProfiles = assistant.getEnvironmentProfiles();
        this.idBasedBeanRuleMap = assistant.getBeanRuleRegistry().getIdBasedBeanRuleMap();
        this.typeBasedBeanRuleMap = assistant.getBeanRuleRegistry().getTypeBasedBeanRuleMap();
        this.configurableBeanRuleMap = assistant.getBeanRuleRegistry().getConfigurableBeanRuleMap();
        this.configRelater = configRelater;
    }

    public void parse() throws IllegalRuleException {
        if (configurableBeanRuleMap.isEmpty() && idBasedBeanRuleMap.isEmpty() && typeBasedBeanRuleMap.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Now trying to parse annotated configurations");
        }

        if (!configurableBeanRuleMap.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Parsing bean rules for annotated configurations: " + configurableBeanRuleMap.size());
            }
            for (BeanRule beanRule : configurableBeanRuleMap.values()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("configurableBeanRule " + beanRule);
                }
                if (!beanRule.isFactoryOffered()) {
                    parseConfigurableBean(beanRule);
                    parseConstructorAutowire(beanRule);
                    parseFieldAutowire(beanRule);
                    parseMethodAutowire(beanRule);
                }
            }
        }

        if (!idBasedBeanRuleMap.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Parsing for ID-based bean rules: " + idBasedBeanRuleMap.size());
            }
            for (BeanRule beanRule : idBasedBeanRuleMap.values()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("idBasedBeanRule " + beanRule);
                }
                if (!beanRule.isFactoryOffered()) {
                    parseConstructorAutowire(beanRule);
                    parseFieldAutowire(beanRule);
                    parseMethodAutowire(beanRule);
                }
            }
        }

        if (!typeBasedBeanRuleMap.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Parsing for type-based bean rules: " + typeBasedBeanRuleMap.size());
            }
            for (Set<BeanRule> set : typeBasedBeanRuleMap.values()) {
                for (BeanRule beanRule : set) {
                    if (!beanRule.isFactoryOffered()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("typeBasedBeanRule " + beanRule);
                        }
                        parseConstructorAutowire(beanRule);
                        parseFieldAutowire(beanRule);
                        parseMethodAutowire(beanRule);
                    }
                }
            }
        }
    }

    private void parseConfigurableBean(BeanRule beanRule) throws IllegalRuleException {
        Class<?> beanClass = beanRule.getBeanClass();
        Component componentAnno = beanClass.getAnnotation(Component.class);
        if (componentAnno != null) {
            if (beanClass.isAnnotationPresent(Profile.class)) {
                Profile profileAnno = beanClass.getAnnotation(Profile.class);
                if (!environmentProfiles.acceptsProfiles(profileAnno.value())) {
                    return;
                }
            }
            String[] nameArray = explodeNamespace(componentAnno.value());
            if (beanClass.isAnnotationPresent(Aspect.class)) {
                parseAspectRule(beanClass, nameArray);
            }
            if (beanClass.isAnnotationPresent(Bean.class)) {
                parseBeanRule(beanRule, nameArray);
            }
            if (beanClass.isAnnotationPresent(Schedule.class)) {
                parseScheduleRule(beanRule, nameArray);
            }
            for (Method method : beanClass.getMethods()) {
                if (method.isAnnotationPresent(Profile.class)) {
                    Profile profileAnno = method.getAnnotation(Profile.class);
                    if (!environmentProfiles.acceptsProfiles(profileAnno.value())) {
                        continue;
                    }
                }
                if (method.isAnnotationPresent(Bean.class)) {
                    parseFactoryBeanRule(beanClass, method, nameArray);
                } else if (method.isAnnotationPresent(Request.class) ||
                        method.isAnnotationPresent(RequestToGet.class) ||
                        method.isAnnotationPresent(RequestToPost.class) ||
                        method.isAnnotationPresent(RequestToPut.class) ||
                        method.isAnnotationPresent(RequestToPatch.class) ||
                        method.isAnnotationPresent(RequestToDelete.class)) {
                    parseTransletRule(beanClass, method, nameArray);
                }
            }
        }
    }

    private void parseConstructorAutowire(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule.isConstructorAutowireParsed()) {
            return;
        } else {
            beanRule.setConstructorAutowireParsed(true);
        }
        ItemRuleMap ctorArgumentItemRuleMap = beanRule.getConstructorArgumentItemRuleMap();
        if (ctorArgumentItemRuleMap != null && !ctorArgumentItemRuleMap.isEmpty()) {
            return;
        }
        Class<?> beanClass = beanRule.getBeanClass();
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        Constructor<?> candidate = null;
        if (constructors.length == 1) {
            candidate = constructors[0];
        } else {
            for (Constructor<?> ctor : constructors) {
                if (ctor.isAnnotationPresent(Autowired.class)) {
                    candidate = ctor;
                    break;
                }
            }
        }
        if (candidate != null) {
            AutowireRule autowireRule = createAutowireRuleForConstructor(candidate);
            if (autowireRule != null) {
                beanRule.setConstructorAutowireRule(autowireRule);
                configRelater.relate(autowireRule);
            }
        }
    }

    private void parseFieldAutowire(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule.isFieldAutowireParsed()) {
            return;
        } else {
            beanRule.setFieldAutowireParsed(true);
        }
        Class<?> beanClass = beanRule.getBeanClass();
        while (beanClass != null && beanClass != Object.class) {
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    AutowireRule autowireRule = createAutowireRuleForField(field);
                    beanRule.addAutowireRule(autowireRule);
                    configRelater.relate(autowireRule);
                } else if (field.isAnnotationPresent(Value.class)) {
                    AutowireRule autowireRule = createAutowireRuleForFieldValue(field);
                    if (autowireRule != null) {
                        beanRule.addAutowireRule(autowireRule);
                        configRelater.relate(autowireRule);
                    }
                }
            }
            beanClass = beanClass.getSuperclass();
        }
    }

    private void parseMethodAutowire(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule.isMethodAutowireParsed()) {
            return;
        } else {
            beanRule.setMethodAutowireParsed(true);
        }
        Class<?> beanClass = beanRule.getBeanClass();
        while (beanClass != null && beanClass != Object.class) {
            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    AutowireRule autowireRule = createAutowireRuleForMethod(method);
                    if (autowireRule != null) {
                        beanRule.addAutowireRule(autowireRule);
                        configRelater.relate(autowireRule);
                    }
                } else if (method.isAnnotationPresent(Required.class)) {
                    BeanRuleAnalyzer.checkRequiredProperty(beanRule, method);
                } else if (method.isAnnotationPresent(Initialize.class)) {
                    if (!beanRule.isInitializableBean() && beanRule.getInitMethod() == null) {
                        beanRule.setInitMethod(method);
                        beanRule.setInitMethodParameterBindingRules(createParameterBindingRules(method));
                    }
                } else if (method.isAnnotationPresent(Destroy.class)) {
                    if (!beanRule.isDisposableBean() && beanRule.getDestroyMethod() == null) {
                        beanRule.setDestroyMethod(method);
                    }
                }
            }
            beanClass = beanClass.getSuperclass();
        }
    }

    private void parseAspectRule(Class<?> beanClass, String[] nameArray) throws IllegalRuleException {
        Aspect aspectAnno = beanClass.getAnnotation(Aspect.class);
        String aspectId = StringUtils.emptyToNull(aspectAnno.value());
        if (aspectId == null) {
            aspectId = StringUtils.emptyToNull(aspectAnno.id());
        }
        if (aspectId == null) {
            aspectId = beanClass.getName();
        }
        if (nameArray != null) {
            aspectId = applyNamespace(nameArray, aspectId);
        }
        int order = aspectAnno.order();
        boolean isolated = aspectAnno.isolated();

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(aspectId);
        aspectRule.setOrder(order);
        aspectRule.setIsolated(isolated);
        aspectRule.setAdviceBeanClass(beanClass);

        if (beanClass.isAnnotationPresent(Joinpoint.class)) {
            Joinpoint joinpointAnno = beanClass.getAnnotation(Joinpoint.class);
            JoinpointTargetType target = joinpointAnno.target();
            MethodType[] methods = joinpointAnno.methods();
            String[] headers = joinpointAnno.headers();
            String[] pointcut = joinpointAnno.pointcut();

            JoinpointRule joinpointRule = new JoinpointRule();
            joinpointRule.setJoinpointTargetType(target);
            if (methods.length > 0) {
                joinpointRule.setMethods(methods);
            }
            if (headers.length > 0) {
                joinpointRule.setHeaders(headers);
            }
            if (pointcut.length > 0) {
                joinpointRule.setPointcutRule(PointcutRule.newInstance(pointcut));
            }
            aspectRule.setJoinpointRule(joinpointRule);
        }

        if (beanClass.isAnnotationPresent(Settings.class)) {
            Settings settingsAnno = beanClass.getAnnotation(Settings.class);
            String text = StringUtils.toLineDelimitedString(settingsAnno.value());
            if (!text.isEmpty()) {
                SettingsAdviceRule sar = new SettingsAdviceRule(aspectRule);
                SettingsAdviceRule.updateSettingsAdviceRule(sar, text);
                aspectRule.setSettingsAdviceRule(sar);
            }
        }

        for (Method method : beanClass.getMethods()) {
            if (method.isAnnotationPresent(Before.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.BEFORE);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(After.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AFTER);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.newAspectAdviceRule(AspectAdviceType.AROUND);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(ExceptionThrown.class)) {
                ExceptionThrown exceptionThrownAnno = method.getAnnotation(ExceptionThrown.class);
                Class<? extends Throwable>[] types = exceptionThrownAnno.value();
                AnnotatedAction action = createAnnotatedMethodAction(null, beanClass, method);
                ExceptionThrownRule exceptionThrownRule = ExceptionThrownRule.newInstance(types, action);
                aspectRule.putExceptionThrownRule(exceptionThrownRule);
                if (method.isAnnotationPresent(Transform.class)) {
                    Transform transformAnno = method.getAnnotation(Transform.class);
                    TransformRule transformRule = parseTransformRule(transformAnno);
                    exceptionThrownRule.applyResponseRule(transformRule);
                } else if (method.isAnnotationPresent(Dispatch.class)) {
                    Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
                    DispatchRule dispatchRule = parseDispatchRule(dispatchAnno);
                    exceptionThrownRule.applyResponseRule(dispatchRule);
                } else if (method.isAnnotationPresent(Forward.class)) {
                    throw new IllegalRuleException("Cannot apply the forward response rule to the exception thrown rule");
                } else if (method.isAnnotationPresent(Redirect.class)) {
                    Redirect redirectAnno = method.getAnnotation(Redirect.class);
                    RedirectRule redirectRule = parseRedirectRule(redirectAnno);
                    exceptionThrownRule.applyResponseRule(redirectRule);
                }
            }
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        DescriptionRule descriptionRule = parseDescriptionRule(descriptionAnno);
        if (descriptionRule != null) {
            aspectRule.setDescriptionRule(descriptionRule);
        }

        configRelater.relate(aspectRule);
    }

    private void parseBeanRule(BeanRule beanRule, String[] nameArray) throws IllegalRuleException {
        Class<?> beanClass = beanRule.getBeanClass();
        Bean beanAnno = beanClass.getAnnotation(Bean.class);
        String beanId = StringUtils.emptyToNull(beanAnno.value());
        if (beanId == null) {
            beanId = StringUtils.emptyToNull(beanAnno.id());
        }
        if (beanId != null && nameArray != null) {
            beanId = applyNamespace(nameArray, beanId);
        }
        String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
        String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
        boolean lazyInit = beanAnno.lazyInit();
        boolean lazyDestroy = beanAnno.lazyDestroy();
        boolean important = beanAnno.important();

        Scope scopeAnno = beanClass.getAnnotation(Scope.class);
        ScopeType scopeType = (scopeAnno != null ? scopeAnno.value() : ScopeType.SINGLETON);

        beanRule.setId(beanId);
        beanRule.setScopeType(scopeType);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        if (lazyInit) {
            beanRule.setLazyInit(Boolean.TRUE);
        }
        if (lazyDestroy) {
            beanRule.setLazyDestroy(Boolean.TRUE);
        }
        if (important) {
            beanRule.setImportant(Boolean.TRUE);
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        DescriptionRule descriptionRule = parseDescriptionRule(descriptionAnno);
        if (descriptionRule != null) {
            beanRule.setDescriptionRule(descriptionRule);
        }

        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        configRelater.relate(targetBeanClass, beanRule);
    }

    private void parseFactoryBeanRule(Class<?> beanClass, Method method, String[] nameArray)
            throws IllegalRuleException {
        Bean beanAnno = method.getAnnotation(Bean.class);
        String beanId = StringUtils.emptyToNull(beanAnno.value());
        if (beanId == null) {
            beanId = StringUtils.emptyToNull(beanAnno.id());
        }
        if (beanId == null) {
            beanId = method.getName();
        }
        if (nameArray != null) {
            beanId = applyNamespace(nameArray, beanId);
        }
        String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
        String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
        boolean lazyInit = beanAnno.lazyInit();
        boolean lazyDestroy = beanAnno.lazyDestroy();
        boolean important = beanAnno.important();

        Scope scopeAnno = beanClass.getAnnotation(Scope.class);
        ScopeType scopeType = (scopeAnno != null ? scopeAnno.value() : ScopeType.SINGLETON);

        BeanRule beanRule = new BeanRule();
        beanRule.setId(beanId);
        beanRule.setScopeType(scopeType);
        beanRule.setFactoryBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX + beanClass.getName());
        beanRule.setFactoryBeanClass(beanClass);
        beanRule.setFactoryMethodName(method.getName());
        beanRule.setFactoryMethod(method);
        beanRule.setFactoryMethodParameterBindingRules(createParameterBindingRules(method));
        beanRule.setFactoryOffered(true);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        if (lazyInit) {
            beanRule.setLazyInit(Boolean.TRUE);
        }
        if (lazyDestroy) {
            beanRule.setLazyDestroy(Boolean.TRUE);
        }
        if (important) {
            beanRule.setImportant(Boolean.TRUE);
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        DescriptionRule descriptionRule = parseDescriptionRule(descriptionAnno);
        if (descriptionRule != null) {
            beanRule.setDescriptionRule(descriptionRule);
        }

        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        configRelater.relate(targetBeanClass, beanRule);
    }

    private void parseScheduleRule(BeanRule beanRule, String[] nameArray) throws IllegalRuleException {
        Class<?> beanClass = beanRule.getBeanClass();
        Schedule scheduleAnno = beanClass.getAnnotation(Schedule.class);
        String scheduleId = StringUtils.emptyToNull(scheduleAnno.id());
        if (scheduleId != null && nameArray != null) {
            scheduleId = applyNamespace(nameArray, scheduleId);
        }

        ScheduleRule scheduleRule = ScheduleRule.newInstance(scheduleId);

        String schedulerBeanId =  StringUtils.emptyToNull(scheduleAnno.scheduler());
        if (schedulerBeanId != null) {
            scheduleRule.setSchedulerBeanId(schedulerBeanId);
        }

        CronTrigger cronTriggerAnno = scheduleAnno.cronTrigger();
        String expression = StringUtils.emptyToNull(cronTriggerAnno.expression());
        if (expression != null) {
            ScheduleRule.updateTriggerExpression(scheduleRule, cronTriggerAnno);
        } else {
            SimpleTrigger simpleTriggerAnno = scheduleAnno.simpleTrigger();
            ScheduleRule.updateTriggerExpression(scheduleRule, simpleTriggerAnno);
        }

        Job[] jobs = scheduleAnno.jobs();
        for (Job job : jobs) {
            String transletName = StringUtils.emptyToNull(job.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(job.translet());
            }
            ScheduledJobRule jobRule = new ScheduledJobRule(scheduleRule);
            jobRule.setTransletName(transletName);;
            if (job.disabled()) {
                jobRule.setDisabled(true);
            }
            scheduleRule.addScheduledJobRule(jobRule);
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        DescriptionRule descriptionRule = parseDescriptionRule(descriptionAnno);
        if (descriptionRule != null) {
            scheduleRule.setDescriptionRule(descriptionRule);
        }

        configRelater.relate(scheduleRule);
    }

    private void parseTransletRule(Class<?> beanClass, Method method, String[] nameArray)
            throws IllegalRuleException {
        Request requestAnno = method.getAnnotation(Request.class);
        RequestToGet requestToGetAnno = method.getAnnotation(RequestToGet.class);
        RequestToPost requestToPostAnno = method.getAnnotation(RequestToPost.class);
        RequestToPut requestToPutAnno = method.getAnnotation(RequestToPut.class);
        RequestToPatch requestToPatchAnno = method.getAnnotation(RequestToPatch.class);
        RequestToDelete requestToDeleteAnno = method.getAnnotation(RequestToDelete.class);

        String transletName = null;
        MethodType[] allowedMethods = null;
        if (requestAnno != null) {
            transletName = StringUtils.emptyToNull(requestAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestAnno.translet());
            }
            allowedMethods = requestAnno.method();
        } else if (requestToGetAnno != null) {
            transletName = StringUtils.emptyToNull(requestToGetAnno.value());
            allowedMethods = new MethodType[] { MethodType.GET };
        } else if (requestToPostAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPostAnno.value());
            allowedMethods = new MethodType[] { MethodType.POST };
        } else if (requestToPutAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPutAnno.value());
            allowedMethods = new MethodType[] { MethodType.PUT };
        } else if (requestToPatchAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPatchAnno.value());
            allowedMethods = new MethodType[] { MethodType.PATCH };
        } else if (requestToDeleteAnno != null) {
            transletName = StringUtils.emptyToNull(requestToDeleteAnno.value());
            allowedMethods = new MethodType[] { MethodType.DELETE };
        }
        if (nameArray != null) {
            transletName = applyNamespaceForTranslet(nameArray, transletName);
        }

        TransletRule transletRule = TransletRule.newInstance(transletName, allowedMethods);

        ParamItem[] paramItemAnnos = method.getAnnotationsByType(ParamItem.class);
        if (paramItemAnnos.length > 0) {
            ItemRuleMap itemRuleMap = new ItemRuleMap();
            for (ParamItem paramItemAnno : paramItemAnnos) {
                String profile = StringUtils.emptyToNull(paramItemAnno.profile());
                if (profile == null || environmentProfiles.acceptsProfiles(profile)) {
                    itemRuleMap.putItemRule(ItemRuleUtils.toItemRule(paramItemAnno));
                }
            }
            transletRule.touchRequestRule(false).setParameterItemRuleMap(itemRuleMap);
        }

        AttrItem[] attrItemAnnos = method.getAnnotationsByType(AttrItem.class);
        if (attrItemAnnos.length > 0) {
            ItemRuleMap itemRuleMap = new ItemRuleMap();
            for (AttrItem attrItemAnno : attrItemAnnos) {
                String profile = StringUtils.emptyToNull(attrItemAnno.profile());
                if (profile == null || environmentProfiles.acceptsProfiles(profile)) {
                    itemRuleMap.putItemRule(ItemRuleUtils.toItemRule(attrItemAnno));
                }
            }
            transletRule.touchRequestRule(false).setAttributeItemRuleMap(itemRuleMap);
        }

        Action actionAnno = method.getAnnotation(Action.class);
        String actionId = (actionAnno != null ? StringUtils.emptyToNull(actionAnno.value()) : null);
        Executable annotatedAction = createAnnotatedMethodAction(actionId, beanClass, method);
        transletRule.applyActionRule(annotatedAction);

        Class<?> returnType = method.getReturnType();
        if (CustomTransformer.class.isAssignableFrom(returnType)) {
            transletRule.setResponseRule(ResponseRule.newInstance(new CustomTransformResponse()));
        } else {
            if (method.isAnnotationPresent(Transform.class)) {
                Transform transformAnno = method.getAnnotation(Transform.class);
                TransformRule transformRule = parseTransformRule(transformAnno);
                transletRule.setResponseRule(ResponseRule.newInstance(transformRule));
            } else if (method.isAnnotationPresent(Dispatch.class)) {
                Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
                DispatchRule dispatchRule = parseDispatchRule(dispatchAnno);
                transletRule.setResponseRule(ResponseRule.newInstance(dispatchRule));
            } else if (method.isAnnotationPresent(Forward.class)) {
                Forward forwardAnno = method.getAnnotation(Forward.class);
                ForwardRule forwardRule = parseForwardRule(forwardAnno);
                transletRule.setResponseRule(ResponseRule.newInstance(forwardRule));
            } else if (method.isAnnotationPresent(Redirect.class)) {
                Redirect redirectAnno = method.getAnnotation(Redirect.class);
                RedirectRule redirectRule = parseRedirectRule(redirectAnno);
                transletRule.setResponseRule(ResponseRule.newInstance(redirectRule));
            }
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        DescriptionRule descriptionRule = parseDescriptionRule(descriptionAnno);
        if (descriptionRule != null) {
            transletRule.setDescriptionRule(descriptionRule);
        }

        configRelater.relate(transletRule);
    }

    private TransformRule parseTransformRule(Transform transformAnno) {
        FormatType formatType = transformAnno.value();
        if (formatType == FormatType.NONE) {
            formatType = transformAnno.format();
        }
        String contentType = StringUtils.emptyToNull(transformAnno.contentType());
        String templateId = StringUtils.emptyToNull(transformAnno.template());
        String encoding = StringUtils.emptyToNull(transformAnno.encoding());
        boolean pretty = transformAnno.pretty();
        TransformRule transformRule = TransformRule.newInstance(formatType, contentType, encoding, pretty);
        transformRule.setTemplateId(templateId);
        return transformRule;
    }

    private DispatchRule parseDispatchRule(Dispatch dispatchAnno) throws IllegalRuleException {
        String name = StringUtils.emptyToNull(dispatchAnno.value());
        if (name == null) {
            name = StringUtils.emptyToNull(dispatchAnno.name());
        }
        String dispatcher = StringUtils.emptyToNull(dispatchAnno.dispatcher());
        String contentType = StringUtils.emptyToNull(dispatchAnno.contentType());
        String encoding = StringUtils.emptyToNull(dispatchAnno.encoding());
        return DispatchRule.newInstance(name, dispatcher, contentType, encoding);
    }

    private ForwardRule parseForwardRule(Forward forwardAnno) throws IllegalRuleException {
        String translet = StringUtils.emptyToNull(forwardAnno.value());
        if (translet == null) {
            translet = StringUtils.emptyToNull(forwardAnno.translet());
        }
        ForwardRule forwardRule = ForwardRule.newInstance(translet);
        AttrItem[] attrItemAnnos = forwardAnno.attributes();
        if (attrItemAnnos.length > 0) {
            ItemRuleMap itemRuleMap = new ItemRuleMap();
            for (AttrItem attrItemAnno : attrItemAnnos) {
                String profile = StringUtils.emptyToNull(attrItemAnno.profile());
                if (profile == null || environmentProfiles.acceptsProfiles(profile)) {
                    itemRuleMap.putItemRule(ItemRuleUtils.toItemRule(attrItemAnno));
                }
            }
            forwardRule.setAttributeItemRuleMap(itemRuleMap);
        }
        return forwardRule;
    }

    private RedirectRule parseRedirectRule(Redirect redirectAnno) throws IllegalRuleException {
        String path = StringUtils.emptyToNull(redirectAnno.value());
        if (path == null) {
            path = StringUtils.emptyToNull(redirectAnno.path());
        }
        RedirectRule redirectRule = RedirectRule.newInstance(path);
        ParamItem[] paramItemAnnos = redirectAnno.parameters();
        if (paramItemAnnos.length > 0) {
            ItemRuleMap itemRuleMap = new ItemRuleMap();
            for (ParamItem paramItemAnno : paramItemAnnos) {
                String profile = StringUtils.emptyToNull(paramItemAnno.profile());
                if (profile == null || environmentProfiles.acceptsProfiles(profile)) {
                    itemRuleMap.putItemRule(ItemRuleUtils.toItemRule(paramItemAnno));
                }
            }
            redirectRule.setParameterItemRuleMap(itemRuleMap);
        }
        return redirectRule;
    }

    private DescriptionRule parseDescriptionRule(Description descriptionAnno) {
        if (descriptionAnno != null) {
            String description = StringUtils.emptyToNull(descriptionAnno.value());
            if (description != null) {
                DescriptionRule descriptionRule = new DescriptionRule();
                descriptionRule.setContent(description);
                return descriptionRule;
            }
        }
        return null;
    }

    private AutowireRule createAutowireRuleForConstructor(Constructor<?> constructor) throws IllegalRuleException {
        java.lang.reflect.Parameter[] params = constructor.getParameters();
        if (params.length == 0) {
            return null;
        }

        AutowireTargetRule[] autowireTargetRules = AutowireTargetRule.newArrayInstance(params.length);
        for (int i = 0; i < params.length; i++) {
            if (params[i].getType().isArray()) {
                autowireTargetRules[i].setType(params[i].getType().getComponentType());
            } else {
                autowireTargetRules[i].setType(params[i].getType());
            }
            Value ValueAnno = params[i].getAnnotation(Value.class);
            if (ValueAnno != null) {
                String expression = StringUtils.emptyToNull(ValueAnno.value());
                if (expression != null) {
                    autowireTargetRules[i].setExpression(expression);
                }
            } else {
                Qualifier qualifierAnno = params[i].getAnnotation(Qualifier.class);
                if (qualifierAnno != null) {
                    String qualifier = StringUtils.emptyToNull(qualifierAnno.value());
                    if (qualifier != null) {
                        autowireTargetRules[i].setQualifier(qualifier);
                    }
                }
            }
        }

        Autowired autowiredAnno = constructor.getAnnotation(Autowired.class);
        boolean required = (autowiredAnno == null || autowiredAnno.required());

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.CONSTRUCTOR);
        autowireRule.setTarget(constructor);
        autowireRule.setAutowireTargetRules(autowireTargetRules);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AutowireRule createAutowireRuleForField(Field field) throws IllegalRuleException {
        AutowireTargetRule autowireTargetRule = AutowireTargetRule.newInstance();
        autowireTargetRule.setType(field.getType());
        Value ValueAnno = field.getAnnotation(Value.class);
        if (ValueAnno != null) {
            String expression = StringUtils.emptyToNull(ValueAnno.value());
            if (expression != null) {
                autowireTargetRule.setExpression(expression);
            }
        } else {
            Qualifier qualifierAnno = field.getAnnotation(Qualifier.class);
            if (qualifierAnno != null) {
                String qualifier = StringUtils.emptyToNull(qualifierAnno.value());
                if (qualifier != null) {
                    autowireTargetRule.setQualifier(qualifier);
                }
            }
        }

        Autowired autowiredAnno = field.getAnnotation(Autowired.class);
        boolean required = (autowiredAnno == null || autowiredAnno.required());

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.FIELD);
        autowireRule.setTarget(field);
        autowireRule.setAutowireTargetRules(autowireTargetRule);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AutowireRule createAutowireRuleForFieldValue(Field field) throws IllegalRuleException {
        Value valueAnno = field.getAnnotation(Value.class);
        if (valueAnno != null) {
            String expression = StringUtils.emptyToNull(valueAnno.value());
            if (expression != null) {
                AutowireTargetRule autowireTargetRule = AutowireTargetRule.newInstance();
                autowireTargetRule.setExpression(expression);

                Autowired autowiredAnno = field.getAnnotation(Autowired.class);
                boolean required = (autowiredAnno != null && autowiredAnno.required());

                AutowireRule autowireRule = new AutowireRule();
                autowireRule.setTargetType(AutowireTargetType.FIELD_VALUE);
                autowireRule.setTarget(field);
                autowireRule.setAutowireTargetRules(autowireTargetRule);
                autowireRule.setRequired(required);
                return autowireRule;
            }
        }
        return null;
    }

    private AutowireRule createAutowireRuleForMethod(Method method) throws IllegalRuleException {
        java.lang.reflect.Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return null;
        }

        String upperQualifier = null;
        Qualifier typicalQualifierAnno = method.getAnnotation(Qualifier.class);
        if (typicalQualifierAnno != null) {
            upperQualifier = StringUtils.emptyToNull(typicalQualifierAnno.value());
        }

        AutowireTargetRule[] autowireTargetRules = AutowireTargetRule.newArrayInstance(params.length);
        for (int i = 0; i < params.length; i++) {
            if (params[i].getType().isArray()) {
                autowireTargetRules[i].setType(params[i].getType().getComponentType());
            } else {
                autowireTargetRules[i].setType(params[i].getType());
            }
            Value ValueAnno = params[i].getAnnotation(Value.class);
            if (ValueAnno != null) {
                String expression = StringUtils.emptyToNull(ValueAnno.value());
                if (expression != null) {
                    autowireTargetRules[i].setExpression(expression);
                }
            } else {
                Qualifier qualifierAnno = params[i].getAnnotation(Qualifier.class);
                String qualifier;
                if (qualifierAnno != null) {
                    qualifier = StringUtils.emptyToNull(qualifierAnno.value());
                } else {
                    qualifier = upperQualifier;
                }
                if (qualifier != null) {
                    autowireTargetRules[i].setQualifier(qualifier);
                }
            }
        }

        Autowired autowiredAnno = method.getAnnotation(Autowired.class);
        boolean required = (autowiredAnno == null || autowiredAnno.required());

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.METHOD);
        autowireRule.setTarget(method);
        autowireRule.setAutowireTargetRules(autowireTargetRules);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AnnotatedAction createAnnotatedMethodAction(String actionId, Class<?> beanClass, Method method) {
        AnnotatedActionRule annotatedActionRule = new AnnotatedActionRule();
        annotatedActionRule.setActionId(actionId);
        annotatedActionRule.setBeanClass(beanClass);
        annotatedActionRule.setMethod(method);
        annotatedActionRule.setParameterBindingRules(createParameterBindingRules(method));
        return new AnnotatedAction(annotatedActionRule);
    }

    static ParameterBindingRule[] createParameterBindingRules(Method method) {
        java.lang.reflect.Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return null;
        }
        ParameterBindingRule[] bindingRules = new ParameterBindingRule[params.length];
        int cnt = 0;
        for (java.lang.reflect.Parameter param : params) {
            Qualifier qualifierAnno = param.getAnnotation(Qualifier.class);
            String qualifier = null;
            if (qualifierAnno != null) {
                qualifier = StringUtils.emptyToNull(qualifierAnno.value());
            }
            if (qualifier == null) {
                qualifier = param.getName();
            }
            Format formatAnno = param.getAnnotation(Format.class);
            String format = null;
            if (formatAnno != null) {
                format = StringUtils.emptyToNull(formatAnno.value());
            }
            boolean required = param.isAnnotationPresent(Required.class);

            ParameterBindingRule bindingRule = new ParameterBindingRule();
            bindingRule.setType(param.getType());
            bindingRule.setName(qualifier);
            bindingRule.setFormat(format);
            bindingRule.setRequired(required);
            bindingRules[cnt++] = bindingRule;
        }
        return bindingRules;
    }

    private String[] explodeNamespace(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return null;
        }

        namespace = namespace.replace(ActivityContext.NAME_SEPARATOR_CHAR, ActivityContext.ID_SEPARATOR_CHAR);

        int cnt = StringUtils.search(namespace, ActivityContext.ID_SEPARATOR_CHAR);
        if (cnt == 0) {
            String[] arr = new String[2];
            arr[1] = namespace;
            return arr;
        }

        List<String> list = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(namespace, ActivityContext.ID_SEPARATOR);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        list.add(null);
        Collections.reverse(list);
        return list.toArray(new String[0]);
    }

    private String applyNamespace(String[] nameArray, String name) {
        if (nameArray == null) {
            return name;
        }

        if (StringUtils.startsWith(name, ActivityContext.ID_SEPARATOR_CHAR)) {
            nameArray[0] = name.substring(1);
        } else {
            nameArray[0] = name;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = nameArray.length - 1; i >= 0; i--) {
            sb.append(nameArray[i]);
            if (i > 0) {
                sb.append(ActivityContext.ID_SEPARATOR_CHAR);
            }
        }
        return sb.toString();
    }

    private String applyNamespaceForTranslet(String[] nameArray, String name) {
        if (nameArray == null) {
            return name;
        }

        if (StringUtils.startsWith(name, ActivityContext.NAME_SEPARATOR_CHAR)) {
            nameArray[0] = name.substring(1);
        } else {
            nameArray[0] = name;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = nameArray.length - 1; i >= 0; i--) {
            if (nameArray[i] != null) {
                sb.append(ActivityContext.NAME_SEPARATOR_CHAR);
                sb.append(nameArray[i]);
            }
        }
        return sb.toString();
    }

}
