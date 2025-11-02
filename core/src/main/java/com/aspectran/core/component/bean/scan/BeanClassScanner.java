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
package com.aspectran.core.component.bean.scan;

import com.aspectran.utils.ClassScanner;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

import static com.aspectran.core.context.ActivityContext.ID_SEPARATOR_CHAR;

/**
 * Scanner that discovers candidate bean classes on the classpath.
 * <p>
 * Supports include/exclude wildcard patterns, id masking, and a
 * pluggable {@link BeanClassFilter} to customize which classes are
 * registered with the bean registry.
 * </p>
 */
public class BeanClassScanner extends ClassScanner {

    private static final Logger logger = LoggerFactory.getLogger(BeanClassScanner.class);

    private BeanClassFilter beanClassFilter;

    private WildcardPattern beanIdMaskPattern;

    private IncludeExcludeWildcardPatterns filterPatterns;

    /**
     * Instantiates a new Bean class scanner.
     * @param classLoader the class loader to use
     */
    public BeanClassScanner(ClassLoader classLoader) {
        super(classLoader);
    }

    /**
     * Sets the bean class filter.
     * @param beanClassFilter the bean class filter
     */
    public void setBeanClassFilter(BeanClassFilter beanClassFilter) {
        this.beanClassFilter = beanClassFilter;
    }

    /**
     * Sets patterns for classes to exclude.
     */
    public void setFilterPatterns(IncludeExcludeWildcardPatterns filterPatterns) {
        this.filterPatterns = filterPatterns;
    }

    /**
     * Sets the bean id mask pattern.
     * @param beanIdMaskPattern the bean id mask pattern
     */
    public void setBeanIdMaskPattern(String beanIdMaskPattern) {
        if (beanIdMaskPattern == null) {
            throw new IllegalArgumentException("beanIdMaskPattern must not be null");
        }
        this.beanIdMaskPattern = new WildcardPattern(beanIdMaskPattern, ID_SEPARATOR_CHAR);
    }

    @Override
    public void scan(String classNamePattern, SaveHandler saveHandler) {
        try {
            super.scan(classNamePattern, new BeanSaveHandler(saveHandler));
        } catch (Exception e) {
            throw new BeanClassScanException("Failed to scan bean classes with given pattern: " +
                    classNamePattern, e);
        }
    }

    private class BeanSaveHandler implements SaveHandler {

        private final SaveHandler saveHandler;

        /**
         * Instantiates a new Bean save handler.
         * @param saveHandler the save handler
         */
        public BeanSaveHandler(SaveHandler saveHandler) {
            this.saveHandler = saveHandler;
        }

        @Override
        public void save(@NonNull String resourceName, @NonNull Class<?> targetClass) {
            if (!Modifier.isPublic(targetClass.getModifiers()) ||
                    (!Modifier.isInterface(targetClass.getModifiers()) &&
                            Modifier.isAbstract(targetClass.getModifiers()))) {
                return;
            }

            String className = targetClass.getName();
            String beanId = className;

            if (beanIdMaskPattern != null) {
                String maskedBeanId = beanIdMaskPattern.mask(beanId);
                if (maskedBeanId != null) {
                    beanId = maskedBeanId;
                } else {
                    logger.warn("Bean name [{}] can not be masked by mask pattern [{}]", beanId, beanIdMaskPattern);
                }
            }

            if (beanClassFilter != null) {
                beanId = beanClassFilter.filter(beanId, resourceName, targetClass);
                if (beanId == null) {
                    return;
                }
            }

            if (filterPatterns != null && filterPatterns.matches(className)) {
                return;
            }

            saveHandler.save(beanId, targetClass);

            if (logger.isTraceEnabled()) {
                logger.trace("Scanned bean {id={}, class={}}", beanId, className);
            }
        }

    }

}
