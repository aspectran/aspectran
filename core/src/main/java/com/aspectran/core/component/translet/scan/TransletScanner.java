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
package com.aspectran.core.component.translet.scan;

import com.aspectran.utils.FileScanner;
import com.aspectran.utils.wildcard.WildcardMatcher;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR_CHAR;
import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * The Class TransletScanner.
 *
 * @since 2.0.0
 */
public class TransletScanner extends FileScanner {

    private static final Logger logger = LoggerFactory.getLogger(TransletScanner.class);

    private TransletScanFilter transletScanFilter;

    private WildcardPattern transletNameMaskPattern;

    private WildcardPattern[] excludePatterns;

    public TransletScanner(String basePath) {
        super(basePath);
    }

    public void setTransletScanFilter(TransletScanFilter transletScanFilter) {
        this.transletScanFilter = transletScanFilter;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        if (excludePatterns != null && excludePatterns.length > 0) {
            this.excludePatterns = new WildcardPattern[excludePatterns.length];
            for (int i = 0; i < excludePatterns.length; i++) {
                WildcardPattern pattern = new WildcardPattern(excludePatterns[i], REGULAR_FILE_SEPARATOR_CHAR);
                this.excludePatterns[i] = pattern;
            }
        } else {
            this.excludePatterns = null;
        }
    }

    public void setTransletNameMaskPattern(String transletNameMaskPattern) {
        if (transletNameMaskPattern == null) {
            throw new IllegalArgumentException("transletNameMaskPattern must not be null");
        }
        this.transletNameMaskPattern = new WildcardPattern(transletNameMaskPattern, NAME_SEPARATOR_CHAR);
    }

    @Override
    protected void scan(String targetPath, WildcardMatcher matcher, SaveHandler saveHandler) {
        try {
            super.scan(targetPath, matcher, new TransletSaveHandler(saveHandler));
        } catch (Exception e) {
            throw new TransletScanFailedException("Failed to scan translets from given path: " +
                    targetPath, e);
        }
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
                    logger.warn("Translet name [{}] can not be masked by mask pattern [{}]",
                            transletName, transletNameMaskPattern);
                }
            }

            if (transletScanFilter != null) {
                boolean passing = transletScanFilter.filter(transletName, scannedFile);
                if (!passing) {
                    return;
                }
            }

            if (excludePatterns != null) {
                for (WildcardPattern pattern : excludePatterns) {
                    if (pattern.matches(filePath)) {
                        return;
                    }
                }
            }

            saveHandler.save(transletName, scannedFile);

            if (logger.isTraceEnabled()) {
                logger.trace("Scanned file: {}", filePath);
            }
        }
    }

}
