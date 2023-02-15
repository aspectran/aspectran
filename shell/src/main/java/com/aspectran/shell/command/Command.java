/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
 * The Command interface allows the command interpreter to delegate tasks.
 */
public interface Command {

    Options getOptions();

    List<Arguments> getArgumentsList();

    /**
     * This method will be called as the starting point to execute the logic
     * for the action mapped to this command.
     * @param options the parsed options
     * @param console the console
     * @throws Exception if an error occurs during command execution
     */
    void execute(ParsedOptions options, ShellConsole console) throws Exception;

    /**
     * Prints the usage statement for the specified command.
     * @param console the Console instance
     */
    void printHelp(ShellConsole console);

    void printQuickHelp(ShellConsole console);

    /**
     * This method returns an instance of Command.Descriptor.
     * The descriptor is meta information about the command.
     * @return a Descriptor that is meta information about the command
     */
    Command.Descriptor getDescriptor();

    /**
     * An interface that can be used to describe the the functionality of the
     * command implementation.  This is a very important concept in a text-driven
     * environment such as a command-line user interface.
     */
    interface Descriptor {

        /**
         * The purpose of the namespace is to provide an identifier to group
         * commands without relying on class name or other convoluted approaches
         * to group commands.
         *
         * @return the command's namespace
         */
        String getNamespace();

        /**
         * Implementation of this method should return a simple string (with no spaces)
         * that identifies the action mapped to this command.
         *
         * @return the name of the action mapped to this command.
         */
        String getName();

        /**
         * This method should return a descriptive text about the command
         * it is attached to.
         *
         * @return a descriptive text about the command
         */
        String getDescription();

        /**
         * Implementation of this method should return helpful hint on how
         * to use the associated command and further description of options that
         * are supported by the command.
         *
         * @return Usage of command
         */
        String getUsage();

    }

}
