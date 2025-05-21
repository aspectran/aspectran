/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * The Class BeanRuleRegistry.
 *
 * @since 2.0.0
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

    public Set<String> getBasePackages() {
        return Collections.unmodifiableSet(basePackages);
    }

    public void ignoreDependencyType(Class<?> type) {
        this.ignoredDependencyTypes.add(type);
    }

    public void ignoreDependencyInterface(Class<?> ifc) {
        this.ignoredDependencyInterfaces.add(ifc);
    }

    public BeanRule getBeanRule(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return idBasedBeanRuleMap.get(id);
    }

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

    public BeanRule getBeanRuleForConfig(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return configurableBeanRuleMap.get(type);
    }

    public boolean containsBeanRule(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return idBasedBeanRuleMap.containsKey(id);
    }

    public boolean containsBeanRule(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        return typeBasedBeanRuleMap.containsKey(type);
    }

    public Collection<BeanRule> getIdBasedBeanRules() {
        return idBasedBeanRuleMap.values();
    }

    public Collection<Set<BeanRule>> getTypeBasedBeanRules() {
        return typeBasedBeanRuleMap.values();
    }

    public Collection<BeanRule> getConfigurableBeanRules() {
        return configurableBeanRuleMap.values();
    }

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
     * Scans for annotated components.
     * @param basePackages the base packages to scan for annotated components
     * @throws BeanRuleException if an illegal bean rule is found
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
     * Adds a bean rule.
     * @param beanRule the bean rule to add
     * @throws IllegalRuleException if an error occurs while adding a bean rule
     */
    public void addBeanRule(final BeanRule beanRule) throws IllegalRuleException {
        if (beanRule == null) {
            throw new IllegalArgumentException("beanRule must not be null");
        }
        String scanPattern = beanRule.getScanPattern();
        if (scanPattern != null) {
            PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.of(beanRule.getId());
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
            String beanClassFilterClassName = filterParameters.getString(FilterParameters.filterClass);
            if (beanClassFilterClassName != null) {
                BeanClassFilter beanClassFilter;
                try {
                    Class<?> filterClass = classLoader.loadClass(beanClassFilterClassName);
                    beanClassFilter = (BeanClassFilter)ClassUtils.createInstance(filterClass);
                } catch (Exception e) {
                    throw new IllegalRuleException("Failed to instantiate BeanClassFilter [" +
                            beanClassFilterClassName + "]", e);
                }
                scanner.setBeanClassFilter(beanClassFilter);
            }
            String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
            if (excludePatterns != null) {
                scanner.setExcludePatterns(excludePatterns);
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
