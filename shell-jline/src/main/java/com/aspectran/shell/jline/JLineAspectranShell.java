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

import com.aspectran.core.context.InsufficientEnvironmentException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.jline.command.JLineConsoleCommander;
import com.aspectran.shell.jline.console.JLineShellConsole;

import java.io.File;
import java.io.IOException;

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
            bootstrap(basePath, aspectranConfigFile, console);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void bootstrap(File aspectranConfigFile) throws IOException {
        bootstrap(null, aspectranConfigFile, new JLineShellConsole());
    }

    public static void bootstrap(File aspectranConfigFile, ShellConsole console) {
        bootstrap(null, aspectranConfigFile, console);
    }

    public static void bootstrap(@Nullable String basePath, File aspectranConfigFile, ShellConsole console) {
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }

        JLineConsoleCommander commander = null;
        int exitStatus = 0;

        try {
            Aspectran.printPrettyAboutMe(System.out);

            commander = new JLineConsoleCommander(console);
            commander.prepare(basePath, aspectranConfigFile);
            commander.perform();
        } catch (Exception e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            if (t instanceof InsufficientEnvironmentException) {
                System.err.println(((InsufficientEnvironmentException)t).getPrettyMessage());
            } else {
                e.printStackTrace(System.err);
            }
            exitStatus = 1;
        } finally {
            if (commander != null) {
                commander.release();
            }
        }

        System.exit(exitStatus);
    }

}
