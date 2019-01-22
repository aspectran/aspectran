/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.config.ShellConfig;
import com.aspectran.core.util.Aspectran;
import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.shell.command.ShellCommandInterpreter;
import com.aspectran.shell.command.ShellCommandRegistry;
import com.aspectran.shell.command.builtins.QuitCommand;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import com.aspectran.shell.service.AspectranShellService;

import java.io.File;

/**
 * Main entry point for the Aspectran Shell.
 * <P>Aspectran Shell is a command interpreter that allows you to
 * run the built-in commands and translets provided by Aspectran
 * in the console environment.</P>
 *
 * @since 2016. 1. 17.
 */
public class AspectranShell {

    public static void main(String[] args) {
        String basePath = AspectranConfig.determineBasePath(args);
        File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args);
        Console console = new DefaultConsole();
        bootstrap(basePath, aspectranConfigFile, console);
    }

    public static void bootstrap(File aspectranConfigFile, Console console) {
        bootstrap(null, aspectranConfigFile, console);
    }

    public static void bootstrap(String basePath, File aspectranConfigFile, Console console) {
        if (aspectranConfigFile == null) {
            throw new IllegalArgumentException("aspectranConfigFile must not be null");
        }
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }

        AspectranShellService service = null;
        int exitStatus = 0;

        try {
            Aspectran.printPrettyAboutMe(System.out);

            AspectranConfig aspectranConfig = new AspectranConfig();
            try {
                AponReader.parse(aspectranConfigFile, aspectranConfig);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                        aspectranConfigFile, e);
            }
            if (basePath != null) {
                aspectranConfig.updateBasePath(basePath);
            }

            ShellConfig shellConfig = aspectranConfig.touchShellConfig();
            String commandPrompt = shellConfig.getString(ShellConfig.prompt);
            if (commandPrompt != null) {
                console.setCommandPrompt(commandPrompt);
            }

            ShellCommandInterpreter interpreter = new ShellCommandInterpreter(console);
            console.setInterpreter(interpreter);

            service = AspectranShellService.create(aspectranConfig, console);
            service.start();
            interpreter.setService(service);

            ShellCommandRegistry commandRegistry = new ShellCommandRegistry(service);
            commandRegistry.addCommand(shellConfig.getStringArray(ShellConfig.commands));
            if (commandRegistry.getCommand(QuitCommand.class) == null) {
                commandRegistry.addCommand(QuitCommand.class);
            }
            interpreter.setCommandRegistry(commandRegistry);

            interpreter.perform();
        } catch (Exception e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            if (t instanceof InsufficientEnvironmentException) {
                System.err.println(((InsufficientEnvironmentException)t).getPrettyMessage());
            } else {
                e.printStackTrace(System.err);
            }
            exitStatus = 1;
        } finally {
            if (service != null) {
                service.stop();
            }
        }

        System.exit(exitStatus);
    }

}
