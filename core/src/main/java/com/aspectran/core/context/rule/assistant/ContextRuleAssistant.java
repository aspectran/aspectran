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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.component.bean.BeanRuleRegistry;
import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.component.template.TemplateRuleRegistry;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.PropertiesLoaderUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class ContextRuleAssistant.
 * 
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ContextRuleAssistant {

    private final ContextEnvironment contextEnvironment;

    private final ApplicationAdapter applicationAdapter;

    private final String basePath;

    private final ClassLoader classLoader;

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

    protected ContextRuleAssistant() {
        this(null);
    }

    public ContextRuleAssistant(ContextEnvironment contextEnvironment) {
        if (contextEnvironment != null) {
            this.contextEnvironment = contextEnvironment;
            this.applicationAdapter = contextEnvironment.getApplicationAdapter();
            this.basePath = contextEnvironment.getBasePath();
            this.classLoader = contextEnvironment.getClassLoader();
        } else {
            this.contextEnvironment = null;
            this.applicationAdapter = null;
            this.basePath = null;
            this.classLoader = null;
        }
    }

    public void ready() {
        settings = new HashMap<>();
        environmentRules = new LinkedList<>();
        typeAliases = new HashMap<>();
        assistantLocal = new AssistantLocal(this);

        if (contextEnvironment != null) {
            aspectRuleRegistry = new AspectRuleRegistry();

            beanRuleRegistry = new BeanRuleRegistry(classLoader);

            transletRuleRegistry = new TransletRuleRegistry(contextEnvironment);
            transletRuleRegistry.setAssistantLocal(assistantLocal);

            scheduleRuleRegistry = new ScheduleRuleRegistry();
            scheduleRuleRegistry.setAssistantLocal(assistantLocal);

            templateRuleRegistry = new TemplateRuleRegistry();
            templateRuleRegistry.setAssistantLocal(assistantLocal);

            beanReferenceInspector = new BeanReferenceInspector();

            BeanDescriptor.clearCache();
            MethodUtils.clearCache();
            PropertiesLoaderUtils.clearCache();
        }
    }

    public void release() {
        settings = null;
        environmentRules = null;
        typeAliases = null;
        assistantLocal = null;

        if (contextEnvironment != null) {
            scheduleRuleRegistry.setAssistantLocal(null);
            transletRuleRegistry.setAssistantLocal(null);
            templateRuleRegistry.setAssistantLocal(null);

            aspectRuleRegistry = null;
            beanRuleRegistry = null;
            scheduleRuleRegistry = null;
            transletRuleRegistry = null;
            templateRuleRegistry = null;

            beanReferenceInspector = null;

            BeanDescriptor.clearCache();
            MethodUtils.clearCache();
            PropertiesLoaderUtils.clearCache();
        }
    }

    public ContextEnvironment getContextEnvironment() {
        return contextEnvironment;
    }

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    public String getBasePath() {
        return basePath;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Gets the settings.
     *
     * @return the settings
     */
    public Map<DefaultSettingType, String> getSettings() {
        return settings;
    }

    /**
     * Gets the setting value.
     *
     * @param settingType the setting type
     * @return the setting
     */
    public Object getSetting(DefaultSettingType settingType) {
        return settings.get(settingType);
    }

    /**
     * Puts the setting value.
     *
     * @param name the name
     * @param value the value
     */
    public void putSetting(String name, String value) {
        DefaultSettingType settingType = DefaultSettingType.resolve(name);
        if (settingType == null) {
            throw new IllegalArgumentException("No such default setting name as '" + name + "'");
        }
        settings.put(settingType, value);
    }

    /**
     * Apply settings.
     *
     * @throws ClassNotFoundException the class not found exception
     */
    public void applySettings() throws ClassNotFoundException {
        DefaultSettings defaultSettings = assistantLocal.touchDefaultSettings();
        defaultSettings.apply(getSettings());

        applyTransletInterface(defaultSettings);
    }

    /**
     * Apply translet interface.
     *
     * @param defaultSettings the default settings
     * @throws ClassNotFoundException the class not found exception
     */
    @SuppressWarnings("unchecked")
    public void applyTransletInterface(DefaultSettings defaultSettings) throws ClassNotFoundException {
        if (defaultSettings.getTransletInterfaceClassName() != null) {
            Class<?> transletInterfaceClass = classLoader.loadClass(defaultSettings.getTransletInterfaceClassName());
            defaultSettings.setTransletInterfaceClass((Class<Translet>)transletInterfaceClass);
        }
        if (defaultSettings.getTransletImplementationClassName() != null) {
            Class<?> transletImplementationClass = classLoader.loadClass(defaultSettings.getTransletImplementationClassName());
            defaultSettings.setTransletImplementationClass((Class<CoreTranslet>)transletImplementationClass);
        }
    }

    /**
     * Gets the environment rules.
     *
     * @return the environment rules
     */
    public List<EnvironmentRule> getEnvironmentRules() {
        return environmentRules;
    }

    /**
     * Adds the environment rule.
     *
     * @param environmentRule the environment rule
     */
    public void addEnvironmentRule(EnvironmentRule environmentRule) {
        environmentRules.add(environmentRule);
    }

    /**
     * Gets the type aliases.
     *
     * @return the type aliases
     */
    public Map<String, String> getTypeAliases() {
        return typeAliases;
    }

    /**
     * Adds a type alias to use for simplifying complex type signatures.
     * A type alias is defined by assigning the type to the alias.
     *
     * @param alias the name of the alias
     * @param type the type identifier that you are creating an alias for
     */
    public void addTypeAlias(String alias, String type) {
        typeAliases.put(alias, type);
    }

    /**
     * Returns a type of an aliased type that is defined by assigning the type to the alias.
     *
     * @param alias the name of the alias
     * @return the aliased type
     */
    public String getAliasedType(String alias) {
        return typeAliases.get(alias);
    }

    /**
     * Returns a type of an aliased type that is defined by assigning the type to the alias.
     * If aliased type is not found, it returns alias.
     *
     * @param alias the name of the alias
     * @return the aliased type
     */
    public String resolveAliasType(String alias) {
        String type = getAliasedType(alias);
        return (type == null ? alias: type);
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     *
     * @param transletName the translet name
     * @return the string
     */
    public String applyTransletNamePattern(String transletName) {
        if (transletName == null) {
            return null;
        }
        return transletRuleRegistry.applyTransletNamePattern(transletName, true);
    }

    /**
     * Gets the assistant local.
     *
     * @return the assistant local
     */
    public AssistantLocal getAssistantLocal() {
        return assistantLocal;
    }

    /**
     * Sets the assistant local.
     *
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
     *
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
     *
     * @param oldAssistantLocal the old assistant local
     */
    public void restoreAssistantLocal(AssistantLocal oldAssistantLocal) {
        setAssistantLocal(oldAssistantLocal);
    }

    /**
     * Returns whether the pointcut pattern validation is required.
     *
     * @return true if pointcut pattern validation is required
     */
    public boolean isPointcutPatternVerifiable() {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        return (defaultSettings == null || defaultSettings.isPointcutPatternVerifiable());
    }

    /**
     * Resolve bean class for the aspect rule.
     *
     * @param aspectRule the aspect rule
     */
    public void resolveAdviceBeanClass(AspectRule aspectRule) {
        String beanIdOrClass = aspectRule.getAdviceBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveBeanClass(beanIdOrClass, aspectRule);
            if (beanClass != null) {
                aspectRule.setAdviceBeanClass(beanClass);
                reserveBeanReference(beanClass, aspectRule);
            } else {
                reserveBeanReference(beanIdOrClass, aspectRule);
            }
        }
    }

    /**
     * Resolve bean class for bean action rule.
     *
     * @param beanActionRule the bean action rule
     */
    public void resolveActionBeanClass(BeanActionRule beanActionRule) {
        String beanIdOrClass = beanActionRule.getBeanId();
        if (beanIdOrClass != null) {
            Class<?> beanClass = resolveBeanClass(beanIdOrClass, beanActionRule);
            if (beanClass != null) {
                beanActionRule.setBeanClass(beanClass);
                reserveBeanReference(beanClass, beanActionRule);
            } else {
                reserveBeanReference(beanIdOrClass, beanActionRule);
            }
        }
    }

    /**
     * Resolve bean class for factory bean rule.
     *
     * @param beanRule the bean rule
     */
    public void resolveFactoryBeanClass(BeanRule beanRule) {
        String beanIdOrClass = beanRule.getFactoryBeanId();
        if (beanRule.isFactoryOffered() && beanIdOrClass != null) {
            Class<?> beanClass = resolveBeanClass(beanIdOrClass, beanRule);
            if (beanClass != null) {
                beanRule.setFactoryBeanClass(beanClass);
                reserveBeanReference(beanClass, beanRule);
            } else {
                reserveBeanReference(beanIdOrClass, beanRule);
            }
        }
    }

    /**
     * Resolve bean class.
     *
     * @param itemRule the item rule
     */
    public void resolveBeanClass(ItemRule itemRule) {
        Iterator<Token[]> iter = ItemRule.tokenIterator(itemRule);
        if (iter != null) {
            while (iter.hasNext()) {
                Token[] tokens = iter.next();
                if (tokens != null) {
                    for (Token token : tokens) {
                        resolveBeanClass(token);
                    }
                }
            }
        }
    }

    /**
     * Resolve bean class for token.
     *
     * @param tokens an array of tokens
     */
    public void resolveBeanClass(Token[] tokens) {
        if (tokens != null) {
            for (Token token : tokens) {
                resolveBeanClass(token);
            }
        }
    }

    /**
     * Resolve bean class for token.
     *
     * @param token the token
     */
    public void resolveBeanClass(Token token) {
        if (token != null && token.getType() == TokenType.BEAN) {
            if (token.getDirectiveType() == TokenDirectiveType.CLASS) {
                Class<?> beanClass = loadClass(token.getValue(), token);
                token.setAlternativeValue(beanClass);
                reserveBeanReference(beanClass, token);
            } else {
                reserveBeanReference(token.getName(), token);
            }
        }
    }

    /**
     * Resolve bean class for the autowire rule.
     *
     * @param autowireRule the autowire rule
     */
    public void resolveBeanClass(AutowireRule autowireRule) {
        if (autowireRule.getTargetType() == AutowireTargetType.FIELD) {
            if (autowireRule.isRequired()) {
                Class<?>[] types = autowireRule.getTypes();
                String[] qualifiers = autowireRule.getQualifiers();
                reserveBeanReference(qualifiers[0], types[0], autowireRule);
            }
        } else if (autowireRule.getTargetType() == AutowireTargetType.FIELD_VALUE) {
            Token token = autowireRule.getToken();
            if (token.getType() == TokenType.BEAN) {
                if (token.getDirectiveType() == TokenDirectiveType.CLASS) {
                    Class<?> beanClass = loadClass(token.getValue(), token);
                    token.setAlternativeValue(beanClass);
                    reserveBeanReference(beanClass, autowireRule);
                } else {
                    reserveBeanReference(token.getName(), autowireRule);
                }
            }
        } else if (autowireRule.getTargetType() == AutowireTargetType.METHOD) {
            if (autowireRule.isRequired()) {
                Class<?>[] types = autowireRule.getTypes();
                String[] qualifiers = autowireRule.getQualifiers();
                for (int i = 0; i < types.length; i++) {
                    reserveBeanReference(qualifiers[i], types[i], autowireRule);
                }
            }
        }
    }

    /**
     * Resolve bean class for the schedule rule.
     *
     * @param scheduleRule the schedule rule
     */
    public void resolveBeanClass(ScheduleRule scheduleRule) {
        String beanId = scheduleRule.getSchedulerBeanId();
        if (beanId != null) {
            Class<?> beanClass = resolveBeanClass(beanId, scheduleRule);
            if (beanClass != null) {
                scheduleRule.setSchedulerBeanClass(beanClass);
                reserveBeanReference(beanClass, scheduleRule);
            } else {
                reserveBeanReference(beanId, scheduleRule);
            }
        }
    }

    /**
     * Resolve bean class for the template rule.
     *
     * @param templateRule the template rule
     */
    public void resolveBeanClass(TemplateRule templateRule) {
        String beanId = templateRule.getEngineBeanId();
        if (beanId != null) {
            Class<?> beanClass = resolveBeanClass(beanId, templateRule);
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

    private Class<?> resolveBeanClass(String beanIdOrClass, Object referer) {
        if (beanIdOrClass != null && beanIdOrClass.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = beanIdOrClass.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            return loadClass(className, referer);
        } else {
            return null;
        }
    }

    private Class<?> loadClass(String className, Object referer) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class: " + className +
                    " on " + ruleAppendHandler.getCurrentRuleAppender().getQualifiedName() + " " + referer, e);
        }
    }

    public void reserveBeanReference(String beanId, BeanReferenceInspectable inspectable) {
        reserveBeanReference(beanId, null, inspectable);
    }

    public void reserveBeanReference(Class<?> beanClass, BeanReferenceInspectable inspectable) {
        reserveBeanReference(null, beanClass, inspectable);
    }

    public void reserveBeanReference(String beanId, Class<?> beanClass, BeanReferenceInspectable inspectable) {
        beanReferenceInspector.reserve(beanId, beanClass, inspectable, ruleAppendHandler.getCurrentRuleAppender());
    }

    /**
     * Returns the bean reference inspector.
     *
     * @return the bean reference inspector
     */
    public BeanReferenceInspector getBeanReferenceInspector() {
        return beanReferenceInspector;
    }

    /**
     * Adds the aspect rule.
     *
     * @param aspectRule the aspect rule to add
     */
    public void addAspectRule(AspectRule aspectRule) {
        aspectRuleRegistry.addAspectRule(aspectRule);
    }

    /**
     * Adds the bean rule.
     *
     * @param beanRule the bean rule to add
     * @throws IllegalRuleException if an error occurs while adding a bean rule
     */
    public void addBeanRule(BeanRule beanRule) throws IllegalRuleException {
        beanRuleRegistry.addBeanRule(beanRule);
    }

    /**
     * Adds the schedule rule.
     *
     * @param scheduleRule the schedule rule to add
     */
    public void addScheduleRule(ScheduleRule scheduleRule) {
        scheduleRuleRegistry.addScheduleRule(scheduleRule);
    }

    /**
     * Add the translet rule.
     *
     * @param transletRule the translet rule to add
     */
    public void addTransletRule(TransletRule transletRule) {
        transletRuleRegistry.addTransletRule(transletRule);
    }

    /**
     * Add the template rule.
     *
     * @param templateRule the template rule to add
     */
    public void addTemplateRule(TemplateRule templateRule) {
        templateRuleRegistry.addTemplateRule(templateRule);
    }

    /**
     * Gets the aspect rule registry.
     *
     * @return the aspect rule registry
     */
    public AspectRuleRegistry getAspectRuleRegistry() {
        return aspectRuleRegistry;
    }

    /**
     * Returns the bean rule registry.
     *
     * @return the bean rule registry
     */
    public BeanRuleRegistry getBeanRuleRegistry() {
        return beanRuleRegistry;
    }

    /**
     * Returns the schedule rule registry.
     *
     * @return the template rule registry
     */
    public ScheduleRuleRegistry getScheduleRuleRegistry() {
        return scheduleRuleRegistry;
    }

    /**
     * Returns the translet rule registry.
     *
     * @return the translet rule registry
     */
    public TransletRuleRegistry getTransletRuleRegistry() {
        return transletRuleRegistry;
    }

    /**
     * Returns the template rule registry.
     *
     * @return the template rule registry
     */
    public TemplateRuleRegistry getTemplateRuleRegistry() {
        return templateRuleRegistry;
    }

    /**
     * Returns all Aspect rules.
     *
     * @return the aspect rules
     */
    public Collection<AspectRule> getAspectRules() {
        return aspectRuleRegistry.getAspectRules();
    }

    /**
     * Returns all bean rules.
     *
     * @return the bean rules
     */
    public Collection<BeanRule> getBeanRules() {
        Set<BeanRule> beanRuleSet = new HashSet<>();
        beanRuleSet.addAll(beanRuleRegistry.getIdBasedBeanRules());
        for (Set<BeanRule> brs : beanRuleRegistry.getTypeBasedBeanRules()) {
            beanRuleSet.addAll(brs);
        }
        beanRuleSet.addAll(beanRuleRegistry.getConfigBeanRules());
        return beanRuleSet;
    }

    /**
     * Returns all schedule rules.
     *
     * @return the schedule rules
     */
    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRuleRegistry.getScheduleRules();
    }

    /**
     * Returns all translet rules.
     *
     * @return the translet rules
     */
    public Collection<TransletRule> getTransletRules() {
        return transletRuleRegistry.getTransletRules();
    }

    /**
     * Returns all template rules.
     *
     * @return the template rules
     */
    public Collection<TemplateRule> getTemplateRules() {
        return templateRuleRegistry.getTemplateRules();
    }

    /**
     * Returns the rule append handler.
     *
     * @return the rule append handler
     */
    public RuleAppendHandler getRuleAppendHandler() {
        return ruleAppendHandler;
    }

    /**
     * Sets the rule append handler.
     *
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

}
