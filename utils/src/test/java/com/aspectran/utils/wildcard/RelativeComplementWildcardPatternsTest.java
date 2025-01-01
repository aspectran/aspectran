/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.wildcard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelativeComplementWildcardPatternsTest {

    @Test
    void matches() {
        String[] includePatterns = {
                "/aaa/b*/**",
                "/aaa/c*"
        };
        String[] excludePatterns = {
                "/aaa/bb*",
                "/aaa/cc*"
        };

        RelativeComplementWildcardPatterns patterns = RelativeComplementWildcardPatterns.of(includePatterns, excludePatterns, '/');
        assertTrue(patterns.matches("/aaa/bbb/ccc"));
        assertFalse(patterns.matches("/aaa/ccc"));
        assertTrue(patterns.matches("/aaa/bcd/ccc"));
    }

}
