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

import org.junit.jupiter.api.Test;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
class JLineAnsiStylerTest {

    @Test
    void testParse() {
        String s0 = "{{black}} Black {{red}} Red {{green}} Green {{yellow}} Yellow {{blue}} Blue {{magenta}} Magenta {{cyan}} Cyan {{gray}} Gray {{reset}}";
        System.out.println(JLineAnsiStyler.parse(s0));
        String s1 = "{{GRAY}} Bright Gray {{RED}} Bright Red {{GREEN}} Bright Green {{YELLOW}} Bright Yellow {{BLUE}} Bright Blue {{MAGENTA}} Bright Magenta {{CYAN}} Bright Cyan {{white}} White {{reset}}";
        System.out.println(JLineAnsiStyler.parse(s1));
        String s2 = "{{white,bg:black}} black {{black,bg:red}} red {{white,bg:green}} green {{black,bg:yellow}} yellow {{bg:blue}} blue {{bg:magenta}} magenta {{bg:cyan}} cyan {{black,bg:gray}} gray  {{reset}}\n" +
                    "{{white,bg:GRAY}} GRAY  {{black,bg:RED}} RED {{white,bg:GREEN}} GREEN {{black,bg:YELLOW}} YELLOW {{bg:BLUE}} BLUE {{bg:MAGENTA}} MAGENTA {{bg:CYAN}} CYAN {{black,bg:white}} white {{reset}}";
        System.out.println(JLineAnsiStyler.parse(s2));
    }

}