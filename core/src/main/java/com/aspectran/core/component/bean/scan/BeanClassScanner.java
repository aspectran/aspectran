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
package com.aspectran.core.component.bean.scan;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.util.ClassScanner;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class BeanClassScanner.
 */
public class BeanClassScanner extends ClassScanner {

    private final Log log = LogFactory.getLog(BeanClassScanner.class);

    private Parameters filterParameters;

    private BeanClassScanFilter beanClassScanFilter;

    private WildcardPattern beanIdMaskPattern;

    private Map<String, WildcardPattern> excludePatternCache = new HashMap<>();

    public BeanClassScanner(ClassLoader classLoader) {
        super(classLoader);
    }

    public Parameters getFilterParameters() {
        return filterParameters;
    }

    public void setFilterParameters(Parameters filterParameters) {
        this.filterParameters = filterParameters;

        String classScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
        if (classScanFilterClassName != null) {
            setBeanClassScanFilter(classScanFilterClassName);
        }
    }

    public BeanClassScanFilter getBeanClassScanFilter() {
        return beanClassScanFilter;
    }

    public void setBeanClassScanFilter(BeanClassScanFilter beanClassScanFilter) {
        this.beanClassScanFilter = beanClassScanFilter;
    }

    public void setBeanClassScanFilter(Class<?> beanClassScanFilterClass) {
        try {
            beanClassScanFilter = (BeanClassScanFilter)ClassUtils.createInstance(beanClassScanFilterClass);
        } catch (Exception e) {
            throw new BeanClassScanFailedException("Failed to instantiate BeanClassScanFilter [" + beanClassScanFilterClass + "]", e);
        }
    }

    public WildcardPattern getBeanIdMaskPattern() {
        return beanIdMaskPattern;
    }

    public void setBeanIdMaskPattern(WildcardPattern beanIdMaskPattern) {
        this.beanIdMaskPattern = beanIdMaskPattern;
    }

    public void setBeanIdMaskPattern(String beanIdMaskPattern) {
        this.beanIdMaskPattern = new WildcardPattern(beanIdMaskPattern, ActivityContext.ID_SEPARATOR_CHAR);
    }

    public void setBeanClassScanFilter(String classScanFilterClassName) {
        Class<?> filterClass;
        try {
            filterClass = getClassLoader().loadClass(classScanFilterClassName);
        } catch (ClassNotFoundException e) {
            throw new BeanClassScanFailedException("Failed to instantiate BeanClassScanFilter [" + classScanFilterClassName + "]", e);
        }
        setBeanClassScanFilter(filterClass);
    }

    @Override
    public void scan(String classNamePattern, SaveHandler saveHandler) throws IOException {
        super.scan(classNamePattern, new InnerSaveHandler(saveHandler));
    }

    private class InnerSaveHandler implements SaveHandler {

        private SaveHandler saveHandler;

        public InnerSaveHandler(SaveHandler saveHandler) {
            this.saveHandler = saveHandler;
        }

        @Override
        public void save(String resourceName, Class<?> scannedClass) {
            if (scannedClass.isInterface()
                    || Modifier.isAbstract(scannedClass.getModifiers())
                    || !Modifier.isPublic(scannedClass.getModifiers())) {
                return;
            }

            String className = scannedClass.getName();
            String beanId = className;

            if (beanIdMaskPattern != null) {
                String maskedBeanId = beanIdMaskPattern.mask(beanId);
                if (maskedBeanId != null) {
                    beanId = maskedBeanId;
                } else {
                    log.warn(String.format("Unmatched pattern can not be masking. beanId: %s (maskPattern: %s)",
                            beanId, beanIdMaskPattern));
                }
            }

            if (beanClassScanFilter != null) {
                beanId = beanClassScanFilter.filter(beanId, resourceName, scannedClass);
                if (beanId == null) {
                    return;
                }
            }

            if (filterParameters != null) {
                String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
                if (excludePatterns != null) {
                    for (String excludePattern : excludePatterns) {
                        WildcardPattern pattern = excludePatternCache.get(excludePattern);
                        if (pattern == null) {
                            pattern = new WildcardPattern(excludePattern, ClassUtils.PACKAGE_SEPARATOR_CHAR);
                            excludePatternCache.put(excludePattern, pattern);
                        }
                        if (pattern.matches(className)) {
                            return;
                        }
                    }
                }
            }

            saveHandler.save(beanId, scannedClass);

            if (log.isTraceEnabled()) {
                log.trace(String.format("scanned bean class {beanId: %s, className: %s}", beanId, className));
            }
        }
    }

}
