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
package com.aspectran.shell.console;

import com.aspectran.core.util.StringUtils;

/**
 * <p>Created: 2017. 3. 5.</p>
 */
public class DefaultConsoleReadPasswordTest {

    public static void main(String[] args) {
        ShellConsole console = new DefaultShellConsole();
        String prompt = "> ";
        while (true) {
            String line = console.readPassword(prompt);
            if (StringUtils.hasLength(line)) {
                console.writeLine(line);
                if ("quit".equals(line)) {
                    break;
                }
            }
        }
    }

}
