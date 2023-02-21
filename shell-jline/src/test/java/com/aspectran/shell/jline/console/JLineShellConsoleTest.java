/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.console.ShellConsole;

import java.io.IOException;

/**
 * <p>Created: 2017. 3. 5.</p>
 */
class JLineShellConsoleTest {

    public static void main(String[] args) throws IOException {
        ShellConsole console = new JLineShellConsole();
        console.setStyle("white", "bg:gray");
        String prompt = "Test> ";
        while (true) {
            String line = console.readLine(prompt);
            if (StringUtils.hasLength(line)) {
                console.writeLine(line);
                if ("quit".equals(line)) {
                    break;
                }
            }
        }
    }

}