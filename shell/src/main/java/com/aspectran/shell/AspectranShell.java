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
package com.aspectran.shell;

import com.aspectran.core.context.InsufficientEnvironmentException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.shell.command.ShellCommandRunner;
import com.aspectran.shell.console.DefaultShellConsole;
import com.aspectran.shell.console.ShellConsole;

import java.io.File;

/**
 * Main entry point for the Aspectran Shell.
 * <P>Aspectran Shell is a command runner that allows you to
 * run the built-in commands and translets provided by Aspectran
 * in the console environment.</P>
 *
 * @since 2016. 1. 17.
 */
public class AspectranShell {

    public static void main(String[] args) {
        String basePath = AspectranConfig.determineBasePath(args);
        File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args);
        ShellConsole console = new DefaultShellConsole();
        bootstrap(basePath, aspectranConfigFile, console);
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

        ShellCommandRunner runner = null;
        int exitStatus = 0;

        try {
            Aspectran.printPrettyAboutMe(System.out);

            runner = new ShellCommandRunner(console);
            runner.init(basePath, aspectranConfigFile);
            runner.run();
        } catch (Exception e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            if (t instanceof InsufficientEnvironmentException) {
                System.err.println(((InsufficientEnvironmentException)t).getPrettyMessage());
            } else {
                e.printStackTrace(System.err);
            }
            exitStatus = 1;
        } finally {
            if (runner != null) {
                runner.release();
            }
        }

        System.exit(exitStatus);
    }

}
