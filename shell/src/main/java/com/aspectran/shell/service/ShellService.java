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
package com.aspectran.shell.service;

import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.service.CoreService;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.Console;

/**
 * The Interface ShellService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface ShellService extends CoreService {

    /**
     * Returns the console.
     *
     * @return the console
     */
    Console getConsole();

    /**
     * Creates a new session adapter for the shell service and returns.
     *
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

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
     * @param transletCommandLine the translet command line
     */
    void execute(TransletCommandLine transletCommandLine);

}
