/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.shell.jline.console;

import com.aspectran.core.util.wildcard.WildcardPattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-01-26</p>
 */
class CommandCompleterTest {

    @Test
    void testTransletName() {
        WildcardPattern wildcardPattern = new WildcardPattern("speak * msg * speak + msg + speak ? msg ?");
        String transletName = wildcardPattern.toString();
        transletName = transletName.replaceAll(" [*+?] | [*+?]$|[*+?]", " ");
        assertEquals("speak msg speak msg speak msg ", transletName);
    }

    @Test
    void testTransletName2() {
        WildcardPattern wildcardPattern = new WildcardPattern("speak *");
        String transletName = wildcardPattern.toString();
        transletName = transletName.replaceAll(" [*+?] | [*+?]$|[*+?]", " ");
        assertEquals("speak ", transletName);
    }

}
