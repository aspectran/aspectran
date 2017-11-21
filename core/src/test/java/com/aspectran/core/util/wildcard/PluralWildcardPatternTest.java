package com.aspectran.core.util.wildcard;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

import org.junit.Test;

public class PluralWildcardPatternTest {

    @Test
    public void matches() throws Exception {
        String[] includePatterns = {
                "/aaa/b*/**",
                "/aaa/c*"
        };
        String[] excludePatterns = {
                "/aaa/bb*",
                "/aaa/cc*"
        };

        PluralWildcardPattern pattern = new PluralWildcardPattern(includePatterns, excludePatterns, '/');
        assertTrue(pattern.matches("/aaa/bbb/ccc"));
        assertFalse(pattern.matches("/aaa/ccc"));
        assertTrue(pattern.matches("/aaa/bcd/ccc"));
    }

}