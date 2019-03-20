package com.aspectran.core.component.translet.scan;

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.params.FilterParameters;
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
    void testScanTranslet() throws IOException {
        File baseDir = ResourceUtils.getResourceAsFile(".");
        String basePath = baseDir.getCanonicalPath();

        TransletRule transletRule = new TransletRule();
        transletRule.setName("/test/*/bbb");
        transletRule.setScanPath("config/sample/**");

        FilterParameters filterParameters = new FilterParameters();
        filterParameters.addExcludePattern("**/*.dtd");
        transletRule.setFilterParameters(filterParameters);

        String scanPath = transletRule.getScanPath();
        TransletScanner scanner = new TransletScanner(basePath, AspectranClassLoader.getDefaultClassLoader());
        scanner.setFilterParameters(transletRule.getFilterParameters());
        scanner.setTransletNameMaskPattern(scanPath);
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

}