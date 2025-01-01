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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2016. 4. 3.</p>
 */
class WildcardMaskerTest {

    @Test
    void maskTest() {
        assertEquals(mask("**.*", "com.aspectran.core.embedded.ABean"), "com.aspectran.core.embedded.ABean");
        assertEquals(mask("**", "..com.aspectran.core.embedded.ABean"), "com.aspectran.core.embedded.ABean");
        assertEquals(mask("com.aspectran.core.**.*", "com.aspectran.core.embedded.ABean"), "embedded.ABean");
        assertEquals(mask("com.aspectran.core.embedded.*", "com.aspectran.core.embedded.ABean"), "ABean");
        assertEquals(mask("com.aspectran.core.embedded.**", "com.aspectran.core.embedded.ABean"), "ABean");
        assertEquals(mask("com.aspectran.core.embedded.**.*", "com.aspectran.core.embedded.ABean"), "ABean");

//        List<String> list = new ArrayList<>();
//        list.add(mask("**.*", "com.aspectran.core.embedded.ABean"));
//        list.add(mask("**", "..com.aspectran.core.embedded.ABean"));
//        list.add(mask("com.aspectran.core.**.*", "com.aspectran.core.embedded.ABean"));
//        list.add(mask("com.aspectran.core.embedded.*", "com.aspectran.core.embedded.ABean"));
//        list.add(mask("com.aspectran.core.embedded.**", "com.aspectran.core.embedded.ABean"));
//        list.add(mask("com.aspectran.core.embedded.**.*", "com.aspectran.core.embedded.ABean"));
//
//        System.out.println("-------------------------------------");
//        for (String string : list) {
//            System.out.println(string);
//        }
//        System.out.println("-------------------------------------");
    }



    private static String mask(String maskPattern, String nakedString) {
        WildcardPattern pattern = new WildcardPattern(maskPattern, '.');
        return pattern.mask(nakedString);
    }

}
