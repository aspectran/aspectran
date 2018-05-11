/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.shell.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.console.Console;

import java.io.File;
import java.io.IOException;

import static com.aspectran.core.context.ActivityContext.BASE_DIR_PROPERTY_NAME;
import static com.aspectran.core.context.config.AspectranConfig.DEFAULT_ASPECTRAN_CONFIG_FILE;

/**
 * The Interface ShellService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface ShellService extends CoreService {

    SessionAdapter newSessionAdapter();

    Console getConsole();

    String[] getCommands();

    CommandRegistry getCommandRegistry();

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
     *
     * @return true if the verbose mode is enabled
     */
    boolean isVerbose();

    /**
     * Enables or disables the verbose mode.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Sets a flag indicating whether to show the description or not.
     *
     * @param verbose true to enable the verbose mode; false to disable
     */
    void setVerbose(boolean verbose);

    String getGreetings();

    void setGreetings(String greetings);

    /**
     * Prints welcome message.
     */
    void printGreetings();

    /**
     * Prints help information.
     */
    void printHelp();

    /**
     * Checks whether the Translet can be exposed.
     *
     * @param transletName the name of the Translet to check
     * @return true if the Translet can be exposed; false otherwise
     */
    boolean isExposable(String transletName);

    /**
     * Executes a Shell Activity.
     *
     * @param command the command
     */
    void execute(String command);

    /**
     * Executes a Shell Activity.
     *
     * @param commandLineParser the command line parser
     */
    void execute(CommandLineParser commandLineParser);

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(String aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(String aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile, console);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
     static ShellService create(File aspectranConfigFile)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile);
    }

    /**
     * Returns a new instance of ShellService.
     *
     * @param aspectranConfigFile the aspectran configuration file
     * @param console the console
     * @return the instance of ShellService
     * @throws AspectranServiceException the aspectran service exception
     * @throws IOException if an I/O error has occurred
     */
    static ShellService create(File aspectranConfigFile, Console console)
            throws AspectranServiceException, IOException {
        return AspectranShellService.create(aspectranConfigFile, console);
    }

    static File determineAspectranConfigFile(String arg) {
        File file;
        if (!StringUtils.isEmpty(arg)) {
            file = new File(arg);
        } else {
            String baseDir = SystemUtils.getProperty(BASE_DIR_PROPERTY_NAME);
            if (baseDir != null) {
                file = new File(baseDir, DEFAULT_ASPECTRAN_CONFIG_FILE);
            } else {
                file = new File(DEFAULT_ASPECTRAN_CONFIG_FILE);
            }
        }
        return file;
    }

}
