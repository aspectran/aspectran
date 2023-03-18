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
package com.aspectran.shell.service;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.service.CoreService;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.ShellConsole;

/**
 * The Interface ShellService.
 *
 * <p>Created: 2017. 10. 28.</p>
 */
public interface ShellService extends CoreService {

    ShellConsole getConsole();

    /**
     * Tests if the verbose mode is enabled.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Returns a flag indicating whether to show the description or not.
     * @return true if the verbose mode is enabled
     */
    boolean isVerbose();

    /**
     * Enables or disables the verbose mode.
     * If verbose mode is on, a detailed description is printed each time the command is executed.
     * Sets a flag indicating whether to show the description or not.
     * @param verbose true to enable the verbose mode; false to disable
     */
    void setVerbose(boolean verbose);

    /**
     * Returns the greeting message.
     * @return the greeting message
     */
    String getGreetings();

    /**
     * Specifies the greeting message.
     * @param greetings the greeting message
     */
    void setGreetings(String greetings);

    /**
     * Prints greeting message.
     */
    void printGreetings();

    /**
     * Prints help information.
     */
    void printHelp();

    /**
     * Returns whether or not the translet can be exposed to the shell service.
     * @param transletName the name of the translet to check
     * @return true if the translet can be exposed; false otherwise
     */
    boolean isExposable(String transletName);

    /**
     * Create and return a new session adapter from the shell service.
     * @return the session adapter
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes translet.
     * @param transletCommandLine the translet command line
     * @return the {@code Translet} instance
     */
    Translet translate(TransletCommandLine transletCommandLine) throws TransletNotFoundException;

}
