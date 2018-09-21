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
package com.aspectran.core.component.bean;

import com.aspectran.core.activity.process.action.MethodAction;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.After;
import com.aspectran.core.component.bean.annotation.Around;
import com.aspectran.core.component.bean.annotation.Aspect;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Before;
import com.aspectran.core.component.bean.annotation.Configuration;
import com.aspectran.core.component.bean.annotation.Description;
import com.aspectran.core.component.bean.annotation.Destroy;
import com.aspectran.core.component.bean.annotation.Dispatch;
import com.aspectran.core.component.bean.annotation.ExceptionThrown;
import com.aspectran.core.component.bean.annotation.Forward;
import com.aspectran.core.component.bean.annotation.Initialize;
import com.aspectran.core.component.bean.annotation.Joinpoint;
import com.aspectran.core.component.bean.annotation.Profile;
import com.aspectran.core.component.bean.annotation.Qualifier;
import com.aspectran.core.component.bean.annotation.Redirect;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.component.bean.annotation.RequestAsDelete;
import com.aspectran.core.component.bean.annotation.RequestAsGet;
import com.aspectran.core.component.bean.annotation.RequestAsPatch;
import com.aspectran.core.component.bean.annotation.RequestAsPost;
import com.aspectran.core.component.bean.annotation.RequestAsPut;
import com.aspectran.core.component.bean.annotation.Required;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.component.bean.annotation.Value;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.env.Environment;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.AspectAdviceRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DispatchResponseRule;
import com.aspectran.core.context.rule.ExceptionThrownRule;
import com.aspectran.core.context.rule.ForwardResponseRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.JoinpointRule;
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.RedirectResponseRule;
import com.aspectran.core.context.rule.ResponseRule;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

    private final Log log = LogFactory.getLog(AnnotatedConfigParser.class);

    private final AnnotatedConfigRelater relater;

    private final Environment environment;

    private final Map<String, BeanRule> idBasedBeanRuleMap;

    private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap;

    private final Map<Class<?>, BeanRule> configBeanRuleMap;

    public AnnotatedConfigParser(ContextRuleAssistant assistant, AnnotatedConfigRelater relater) {
        this.environment = assistant.getContextEnvironment();
        this.idBasedBeanRuleMap = assistant.getBeanRuleRegistry().getIdBasedBeanRuleMap();
        this.typeBasedBeanRuleMap = assistant.getBeanRuleRegistry().getTypeBasedBeanRuleMap();
        this.configBeanRuleMap = assistant.getBeanRuleRegistry().getConfigBeanRuleMap();
        this.relater = relater;
    }

    public void parse() throws IllegalRuleException {
        if (log.isDebugEnabled()) {
            log.debug("Now try to parse annotated configurations");
            log.debug("Parsing bean rules for configuring: " + configBeanRuleMap.size());
        }

        for (BeanRule beanRule : configBeanRuleMap.values()) {
            if (log.isTraceEnabled()) {
                log.trace("configBeanRule " + beanRule);
            }
            if (!beanRule.isFactoryOffered()) {
                parseConfigBean(beanRule);
                parseFieldAutowire(beanRule);
                parseMethodAutowire(beanRule);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Parsed ID-based bean rules: " + idBasedBeanRuleMap.size());
        }

        for (BeanRule beanRule : idBasedBeanRuleMap.values()) {
            if (log.isTraceEnabled()) {
                log.trace("idBasedBeanRule " + beanRule);
            }
            if (!beanRule.isFactoryOffered()) {
                parseFieldAutowire(beanRule);
                parseMethodAutowire(beanRule);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Parsed Type-based bean rules: " + typeBasedBeanRuleMap.size());
        }

        for (Set<BeanRule> set : typeBasedBeanRuleMap.values()) {
            for (BeanRule beanRule : set) {
                if (!beanRule.isFactoryOffered()) {
                    if (log.isTraceEnabled()) {
                        log.trace("typeBasedBeanRule " + beanRule);
                    }
                    parseFieldAutowire(beanRule);
                    parseMethodAutowire(beanRule);
                }
            }
        }
    }

    private void parseConfigBean(BeanRule beanRule) throws IllegalRuleException {
        Class<?> beanClass = beanRule.getBeanClass();
        Configuration configAnno = beanClass.getAnnotation(Configuration.class);
        if (configAnno != null) {
            if (beanClass.isAnnotationPresent(Profile.class)) {
                Profile profileAnno = beanClass.getAnnotation(Profile.class);
                if (!environment.acceptsProfiles(profileAnno.value())) {
                    return;
                }
            }
            String[] nameArray = splitNamespace(configAnno.namespace());
            for (Method method : beanClass.getMethods()) {
                if (method.isAnnotationPresent(Profile.class)) {
                    Profile profileAnno = method.getAnnotation(Profile.class);
                    if (!environment.acceptsProfiles(profileAnno.value())) {
                        continue;
                    }
                }
                if (method.isAnnotationPresent(Bean.class)) {
                    parseBeanRule(beanClass, method, nameArray);
                } else if (method.isAnnotationPresent(Request.class) ||
                        method.isAnnotationPresent(RequestAsGet.class) ||
                        method.isAnnotationPresent(RequestAsPost.class) ||
                        method.isAnnotationPresent(RequestAsPut.class) ||
                        method.isAnnotationPresent(RequestAsPatch.class) ||
                        method.isAnnotationPresent(RequestAsDelete.class)) {
                    parseTransletRule(beanClass, method, nameArray);
                }
            }
            if (beanClass.isAnnotationPresent(Aspect.class)) {
                parseAspectRule(beanClass, nameArray);
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
                    Autowired autowiredAnno = field.getAnnotation(Autowired.class);
                    boolean required = autowiredAnno.required();
                    Qualifier qualifierAnno = field.getAnnotation(Qualifier.class);
                    String qualifier = (qualifierAnno != null ? StringUtils.emptyToNull(qualifierAnno.value()) : null);
                    Class<?> type = field.getType();

                    AutowireRule autowireRule = new AutowireRule();
                    autowireRule.setTargetType(AutowireTargetType.FIELD);
                    autowireRule.setTarget(field);
                    autowireRule.setTypes(type);
                    autowireRule.setQualifiers(qualifier);
                    autowireRule.setRequired(required);

                    beanRule.addAutowireRule(autowireRule);
                    relater.relay(autowireRule);
                } else if (field.isAnnotationPresent(Value.class)) {
                    Value valueAnno = field.getAnnotation(Value.class);
                    String value = StringUtils.emptyToNull(valueAnno.value());

                    if (value != null) {
                        Token[] tokens = TokenParser.parse(value);
                        if (tokens != null && tokens.length > 0) {
                            AutowireRule autowireRule = new AutowireRule();
                            autowireRule.setTargetType(AutowireTargetType.FIELD_VALUE);
                            autowireRule.setTarget(field);
                            autowireRule.setToken(tokens[0]);

                            beanRule.addAutowireRule(autowireRule);
                            relater.relay(autowireRule);
                        }
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
                    Autowired autowiredAnno = method.getAnnotation(Autowired.class);
                    boolean required = autowiredAnno.required();
                    Qualifier qualifierAnno = method.getAnnotation(Qualifier.class);
                    String qualifier = (qualifierAnno != null ? StringUtils.emptyToNull(qualifierAnno.value()) : null);

                    Parameter[] params = method.getParameters();
                    Class<?>[] paramTypes = new Class<?>[params.length];
                    String[] paramQualifiers = new String[params.length];
                    for (int i = 0; i < params.length; i++) {
                        Qualifier paramQualifierAnno = params[i].getAnnotation(Qualifier.class);
                        String paramQualifier;
                        if (paramQualifierAnno != null) {
                            paramQualifier = StringUtils.emptyToNull(paramQualifierAnno.value());
                        } else {
                            paramQualifier = qualifier;
                        }

                        paramTypes[i] = params[i].getType();
                        paramQualifiers[i] = paramQualifier;
                    }

                    AutowireRule autowireRule = new AutowireRule();
                    autowireRule.setTargetType(AutowireTargetType.METHOD);
                    autowireRule.setTarget(method);
                    autowireRule.setTypes(paramTypes);
                    autowireRule.setQualifiers(paramQualifiers);
                    autowireRule.setRequired(required);

                    beanRule.addAutowireRule(autowireRule);
                    relater.relay(autowireRule);
                } else if (method.isAnnotationPresent(Required.class)) {
                    BeanRuleAnalyzer.checkRequiredProperty(beanRule, method);
                } else if (method.isAnnotationPresent(Initialize.class)) {
                    if (!beanRule.isInitializableBean() && !beanRule.isInitializableTransletBean() &&
                            beanRule.getInitMethod() == null) {
                        beanRule.setInitMethod(method);
                        beanRule.setInitMethodRequiresTranslet(MethodActionRule.isRequiresTranslet(method));
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

    private void parseBeanRule(Class<?> beanClass, Method method, String[] nameArray) {
        Bean beanAnno = method.getAnnotation(Bean.class);
        String beanId = StringUtils.emptyToNull(beanAnno.id());
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

        Description descriptionAnno = method.getAnnotation(Description.class);
        String description = (descriptionAnno != null ? StringUtils.emptyToNull(descriptionAnno.value()) : null);

        BeanRule beanRule = new BeanRule();
        beanRule.setId(beanId);
        beanRule.setScopeType(scopeType);
        beanRule.setFactoryBeanId(BeanRule.CLASS_DIRECTIVE_PREFIX + beanClass.getName());
        beanRule.setFactoryBeanClass(beanClass);
        beanRule.setFactoryMethodName(method.getName());
        beanRule.setFactoryMethod(method);
        beanRule.setFactoryOffered(true);
        beanRule.setInitMethodName(initMethodName);
        beanRule.setDestroyMethodName(destroyMethodName);
        if (lazyInit) {
            beanRule.setLazyInit(Boolean.TRUE);
        }
        if (important) {
            beanRule.setImportant(Boolean.TRUE);
        }
        beanRule.setDescription(description);

        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        relater.relay(targetBeanClass, beanRule);
    }

    private void parseTransletRule(Class<?> beanClass, Method method, String[] nameArray) throws IllegalRuleException {
        String transletName = null;
        MethodType[] allowedMethods = null;

        Request requestAnno = method.getAnnotation(Request.class);
        RequestAsGet requestAsGetAnno = method.getAnnotation(RequestAsGet.class);
        RequestAsPost requestAsPostAnno = method.getAnnotation(RequestAsPost.class);
        RequestAsPut requestAsPutAnno = method.getAnnotation(RequestAsPut.class);
        RequestAsPatch requestAsPatchAnno = method.getAnnotation(RequestAsPatch.class);
        RequestAsDelete requestAsDeleteAnno = method.getAnnotation(RequestAsDelete.class);

        if (requestAnno != null) {
            transletName = StringUtils.emptyToNull(requestAnno.translet());
            allowedMethods = requestAnno.method();
        } else if (requestAsGetAnno != null) {
            transletName = StringUtils.emptyToNull(requestAsGetAnno.value());
            allowedMethods = new MethodType[] { MethodType.GET };
        } else if (requestAsPostAnno != null) {
            transletName = StringUtils.emptyToNull(requestAsPostAnno.value());
            allowedMethods = new MethodType[] { MethodType.POST };
        } else if (requestAsPutAnno != null) {
            transletName = StringUtils.emptyToNull(requestAsPutAnno.value());
            allowedMethods = new MethodType[] { MethodType.PUT };
        } else if (requestAsPatchAnno != null) {
            transletName = StringUtils.emptyToNull(requestAsPatchAnno.value());
            allowedMethods = new MethodType[] { MethodType.PATCH };
        } else if (requestAsDeleteAnno != null) {
            transletName = StringUtils.emptyToNull(requestAsDeleteAnno.value());
            allowedMethods = new MethodType[] { MethodType.DELETE };
        }

        if (transletName == null) {
            transletName = method.getName();
            transletName = transletName.replace('_', ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
        }
        if (nameArray != null) {
            transletName = applyNamespaceForTranslet(nameArray, transletName);
        }

        Action actionAnno = method.getAnnotation(Action.class);
        String actionId = (actionAnno != null ? StringUtils.emptyToNull(actionAnno.id()) : null);

        Description descriptionAnno = method.getAnnotation(Description.class);
        String description = (descriptionAnno != null ? StringUtils.emptyToNull(descriptionAnno.value()) : null);

        TransletRule transletRule = TransletRule.newInstance(transletName, allowedMethods);
        transletRule.setDescription(description);

        MethodActionRule methodActionRule = new MethodActionRule();
        methodActionRule.setActionId(actionId);
        methodActionRule.setConfigBeanClass(beanClass);
        methodActionRule.setMethod(method);
        transletRule.applyActionRule(methodActionRule);

        if (method.isAnnotationPresent(Dispatch.class)) {
            Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
            DispatchResponseRule drr = parseDispatchResponseRule(dispatchAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(drr));
        } else if (method.isAnnotationPresent(Transform.class)) {
            Transform transformAnno = method.getAnnotation(Transform.class);
            TransformRule tr = parseTransformRule(transformAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(tr));
        } else if (method.isAnnotationPresent(Forward.class)) {
            Forward forwardAnno = method.getAnnotation(Forward.class);
            ForwardResponseRule frr = parseForwardResponseRule(forwardAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(frr));
        } else if (method.isAnnotationPresent(Redirect.class)) {
            Redirect redirectAnno = method.getAnnotation(Redirect.class);
            RedirectResponseRule rrr = parseRedirectResponseRule(redirectAnno);
            transletRule.setResponseRule(ResponseRule.newInstance(rrr));
        }

        relater.relay(transletRule);
    }

    private void parseAspectRule(Class<?> beanClass, String[] nameArray) throws IllegalRuleException {
        Aspect aspectAnno = beanClass.getAnnotation(Aspect.class);
        String aspectId = StringUtils.emptyToNull(aspectAnno.id());
        if (aspectId == null) {
            aspectId = beanClass.getName();
        }
        if (nameArray != null) {
            aspectId = applyNamespace(nameArray, aspectId);
        }
        int order = aspectAnno.order();
        boolean isolated = aspectAnno.isolated();

        Description descriptionAnno = beanClass.getAnnotation(Description.class);
        String description = (descriptionAnno != null ? StringUtils.emptyToNull(descriptionAnno.value()) : null);

        AspectRule aspectRule = new AspectRule();
        aspectRule.setId(aspectId);
        aspectRule.setOrder(order);
        aspectRule.setIsolated(isolated);
        aspectRule.setDescription(description);
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
            joinpointRule.setPointcutRule(PointcutRule.newInstance(pointcut));
            aspectRule.setJoinpointRule(joinpointRule);
        }

        for (Method method : beanClass.getMethods()) {
            if (method.isAnnotationPresent(Before.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.BEFORE);
                aspectAdviceRule.setExecutableAction(MethodActionRule.newMethodAction(beanClass, method));
            } else if (method.isAnnotationPresent(After.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AFTER);
                aspectAdviceRule.setExecutableAction(MethodActionRule.newMethodAction(beanClass, method));
            } else if (method.isAnnotationPresent(Around.class)) {
                AspectAdviceRule aspectAdviceRule = aspectRule.touchAspectAdviceRule(AspectAdviceType.AROUND);
                aspectAdviceRule.setExecutableAction(MethodActionRule.newMethodAction(beanClass, method));
            } else if (method.isAnnotationPresent(ExceptionThrown.class)) {
                ExceptionThrown exceptionThrownAnno = method.getAnnotation(ExceptionThrown.class);
                Class<? extends Throwable>[] types = exceptionThrownAnno.type();
                MethodAction action = MethodActionRule.newMethodAction(beanClass, method);
                ExceptionThrownRule exceptionThrownRule = ExceptionThrownRule.newInstance(types, action);
                aspectRule.putExceptionThrownRule(exceptionThrownRule);
                if (method.isAnnotationPresent(Dispatch.class)) {
                    Dispatch dispatchAnno = method.getAnnotation(Dispatch.class);
                    DispatchResponseRule drr = parseDispatchResponseRule(dispatchAnno);
                    exceptionThrownRule.applyResponseRule(drr);
                } else if (method.isAnnotationPresent(Transform.class)) {
                    Transform transformAnno = method.getAnnotation(Transform.class);
                    TransformRule tr = parseTransformRule(transformAnno);
                    exceptionThrownRule.applyResponseRule(tr);
                } else if (method.isAnnotationPresent(Forward.class)) {
                    Forward forwardAnno = method.getAnnotation(Forward.class);
                    ForwardResponseRule frr = parseForwardResponseRule(forwardAnno);
                    exceptionThrownRule.applyResponseRule(frr);
                } else if (method.isAnnotationPresent(Redirect.class)) {
                    Redirect redirectAnno = method.getAnnotation(Redirect.class);
                    RedirectResponseRule rrr = parseRedirectResponseRule(redirectAnno);
                    exceptionThrownRule.applyResponseRule(rrr);
                }
            }
        }

        relater.relay(aspectRule);
    }

    private DispatchResponseRule parseDispatchResponseRule(Dispatch dispatchAnno) {
        String name = StringUtils.emptyToNull(dispatchAnno.name());
        String dispatcher = StringUtils.emptyToNull(dispatchAnno.dispatcher());
        String contentType = StringUtils.emptyToNull(dispatchAnno.contentType());
        String encoding = StringUtils.emptyToNull(dispatchAnno.encoding());
        return DispatchResponseRule.newInstance(name, dispatcher, contentType, encoding);
    }

    private TransformRule parseTransformRule(Transform transformAnno) throws IllegalRuleException {
        TransformType transformType = transformAnno.type();
        String contentType = StringUtils.emptyToNull(transformAnno.contentType());
        String templateId = StringUtils.emptyToNull(transformAnno.template());
        String encoding = StringUtils.emptyToNull(transformAnno.encoding());
        boolean pretty = transformAnno.pretty();
        TransformRule transformRule = TransformRule.newInstance(transformType, contentType, encoding, null, pretty);
        transformRule.setTemplateId(templateId);
        return transformRule;
    }

    private ForwardResponseRule parseForwardResponseRule(Forward forwardAnno) throws IllegalRuleException {
        String translet = StringUtils.emptyToNull(forwardAnno.translet());
        return ForwardResponseRule.newInstance(translet);
    }

    private RedirectResponseRule parseRedirectResponseRule(Redirect redirectAnno) throws IllegalRuleException {
        String path = StringUtils.emptyToNull(redirectAnno.path());
        return RedirectResponseRule.newInstance(path);
    }

    private String[] splitNamespace(String namespace) {
        if (StringUtils.isEmpty(namespace)) {
            return null;
        }

        namespace = namespace.replace(ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR, ActivityContext.ID_SEPARATOR_CHAR);

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

        if (StringUtils.startsWith(name, ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR)) {
            nameArray[0] = name.substring(1);
        } else {
            nameArray[0] = name;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = nameArray.length - 1; i >= 0; i--) {
            sb.append(ActivityContext.TRANSLET_NAME_SEPARATOR_CHAR);
            sb.append(nameArray[i]);
        }
        return sb.toString();
    }

}
