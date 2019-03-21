package com.aspectran.core.component.bean.scan;

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.PrefixSuffixPattern;
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
        PrefixSuffixPattern prefixSuffixPattern = PrefixSuffixPattern.parse(beanRule.getId());
        List<BeanRule> beanRules = new ArrayList<>();
        scanner.scan(beanRule.getScanPattern(), (resourceName, targetClass) -> {
            BeanRule beanRule2 = beanRule.replicate();
            if (prefixSuffixPattern != null) {
                beanRule2.setId(prefixSuffixPattern.join(resourceName));
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

    private BeanClassScanner createBeanClassScanner(BeanRule beanRule) throws IllegalRuleException {
        ClassLoader classLoader = AspectranClassLoader.getDefaultClassLoader();
        BeanClassScanner scanner = new BeanClassScanner(classLoader);
        if (beanRule.getFilterParameters() != null) {
            FilterParameters filterParameters = beanRule.getFilterParameters();
            String beanClassScanFilterClassName = filterParameters.getString(FilterParameters.filterClass);
            if (beanClassScanFilterClassName != null) {
                BeanClassScanFilter beanClassScanFilter;
                try {
                    Class<?> filterClass = classLoader.loadClass(beanClassScanFilterClassName);
                    beanClassScanFilter = (BeanClassScanFilter)ClassUtils.createInstance(filterClass);
                } catch (Exception e) {
                    throw new IllegalRuleException("Failed to instantiate BeanClassScanFilter [" +
                            beanClassScanFilterClassName + "]", e);
                }
                scanner.setBeanClassScanFilter(beanClassScanFilter);
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