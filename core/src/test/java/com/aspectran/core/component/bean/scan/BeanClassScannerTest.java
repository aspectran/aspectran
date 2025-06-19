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

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.PrefixSuffixPattern;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-03-21</p>
 */
class BeanClassScannerTest {

    @Test
    void testScanBean() throws IllegalRuleException {
        BeanRule beanRule = new BeanRule();
        beanRule.setId("test.*.bbb");
        beanRule.setScanPattern("com.aspectran.core.component.bean.*.*Bean");

        FilterParameters filterParameters = new FilterParameters();
        filterParameters.addExcludePattern("**.*Test");
        filterParameters.addExcludePattern("**.*Exception");
        filterParameters.addExcludePattern("**.*Analyzer");
        filterParameters.addExcludePattern("**.*Bean2");
        beanRule.setFilterParameters(filterParameters);

        BeanClassScanner scanner = createBeanClassScanner(beanRule);
        PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.of(beanRule.getId());
        List<BeanRule> beanRules = new ArrayList<>();
        scanner.scan(beanRule.getScanPattern(), (resourceName, targetClass) -> {
            BeanRule beanRule2 = beanRule.replicate();
            if (prefixSuffixPattern != null) {
                beanRule2.setId(prefixSuffixPattern.enclose(resourceName));
            } else {
                if (beanRule.getId() != null) {
                    beanRule2.setId(beanRule.getId() + resourceName);
                } else if (beanRule.getMaskPattern() != null) {
                    beanRule2.setId(resourceName);
                }
            }
            beanRule2.setBeanClass(targetClass);
            beanRules.add(beanRule2);
        });
        for (BeanRule beanRule2 : beanRules) {
            assertEquals("test." + beanRule2.getClassName() + ".bbb", beanRule2.getId());
        }
    }

    @NonNull
    private BeanClassScanner createBeanClassScanner(@NonNull BeanRule beanRule) throws IllegalRuleException {
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
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

}
