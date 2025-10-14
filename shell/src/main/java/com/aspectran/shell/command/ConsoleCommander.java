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
package com.aspectran.shell.command;

import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;

/**
 * The main orchestrator for the Aspectran Shell.
 * <p>This interface defines the contract for a component that manages the shell's
 * lifecycle, including configuration, running the read-eval-print loop (REPL),
 * and dispatching commands for execution. It connects the {@link ShellConsole},
 * the {@link CommandRegistry}, and the {@link ShellService}.</p>
 *
 * <p>Created: 2017. 6. 3.</p>
 */
public interface ConsoleCommander {

    /**
     * Returns the console instance managed by this commander.
     * @param <T> the type of the shell console
     * @return the shell console
     */
    <T extends ShellConsole> T getConsole();

    /**
     * Returns the registry containing all available commands.
     * @return the command registry
     */
    CommandRegistry getCommandRegistry();

    /**
     * Returns the underlying shell service.
     * @return the shell service
     */
    ShellService getShellService();

}
