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
package com.aspectran.core.context.parser.assistant;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.aspect.AspectRuleRegistry;
import com.aspectran.core.context.bean.BeanRuleRegistry;
import com.aspectran.core.context.parser.importer.FileImporter;
import com.aspectran.core.context.parser.importer.ImportHandler;
import com.aspectran.core.context.parser.importer.Importer;
import com.aspectran.core.context.parser.importer.ResourceImporter;
import com.aspectran.core.context.parser.importer.UrlImporter;
import com.aspectran.core.context.env.ContextEnvironment;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.BeanActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.EnvironmentRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TemplateRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.ability.BeanReferenceInspectable;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.context.rule.type.ImporterFileFormatType;
import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.context.schedule.ScheduleRuleRegistry;
import com.aspectran.core.context.template.TemplateRuleRegistry;
import com.aspectran.core.context.translet.TransletRuleRegistry;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.core.util.BeanDescriptor;
import com.aspectran.core.util.MethodUtils;
import com.aspectran.core.util.PropertiesLoaderUtils;
import com.aspectran.core.util.StringUtils;

/**
 * The Class ContextParserAssistant.
 * 
 * <p>Created: 2008. 04. 01 PM 10:25:35</p>
 */
public class ContextParserAssistant {

    private final ContextEnvironment environment;

    private final ApplicationAdapter applicationAdapter;

    private final String basePath;

    private final ClassLoader classLoader;

    private ArrayStack objectStack;

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

    private ImportHandler importHandler;

    protected ContextParserAssistant() {
        this(null);
    }

    public ContextParserAssistant(ContextEnvironment environment) {
        if (environment != null) {
            this.environment = environment;
            this.applicationAdapter = environment.getApplicationAdapter();
            this.basePath = applicationAdapter.getBasePath();
            this.classLoader = applicationAdapter.getClassLoader();
        } else {
            this.environment = null;
            this.applicationAdapter = null;
            this.basePath = null;
            this.classLoader = null;
        }
    }

    public void ready() {
        objectStack = new ArrayStack();
        settings = new HashMap<>();
        environmentRules = new LinkedList<>();
        typeAliases = new HashMap<>();
        assistantLocal = new AssistantLocal(this);

        if (environment != null) {
            aspectRuleRegistry = new AspectRuleRegistry();

            beanRuleRegistry = new BeanRuleRegistry(classLoader);

            transletRuleRegistry = new TransletRuleRegistry(applicationAdapter);
            transletRuleRegistry.setAssistantLocal(assistantLocal);

            beanRuleRegistry.setTransletRuleRegistry(transletRuleRegistry);

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
        objectStack = null;
        settings = null;
        environmentRules = null;
        typeAliases = null;
        assistantLocal = null;

        if (environment != null) {
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
        return environment;
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

    public void pushObject(Object object) {
        objectStack.push(object);
    }

    @SuppressWarnings("unchecked")
    public <T> T popObject() {
        return (T)objectStack.pop();
    }

    @SuppressWarnings("unchecked")
    public <T> T peekObject() {
        return (T)objectStack.peek();
    }

    @SuppressWarnings("unchecked")
    public <T> T peekObject(int n) {
        return (T)objectStack.peek(n);
    }

    /**
     * Clear object stack.
     */
    public void clearObjectStack() {
        objectStack.clear();
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
     * Gets the setting vlaue.
     *
     * @param settingType the setting type
     * @return the setting
     */
    public Object getSetting(DefaultSettingType settingType) {
        return settings.get(settingType);
    }

    /**
     * Puts the setting vlaue.
     *
     * @param name the name
     * @param value the value
     */
    public void putSetting(String name, String value) {
        DefaultSettingType settingType = DefaultSettingType.resolve(name);
        if (settingType == null) {
            throw new IllegalArgumentException("Unknown default setting name '" + name + "'");
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
     * Backup assistant local.
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
     * Restore assistant local.
     *
     * @param oldAssistantLocal the old assistant local
     */
    public void restoreAssistantLocal(AssistantLocal oldAssistantLocal) {
        setAssistantLocal(oldAssistantLocal);
    }

    /**
     * Checks if is pointcut pattern verifiable.
     *
     * @return true, if is pointcut pattern verifiable
     */
    public boolean isPointcutPatternVerifiable() {
        DefaultSettings defaultSettings = assistantLocal.getDefaultSettings();
        return (defaultSettings == null || defaultSettings.isPointcutPatternVerifiable());
    }

    /**
     * Resolve bean class for the aspect rule.
     *
     * @param beanId the bean id
     * @param aspectRule the aspect rule
     */
    public void resolveAdviceBeanClass(String beanId, AspectRule aspectRule) {
        Class<?> beanClass = resolveBeanClass(beanId);
        if (beanClass != null) {
            aspectRule.setAdviceBeanClass(beanClass);
            reserveBeanReference(beanClass, aspectRule);
        } else {
            reserveBeanReference(beanId, aspectRule);
        }
    }

    /**
     * Resolve bean class for bean action rule.
     *
     * @param beanId the bean id
     * @param beanActionRule the bean action rule
     */
    public void resolveActionBeanClass(String beanId, BeanActionRule beanActionRule) {
        Class<?> beanClass = resolveBeanClass(beanId);
        if (beanClass != null) {
            beanActionRule.setBeanClass(beanClass);
            reserveBeanReference(beanClass, beanActionRule);
        } else {
            reserveBeanReference(beanId, beanActionRule);
        }
    }

    /**
     * Resolve bean class for factory bean rule.
     *
     * @param beanId the bean id
     * @param beanRule the bean rule
     */
    public void resolveFactoryBeanClass(String beanId, BeanRule beanRule) {
        Class<?> beanClass = resolveBeanClass(beanId);
        if (beanClass != null) {
            beanRule.setFactoryBeanClass(beanClass);
            reserveBeanReference(beanClass, beanRule);
        } else {
            reserveBeanReference(beanId, beanRule);
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
                Class<?> beanClass = loadClass(token.getValue());
                token.setAlternativeValue(beanClass);
                reserveBeanReference(beanClass, token);
            } else {
                reserveBeanReference(token.getName(), token);
            }
        }
    }

    /**
     * Resolve bean class for the schedule rule.
     *
     * @param beanId the bean id
     * @param scheduleRule the schedule rule
     */
    public void resolveBeanClass(String beanId, ScheduleRule scheduleRule) {
        Class<?> beanClass = resolveBeanClass(beanId);
        if (beanClass != null) {
            scheduleRule.setSchedulerBeanClass(beanClass);
            reserveBeanReference(beanClass, scheduleRule);
        } else {
            reserveBeanReference(beanId, scheduleRule);
        }
    }

    /**
     * Resolve bean class for the template rule.
     *
     * @param beanId the bean id
     * @param templateRule the template rule
     */
    public void resolveBeanClass(String beanId, TemplateRule templateRule) {
        Class<?> beanClass = resolveBeanClass(beanId);
        if (beanClass != null) {
            templateRule.setEngineBeanClass(beanClass);
            reserveBeanReference(beanClass, templateRule);
        } else {
            reserveBeanReference(beanId, templateRule);
        }
    }

    private Class<?> resolveBeanClass(String beanId) {
        if (beanId != null && beanId.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = beanId.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            return loadClass(className);
        } else {
            return null;
        }
    }

    private Class<?> loadClass(String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to load class: " + className, e);
        }
    }

    public void reserveBeanReference(String beanId, BeanReferenceInspectable someRule) {
        beanReferenceInspector.reserve(beanId, someRule);
    }

    public void reserveBeanReference(Class<?> beanClass, BeanReferenceInspectable someRule) {
        beanReferenceInspector.reserve(beanClass, someRule);
    }

    public BeanReferenceInspector getBeanReferenceInspector() {
        return beanReferenceInspector;
    }

    /**
     * Adds the aspect rule.
     *
     * @param aspectRule the aspect rule
     */
    public void addAspectRule(AspectRule aspectRule) {
        aspectRuleRegistry.addAspectRule(aspectRule);
    }

    /**
     * Adds the bean rule.
     *
     * @param beanRule the bean rule
     * @throws ClassNotFoundException the class not found exception
     */
    public void addBeanRule(BeanRule beanRule) throws ClassNotFoundException {
        beanRuleRegistry.addBeanRule(beanRule);
    }

    /**
     * Adds the schedule rule.
     *
     * @param scheduleRule the schedule rule
     */
    public void addScheduleRule(ScheduleRule scheduleRule) {
        scheduleRuleRegistry.addScheduleRule(scheduleRule);
    }

    /**
     * Add the translet rule.
     *
     * @param transletRule the translet rule
     */
    public void addTransletRule(TransletRule transletRule) {
        transletRuleRegistry.addTransletRule(transletRule);
    }

    /**
     * Add the template rule.
     *
     * @param templateRule the template rule
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
     * Gets the bean rule registry.
     *
     * @return the bean rule registry
     */
    public BeanRuleRegistry getBeanRuleRegistry() {
        return beanRuleRegistry;
    }

    /**
     * Gets the schedule rule registry.
     *
     * @return the template rule registry
     */
    public ScheduleRuleRegistry getScheduleRuleRegistry() {
        return scheduleRuleRegistry;
    }

    /**
     * Gets the translet rule registry.
     *
     * @return the translet rule registry
     */
    public TransletRuleRegistry getTransletRuleRegistry() {
        return transletRuleRegistry;
    }

    /**
     * Gets the template rule registry.
     *
     * @return the template rule registry
     */
    public TemplateRuleRegistry getTemplateRuleRegistry() {
        return templateRuleRegistry;
    }

    /**
     * Gets all aspect rules.
     *
     * @return the aspect rules
     */
    public Collection<AspectRule> getAspectRules() {
        return aspectRuleRegistry.getAspectRuleMap().values();
    }

    /**
     * Gets all bean rules.
     *
     * @return the bean rules
     */
    public Collection<BeanRule> getBeanRules() {
        Set<BeanRule> beanRuleSet = new HashSet<BeanRule>();
        beanRuleSet.addAll(beanRuleRegistry.getIdBasedBeanRuleMap().values());
        for (Set<BeanRule> brs : beanRuleRegistry.getTypeBasedBeanRuleMap().values()) {
            beanRuleSet.addAll(brs);
        }
        beanRuleSet.addAll(beanRuleRegistry.getConfigBeanRuleMap().values());
        return beanRuleSet;
    }

    /**
     * Gets all schedule rules.
     *
     * @return the schedule rules
     */
    public Collection<ScheduleRule> getScheduleRules() {
        return scheduleRuleRegistry.getScheduleRuleMap().values();
    }

    /**
     * Gets all translet rules.
     *
     * @return the translet rules
     */
    public Collection<TransletRule> getTransletRules() {
        return transletRuleRegistry.getTransletRuleMap().values();
    }

    /**
     * Gets all template rules.
     *
     * @return the template rules
     */
    public Collection<TemplateRule> getTemplateRules() {
        return templateRuleRegistry.getTemplateRuleMap().values();
    }

    /**
     * Gets the import handler.
     *
     * @return the import handler
     */
    public ImportHandler getImportHandler() {
        return importHandler;
    }

    /**
     * Sets the import handler.
     *
     * @param importHandler the new import handler
     */
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    /**
     * Create a new importer.
     *
     * @param file the file to import
     * @param resource the resource to import
     * @param url the url to import
     * @param format the file type ('xml' or 'apon')
     * @param profile the environment profile name
     * @return an {@code Importer} object
     */
    public Importer newImporter(String file, String resource, String url, String format, String profile) {
        ImporterFileFormatType importerFileFormatType = ImporterFileFormatType.resolve(format);
        Importer importer = null;

        if (StringUtils.hasText(file)) {
            importer = new FileImporter(getBasePath(), file, importerFileFormatType);
        } else if (StringUtils.hasText(resource)) {
            importer = new ResourceImporter(getClassLoader(), resource, importerFileFormatType);
        } else if (StringUtils.hasText(url)) {
            importer = new UrlImporter(url, importerFileFormatType);
        }

        if (importer != null) {
            if (profile != null && !profile.isEmpty()) {
                String[] arr = StringUtils.splitCommaDelimitedString(profile);
                if (arr.length > 0) {
                    importer.setProfiles(arr);
                }
            }
        }

        if (importer == null) {
            throw new IllegalArgumentException("The 'import' element requires either a 'file' or a 'resource' or a 'url' attribute");
        }

        return importer;
    }

}
