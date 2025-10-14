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

import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Options;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;

import java.util.List;

/**
 * The central interface for all executable commands in the Aspectran Shell.
 * <p>Implementations of this interface define a specific action that can be invoked
 * from the shell, complete with its own options, arguments, and execution logic.</p>
 */
public interface Command {

    /**
     * Returns the options (e.g., "-v", "--file") defined for this command.
     * @return the command's options
     */
    Options getOptions();

    /**
     * Returns the list of positional arguments for this command.
     * @return the list of arguments
     */
    List<Arguments> getArgumentsList();

    /**
     * Executes the logic for the action mapped to this command.
     * @param options the parsed command-line options
     * @param console the shell console for I/O
     * @throws Exception if an error occurs during command execution
     */
    void execute(ParsedOptions options, ShellConsole console) throws Exception;

    /**
     * Prints the detailed usage statement for this command to the console.
     * @param console the shell console to write to
     */
    void printHelp(ShellConsole console);

    /**
     * Prints a concise usage statement for this command.
     * @param console the shell console to write to
     */
    void printQuickHelp(ShellConsole console);

    /**
     * Returns the descriptor containing metadata about the command.
     * @return a {@link Descriptor} with metadata about the command
     */
    Command.Descriptor getDescriptor();

    /**
     * Describes the functionality and identity of a command.
     * <p>This metadata is used for command registration, help generation, and display.</p>
     */
    interface Descriptor {

        /**
         * Returns the command's namespace, used for grouping related commands.
         * @return the command's namespace
         */
        String getNamespace();

        /**
         * Returns the name of the action mapped to this command.
         * This is the string that users will type to invoke the command.
         * @return the name of the command
         */
        String getName();

        /**
         * Returns a brief, one-line description of what the command does.
         * @return a description of the command
         */
        String getDescription();

        /**
         * Returns a detailed usage hint, including options and arguments.
         * @return a usage string for the command
         */
        String getUsage();

    }

}
