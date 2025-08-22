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
package com.aspectran.shell.service;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.service.CoreService;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.console.ShellConsole;

/**
 * The main interface for the Aspectran Shell service.
 * <p>This service provides an interactive command-line interface for an Aspectran
 * application. It extends {@link CoreService} to provide access to the core
 * application context and lifecycle management, while adding features specific
 * to a console environment.
 *
 * @since 2.0.0
 */
public interface ShellService extends CoreService {

    /**
     * Returns the shell console used for input and output.
     * @return the shell console
     */
    ShellConsole getConsole();

    /**
     * Returns whether verbose mode is enabled.
     * @return true if verbose mode is enabled, false otherwise
     */
    boolean isVerbose();

    /**
     * Sets whether verbose mode is enabled.
     * @param verbose true to enable verbose mode, false to disable
     */
    void setVerbose(boolean verbose);

    /**
     * Returns the greetings message displayed when the shell starts.
     * @return the greetings message
     */
    String getGreetings();

    /**
     * Sets the greetings message to be displayed when the shell starts.
     * @param greetings the greetings message
     */
    void setGreetings(String greetings);

    /**
     * Prints the greetings message to the console.
     */
    void printGreetings();

    /**
     * Prints help information to the console.
     */
    void printHelp();

    /**
     * Creates and returns a new session adapter for the shell environment.
     * @return a new {@link SessionAdapter}
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes a translet based on a parsed command line.
     * @param transletCommandLine the parsed command line representing the request
     * @return the result of the translet execution
     * @throws TransletNotFoundException if the requested translet does not exist
     */
    Translet translate(TransletCommandLine transletCommandLine) throws TransletNotFoundException;

}
