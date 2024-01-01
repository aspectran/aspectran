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
package com.aspectran.shell.jline.console;

import com.aspectran.shell.console.ShellConsole;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
class JLineTextStylerTest {

    @Test
    void testParse() throws IOException {
        String s1 = "{{black}} Black {{red}} Red {{green}} Green {{yellow}} Yellow {{blue}} Blue {{magenta}} Magenta {{cyan}} Cyan {{gray}} Gray {{reset}}";
        String s2 = "{{GRAY}} Bright Gray {{RED}} Bright Red {{GREEN}} Bright Green {{YELLOW}} Bright Yellow {{BLUE}} Bright Blue {{MAGENTA}} Bright Magenta {{CYAN}} Bright Cyan {{white}} White {{reset}}";
        String s3 = "{{white,bg:black}} black {{black,bg:red}} red {{white,bg:green}} green {{black,bg:yellow}} yellow {{bg:blue}} blue {{bg:magenta}} magenta {{bg:cyan}} cyan {{black,bg:gray}} gray  {{reset}}\n" +
                    "{{white,bg:GRAY}} GRAY  {{black,bg:RED}} RED {{white,bg:GREEN}} GREEN {{black,bg:YELLOW}} YELLOW {{bg:BLUE}} BLUE {{bg:MAGENTA}} MAGENTA {{bg:CYAN}} CYAN {{black,bg:white}} white {{reset}}";

        ShellConsole console = new JLineShellConsole();
        console.setStyle("bg:black", "WHITE", "underline");
        console.writeLine("          START          ");
        console.setStyle("underline:off");
        console.writeLine(s1);
        console.writeLine(s2);
        console.writeLine(s3);
        console.setStyle("bg:blue");
        console.writeLine();
        console.setStyle("bg:GRAY");
        console.writeLine(s1);
        console.writeLine(s2);
        console.writeLine(s3);
        console.setStyle("bg:black", "underline");
        console.writeLine("          END          ");
    }

}
