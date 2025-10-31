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
package com.aspectran.core.context.rule.parsing;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.asel.bean.ValueProvider;
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
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.rule.util.Namespace;
import com.aspectran.core.context.rule.util.TextStyler;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A central class that holds the state and context during the parsing of Aspectran's
 * configuration rules.

/**
 * A central class that holds the state and context during the parsing of Aspectran's
 * configuration rules.
 * <p>It acts as a temporary container for all discovered rules (e.g., beans, aspects,
 * translets) and provides access to essential services like the {@link ClassLoader} and
 * {@link com.aspectran.core.context.env.EnvironmentProfiles}. The parser populates this
 * context, which is then used by the {@link com.aspectran.core.context.builder.ActivityContextBuilder}
 * to construct the final {@link com.aspectran.core.context.ActivityContext}.</p>
 *
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class RuleParsingContext {

    private final boolean shallow;

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

    private RuleParsingScope ruleParsingScope;

    private RuleAppendHandler ruleAppendHandler;


    protected RuleParsingContext() {
        this.shallow = true;
        this.classLoader = null;
        this.applicationAdapter = null;
        this.environmentProfiles = null;
    }

    public RuleParsingContext(ClassLoader classLoader,
                              ApplicationAdapter applicationAdapter,
                              EnvironmentProfiles environmentProfiles) {
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.notNull(applicationAdapter, "applicationAdapter must not be null");
        Assert.notNull(environmentProfiles, "environmentProfiles must not be null");
        this.shallow = false;
        this.classLoader = classLoader;
        this.applicationAdapter = applicationAdapter;
        this.environmentProfiles = environmentProfiles;
    }

    /**
     * Initializes the rule parsing context.
     */
    public void prepare() {
        settings = new HashMap<>();
        environmentRules = new LinkedList<>();
        typeAliases = new HashMap<>();
        ruleParsingScope = new RuleParsingScope(this);

        if (!shallow) {
            aspectRuleRegistry = new AspectRuleRegistry();
            beanRuleRegistry = new BeanRuleRegistry(classLoader);
            transletRuleRegistry = new TransletRuleRegistry(getBasePath(), classLoader);
            scheduleRuleRegistry = new ScheduleRuleRegistry();
            templateRuleRegistry = new TemplateRuleRegistry();

            transletRuleRegistry.setRuleParsingScope(ruleParsingScope);
            scheduleRuleRegistry.setRuleParsingScope(ruleParsingScope);
            templateRuleRegistry.setRuleParsingScope(ruleParsingScope);

            beanReferenceInspector = new BeanReferenceInspector();
        }
    }

    /**
     * Releases all resources used by the rule parsing context.
     */
    public void release() {
        settings = null;
        environmentRules = null;
        typeAliases = null;
        ruleParsingScope = null;

        if (!shallow) {
            scheduleRuleRegistry.setRuleParsingScope(null);
            transletRuleRegistry.setRuleParsingScope(null);
            templateRuleRegistry.setRuleParsingScope(null);

            aspectRuleRegistry = null;
            beanRuleRegistry = null;
            scheduleRuleRegistry = null;
            transletRuleRegistry = null;
            templateRuleRegistry = null;

            beanReferenceInspector = null;
        }
    }

    /**
     * Returns the class loader used for loading classes.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        Assert.notNull(classLoader, "ClassLoader is not set");
        return classLoader;
    }

    /**
     * Returns the application adapter.
     * @return the application adapter
     */
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    /**
     * Returns the base path of the application.
     * @return the base path
     */
    public String getBasePath() {
        if (applicationAdapter != null) {
            return applicationAdapter.getBasePathString();
        } else {
            return null;
        }
    }

    /**
     * Returns the environment profiles.
     * @return the environment profiles
     */
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
        DefaultSettings defaultSettings = ruleParsingScope.touchDefaultSettings();
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
     * @param alias the name of the alias
     * @param type the type identifier that you are creating an alias for
     */
    public void addTypeAlias(String alias, String type) {
        typeAliases.put(alias, type);
    }

    /**
     * Returns the type for the given alias.
     * @param alias the name of the alias
     * @return the aliased type
     */
    public String getAliasedType(String alias) {
        return typeAliases.get(alias);
    }

    /**
     * Returns the type for the given alias.
     * If no type is found for a given alias, the alias is returned as is.
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
            ruleParsingScope.getDefaultSettings(), transletName, true);
    }

    /**
     * Gets the rule-parsing scope.
     * @return the rule-parsing scope
     */
    public RuleParsingScope getRuleParsingScope() {
        return ruleParsingScope;
    }

    /**
     * Sets the rule-parsing scope.
     * @param newRuleParsingScope the new rule-parsing scope
     */
    private void setRuleParsingScope(RuleParsingScope newRuleParsingScope) {
        this.ruleParsingScope = newRuleParsingScope;
        scheduleRuleRegistry.setRuleParsingScope(newRuleParsingScope);
        transletRuleRegistry.setRuleParsingScope(newRuleParsingScope);
        templateRuleRegistry.setRuleParsingScope(newRuleParsingScope);
    }

    /**
     * Backup the rule-parsing scope.
     * @return the rule-parsing scope
     */
    public RuleParsingScope backupRuleParsingScope() {
        RuleParsingScope oldRuleParsingScope = ruleParsingScope;
        RuleParsingScope newRuleParsingScope = ruleParsingScope.replicate();
        setRuleParsingScope(newRuleParsingScope);
        return oldRuleParsingScope;
    }

    /**
     * Restore the rule-parsing scope.
     * @param oldRuleParsingScope the old rule-parsing scope
     */
    public void restoreRuleParsingScope(RuleParsingScope oldRuleParsingScope) {
        setRuleParsingScope(oldRuleParsingScope);
    }

    /**
     * Returns whether the pointcut pattern validation is required.
     * @return true if pointcut pattern validation is required
     */
    public boolean isPointcutPatternVerifiable() {
        DefaultSettings defaultSettings = ruleParsingScope.getDefaultSettings();
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
            try {
                Token.resolveValueProvider(token, getClassLoader());
            } catch (RuntimeException e) {
                throw new IllegalRuleException("Failed to resolve value provider for token: " + token, e);
            }

            ValueProvider provider = token.getValueProvider();
            if (provider != null) {
                if (provider.isRequiresBeanInstance()) {
                    reserveBeanReference(provider.getDependentBeanType(), referenceable);
                }
            } else if (token.getDirectiveType() == null) {
                // This is for simple #{beanId} tokens that don't have a provider
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
                    resolveBeanClassOrReference(autowireRule, autowireTargetRule, true);
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
                AutowireTargetRule autowireTargetRule = AutowireRule.getAutowireTargetRule(autowireRule);
                if (autowireRule.isRequired() && autowireTargetRule != null && !autowireTargetRule.isInnerBean()) {
                    resolveBeanClassOrReference(autowireRule, autowireTargetRule, false);
                }
            } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD ||
                autowireRule.getTargetType() == AutowireTargetType.CONSTRUCTOR) {
                AutowireTargetRule[] autowireTargetRules = autowireRule.getAutowireTargetRules();
                if (autowireTargetRules != null && autowireRule.isRequired()) {
                    for (AutowireTargetRule autowireTargetRule : autowireTargetRules) {
                        if (!autowireTargetRule.isOptional() && !autowireTargetRule.isInnerBean()) {
                            resolveBeanClassOrReference(autowireRule, autowireTargetRule, true);
                        }
                    }
                }
            }
        }
    }

    private void resolveBeanClassOrReference(
            AutowireRule autowireRule, @NonNull AutowireTargetRule autowireTargetRule, boolean forReference)
            throws IllegalRuleException {
        ValueExpression valueExpression = autowireTargetRule.getValueExpression();
        if (valueExpression != null) {
            Token[] tokens = valueExpression.getTokens();
            resolveBeanClass(tokens, autowireRule);
        } else if (forReference) {
            Class<?> type = autowireTargetRule.getType();
            String qualifier = autowireTargetRule.getQualifier();
            reserveBeanReference(qualifier, type, autowireRule);
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
            throw new IllegalRuleException("Unable to load class " + className + " for " + referer, e);
        }
    }

    /**
     * Reserves to bean reference inspection.
     * @param beanId the bean id
     * @param referenceable the object to be inspected
     */
    public void reserveBeanReference(String beanId, BeanReferenceable referenceable) {
        reserveBeanReference(beanId, null, referenceable);
    }

    /**
     * Reserves to bean reference inspection.
     * @param beanClass the bean class
     * @param referenceable the object to be inspected
     */
    public void reserveBeanReference(Class<?> beanClass, BeanReferenceable referenceable) {
        reserveBeanReference(null, beanClass, referenceable);
    }

    /**
     * Reserves to bean reference inspection.
     * @param beanId the bean id
     * @param beanClass the bean class
     * @param referenceable the object to be inspected
     */
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

    /**
     * Adds the inner bean rule.
     * @param beanRule the inner bean rule to add
     * @throws IllegalRuleException if an error occurs while adding an inner bean rule
     */
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


    /**
     * Returns a new description rule that is a combination of two description rules.
     * @param newDr the new description rule
     * @param oldDr the old description rule
     * @return the combined description rule
     */
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

    /**
     * Returns a new item rule map that is a combination of two item rule maps.
     * @param newIrm the new item rule map
     * @param oldIrm the old item rule map
     * @return the combined item rule map
     */
    public ItemRuleMap profiling(@NonNull ItemRuleMap newIrm, @Nullable ItemRuleMap oldIrm) {
        if (newIrm.getProfiles() != null && getEnvironmentProfiles() != null) {
            if (getEnvironmentProfiles().acceptsProfiles(newIrm.getProfiles())) {
                return mergeItemRuleMap(newIrm, oldIrm);
            } else if (oldIrm == null) {
                ItemRuleMap irm = new ItemRuleMap();
                irm.addCandidate(newIrm);
                return irm;
            } else {
                oldIrm.addCandidate(newIrm);
                return oldIrm;
            }
        } else {
            return mergeItemRuleMap(newIrm, oldIrm);
        }
    }

    private ItemRuleMap mergeItemRuleMap(@NonNull ItemRuleMap newIrm, ItemRuleMap oldIrm) {
        if (oldIrm == null) {
            return newIrm;
        }
        ItemRuleMap irm = new ItemRuleMap();
        irm.putAll(oldIrm);
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
