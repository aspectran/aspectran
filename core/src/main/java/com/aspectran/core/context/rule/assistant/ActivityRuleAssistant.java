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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.value.ValueExpression;
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.AutowireTargetRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.ItemRuleUtils;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.rule.util.Namespace;
import com.aspectran.core.context.rule.util.TextStyler;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class ActivityRuleAssistant.
 *
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ActivityRuleAssistant {

    private final ClassLoader classLoader;

    private final ApplicationAdapter applicationAdapter;

    private final EnvironmentProfiles environmentProfiles;

    private Map<DefaultSettingType, String> settings;

    private List<EnvironmentRule> environmentRules;

    private Map<String, String> typeAliases;

    private AspectRuleRegistry aspectRuleRegistry;

    private BeanRuleRegistry beanRuleRegistry;

    private ScheduleRuleRegistry scheduleRuleRegistry;

    private TransletRuleRegistry transletRuleRegistry;

    private TemplateRuleRegistry templateRuleRegistry;

    private BeanReferenceInspector beanReferenceInspector;

    private AssistantLocal assistantLocal;

    private RuleAppendHandler ruleAppendHandler;

    protected ActivityRuleAssistant() {
        this.classLoader = null;
        this.applicationAdapter = null;
        this.environmentProfiles = null;
    }

    public ActivityRuleAssistant(ClassLoader classLoader,
                                 ApplicationAdapter applicationAdapter,
                                 EnvironmentProfiles environmentProfiles) {
        this.classLoader = classLoader;
        this.applicationAdapter = applicationAdapter;
        this.environmentProfiles = environmentProfiles;
    }

    public void prepare() {
        settings = new HashMap<>();
        environmentRules = new LinkedList<>();
        typeAliases = new HashMap<>();
        assistantLocal = new AssistantLocal(this);

        if (applicationAdapter != null) {
            aspectRuleRegistry = new AspectRuleRegistry();

            beanRuleRegistry = new BeanRuleRegistry(classLoader);

            transletRuleRegistry = new TransletRuleRegistry(getBasePath(), classLoader);
            transletRuleRegistry.setAssistantLocal(assistantLocal);

            scheduleRuleRegistry = new ScheduleRuleRegistry();
            scheduleRuleRegistry.setAssistantLocal(assistantLocal);

            templateRuleRegistry = new TemplateRuleRegistry();
            templateRuleRegistry.setAssistantLocal(assistantLocal);

            beanReferenceInspector = new BeanReferenceInspector();
        }
    }

    public void release() {
        settings = null;
        environmentRules = null;
        typeAliases = null;
        assistantLocal = null;

        if (applicationAdapter != null) {
            scheduleRuleRegistry.setAssistantLocal(null);
            transletRuleRegistry.setAssistantLocal(null);
            templateRuleRegistry.setAssistantLocal(null);

            aspectRuleRegistry = null;
            beanRuleRegistry = null;
            scheduleRuleRegistry = null;
            transletRuleRegistry = null;
            templateRuleRegistry = null;

            beanReferenceInspector = null;
        }
    }

    public ClassLoader getClassLoader() {
        Assert.notNull(classLoader, "ClassLoader is not set");
        return classLoader;
    }

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    public String getBasePath() {
        if (applicationAdapter != null) {
            return applicationAdapter.getBasePath();
        } else {
            return null;
        }
    }

    public EnvironmentProfiles getEnvironmentProfiles() {
        return environmentProfiles;
    }

    /**
     * Gets the settings.
     * @return the settings
     */
    public Map<DefaultSettingType, String> getSettings() {
        return settings;
    }

    /**
     * Gets the setting value.
     * @param settingType the setting type
     * @return the setting
     */
    public Object getSetting(DefaultSettingType settingType) {
        return settings.get(settingType);
    }

    /**
     * Puts the setting value.
     * @param name the name
     * @param value the value
     * @throws IllegalRuleException if an unknown setting name is found
     */
    public void putSetting(String name, String value) throws IllegalRuleException {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalRuleException("Default setting name must not be null or empty");
        }
        DefaultSettingType settingType = DefaultSettingType.resolve(name);
        if (settingType == null) {
            throw new IllegalRuleException("No such default setting name as '" + name + "'");
        }
        settings.put(settingType, value);
    }

    /**
     * Apply settings.
     */
    public void applySettings() {
        DefaultSettings defaultSettings = assistantLocal.touchDefaultSettings();
        defaultSettings.apply(getSettings());
    }

    /**
     * Gets the environment rules.
     * @return the environment rules
     */
    public List<EnvironmentRule> getEnvironmentRules() {
        return environmentRules;
    }

    /**
     * Adds the environment rule.
     * @param environmentRule the environment rule
     */
    public void addEnvironmentRule(EnvironmentRule environmentRule) {
        environmentRules.add(environmentRule);
    }

    /**
     * Gets the type aliases.
     * @return the type aliases
     */
    public Map<String, String> getTypeAliases() {
        return typeAliases;
    }

    /**
     * Adds a type alias to use for simplifying complex type signatures.
     * A type alias is defined by assigning the type to the alias.
     * @param alias the name of the alias
     * @param type the type identifier that you are creating an alias for
     */
    public void addTypeAlias(String alias, String type) {
        typeAliases.put(alias, type);
    }

    /**
     * Returns a type of aliased type that is defined by assigning the type to the alias.
     * @param alias the name of the alias
     * @return the aliased type
     */
    public String getAliasedType(String alias) {
        return typeAliases.get(alias);
    }

    /**
     * Returns a type of aliased type that is defined by assigning the type to the alias.
     * If aliased type is not found, it returns alias.
     * @param alias the name of the alias
     * @return the aliased type
     */
    public String resolveAliasType(String alias) {
        String type = getAliasedType(alias);
        return (type == null ? alias: type);
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     * @param transletName the translet name
     * @return the string
     */
    public String applyTransletNamePattern(String transletName) {
        if (transletName == null) {
            return null;
        }
        return Namespace.applyTransletNamePattern(
            assistantLocal.getDefaultSettings(), transletName, true);
    }

    /**
     * Gets the assistant local.
     * @return the assistant local
     */
    public AssistantLocal getAssistantLocal() {
        return assistantLocal;
    }

    /**
     * Sets the assistant local.
     * @param newAssistantLocal the new assistant local
     */
    private void setAssistantLocal(AssistantLocal newAssistantLocal) {
        this.assistantLocal = newAssistantLocal;
        scheduleRuleRegistry.setAssistantLocal(newAssistantLocal);
        transletRuleRegistry.setAssistantLocal(newAssistantLocal);
        templateRuleRegistry.setAssistantLocal(newAssistantLocal);
    }

    /**
     * Backup the assistant local.
     * @return the assistant local
     */
    public AssistantLocal backupAssistantLocal() {
        AssistantLocal oldAssistantLocal = assistantLocal;
        AssistantLocal newAssistantLocal = assistantLocal.replicate();
        setAssistantLocal(newAssistantLocal);
        return oldAssistantLocal;
    }

    /**
     * Restore the assistant local.
     * @param oldAssistantLocal the old assistant local
     */
    public void restoreAssistantLocal(AssistantLocal oldAssistantLocal) {
        setAssistantLocal(oldAssistantLocal);
    }

    /**
     * Returns whether the pointcut pattern validation is required.
     * @return true if pointcut pattern validation is required
     */
    public boolean isPointcutPatternVerifiable() {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        return (defaultSettings != null && defaultSettings.isPointcutPatternVerifiable());
    }

    public void resolveBeanClass(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule != null && !beanRule.isFactoryOffered() && beanRule.getClassName() != null) {
            Class<?> beanClass = loadClass(beanRule.getClassName(), beanRule);
            beanRule.setBeanClass(beanClass);
        }
    }

    /**
     * Resolve bean class for factory bean rule.
     * @param beanRule the bean rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveFactoryBeanClass(BeanRule beanRule) throws IllegalRuleException {
        if (beanRule != null && beanRule.isFactoryOffered() && beanRule.getFactoryBeanId() != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanRule.getFactoryBeanId(), beanRule);
            if (beanClass != null) {
                beanRule.setFactoryBeanClass(beanClass);
                reserveBeanReference(beanClass, beanRule);
            } else {
                reserveBeanReference(beanRule.getFactoryBeanId(), beanRule);
            }
        }
    }

    /**
     * Resolve bean class for the aspect rule.
     * @param aspectRule the aspect rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveAdviceBeanClass(@NonNull AspectRule aspectRule) throws IllegalRuleException {
        String beanIdOrClass = aspectRule.getAdviceBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanIdOrClass, aspectRule);
            if (beanClass != null) {
                aspectRule.setAdviceBeanClass(beanClass);
                reserveBeanReference(beanClass, aspectRule);
            } else {
                reserveBeanReference(beanIdOrClass, aspectRule);
            }
        }
    }

    /**
     * Resolve bean class for bean method action rule.
     * @param invokeActionRule the invoke action rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveActionBeanClass(@NonNull InvokeActionRule invokeActionRule) throws IllegalRuleException {
        String beanIdOrClass = invokeActionRule.getBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveDirectiveBeanClass(beanIdOrClass, invokeActionRule);
            if (beanClass != null) {
                invokeActionRule.setBeanClass(beanClass);
                reserveBeanReference(beanClass, invokeActionRule);
            } else {
                reserveBeanReference(beanIdOrClass, invokeActionRule);
            }
        }
    }

    /**
     * Resolve bean class.
     * @param itemRule the item rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(@Nullable ItemRule itemRule) throws IllegalRuleException {
        if (itemRule != null) {
            if (itemRule.getValueType() == ItemValueType.BEAN) {
                if (itemRule.isListableType()) {
                    if (itemRule.getBeanRuleList() != null) {
                        for (BeanRule beanRule : itemRule.getBeanRuleList()) {
                            resolveBeanClass(beanRule);
                        }
                    }
                } else if (itemRule.isMappableType()) {
                    if (itemRule.getBeanRuleMap() != null) {
                        for (BeanRule beanRule : itemRule.getBeanRuleMap().values()) {
                            resolveBeanClass(beanRule);
                        }
                    }
                } else {
                    resolveBeanClass(itemRule.getBeanRule());
                }
            } else {
                Iterator<Token[]> it = ItemRuleUtils.tokenIterator(itemRule);
                if (it != null) {
                    while (it.hasNext()) {
                        Token[] tokens = it.next();
                        if (tokens != null) {
                            for (Token token : tokens) {
                                resolveBeanClass(token);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolve bean class for token.
     * @param tokens an array of tokens
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(@Nullable Token[] tokens) throws IllegalRuleException {
        if (tokens != null) {
            for (Token token : tokens) {
                resolveBeanClass(token);
            }
        }
    }

    /**
     * Resolve bean class for token.
     * @param token the token
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(Token token) throws IllegalRuleException {
        resolveBeanClass(token, token);
    }

    private void resolveBeanClass(@Nullable Token token, @Nullable BeanReferenceable referenceable)
            throws IllegalRuleException {
        if (token != null && token.getType() == TokenType.BEAN) {
            if (token.getDirectiveType() == TokenDirectiveType.FIELD) {
                if (token.getGetterName() == null) {
                    throw new IllegalRuleException("Token with no target field name specified: " + token);
                }
                Class<?> beanClass = loadClass(token.getValue(), token);
                try {
                    Field field = beanClass.getField(token.getGetterName());
                    token.setAlternativeValue(field);
                    if (!Modifier.isStatic(field.getModifiers())) {
                        reserveBeanReference(beanClass, referenceable);
                    }
                } catch (NoSuchFieldException e) {
                    throw new IllegalRuleException("Could not access field: " + token.getGetterName() +
                            " on " + ruleAppendHandler.getCurrentRuleAppender().getQualifiedName() + " " + token, e);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.METHOD) {
                if (token.getGetterName() == null) {
                    throw new IllegalRuleException("Token with no target method name specified: " + token);
                }
                Class<?> beanClass = loadClass(token.getValue(), token);
                try {
                    Method method = beanClass.getMethod(token.getGetterName());
                    token.setAlternativeValue(method);
                    if (!Modifier.isStatic(method.getModifiers())) {
                        reserveBeanReference(beanClass, referenceable);
                    }
                } catch (NoSuchMethodException e) {
                    throw new IllegalRuleException("Could not access method: " + token.getGetterName() +
                            " on " + ruleAppendHandler.getCurrentRuleAppender().getQualifiedName() + " " + token, e);
                }
            } else if (token.getDirectiveType() == TokenDirectiveType.CLASS) {
                Class<?> beanClass = loadClass(token.getValue(), token);
                token.setAlternativeValue(beanClass);
                reserveBeanReference(beanClass, referenceable);
            } else {
                reserveBeanReference(token.getName(), referenceable);
            }
        }
    }

    private void resolveBeanClass(Token[] tokens, BeanReferenceable referenceable) throws IllegalRuleException {
        if (tokens != null) {
            for (Token token : tokens) {
                resolveBeanClass(token, referenceable);
            }
        }
    }

    /**
     * Resolve bean class for the autowire rule.
     * @param autowireRule the autowire rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(@Nullable AutowireRule autowireRule) throws IllegalRuleException {
        if (autowireRule != null) {
            if (autowireRule.getTargetType() == AutowireTargetType.FIELD) {
                AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                if (autowireRule.isRequired() && autowireTargetRule != null && !autowireTargetRule.isInnerBean()) {
                    ValueExpression valueExpression = autowireTargetRule.getValueExpression();
                    if (valueExpression != null) {
                        Token[] tokens = valueExpression.getTokens();
                        resolveBeanClass(tokens, autowireRule);
                    } else {
                        Class<?> type = autowireTargetRule.getType();
                        String qualifier = autowireTargetRule.getQualifier();
                        reserveBeanReference(qualifier, type, autowireRule);
                    }
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                if (autowireRule.isRequired() && autowireTargetRule != null && !autowireTargetRule.isInnerBean()) {
                    ValueExpression valueExpression = autowireTargetRule.getValueExpression();
                    if (valueExpression != null) {
                        Token[] tokens = valueExpression.getTokens();
                        resolveBeanClass(tokens, autowireRule);
                    }
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD ||
                autowireRule.getTargetType() == AutowireTargetType.CONSTRUCTOR) {
                AutowireTargetRule[] autowireTargetRules = autowireRule.getAutowireTargetRules();
                if (autowireRule.isRequired() && autowireTargetRules != null) {
                    for (AutowireTargetRule autowireTargetRule : autowireTargetRules) {
                        if (!autowireTargetRule.isInnerBean()) {
                            ValueExpression valueExpression = autowireTargetRule.getValueExpression();
                            if (valueExpression != null) {
                                Token[] tokens = valueExpression.getTokens();
                                resolveBeanClass(tokens, autowireRule);
                            } else {
                                Class<?> type = autowireTargetRule.getType();
                                String qualifier = autowireTargetRule.getQualifier();
                                reserveBeanReference(qualifier, type, autowireRule);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolve bean class for the schedule rule.
     * @param scheduleRule the schedule rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(ScheduleRule scheduleRule) throws IllegalRuleException {
        if (scheduleRule != null) {
            String beanId = scheduleRule.getSchedulerBeanId();
            if (beanId != null) {
                Class<?> beanClass = resolveDirectiveBeanClass(beanId, scheduleRule);
                if (beanClass != null) {
                    scheduleRule.setSchedulerBeanClass(beanClass);
                    reserveBeanReference(beanClass, scheduleRule);
                } else {
                    reserveBeanReference(beanId, scheduleRule);
                }
            }
        }
    }

    /**
     * Resolve bean class for the template rule.
     * @param templateRule the template rule
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void resolveBeanClass(TemplateRule templateRule) throws IllegalRuleException {
        if (templateRule != null) {
            String beanId = templateRule.getEngineBeanId();
            if (beanId != null) {
                Class<?> beanClass = resolveDirectiveBeanClass(beanId, templateRule);
                if (beanClass != null) {
                    templateRule.setEngineBeanClass(beanClass);
                    reserveBeanReference(beanClass, templateRule);
                } else {
                    reserveBeanReference(beanId, templateRule);
                }
            } else {
                resolveBeanClass(templateRule.getTemplateTokens());
            }
        }
    }

    private Class<?> resolveDirectiveBeanClass(String beanIdOrClass, Object referer) throws IllegalRuleException {
        if (beanIdOrClass != null && beanIdOrClass.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = beanIdOrClass.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            return loadClass(className, referer);
        } else {
            return null;
        }
    }

    private Class<?> loadClass(String className, Object referer) throws IllegalRuleException {
        try {
            return getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalRuleException("Unable to load class: " + className +
                    " on " + ruleAppendHandler.getCurrentRuleAppender().getQualifiedName() + " " + referer, e);
        }
    }

    public void reserveBeanReference(String beanId, BeanReferenceable referenceable) {
        reserveBeanReference(beanId, null, referenceable);
    }

    public void reserveBeanReference(Class<?> beanClass, BeanReferenceable referenceable) {
        reserveBeanReference(null, beanClass, referenceable);
    }

    public void reserveBeanReference(String beanId, Class<?> beanClass, BeanReferenceable referenceable) {
        beanReferenceInspector.reserve(beanId, beanClass, referenceable, ruleAppendHandler.getCurrentRuleAppender());
    }

    /**
     * Returns the bean reference inspector.
     * @return the bean reference inspector
     */
    public BeanReferenceInspector getBeanReferenceInspector() {
        return beanReferenceInspector;
    }

    /**
     * Adds the aspect rule.
     * @param aspectRule the aspect rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addAspectRule(AspectRule aspectRule) throws IllegalRuleException {
        aspectRuleRegistry.addAspectRule(aspectRule);
    }

    /**
     * Adds the bean rule.
     * @param beanRule the bean rule to add
     * @throws IllegalRuleException if an error occurs while adding a bean rule
     */
    public void addBeanRule(BeanRule beanRule) throws IllegalRuleException {
        beanRuleRegistry.addBeanRule(beanRule);
    }

    public void addInnerBeanRule(BeanRule beanRule) throws IllegalRuleException {
        beanRuleRegistry.addInnerBeanRule(beanRule);
    }

    /**
     * Adds the schedule rule.
     * @param scheduleRule the schedule rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addScheduleRule(ScheduleRule scheduleRule) throws IllegalRuleException {
        scheduleRuleRegistry.addScheduleRule(scheduleRule);
    }

    /**
     * Add the translet rule.
     * @param transletRule the translet rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addTransletRule(TransletRule transletRule) throws IllegalRuleException {
        transletRuleRegistry.addTransletRule(transletRule);
    }

    /**
     * Add the template rule.
     * @param templateRule the template rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addTemplateRule(TemplateRule templateRule) throws IllegalRuleException {
        templateRuleRegistry.addTemplateRule(templateRule);
    }

    /**
     * Gets the aspect rule registry.
     * @return the aspect rule registry
     */
    public AspectRuleRegistry getAspectRuleRegistry() {
        return aspectRuleRegistry;
    }

    /**
     * Returns the bean rule registry.
     * @return the bean rule registry
     */
    public BeanRuleRegistry getBeanRuleRegistry() {
        return beanRuleRegistry;
    }

    /**
     * Returns the schedule rule registry.
     * @return the template rule registry
     */
    public ScheduleRuleRegistry getScheduleRuleRegistry() {
        return scheduleRuleRegistry;
    }

    /**
     * Returns the translet rule registry.
     * @return the translet rule registry
     */
    public TransletRuleRegistry getTransletRuleRegistry() {
        return transletRuleRegistry;
    }

    /**
     * Returns the template rule registry.
     * @return the template rule registry
     */
    public TemplateRuleRegistry getTemplateRuleRegistry() {
        return templateRuleRegistry;
    }

    /**
     * Returns all Aspect rules.
     * @return the aspect rules
     */
    public Collection<AspectRule> getAspectRules() {
        return aspectRuleRegistry.getAspectRules();
    }

    /**
     * Returns all bean rules.
     * @return the bean rules
     */
    public Collection<BeanRule> getBeanRules() {
        Collection<BeanRule> idBasedBeanRules = beanRuleRegistry.getIdBasedBeanRules();
        Collection<Set<BeanRule>> typeBasedBeanRules = beanRuleRegistry.getTypeBasedBeanRules();
        Collection<BeanRule> configurableBeanRules = beanRuleRegistry.getConfigurableBeanRules();

        int capacity = idBasedBeanRules.size();
        for (Set<BeanRule> brs : typeBasedBeanRules) {
            capacity += brs.size();
        }
        capacity += configurableBeanRules.size();
        capacity = (int)(capacity / 0.9f) + 1;

        Set<BeanRule> beanRuleSet = new HashSet<>(capacity, 0.9f);
        beanRuleSet.addAll(idBasedBeanRules);
        for (Set<BeanRule> brs : typeBasedBeanRules) {
            beanRuleSet.addAll(brs);
        }
        beanRuleSet.addAll(configurableBeanRules);
        return beanRuleSet;
    }

    /**
     * Returns all schedule rules.
     * @return the schedule rules
     */
    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRuleRegistry.getScheduleRules();
    }

    /**
     * Returns all translet rules.
     * @return the translet rules
     */
    public Collection<TransletRule> getTransletRules() {
        return transletRuleRegistry.getTransletRules();
    }

    /**
     * Returns all template rules.
     * @return the template rules
     */
    public Collection<TemplateRule> getTemplateRules() {
        return templateRuleRegistry.getTemplateRules();
    }

    /**
     * Returns the rule append handler.
     * @return the rule append handler
     */
    public RuleAppendHandler getRuleAppendHandler() {
        return ruleAppendHandler;
    }

    /**
     * Sets the rule append handler.
     * @param ruleAppendHandler the new rule append handler
     */
    public void setRuleAppendHandler(RuleAppendHandler ruleAppendHandler) {
        this.ruleAppendHandler = ruleAppendHandler;
    }

    /**
     * Removes the last rule appender after rule parsing is complete.
     */
    public void clearCurrentRuleAppender() {
        if (ruleAppendHandler != null) {
            ruleAppendHandler.setCurrentRuleAppender(null);
        }
    }

    public DescriptionRule profiling(@NonNull DescriptionRule newDr, @Nullable DescriptionRule oldDr) {
        if (newDr.getProfiles() != null && getEnvironmentProfiles() != null) {
            if (getEnvironmentProfiles().acceptsProfiles(newDr.getProfiles())) {
                return mergeDescriptionRule(newDr, oldDr);
            } else {
                if (oldDr == null) {
                    DescriptionRule dr = new DescriptionRule();
                    dr.addCandidate(newDr);
                    return dr;
                } else {
                    oldDr.addCandidate(newDr);
                    return oldDr;
                }
            }
        } else {
            return mergeDescriptionRule(newDr, oldDr);
        }
    }

    @NonNull
    private DescriptionRule mergeDescriptionRule(@NonNull DescriptionRule newDr, @Nullable DescriptionRule oldDr) {
        if (oldDr == null) {
            if (newDr.getContent() != null) {
                String formatted = TextStyler.styling(newDr.getContent(), newDr.getContentStyle());
                newDr.setFormattedContent(formatted);
            }
            return newDr;
        }
        DescriptionRule dr = new DescriptionRule();
        if (newDr.getContent() != null) {
            String formatted = TextStyler.styling(newDr.getContent(), newDr.getContentStyle());
            if (oldDr.getFormattedContent() != null) {
                formatted = oldDr.getFormattedContent() + formatted;
            }
            dr.setFormattedContent(formatted);
        } else if (oldDr.getFormattedContent() != null) {
            dr.setFormattedContent(oldDr.getFormattedContent());
        }
        oldDr.setFormattedContent(null);
        if (oldDr.getCandidates() == null) {
            dr.addCandidate(oldDr);
        } else {
            dr.setCandidates(oldDr.getCandidates());
            oldDr.setCandidates(null);
        }
        dr.addCandidate(newDr);
        return dr;
    }

    public ItemRuleMap profiling(@NonNull ItemRuleMap newIrm, @Nullable ItemRuleMap oldIrm) {
        if (newIrm.getProfiles() != null && getEnvironmentProfiles() != null) {
            if (getEnvironmentProfiles().acceptsProfiles(newIrm.getProfiles())) {
                return mergeItemRuleMap(newIrm, oldIrm);
            } else {
                if (oldIrm == null) {
                    ItemRuleMap irm = new ItemRuleMap();
                    irm.addCandidate(newIrm);
                    return irm;
                } else {
                    oldIrm.addCandidate(newIrm);
                    return oldIrm;
                }
            }
        } else {
            return mergeItemRuleMap(newIrm, oldIrm);
        }
    }

    private ItemRuleMap mergeItemRuleMap(@NonNull ItemRuleMap newIrm, ItemRuleMap oldIrm) {
        if (oldIrm == null) {
            return newIrm;
        }
        ItemRuleMap irm = new ItemRuleMap(oldIrm);
        irm.putAll(newIrm);
        if (oldIrm.getCandidates() == null) {
            irm.addCandidate(oldIrm);
        } else {
            irm.setCandidates(oldIrm.getCandidates());
            oldIrm.setCandidates(null);
            oldIrm.clear();
        }
        irm.addCandidate(newIrm);
        return irm;
    }

}
