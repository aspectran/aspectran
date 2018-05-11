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
package com.aspectran.daemon.command;

import com.aspectran.daemon.command.polling.CommandParameters;

/**
 * The Command interface is there to allow Commander to delegate tasks.
 */
public interface Command {

    /**
     * This method will be called as the starting point to execute the logic
     * for the action mapped to this command.
     *
     * @param parameters the command parameters
     * @return the message output to the console as a result of an executed command
     * @throws Exception if an error occurs during command execution
     */
    String execute(CommandParameters parameters) throws Exception;

    /**
     * This method returns an instance of Command.Descriptor.
     * The descriptor is meta information about the command.
     *
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

    }

}
