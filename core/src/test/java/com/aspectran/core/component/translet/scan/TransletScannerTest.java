package com.aspectran.core.component.translet.scan;

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.FilterParameters;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.PrefixSuffixPattern;
import com.aspectran.core.util.ResourceUtils;
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
        PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern(transletRule.getName());
        scanner.scan(scanPath, (filePath, scannedFile) -> {
            TransletRule newTransletRule = TransletRule.replicate(transletRule, filePath);
            if (prefixSuffixPattern.isSplitted()) {
                newTransletRule.setName(prefixSuffixPattern.join(filePath));
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
                    Class<?> filterClass = AspectranClassLoader.getDefaultClassLoader().loadClass(transletScanFilterClassName);
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