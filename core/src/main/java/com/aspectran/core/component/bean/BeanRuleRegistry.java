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
package com.aspectran.core.component.bean;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.bean.aware.ClassLoaderAware;
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
import com.aspectran.utils.Assert;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
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

    private final Map<String, BeanRule> idBasedBeanRuleMap = new LinkedHashMap<>();

    private final Map<Class<?>, Set<BeanRule>> typeBasedBeanRuleMap = new LinkedHashMap<>();

    private final Map<Class<?>, BeanRule> configurableBeanRuleMap = new LinkedHashMap<>();

    private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<>();

    private final Set<BeanRule> postProcessBeanRuleMap = new HashSet<>();

    private final Set<String> importantBeanIdSet = new HashSet<>();

    private final Set<Class<?>> importantBeanTypeSet = new HashSet<>();

    private final ClassLoader classLoader;

    public BeanRuleRegistry(ClassLoader classLoader) {
        this.classLoader = classLoader;

        ignoreDependencyInterface(DisposableBean.class);
        ignoreDependencyInterface(FactoryBean.class);
        ignoreDependencyInterface(InitializableBean.class);
        ignoreDependencyInterface(ActivityContextAware.class);
        ignoreDependencyInterface(ApplicationAdapterAware.class);
        ignoreDependencyInterface(ClassLoaderAware.class);
        ignoreDependencyInterface(CurrentActivityAware.class);
        ignoreDependencyInterface(EnvironmentAware.class);
        ignoreDependencyInterface(java.lang.Cloneable.class);
        ignoreDependencyInterface(java.lang.Comparable.class);
        ignoreDependencyInterface(java.lang.CharSequence.class);
        ignoreDependencyInterface(java.io.Serializable.class);
        ignoreDependencyInterface(java.io.Closeable.class);
    }

    public BeanRule getBeanRule(String id) {
        return idBasedBeanRuleMap.get(id);
    }

    public BeanRule[] getBeanRules(String name) throws ClassNotFoundException {
        Assert.notNull(name, "name must not be null");
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
        Set<BeanRule> list = typeBasedBeanRuleMap.get(type);
        if (list != null && !list.isEmpty()) {
            return list.toArray(new BeanRule[0]);
        } else {
            return null;
        }
    }

    public BeanRule getBeanRuleForConfig(Class<?> type) {
        return configurableBeanRuleMap.get(type);
    }

    public boolean containsBeanRule(String id) {
        return idBasedBeanRuleMap.containsKey(id);
    }

    public boolean containsBeanRule(Class<?> type) {
        return typeBasedBeanRuleMap.containsKey(type);
    }

    public Map<String, BeanRule> getIdBasedBeanRuleMap() {
        return idBasedBeanRuleMap;
    }

    public Map<Class<?>, Set<BeanRule>> getTypeBasedBeanRuleMap() {
        return typeBasedBeanRuleMap;
    }

    public Map<Class<?>, BeanRule> getConfigurableBeanRuleMap() {
        return configurableBeanRuleMap;
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

        logger.info("Auto-scanning of components in specified packages [" +
            StringUtils.joinCommaDelimitedList(basePackages) + "]");

        for (String basePackage : basePackages) {
            BeanClassScanner scanner = new BeanClassScanner(classLoader);
            List<BeanRule> beanRules = new ArrayList<>();
            scanner.scan(basePackage + ".**", (resourceName, targetClass) -> {
                if (targetClass.isAnnotationPresent(Component.class)) {
                    BeanRule beanRule = new BeanRule();
                    beanRule.setBeanClass(targetClass);
                    beanRule.setScopeType(ScopeType.SINGLETON);
                    beanRules.add(beanRule);
                }
            });
            for (BeanRule beanRule : beanRules) {
                saveConfigurableBeanRule(beanRule);
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
            List<BeanRule> scannedBeanRules = new ArrayList<>();
            BeanClassScanner scanner = createBeanClassScanner(beanRule);
            scanner.scan(scanPattern, (resourceName, targetClass) -> {
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
                replicated.setBeanClass(targetClass);
                scannedBeanRules.add(replicated);
            });
            for (BeanRule scannedBeanRule : scannedBeanRules) {
                dissectBeanRule(scannedBeanRule);
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

    private void dissectBeanRule(BeanRule beanRule) throws BeanRuleException {
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
                    saveBeanRuleWithInterfaces(targetBeanClass, beanRule);
                }
            }
            if (logger.isTraceEnabled()) {
                logger.trace("add BeanRule " + beanRule);
            }
        }
    }

    private void saveBeanRule(String beanId, BeanRule beanRule) throws BeanRuleException {
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
        Set<BeanRule> set = typeBasedBeanRuleMap.computeIfAbsent(beanClass, k -> new HashSet<>());
        set.add(beanRule);
    }

    private void saveBeanRuleWithInterfaces(@NonNull Class<?> beanClass, @NonNull BeanRule beanRule)
            throws BeanRuleException {
        if (beanClass.isInterface()) {
            Class<?>[] ifcs = beanClass.getInterfaces();
            for (Class<?> ifc : ifcs) {
                if (!ignoredDependencyInterfaces.contains(ifc) &&
                        ClassUtils.isVisible(ifc, classLoader)) {
                    saveBeanRule(ifc, beanRule);
                }
            }
        } else {
            Class<?> current = beanClass;
            while (current != null) {
                Class<?>[] ifcs = current.getInterfaces();
                for (Class<?> ifc : ifcs) {
                    if (!ignoredDependencyInterfaces.contains(ifc) &&
                            ClassUtils.isVisible(ifc, classLoader)) {
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
            throw new BeanRuleException("No specified bean class", beanRule);
        }
        configurableBeanRuleMap.put(beanRule.getBeanClass(), beanRule);
    }

    public void addInnerBeanRule(BeanRule beanRule) throws BeanRuleException {
        Assert.notNull(beanRule, "beanRule must not be null");
        if (!beanRule.isInnerBean()) {
            throw new BeanRuleException("Not an inner bean", beanRule);
        }
        Class<?> targetBeanClass = BeanRuleAnalyzer.determineBeanClass(beanRule);
        if (targetBeanClass == null) {
            postProcessBeanRuleMap.add(beanRule);
        }
    }

    public void postProcess(ActivityRuleAssistant assistant) throws IllegalRuleException {
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

    private void parseAnnotatedConfig(ActivityRuleAssistant assistant) throws IllegalRuleException {
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

    public void ignoreDependencyInterface(Class<?> ifc) {
        this.ignoredDependencyInterfaces.add(ifc);
    }

}
