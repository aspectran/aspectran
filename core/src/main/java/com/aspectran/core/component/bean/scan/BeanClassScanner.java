/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import com.aspectran.core.util.ClassScanner;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.lang.reflect.Modifier;

import static com.aspectran.core.context.ActivityContext.ID_SEPARATOR_CHAR;
import static com.aspectran.core.util.ClassUtils.PACKAGE_SEPARATOR_CHAR;

/**
 * The Class BeanClassScanner.
 */
public class BeanClassScanner extends ClassScanner {

    private static final Log log = LogFactory.getLog(BeanClassScanner.class);

    private BeanClassScanFilter beanClassScanFilter;

    private WildcardPattern beanIdMaskPattern;

    private WildcardPattern[] excludePatterns;

    public BeanClassScanner(ClassLoader classLoader) {
        super(classLoader);
    }

    public void setBeanClassScanFilter(BeanClassScanFilter beanClassScanFilter) {
        this.beanClassScanFilter = beanClassScanFilter;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        if (excludePatterns != null && excludePatterns.length > 0) {
            this.excludePatterns = new WildcardPattern[excludePatterns.length];
            for (int i = 0; i < excludePatterns.length; i++) {
                WildcardPattern pattern = new WildcardPattern(excludePatterns[i], PACKAGE_SEPARATOR_CHAR);
                this.excludePatterns[i] = pattern;
            }
        } else {
            this.excludePatterns = null;
        }
    }

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
            throw new BeanClassScanFailedException("Failed to scan bean classes with given pattern: " +
                    classNamePattern, e);
        }
    }

    private class BeanSaveHandler implements SaveHandler {

        private SaveHandler saveHandler;

        public BeanSaveHandler(SaveHandler saveHandler) {
            this.saveHandler = saveHandler;
        }

        @Override
        public void save(String resourceName, Class<?> targetClass) {
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
                    log.warn("Bean name [" + beanId + "] can not be masked by mask pattern [" +
                            beanIdMaskPattern + "]");
                }
            }

            if (beanClassScanFilter != null) {
                beanId = beanClassScanFilter.filter(beanId, resourceName, targetClass);
                if (beanId == null) {
                    return;
                }
            }

            if (excludePatterns != null) {
                for (WildcardPattern pattern : excludePatterns) {
                    if (pattern.matches(className)) {
                        return;
                    }
                }
            }

            saveHandler.save(beanId, targetClass);

            if (log.isTraceEnabled()) {
                log.trace(String.format("Scanned bean {id=%s, class=%s}", beanId, className));
            }
        }
    }

}
