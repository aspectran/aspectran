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
import com.aspectran.utils.wildcard.IncludeExcludeWildcardPatterns;
import com.aspectran.utils.wildcard.WildcardMatcher;
import com.aspectran.utils.wildcard.WildcardPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR_CHAR;

/**
 * A specialized {@link FileScanner} for discovering files that will be used to
 * dynamically create {@link com.aspectran.core.context.rule.TransletRule}s.
 * <p>This scanner applies various filtering and name-mangling rules before passing
 * the discovered files to a handler.</p>
 *
 * @since 2.0.0
 */
public class TransletScanner extends FileScanner {

    private static final Logger logger = LoggerFactory.getLogger(TransletScanner.class);

    private TransletScanFilter transletScanFilter;

    private WildcardPattern transletNameMaskPattern;

    private IncludeExcludeWildcardPatterns filterPatterns;

    /**
     * Constructs a new TransletScanner with a specified base path.
     * @param basePath the base directory for the scan
     */
    public TransletScanner(String basePath) {
        super(basePath);
    }

    /**
     * Sets a custom filter to programmatically control which files are included.
     * @param transletScanFilter the custom scan filter
     */
    public void setTransletScanFilter(TransletScanFilter transletScanFilter) {
        this.transletScanFilter = transletScanFilter;
    }

    /**
     * Sets include/exclude wildcard patterns for filtering.
     * @param filterPatterns the patterns to apply
     */
    public void setFilterPatterns(IncludeExcludeWildcardPatterns filterPatterns) {
        this.filterPatterns = filterPatterns;
    }

    /**
     * Sets a mask pattern to transform the discovered file path into a translet name.
     * @param transletNameMaskPattern a wildcard pattern for masking
     */
    public void setTransletNameMaskPattern(String transletNameMaskPattern) {
        if (transletNameMaskPattern == null) {
            throw new IllegalArgumentException("transletNameMaskPattern must not be null");
        }
        this.transletNameMaskPattern = new WildcardPattern(transletNameMaskPattern, NAME_SEPARATOR_CHAR);
    }

    /**
     * Overrides the parent scan method to wrap the save handler with translet-specific logic
     * and to throw a {@link TransletScanFailedException} on failure.
     * @param targetPath the path to scan
     * @param matcher the wildcard matcher for file names
     * @param saveHandler the handler for saving discovered files
     */
    @Override
    protected void scan(String targetPath, WildcardMatcher matcher, SaveHandler saveHandler) {
        try {
            super.scan(targetPath, matcher, new TransletSaveHandler(saveHandler));
        } catch (Exception e) {
            throw new TransletScanFailedException("Failed to scan translets from given path: " +
                    targetPath, e);
        }
    }

    /**
     * An inner class that decorates the original {@link SaveHandler} to apply
     * translet-specific filtering and name masking before saving.
     */
    private class TransletSaveHandler implements SaveHandler {

        private final SaveHandler saveHandler;

        private TransletSaveHandler(SaveHandler saveHandler) {
            this.saveHandler = saveHandler;
        }

        /**
         * Applies all filters and masks to the discovered file, then delegates to the
         * original handler if the file is not excluded.
         * @param filePath the path of the scanned file
         * @param scannedFile the scanned file object
         */
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

            if (filterPatterns != null && filterPatterns.matches(filePath)) {
                return;
            }

            saveHandler.save(transletName, scannedFile);

            if (logger.isTraceEnabled()) {
                logger.trace("Scanned file: {}", filePath);
            }
        }
    }

}
