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
package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;
import com.aspectran.core.component.bean.aware.EnvironmentAware;
import com.aspectran.core.component.bean.scan.BeanClassFilter;
import com.aspectran.core.component.bean.scan.BeanClassScanner;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.AutowireRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.context.rule.type.ScopeType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central registry of {@link com.aspectran.core.context.rule.BeanRule} definitions.
 * <p>
 * This registry maintains mappings by ID and type, tracks configurable and post-process
 * bean rules, and manages important beans. It supports both annotation-based component
 * scanning (for classes annotated with {@code @Component}) and XML-based bean rule
 * scanning (for bean rules defined with a {@code scan} attribute).
 * Additionally, it provides configuration for base packages to scan and exposes hooks
 * to ignore certain dependency types and interfaces during autowiring.
 * </p>
 * @see com.aspectran.core.context.rule.BeanRule
 * @see BeanClassScanner
 * @see AnnotatedConfigParser
 */
public class BeanRuleRegistry {

    private static final Logger logger = LoggerFactory.getLogger(BeanRuleRegistry.class);

    private final Set<String> basePackages = new HashSet<>();

    private final Set<Class<?>> ignoredDependencyTypes = new HashSet<>();

    private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

    private final Map<String, BeanRule> idBasedBeanRuleMap = new LinkedHashMap<>();

    private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap = new LinkedHashMap<>();

    private final Map<Class<?>, BeanRule> configurableBeanRuleMap = new LinkedHashMap<>();

    private final Set<BeanRule> postProcessBeanRuleMap = new HashSet<>();

    private final Set<String> importantBeanIdSet = new HashSet<>();

    private final Set<Class<?>> importantBeanTypeSet = new HashSet<>();

    private final ClassLoader classLoader;

    /**
     * Instantiates a new Bean rule registry.
     * @param classLoader the class loader
     */
    public BeanRuleRegistry(ClassLoader classLoader) {
        this.classLoader = classLoader;

        ignoreDependencyInterface(FactoryBean.class);
        ignoreDependencyInterface(DisposableBean.class);
        ignoreDependencyInterface(InitializableBean.class);
        ignoreDependencyInterface(InitializableFactoryBean.class);
        ignoreDependencyInterface(ActivityContextAware.class);
        ignoreDependencyInterface(ApplicationAdapterAware.class);
        ignoreDependencyInterface(CurrentActivityAware.class);
        ignoreDependencyInterface(EnvironmentAware.class);
        ignoreDependencyInterface(java.lang.Cloneable.class);
        ignoreDependencyInterface(java.lang.Comparable.class);
        ignoreDependencyInterface(java.lang.CharSequence.class);
        ignoreDependencyInterface(java.lang.constant.Constable.class);
        ignoreDependencyInterface(java.lang.constant.ConstantDesc.class);
        ignoreDependencyInterface(java.io.Serializable.class);
        ignoreDependencyInterface(java.io.Closeable.class);
    }

    /**
     * Returns an unmodifiable set of base packages to scan for beans.
     * @return a set of base package names
     */
    public Set<String> getBasePackages() {
        return Collections.unmodifiableSet(basePackages);
    }

    /**
     * Ignores the given dependency type for autowiring.
     * @param type the dependency type to ignore
     */
    public void ignoreDependencyType(Class<?> type) {
        this.ignoredDependencyTypes.add(type);
    }

    /**
     * Ignores the given dependency interface for autowiring.
     * @param ifc the dependency interface to ignore
     */
    public void ignoreDependencyInterface(Class<?> ifc) {
        this.ignoredDependencyInterfaces.add(ifc);
    }

    /**
     * Returns the bean rule for the given id.
     * @param id the bean id
     * @return the bean rule, or {@code null} if not found
     */
    public BeanRule getBeanRule(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return idBasedBeanRuleMap.get(id);
    }

    /**
     * Returns an array of bean rules for the given name.
     * The name can be a bean ID or a class name with the prefix "class:".
     * @param name the bean id or class name
     * @return an array of bean rules, or {@code null} if not found
     * @throws ClassNotFoundException if the class specified in the name is not found
     */
    public BeanRule[] getBeanRules(String name) throws ClassNotFoundException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (name.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = name.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            Class<?> type = classLoader.loadClass(className);
            return getBeanRules(type);
        } else {
            BeanRule beanRule = getBeanRule(name);
            if (beanRule != null) {
                return new BeanRule[] {getBeanRule(name)};
            } else {
                return null;
            }
        }
    }

    /**
     * Returns an array of bean rules for the given type.
     * @param type the bean type
     * @return an array of bean rules, or {@code null} if not found
     */
    public BeanRule[] getBeanRules(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        Set<BeanRule> beanRules = typeBasedBeanRuleMap.get(type);
        if (beanRules != null && !beanRules.isEmpty()) {
            return beanRules.toArray(new BeanRule[0]);
        } else {
            return null;
        }
    }

    /**
     * Returns the configurable bean rule for the given type.
     * @param type the bean type
     * @return the configurable bean rule, or {@code null} if not found
     */
    public BeanRule getBeanRuleForConfig(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return configurableBeanRuleMap.get(type);
    }

    /**
     * Checks if a bean rule with the given id is registered.
     * @param id the bean id
     * @return {@code true} if a bean rule with the given id is registered, {@code false} otherwise
     */
    public boolean containsBeanRule(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return idBasedBeanRuleMap.containsKey(id);
    }

    /**
     * Checks if a bean rule with the given type is registered.
     * @param type the bean type
     * @return {@code true} if a bean rule with the given type is registered, {@code false} otherwise
     */
    public boolean containsBeanRule(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return typeBasedBeanRuleMap.containsKey(type);
    }

    /**
     * Returns a collection of all id-based bean rules.
     * @return a collection of bean rules
     */
    public Collection<BeanRule> getIdBasedBeanRules() {
        return idBasedBeanRuleMap.values();
    }

    /**
     * Returns a collection of all type-based bean rule sets.
     * @return a collection of bean rule sets
     */
    public Collection<Set<BeanRule>> getTypeBasedBeanRules() {
        return typeBasedBeanRuleMap.values();
    }

    /**
     * Returns a collection of all configurable bean rules.
     * @return a collection of configurable bean rules
     */
    public Collection<BeanRule> getConfigurableBeanRules() {
        return configurableBeanRuleMap.values();
    }

    /**
     * Finds all configurable bean classes that are annotated with the given annotation type.
     * @param annotationType the annotation type to look for
     * @return a collection of bean classes
     */
    public Collection<Class<?>> findConfigBeanClassesWithAnnotation(Class<? extends Annotation> annotationType) {
        if (annotationType == null) {
            throw new IllegalArgumentException("annotationType must not be null");
        }
        List<Class<?>> result = new ArrayList<>();
        for (BeanRule beanRule : configurableBeanRuleMap.values()) {
            Class<?> targetBeanClass = beanRule.getTargetBeanClass();
            if (targetBeanClass.isAnnotationPresent(annotationType)) {
                result.add(targetBeanClass);
            }
        }
        return result;
    }

    /**
     * Scans the given base packages for configurable beans annotated with {@link Component}.
     * @param basePackages the base packages to scan
     * @throws BeanRuleException if an error occurs during bean rule creation
     */
    public void scanConfigurableBeans(String... basePackages) throws BeanRuleException {
        if (basePackages == null || basePackages.length == 0) {
            return;
        }

        ToStringBuilder tsb = new ToStringBuilder("Auto Components Scanning");
        tsb.append("basePackages", basePackages);
        logger.info(tsb.toString());

        for (String basePackage : basePackages) {
            this.basePackages.add(basePackage);

            final Set<Class<?>> beanClasses = new HashSet<>();
            BeanClassScanner scanner = new BeanClassScanner(classLoader);
            scanner.scan(basePackage + ".**", (resourceName, targetClass) -> {
                if (targetClass.isAnnotationPresent(Component.class)) {
                    beanClasses.add(targetClass);
                }
            });
            for (Class<?> beanClass : beanClasses) {
                BeanRule beanRule = new BeanRule();
                beanRule.setBeanClass(beanClass);
                beanRule.setScopeType(ScopeType.SINGLETON);

                saveConfigurableBeanRule(beanRule);
                saveBeanRuleWithInterfaces(beanRule.getBeanClass(), beanRule);
            }
        }
    }

    /**
     * Adds a bean rule to the registry.
     * If the rule specifies a scan pattern, it will be expanded into multiple bean rules.
     * @param beanRule the bean rule to add
     * @throws IllegalRuleException if the bean rule is invalid
     */
    public void addBeanRule(final BeanRule beanRule) throws IllegalRuleException {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        String scanPattern = beanRule.getScanPattern();
        if (scanPattern != null) {
            PrefixSuffixPattern prefixSuffixPattern;
            try {
                prefixSuffixPattern = PrefixSuffixPattern.of(beanRule.getId());
            } catch (IllegalArgumentException e) {
                throw new IllegalRuleException("Invalid bean ID pattern \"" + beanRule.getId() + "\"", e);
            }
            final Map<Class<?>, String> beanClasses = new HashMap<>();
            BeanClassScanner scanner = createBeanClassScanner(beanRule);
            scanner.scan(scanPattern, (resourceName, targetClass) -> {
                beanClasses.putIfAbsent(targetClass, resourceName);
            });
            for (Map.Entry<Class<?>, String> entry : beanClasses.entrySet()) {
                Class<?> beanClass = entry.getKey();
                String resourceName = entry.getValue();

                BeanRule replicated = beanRule.replicate();
                if (prefixSuffixPattern != null) {
                    replicated.setId(prefixSuffixPattern.enclose(resourceName));
                } else {
                    if (beanRule.getId() != null) {
                        replicated.setId(beanRule.getId() + resourceName);
                    } else if (beanRule.getMaskPattern() != null) {
                        replicated.setId(resourceName);
                    }
                }
                replicated.setBeanClass(beanClass);

                dissectBeanRule(replicated);
            }
        } else {
            dissectBeanRule(beanRule);
        }
    }

    @NonNull
    private BeanClassScanner createBeanClassScanner(@NonNull BeanRule beanRule) throws IllegalRuleException {
        BeanClassScanner scanner = new BeanClassScanner(classLoader);
        if (beanRule.getFilterParameters() != null) {
            FilterParameters filterParameters = beanRule.getFilterParameters();
            String filterClassName = filterParameters.getString(FilterParameters.filterClass);
            if (filterClassName != null) {
                BeanClassFilter beanClassFilter;
                try {
                    Class<?> filterClass = classLoader.loadClass(filterClassName);
                    beanClassFilter = (BeanClassFilter)ClassUtils.createInstance(filterClass);
                } catch (Exception e) {
                    throw new IllegalRuleException("Failed to instantiate BeanClassFilter [" +
                            filterClassName + "]", e);
                }
                scanner.setBeanClassFilter(beanClassFilter);
            }
            IncludeExcludeWildcardPatterns filterPatterns = IncludeExcludeWildcardPatterns.of(
                    filterParameters, ClassUtils.PACKAGE_SEPARATOR_CHAR);
            if (filterPatterns.hasIncludePatterns()) {
                scanner.setFilterPatterns(filterPatterns);
            }
        }
        if (beanRule.getMaskPattern() != null) {
            scanner.setBeanIdMaskPattern(beanRule.getMaskPattern());
        }
        return scanner;
    }

    private void dissectBeanRule(@NonNull BeanRule beanRule) throws BeanRuleException {
        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        if (targetBeanClass == null) {
            postProcessBeanRuleMap.add(beanRule);
        } else {
            if (beanRule.getId() != null) {
                saveBeanRule(beanRule.getId(), beanRule);
            }
            if (!beanRule.isFactoryOffered()) {
                if (targetBeanClass.isAnnotationPresent(Component.class)) {
                    saveConfigurableBeanRule(beanRule);
                } else {
                    saveBeanRule(targetBeanClass, beanRule);
                }
                saveBeanRuleWithInterfaces(targetBeanClass, beanRule);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("add BeanRule {}", beanRule);
            }
        }
    }

    private void saveBeanRule(@NonNull String beanId, @NonNull BeanRule beanRule) throws BeanRuleException {
        if (importantBeanIdSet.contains(beanId)) {
            throw new BeanRuleException("Already exists an ID-based bean that can not be overridden; Duplicated bean",
                    beanRule);
        }
        if (beanRule.isImportant()) {
            importantBeanIdSet.add(beanRule.getId());
        }
        idBasedBeanRuleMap.put(beanId, beanRule);
    }

    private void saveBeanRule(@NonNull Class<?> beanClass, @NonNull BeanRule beanRule) throws BeanRuleException {
        if (beanRule.getId() == null) {
            if (importantBeanTypeSet.contains(beanClass)) {
                throw new BeanRuleException("Already exists a type-based bean that can not be overridden; Duplicated bean",
                        beanRule);
            }
            if (beanRule.isImportant()) {
                importantBeanTypeSet.add(beanClass);
            }
        }
        if (beanRule.getId() == null || !ignoredDependencyTypes.contains(beanClass)) {
            Set<BeanRule> set = typeBasedBeanRuleMap.computeIfAbsent(beanClass, k -> new HashSet<>());
            set.add(beanRule);
        }
    }

    private void saveBeanRuleWithInterfaces(@NonNull Class<?> beanClass, @NonNull BeanRule beanRule)
            throws BeanRuleException {
        if (beanClass.isInterface()) {
            Class<?>[] ifcs = beanClass.getInterfaces();
            for (Class<?> ifc : ifcs) {
                if (!ignoredDependencyInterfaces.contains(ifc) && ClassUtils.isVisible(ifc, classLoader)) {
                    saveBeanRule(ifc, beanRule);
                }
            }
        } else {
            Class<?> current = beanClass;
            while (current != null) {
                Class<?>[] ifcs = current.getInterfaces();
                for (Class<?> ifc : ifcs) {
                    if (!ignoredDependencyInterfaces.contains(ifc) && ClassUtils.isVisible(ifc, classLoader)) {
                        saveBeanRule(ifc, beanRule);
                        saveBeanRuleWithInterfaces(ifc, beanRule);
                    }
                }
                current = current.getSuperclass();
            }
        }
    }

    private void saveConfigurableBeanRule(@NonNull BeanRule beanRule) throws BeanRuleException {
        if (beanRule.getBeanClass() == null) {
            throw new BeanRuleException("No bean class for", beanRule);
        }
        configurableBeanRuleMap.put(beanRule.getBeanClass(), beanRule);
    }

    /**
     * Adds an inner bean rule to the registry.
     * @param beanRule the inner bean rule to add
     * @throws BeanRuleException if the bean rule is not an inner bean or is invalid
     */
    public void addInnerBeanRule(BeanRule beanRule) throws BeanRuleException {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule cannot be null");
        }
        if (!beanRule.isInnerBean()) {
            throw new BeanRuleException("Not inner bean", beanRule);
        }
        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        if (targetBeanClass == null) {
            postProcessBeanRuleMap.add(beanRule);
        }
    }

    /**
     * Performs post-processing of bean rules.
     * This includes resolving factory-offered beans and parsing annotated configurations.
     * @param assistant the activity rule assistant
     * @throws IllegalRuleException if an error occurs during post-processing
     */
    public void postProcess(ActivityRuleAssistant assistant) throws IllegalRuleException {
        if (assistant == null) {
            throw new IllegalArgumentException("assistant cannot be null");
        }
        if (!postProcessBeanRuleMap.isEmpty()) {
            for (BeanRule beanRule : postProcessBeanRuleMap) {
                if (!beanRule.isInnerBean() && beanRule.getId() != null) {
                    saveBeanRule(beanRule.getId(), beanRule);
                }
                if (beanRule.isFactoryOffered()) {
                    Class<?> offeredFactoryBeanClass = resolveOfferedFactoryBeanClass(beanRule);
                    Class<?> targetBeanClass = BeanRuleAnalyzer.determineFactoryMethodTargetBeanClass(
                            offeredFactoryBeanClass, beanRule);
                    if (beanRule.getInitMethodName() != null) {
                        BeanRuleAnalyzer.determineInitMethod(targetBeanClass, beanRule);
                    }
                    if (beanRule.getDestroyMethodName() != null) {
                        BeanRuleAnalyzer.determineDestroyMethod(targetBeanClass, beanRule);
                    }
                    if (!beanRule.isInnerBean()) {
                        saveBeanRule(targetBeanClass, beanRule);
                        saveBeanRuleWithInterfaces(targetBeanClass, beanRule);
                    }
                }
            }
            postProcessBeanRuleMap.clear();
        }
        importantBeanIdSet.clear();
        importantBeanTypeSet.clear();
        parseAnnotatedConfig(assistant);
    }

    private void parseAnnotatedConfig(@NonNull ActivityRuleAssistant assistant) throws IllegalRuleException {
        AnnotatedConfigRelater relater = new AnnotatedConfigRelater() {
            @Override
            public void relate(Class<?> targetBeanClass, @NonNull BeanRule beanRule) throws IllegalRuleException {
                if (beanRule.getId() != null) {
                    saveBeanRule(beanRule.getId(), beanRule);
                }
                saveBeanRule(targetBeanClass, beanRule);
                saveBeanRuleWithInterfaces(targetBeanClass, beanRule);
            }

            @Override
            public void relate(AspectRule aspectRule) throws IllegalRuleException {
                assistant.addAspectRule(aspectRule);
            }

            @Override
            public void relate(ScheduleRule scheduleRule) throws IllegalRuleException {
                assistant.addScheduleRule(scheduleRule);
            }

            @Override
            public void relate(TransletRule transletRule) throws IllegalRuleException {
                assistant.addTransletRule(transletRule);
            }

            @Override
            public void relate(AutowireRule autowireRule) throws IllegalRuleException {
                assistant.resolveBeanClass(autowireRule);
            }
        };

        AnnotatedConfigParser parser = new AnnotatedConfigParser(assistant, relater);
        parser.parse();
    }

    private Class<?> resolveOfferedFactoryBeanClass(@NonNull BeanRule beanRule) throws BeanRuleException {
        BeanRule offeredFactoryBeanRule;
        if (beanRule.getFactoryBeanClass() == null) {
            offeredFactoryBeanRule = getBeanRule(beanRule.getFactoryBeanId());
            if (offeredFactoryBeanRule == null) {
                throw new BeanRuleException("No factory bean named '" + beanRule.getFactoryBeanId() +
                        "' is defined; Caller bean ", beanRule);
            }
        } else {
            BeanRule[] beanRules = getBeanRules(beanRule.getFactoryBeanClass());
            if (beanRules == null || beanRules.length == 0) {
                throw new BeanRuleException("No matching factory bean of type '" +
                        beanRule.getFactoryBeanClass().getName() + "' found", beanRule);
            }
            if (beanRules.length > 1) {
                throw new BeanRuleException("No unique factory bean of type '" +
                        beanRule.getFactoryBeanClass().getName() +
                        "' is defined: expected single matching bean but found " +
                        beanRules.length + ": (" +
                        NoUniqueBeanException.getBeanDescriptions(beanRules) + "); Caller bean ",
                        beanRule);
            }
            offeredFactoryBeanRule = beanRules[0];
        }
        if (offeredFactoryBeanRule.isFactoryOffered()) {
            throw new BeanRuleException("An offered factory bean can not call " +
                    "another offered factory bean; Caller bean ", beanRule);
        }
        return offeredFactoryBeanRule.getTargetBeanClass();
    }

}
