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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.process.action.AnnotatedMethodAction;
import com.aspectran.core.activity.process.action.Executable;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Around;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Attribute;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.Destroy;
import com.aspectran.core.component.bean.annotation.Dispatch;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Format;
import com.aspectran.core.component.bean.annotation.Forward;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.component.bean.annotation.Parameter;
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
import com.aspectran.core.component.bean.annotation.Settings;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.component.bean.annotation.Value;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.AnnotatedMethodActionRule;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.ParameterMappingRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RedirectRule;
import com.aspectran.core.context.rule.ResponseRule;
import com.aspectran.core.context.rule.SettingsAdviceRule;
import com.aspectran.core.context.rule.TransformRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ContextRuleAssistant;
import com.aspectran.core.context.rule.type.AspectAdviceType;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.JoinpointTargetType;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.core.context.rule.type.TransformType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

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

    private static final Log log = LogFactory.getLog(AnnotatedConfigParser.class);

    private final AnnotatedConfigRelater relater;

    private final Environment environment;

    private final Map<String, BeanRule> idBasedBeanRuleMap;

    private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap;

    private final Map<Class<?>, BeanRule> configuredBeanRuleMap;

    public AnnotatedConfigParser(ContextRuleAssistant assistant, AnnotatedConfigRelater relater) {
        this.environment = assistant.getContextEnvironment();
        this.idBasedBeanRuleMap = assistant.getBeanRuleRegistry().getIdBasedBeanRuleMap();
        this.typeBasedBeanRuleMap = assistant.getBeanRuleRegistry().getTypeBasedBeanRuleMap();
        this.configuredBeanRuleMap = assistant.getBeanRuleRegistry().getConfiguredBeanRuleMap();
        this.relater = relater;
    }

    public void parse() throws IllegalRuleException {
        if (configuredBeanRuleMap.isEmpty() && idBasedBeanRuleMap.isEmpty() && typeBasedBeanRuleMap.isEmpty()) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Now try to parse annotated configurations");
        }

        if (!configuredBeanRuleMap.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing bean rules for configuring: " + configuredBeanRuleMap.size());
            }
            for (BeanRule beanRule : configuredBeanRuleMap.values()) {
                if (log.isTraceEnabled()) {
                    log.trace("configuredBeanRule " + beanRule);
                }
                if (!beanRule.isFactoryOffered()) {
                    parseConstructorAutowire(beanRule);
                    parseConfiguredBean(beanRule);
                    parseFieldAutowire(beanRule);
                    parseMethodAutowire(beanRule);
                }
            }
        }

        if (!idBasedBeanRuleMap.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing ID-based bean rules: " + idBasedBeanRuleMap.size());
            }
            for (BeanRule beanRule : idBasedBeanRuleMap.values()) {
                if (log.isTraceEnabled()) {
                    log.trace("idBasedBeanRule " + beanRule);
                }
                if (!beanRule.isFactoryOffered()) {
                    parseConstructorAutowire(beanRule);
                    parseFieldAutowire(beanRule);
                    parseMethodAutowire(beanRule);
                }
            }
        }

        if (!typeBasedBeanRuleMap.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Parsing Type-based bean rules: " + typeBasedBeanRuleMap.size());
            }
            for (Set<BeanRule> set : typeBasedBeanRuleMap.values()) {
                for (BeanRule beanRule : set) {
                    if (!beanRule.isFactoryOffered()) {
                        if (log.isTraceEnabled()) {
                            log.trace("typeBasedBeanRule " + beanRule);
                        }
                        parseConstructorAutowire(beanRule);
                        parseFieldAutowire(beanRule);
                        parseMethodAutowire(beanRule);
                    }
                }
            }
        }
    }

    private void parseConfiguredBean(BeanRule beanRule) throws IllegalRuleException {
        Class<?> beanClass = beanRule.getBeanClass();
        Component componentAnno = beanClass.getAnnotation(Component.class);
        if (componentAnno != null) {
            if (beanClass.isAnnotationPresent(Profile.class)) {
                Profile profileAnno = beanClass.getAnnotation(Profile.class);
                if (!environment.acceptsProfiles(profileAnno.value())) {
                    return;
                }
            }
            String[] nameArray = splitNamespace(componentAnno.namespace());
            if (beanClass.isAnnotationPresent(Aspect.class)) {
                parseAspectRule(beanClass, nameArray);
            }
            if (beanClass.isAnnotationPresent(Bean.class)) {
                parseBeanRule(beanRule, nameArray);
            }
            for (Method method : beanClass.getMethods()) {
                if (method.isAnnotationPresent(Profile.class)) {
                    Profile profileAnno = method.getAnnotation(Profile.class);
                    if (!environment.acceptsProfiles(profileAnno.value())) {
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

    private void parseConstructorAutowire(BeanRule beanRule) {
        if (beanRule.isConstructorAutowireParsed()) {
            return;
        } else {
            beanRule.setConstructorAutowireParsed(true);
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
                relater.relay(autowireRule);
            }
        }
    }

    private void parseFieldAutowire(BeanRule beanRule) {
        if (beanRule.isFieldAutowireParsed()) {
            return;
        } else {
            beanRule.setFieldAutowireParsed(true);
        }
        Class<?> beanClass = beanRule.getBeanClass();
        while (beanClass != null) {
            for (Field field : beanClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    AutowireRule autowireRule = createAutowireRuleForField(field);
                    beanRule.addAutowireRule(autowireRule);
                    relater.relay(autowireRule);
                } else if (field.isAnnotationPresent(Value.class)) {
                    AutowireRule autowireRule = createAutowireRuleForFieldValue(field);
                    if (autowireRule != null) {
                        beanRule.addAutowireRule(autowireRule);
                        relater.relay(autowireRule);
                    }
                }
            }
            beanClass = beanClass.getSuperclass();
        }
    }

    private void parseMethodAutowire(BeanRule beanRule) {
        if (beanRule.isMethodAutowireParsed()) {
            return;
        } else {
            beanRule.setMethodAutowireParsed(true);
        }
        Class<?> beanClass = beanRule.getBeanClass();
        while (beanClass != null) {
            for (Method method : beanClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    AutowireRule autowireRule = createAutowireRuleForMethod(method);
                    beanRule.addAutowireRule(autowireRule);
                    relater.relay(autowireRule);
                } else if (method.isAnnotationPresent(Required.class)) {
                    BeanRuleAnalyzer.checkRequiredProperty(beanRule, method);
                } else if (method.isAnnotationPresent(Initialize.class)) {
                    if (!beanRule.isInitializableBean() && !beanRule.isInitializableTransletBean() &&
                            beanRule.getInitMethod() == null) {
                        beanRule.setInitMethod(method);
                        beanRule.setInitMethodParameterMappingRules(createParameterMappingRules(method));
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
        String aspectId = StringUtils.emptyToNull(aspectAnno.id());
        if (aspectId == null) {
            aspectId = StringUtils.emptyToNull(aspectAnno.value());
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
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.BEFORE);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(After.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AFTER);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AROUND);
                aspectAdviceRule.setExecutableAction(createAnnotatedMethodAction(null, beanClass, method));
            } else if (method.isAnnotationPresent(ExceptionThrown.class)) {
                ExceptionThrown exceptionThrownAnno = method.getAnnotation(ExceptionThrown.class);
                Class<? extends Throwable>[] types = exceptionThrownAnno.type();
                AnnotatedMethodAction action = createAnnotatedMethodAction(null, beanClass, method);
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
                    Forward forwardAnno = method.getAnnotation(Forward.class);
                    ForwardRule forwardRule = parseForwardRule(forwardAnno);
                    exceptionThrownRule.applyResponseRule(forwardRule);
                } else if (method.isAnnotationPresent(Redirect.class)) {
                    Redirect redirectAnno = method.getAnnotation(Redirect.class);
                    RedirectRule redirectRule = parseRedirectRule(redirectAnno);
                    exceptionThrownRule.applyResponseRule(redirectRule);
                }
            }
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        if (descriptionAnno != null) {
            String description = StringUtils.emptyToNull(descriptionAnno.value());
            aspectRule.setDescription(description);
        }

        relater.relay(aspectRule);
    }

    private void parseBeanRule(BeanRule beanRule, String[] nameArray) {
        Class<?> beanClass = beanRule.getBeanClass();
        Bean beanAnno = beanClass.getAnnotation(Bean.class);
        String beanId = StringUtils.emptyToNull(beanAnno.id());
        if (beanId == null) {
            beanId = StringUtils.emptyToNull(beanAnno.value());
        }
        if (beanId != null && nameArray != null) {
            beanId = applyNamespace(nameArray, beanId);
        }
        ScopeType scopeType = beanAnno.scope();
        String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
        String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
        boolean lazyInit = beanAnno.lazyInit();
        boolean important = beanAnno.important();

        beanRule.setId(beanId);
        beanRule.setScopeType(scopeType);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        if (lazyInit) {
            beanRule.setLazyInit(Boolean.TRUE);
        }
        if (important) {
            beanRule.setImportant(Boolean.TRUE);
        }

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        if (descriptionAnno != null) {
            String description = StringUtils.emptyToNull(descriptionAnno.value());
            beanRule.setDescription(description);
        }

        relater.relay(beanClass, beanRule);
    }

    private void parseFactoryBeanRule(Class<?> beanClass, Method method, String[] nameArray) {
        Bean beanAnno = method.getAnnotation(Bean.class);
        String beanId = StringUtils.emptyToNull(beanAnno.id());
        if (beanId == null) {
            beanId = StringUtils.emptyToNull(beanAnno.value());
        }
        if (beanId == null) {
            beanId = method.getName();
        }
        if (nameArray != null) {
            beanId = applyNamespace(nameArray, beanId);
        }
        ScopeType scopeType = beanAnno.scope();
        String initMethodName = StringUtils.emptyToNull(beanAnno.initMethod());
        String destroyMethodName = StringUtils.emptyToNull(beanAnno.destroyMethod());
        boolean lazyInit = beanAnno.lazyInit();
        boolean important = beanAnno.important();

        BeanRule beanRule = new BeanRule();
        beanRule.setId(beanId);
        beanRule.setScopeType(scopeType);
        beanRule.setFactoryBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX + beanClass.getName());
        beanRule.setFactoryBeanClass(beanClass);
        beanRule.setFactoryMethodName(method.getName());
        beanRule.setFactoryMethod(method);
        beanRule.setFactoryMethodParameterMappingRules(createParameterMappingRules(method));
        beanRule.setFactoryOffered(true);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        if (lazyInit) {
            beanRule.setLazyInit(Boolean.TRUE);
        }
        if (important) {
            beanRule.setImportant(Boolean.TRUE);
        }

        Description descriptionAnno = method.getAnnotation(Description.class);
        if (descriptionAnno != null) {
            String description = StringUtils.emptyToNull(descriptionAnno.value());
            beanRule.setDescription(description);
        }

        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        relater.relay(targetBeanClass, beanRule);
    }

    private void parseTransletRule(Class<?> beanClass, Method method, String[] nameArray) throws IllegalRuleException {
        Request requestAnno = method.getAnnotation(Request.class);
        RequestToGet requestToGetAnno = method.getAnnotation(RequestToGet.class);
        RequestToPost requestToPostAnno = method.getAnnotation(RequestToPost.class);
        RequestToPut requestToPutAnno = method.getAnnotation(RequestToPut.class);
        RequestToPatch requestToPatchAnno = method.getAnnotation(RequestToPatch.class);
        RequestToDelete requestToDeleteAnno = method.getAnnotation(RequestToDelete.class);

        String transletName = null;
        MethodType[] allowedMethods = null;
        Parameter[] parameters = null;
        Attribute[] attributes = null;
        if (requestAnno != null) {
            transletName = StringUtils.emptyToNull(requestAnno.translet());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestAnno.value());
            }
            allowedMethods = requestAnno.method();
            parameters = requestAnno.parameters();
            attributes = requestAnno.attributes();
        } else if (requestToGetAnno != null) {
            transletName = StringUtils.emptyToNull(requestToGetAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestToGetAnno.value());
            }
            allowedMethods = new MethodType[] { MethodType.GET };
            parameters = requestToGetAnno.parameters();
            attributes = requestToGetAnno.attributes();
        } else if (requestToPostAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPostAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestToPostAnno.value());
            }
            allowedMethods = new MethodType[] { MethodType.POST };
            parameters = requestToPostAnno.parameters();
            attributes = requestToPostAnno.attributes();
        } else if (requestToPutAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPutAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestToPutAnno.value());
            }
            allowedMethods = new MethodType[] { MethodType.PUT };
            parameters = requestToPutAnno.parameters();
            attributes = requestToPutAnno.attributes();
        } else if (requestToPatchAnno != null) {
            transletName = StringUtils.emptyToNull(requestToPatchAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestToPatchAnno.value());
            }
            allowedMethods = new MethodType[] { MethodType.PATCH };
            parameters = requestToPatchAnno.parameters();
            attributes = requestToPatchAnno.attributes();
        } else if (requestToDeleteAnno != null) {
            transletName = StringUtils.emptyToNull(requestToDeleteAnno.value());
            if (transletName == null) {
                transletName = StringUtils.emptyToNull(requestToDeleteAnno.value());
            }
            allowedMethods = new MethodType[] { MethodType.DELETE };
            parameters = requestToDeleteAnno.parameters();
            attributes = requestToDeleteAnno.attributes();
        }
        if (transletName == null) {
            transletName = method.getName();
            transletName = transletName.replace('_', ActivityContext.NAME_SEPARATOR_CHAR);
        }
        if (nameArray != null) {
            transletName = applyNamespaceForTranslet(nameArray, transletName);
        }

        TransletRule transletRule = TransletRule.newInstance(transletName, allowedMethods);

        if (parameters != null) {
            transletRule.touchRequestRule(false).setParameterItemRuleMap(ItemRule.toItemRuleMap(parameters));
        }
        if (attributes != null) {
            transletRule.touchRequestRule(false).setAttributeItemRuleMap(ItemRule.toItemRuleMap(attributes));
        }

        Action actionAnno = method.getAnnotation(Action.class);
        String actionId = (actionAnno != null ? StringUtils.emptyToNull(actionAnno.id()) : null);

        Executable annotatedMethodAction = createAnnotatedMethodAction(actionId, beanClass, method);
        transletRule.applyActionRule(annotatedMethodAction);

        if (method.isAnnotationPresent(Dispatch.class)) {
            Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
            DispatchRule dispatchRule = parseDispatchRule(dispatchAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(dispatchRule));
        } else if (method.isAnnotationPresent(Transform.class)) {
            Transform transformAnno = method.getAnnotation(Transform.class);
            TransformRule transformRule = parseTransformRule(transformAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(transformRule));
        } else if (method.isAnnotationPresent(Forward.class)) {
            Forward forwardAnno = method.getAnnotation(Forward.class);
            ForwardRule forwardRule = parseForwardRule(forwardAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(forwardRule));
        } else if (method.isAnnotationPresent(Redirect.class)) {
            Redirect redirectAnno = method.getAnnotation(Redirect.class);
            RedirectRule redirectRule = parseRedirectRule(redirectAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(redirectRule));
        }

        Description descriptionAnno = method.getAnnotation(Description.class);
        if (descriptionAnno != null) {
            String description = StringUtils.emptyToNull(descriptionAnno.value());
            transletRule.setDescription(description);
        }

        relater.relay(transletRule);
    }

    private TransformRule parseTransformRule(Transform transformAnno) {
        TransformType transformType = transformAnno.type();
        String contentType = StringUtils.emptyToNull(transformAnno.contentType());
        String templateId = StringUtils.emptyToNull(transformAnno.template());
        String encoding = StringUtils.emptyToNull(transformAnno.encoding());
        boolean pretty = transformAnno.pretty();
        TransformRule transformRule = TransformRule.newInstance(transformType, contentType, encoding, null, pretty);
        transformRule.setTemplateId(templateId);
        return transformRule;
    }

    private DispatchRule parseDispatchRule(Dispatch dispatchAnno) {
        String name = StringUtils.emptyToNull(dispatchAnno.name());
        if (name == null) {
            name = StringUtils.emptyToNull(dispatchAnno.value());
        }
        String dispatcher = StringUtils.emptyToNull(dispatchAnno.dispatcher());
        String contentType = StringUtils.emptyToNull(dispatchAnno.contentType());
        String encoding = StringUtils.emptyToNull(dispatchAnno.encoding());
        return DispatchRule.newInstance(name, dispatcher, contentType, encoding);
    }

    private ForwardRule parseForwardRule(Forward forwardAnno) throws IllegalRuleException {
        String translet = StringUtils.emptyToNull(forwardAnno.translet());
        if (translet == null) {
            translet = StringUtils.emptyToNull(forwardAnno.value());
        }
        ForwardRule forwardRule = ForwardRule.newInstance(translet);
        Attribute[] attributes = forwardAnno.attributes();
        if (attributes.length > 0) {
            forwardRule.setAttributeItemRuleMap(ItemRule.toItemRuleMap(attributes));
        }
        return forwardRule;
    }

    private RedirectRule parseRedirectRule(Redirect redirectAnno) throws IllegalRuleException {
        String path = StringUtils.emptyToNull(redirectAnno.path());
        if (path == null) {
            path = StringUtils.emptyToNull(redirectAnno.value());
        }
        RedirectRule redirectRule = RedirectRule.newInstance(path);
        Parameter[] parameters = redirectAnno.parameters();
        if (parameters.length > 0) {
            redirectRule.setParameterItemRuleMap(ItemRule.toItemRuleMap(parameters));
        }
        return redirectRule;
    }

    private AutowireRule createAutowireRuleForConstructor(Constructor<?> candidate) {
        java.lang.reflect.Parameter[] params = candidate.getParameters();
        if (params.length == 0) {
            return null;
        }
        Class<?>[] paramTypes = new Class<?>[params.length];
        String[] paramQualifiers = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            String paramQualifier = null;
            Qualifier paramQualifierAnno = params[i].getAnnotation(Qualifier.class);
            if (paramQualifierAnno != null) {
                paramQualifier = StringUtils.emptyToNull(paramQualifierAnno.value());
            }
            paramTypes[i] = params[i].getType();
            paramQualifiers[i] = paramQualifier;
        }

        boolean required = false;
        Autowired autowiredAnno = candidate.getAnnotation(Autowired.class);
        if (autowiredAnno != null) {
            required = autowiredAnno.required();
        }

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.CONSTRUCTOR);
        autowireRule.setTarget(candidate);
        autowireRule.setTypes(paramTypes);
        autowireRule.setQualifiers(paramQualifiers);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AutowireRule createAutowireRuleForField(Field field) {
        Autowired autowiredAnno = field.getAnnotation(Autowired.class);
        boolean required = (autowiredAnno == null || autowiredAnno.required());
        Qualifier qualifierAnno = field.getAnnotation(Qualifier.class);
        String qualifier = (qualifierAnno != null ? StringUtils.emptyToNull(qualifierAnno.value()) : null);
        Class<?> type = field.getType();

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.FIELD);
        autowireRule.setTarget(field);
        autowireRule.setTypes(type);
        autowireRule.setQualifiers(qualifier);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AutowireRule createAutowireRuleForFieldValue(Field field) {
        AutowireRule autowireRule = null;
        Value valueAnno = field.getAnnotation(Value.class);
        if (valueAnno != null) {
            String value = StringUtils.emptyToNull(valueAnno.value());
            if (value != null) {
                Token[] tokens = TokenParser.parse(value);
                if (tokens != null && tokens.length > 0) {
                    autowireRule = new AutowireRule();
                    autowireRule.setTargetType(AutowireTargetType.FIELD_VALUE);
                    autowireRule.setTarget(field);
                    autowireRule.setToken(tokens[0]);
                }
            }
        }
        return autowireRule;
    }

    private AutowireRule createAutowireRuleForMethod(Method method) {
        Autowired autowiredAnno = method.getAnnotation(Autowired.class);
        boolean required = (autowiredAnno == null || autowiredAnno.required());
        Qualifier typicalQualifierAnno = method.getAnnotation(Qualifier.class);
        String typicalQualifier = (typicalQualifierAnno != null ? typicalQualifierAnno.value() : null);

        java.lang.reflect.Parameter[] params = method.getParameters();
        Class<?>[] paramTypes = new Class<?>[params.length];
        String[] paramQualifiers = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getType();
            Qualifier qualifierAnno = params[i].getAnnotation(Qualifier.class);
            if (qualifierAnno != null) {
                paramQualifiers[i] = StringUtils.emptyToNull(qualifierAnno.value());
            } else {
                paramQualifiers[i] = typicalQualifier;
            }
        }

        AutowireRule autowireRule = new AutowireRule();
        autowireRule.setTargetType(AutowireTargetType.METHOD);
        autowireRule.setTarget(method);
        autowireRule.setTypes(paramTypes);
        autowireRule.setQualifiers(paramQualifiers);
        autowireRule.setRequired(required);
        return autowireRule;
    }

    private AnnotatedMethodAction createAnnotatedMethodAction(String actionId, Class<?> beanClass, Method method) {
        AnnotatedMethodActionRule annotatedMethodActionRule = new AnnotatedMethodActionRule();
        annotatedMethodActionRule.setActionId(actionId);
        annotatedMethodActionRule.setBeanClass(beanClass);
        annotatedMethodActionRule.setMethod(method);
        annotatedMethodActionRule.setParameterMappingRules(createParameterMappingRules(method));
        return new AnnotatedMethodAction(annotatedMethodActionRule);
    }

    static ParameterMappingRule[] createParameterMappingRules(Method method) {
        java.lang.reflect.Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return null;
        }
        ParameterMappingRule[] mappingRules = new ParameterMappingRule[params.length];
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

            ParameterMappingRule mappingRule = new ParameterMappingRule();
            mappingRule.setType(param.getType());
            mappingRule.setName(qualifier);
            mappingRule.setFormat(format);
            mappingRule.setRequired(required);
            mappingRules[cnt++] = mappingRule;
        }
        return mappingRules;
    }

    private String[] splitNamespace(String namespace) {
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

        StringTokenizer st = new StringTokenizer(namespace, ActivityContext.ID_SEPARATOR);
        List<String> list = new ArrayList<>();
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
            sb.append(ActivityContext.NAME_SEPARATOR_CHAR);
            sb.append(nameArray[i]);
        }
        return sb.toString();
    }

}
