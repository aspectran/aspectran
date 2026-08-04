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
import com.aspectran.core.context.env.EnvironmentProfiles;
import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.DescriptionRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.core.context.rule.parser.xml.AspectranNodeParsingContext;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.util.Namespace;
import com.aspectran.core.context.rule.validation.BeanReferenceInspector;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private final RuleProfileEvaluator ruleProfileEvaluator;

    private final BeanClassResolver beanClassResolver;

    private boolean rootAppenderParsed;

    private DescriptionRule descriptionRule;

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

    /**
     * Constructs a new RuleParsingContext with the specified parameters.
     * @param classLoader the class loader to be used for loading resources and classes; must not be null
     * @param applicationAdapter the application adapter that provides access to application-specific functionalities; must not be null
     * @param environmentProfiles the environment profiles that define the active and default profiles; must not be null
     */
    public RuleParsingContext(
            ClassLoader classLoader,
            ApplicationAdapter applicationAdapter,
            EnvironmentProfiles environmentProfiles) {
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.notNull(applicationAdapter, "applicationAdapter must not be null");
        Assert.notNull(environmentProfiles, "environmentProfiles must not be null");
        this.shallow = false;
        this.classLoader = classLoader;
        this.applicationAdapter = applicationAdapter;
        this.environmentProfiles = environmentProfiles;
        this.ruleProfileEvaluator = new RuleProfileEvaluator(environmentProfiles);
        this.beanClassResolver = new BeanClassResolver(this);
    }

    /**
     * Constructs a new instance of RuleParsingContext with the specified class loader.
     * <p>This constructor is used for shallow parsing.</p>
     */
    protected RuleParsingContext() {
        this.shallow = true;
        this.classLoader = null;
        this.applicationAdapter = null;
        this.environmentProfiles = null;
        this.ruleProfileEvaluator = new RuleProfileEvaluator(null);
        this.beanClassResolver = new ShallowBeanClassResolver(this);
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
            scheduleRuleRegistry = new ScheduleRuleRegistry();
            transletRuleRegistry = new TransletRuleRegistry(getBasePath(), classLoader);
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
        descriptionRule = null;
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
     * Returns whether the parsing context is used for shallow parsing.
     * @return true if shallow parsing is used; false otherwise
     */
    public boolean isShallow() {
        return shallow;
    }

    /**
     * Returns the class loader used for loading classes.
     * @return the class loader
     */
    public ClassLoader getClassLoader() {
        Assert.state(classLoader != null, "ClassLoader is not set");
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
     * Returns the rule profile evaluator.
     * @return the rule profile evaluator
     */
    public RuleProfileEvaluator getRuleProfileEvaluator() {
        return ruleProfileEvaluator;
    }

    /**
     * Returns the bean class resolver.
     * @return the bean class resolver
     */
    public BeanClassResolver getBeanClassResolver() {
        return beanClassResolver;
    }

    /**
     * Returns the bean reference inspector.
     * @return the bean reference inspector
     */
    public BeanReferenceInspector getBeanReferenceInspector() {
        return beanReferenceInspector;
    }

    /**
     * Sets whether the root rule appender has been parsed.
     * @param rootAppenderParsed true if the root rule appender has been parsed
     */
    public void setRootAppenderParsed(boolean rootAppenderParsed) {
        this.rootAppenderParsed = rootAppenderParsed;
    }

    /**
     * Gets the description rule.
     * @return the description rule
     */
    public DescriptionRule getDescriptionRule() {
        return descriptionRule;
    }

    /**
     * Sets the description rule.
     * @param descriptionRule the description rule
     * @param nestingLevel the nesting level
     */
    public void setDescriptionRule(DescriptionRule descriptionRule, int nestingLevel) {
        if (!rootAppenderParsed && nestingLevel == 1) {
            this.descriptionRule = descriptionRule;
        }
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
     * Returns whether the pointcut pattern validation is required.
     * @return true if pointcut pattern validation is required
     */
    public boolean isPointcutPatternVerifiable() {
        DefaultSettings defaultSettings = ruleParsingScope.getDefaultSettings();
        return (defaultSettings != null && defaultSettings.isPointcutPatternVerifiable());
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
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(environmentRule);
            return;
        }
        environmentRules.add(environmentRule);
        if (ruleParsingScope != null && environmentRule != null && environmentRule.getPropertyItemRuleMap() != null) {
            for (ItemRule itemRule : environmentRule.getPropertyItemRuleMap().values()) {
                ruleParsingScope.addScopedPropertyKey(itemRule.getName());
            }
        }
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
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(new com.aspectran.core.context.rule.TypeAliasRule(alias, type));
            return;
        }
        typeAliases.put(alias, type);
        if (ruleParsingScope != null) {
            ruleParsingScope.addScopedTypeAlias(alias);
        }
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
        return Namespace.applyTransletNamePattern(ruleParsingScope.getDefaultSettings(), transletName, true);
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
     * Adds the aspect rule.
     * @param aspectRule the aspect rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addAspectRule(AspectRule aspectRule) throws IllegalRuleException {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(aspectRule);
            return;
        }
        aspectRuleRegistry.addAspectRule(aspectRule);
        if (ruleParsingScope != null && aspectRule != null) {
            ruleParsingScope.addScopedAspectId(aspectRule.getId());
        }
    }

    /**
     * Adds the bean rule.
     * @param beanRule the bean rule to add
     * @throws IllegalRuleException if an error occurs while adding a bean rule
     */
    public void addBeanRule(BeanRule beanRule) throws IllegalRuleException {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(beanRule);
            return;
        }
        beanRuleRegistry.addBeanRule(beanRule);
        if (ruleParsingScope != null && beanRule != null) {
            ruleParsingScope.addScopedBeanId(beanRule.getId());
        }
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
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(scheduleRule);
            return;
        }
        scheduleRuleRegistry.addScheduleRule(scheduleRule);
        if (ruleParsingScope != null && scheduleRule != null) {
            ruleParsingScope.addScopedScheduleId(scheduleRule.getId());
        }
    }

    /**
     * Add the translet rule.
     * @param transletRule the translet rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addTransletRule(TransletRule transletRule) throws IllegalRuleException {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(transletRule);
            return;
        }
        transletRuleRegistry.addTransletRule(transletRule);
        if (ruleParsingScope != null && transletRule != null) {
            ruleParsingScope.addScopedTransletName(transletRule.getName());
        }
    }

    /**
     * Add the template rule.
     * @param templateRule the template rule to add
     * @throws IllegalRuleException if an illegal rule is found
     */
    public void addTemplateRule(TemplateRule templateRule) throws IllegalRuleException {
        AppendRule appendRule = getActiveAppendRule();
        if (appendRule != null) {
            appendRule.addChildRule(templateRule);
            return;
        }
        templateRuleRegistry.addTemplateRule(templateRule);
        if (ruleParsingScope != null && templateRule != null) {
            ruleParsingScope.addScopedTemplateId(templateRule.getId());
        }
    }

    @Nullable
    protected AppendRule getActiveAppendRule() {
        try {
            return AspectranNodeParsingContext.peekObject(AppendRule.class);
        } catch (Exception e) {
            return null;
        }
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

}
