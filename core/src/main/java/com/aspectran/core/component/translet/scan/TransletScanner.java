/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.component.translet.scan;

import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.FileScanner;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.wildcard.WildcardMatcher;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR_CHAR;
import static com.aspectran.core.util.ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * The Class TransletScanner.
 * 
 * @since 2.0.0
 */
public class TransletScanner extends FileScanner {

    private static final Log log = LogFactory.getLog(TransletScanner.class);

    private final Map<String, WildcardPattern> excludePatternCache = new HashMap<>();

    private final ClassLoader classLoader;

    private FilterParameters filterParameters;

    private TransletScanFilter transletScanFilter;

    private WildcardPattern transletNameMaskPattern;

    public TransletScanner(String basePath, ClassLoader classLoader) {
        super(basePath);
        this.classLoader = classLoader;
    }

    public FilterParameters getFilterParameters() {
        return filterParameters;
    }

    public void setFilterParameters(FilterParameters filterParameters) {
        if (filterParameters == null) {
            throw new IllegalArgumentException("filterParameters must not be null");
        }

        this.filterParameters = filterParameters;

        String transletScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
        if (transletScanFilterClassName != null) {
            setTransletScanFilter(transletScanFilterClassName);
        }
    }

    public TransletScanFilter getTransletScanFilter() {
        return transletScanFilter;
    }

    public void setTransletScanFilter(TransletScanFilter transletScanFilter) {
        this.transletScanFilter = transletScanFilter;
    }

    public void setTransletScanFilter(String transletScanFilterClassName) {
        if (transletScanFilterClassName == null) {
            throw new IllegalArgumentException("transletScanFilterClassName must not be null");
        }
        Class<?> filterClass;
        try {
            filterClass = classLoader.loadClass(transletScanFilterClassName);
        } catch (ClassNotFoundException e) {
            throw new TransletScanFailedException("Failed to instantiate TransletScanFilter [" +
                    transletScanFilterClassName + "]", e);
        }
        setTransletScanFilter(filterClass);
    }

    public void setTransletScanFilter(Class<?> templateFileScanFilterClass) {
        if (templateFileScanFilterClass == null) {
            throw new IllegalArgumentException("templateFileScanFilterClass must not be null");
        }
        try {
            transletScanFilter = (TransletScanFilter)ClassUtils.createInstance(templateFileScanFilterClass);
        } catch (Exception e) {
            throw new TransletScanFailedException("Failed to instantiate TemplateFileScanFilter ["
                    + templateFileScanFilterClass + "]", e);
        }
    }

    public WildcardPattern getTransletNameMaskPattern() {
        return transletNameMaskPattern;
    }

    public void setTransletNameMaskPattern(WildcardPattern transletNameMaskPattern) {
        this.transletNameMaskPattern = transletNameMaskPattern;
    }

    public void setTransletNameMaskPattern(String transletNameMaskPattern) {
        this.transletNameMaskPattern = new WildcardPattern(transletNameMaskPattern, NAME_SEPARATOR_CHAR);
    }

    @Override
    protected void scan(final String targetPath, final WildcardMatcher matcher, final SaveHandler saveHandler) {
        super.scan(targetPath, matcher, new TransletSaveHandler(saveHandler));
    }

    private class TransletSaveHandler implements SaveHandler {

        private final SaveHandler saveHandler;

        private TransletSaveHandler(SaveHandler saveHandler) {
            this.saveHandler = saveHandler;
        }

        @Override
        public void save(String filePath, File scannedFile) {
            String transletName = filePath;

            if (transletNameMaskPattern != null) {
                String maskedTransletName = transletNameMaskPattern.mask(transletName);
                if (maskedTransletName != null) {
                    transletName = maskedTransletName;
                }  else {
                    log.warn("Translet name [" + transletName + "] can not be masked by mask pattern [" +
                            transletNameMaskPattern + "]");
                }
            }

            if (transletScanFilter != null) {
                boolean pass = transletScanFilter.filter(transletName, scannedFile);
                if (!pass) {
                    return;
                }
            }

            if (filterParameters != null) {
                String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
                if (excludePatterns != null) {
                    for (String excludePattern : excludePatterns) {
                        WildcardPattern pattern = excludePatternCache.get(excludePattern);
                        if (pattern == null) {
                            pattern = new WildcardPattern(excludePattern, REGULAR_FILE_SEPARATOR_CHAR);
                            excludePatternCache.put(excludePattern, pattern);
                        }
                        if (pattern.matches(filePath)) {
                            return;
                        }
                    }
                }
            }

            saveHandler.save(transletName, scannedFile);

            if (log.isTraceEnabled()) {
                log.trace("Scanned template file: " + filePath);
            }
        }
    }

}