/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.shell.jline.command.JLineConsoleCommander;
import com.aspectran.shell.jline.console.JLineShellConsole;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.InsufficientEnvironmentException;
import com.aspectran.utils.annotation.jsr305.Nullable;

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
            bootstrap(basePath, aspectranConfigFile);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void bootstrap(File aspectranConfigFile) {
        bootstrap(null, aspectranConfigFile);
    }

    public static void bootstrap(@Nullable String basePath, File aspectranConfigFile) {
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }

        JLineConsoleCommander commander = null;
        int exitStatus = 0;

        try {
            JLineShellConsole console = new JLineShellConsole();
            commander = new JLineConsoleCommander(console);
            commander.configure(basePath, aspectranConfigFile);
            commander.run();
        } catch (Exception e) {
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause instanceof InsufficientEnvironmentException that) {
                System.err.println(that.getPrettyMessage());
            } else {
                System.err.println(cause.getMessage());
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
