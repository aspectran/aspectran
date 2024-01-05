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
package com.aspectran.core.component.translet.scan;

import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-03-20</p>
 */
class TransletScannerTest {

    @Test
    void testScanTranslet() throws IOException, IllegalRuleException {
        File baseDir = ResourceUtils.getResourceAsFile(".");
        String basePath = baseDir.getCanonicalPath();

        TransletRule transletRule = new TransletRule();
        transletRule.setName("/test/*/bbb");
        transletRule.setScanPath("config/sample/**");

        FilterParameters filterParameters = new FilterParameters();
        filterParameters.addExcludePattern("**/*.dtd");
        transletRule.setFilterParameters(filterParameters);

        String scanPath = transletRule.getScanPath();
        TransletScanner scanner = createTransletScanner(basePath, transletRule);
        PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.of(transletRule.getName());
        scanner.scan(scanPath, (filePath, scannedFile) -> {
            TransletRule newTransletRule = TransletRule.replicate(transletRule, filePath);
            if (prefixSuffixPattern != null) {
                newTransletRule.setName(prefixSuffixPattern.enclose(filePath));
            } else {
                if (transletRule.getName() != null) {
                    newTransletRule.setName(transletRule.getName() + filePath);
                }
            }
            assertEquals("/test/" + filePath + "/bbb", newTransletRule.getName());
        });
    }

    private TransletScanner createTransletScanner(String basePath, TransletRule transletRule) throws IllegalRuleException {
        TransletScanner scanner = new TransletScanner(basePath);
        if (transletRule.getFilterParameters() != null) {
            FilterParameters filterParameters = transletRule.getFilterParameters();
            String transletScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
            if (transletScanFilterClassName != null) {
                TransletScanFilter transletScanFilter;
                try {
                    Class<?> filterClass = ClassUtils.getDefaultClassLoader().loadClass(transletScanFilterClassName);
                    transletScanFilter = (TransletScanFilter)ClassUtils.createInstance(filterClass);
                } catch (Exception e) {
                    throw new IllegalRuleException("Failed to instantiate TransletScanFilter [" +
                            transletScanFilterClassName + "]", e);
                }
                scanner.setTransletScanFilter(transletScanFilter);
            }
            String[] excludePatterns = filterParameters.getStringArray(FilterParameters.exclude);
            if (excludePatterns != null) {
                scanner.setExcludePatterns(excludePatterns);
            }
        }
        if (transletRule.getMaskPattern() != null) {
            scanner.setTransletNameMaskPattern(transletRule.getMaskPattern());
        } else {
            scanner.setTransletNameMaskPattern(transletRule.getScanPath());
        }
        return scanner;
    }

}
