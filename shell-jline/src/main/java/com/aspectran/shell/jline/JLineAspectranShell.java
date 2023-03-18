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
package com.aspectran.shell.jline;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.shell.AspectranShell;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.jline.console.JLineShellConsole;

import java.io.File;

/**
 * Main entry point for the Aspectran Shell using JLine.
 *
 * <p>Created: 2017. 10. 21.</p>
 *
 * @since 4.1.0
 */
public class JLineAspectranShell {

    public static void main(String[] args) {
        String basePath = AspectranConfig.determineBasePath(args);
        File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args);
        try {
            ShellConsole console = new JLineShellConsole();
            AspectranShell.bootstrap(basePath, aspectranConfigFile, console);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
