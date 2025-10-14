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

import java.util.Collection;

/**
 * A registry that contains and provides access to all commands known by the shell.
 * <p>This interface acts as a central lookup service for retrieving {@link Command}
 * instances by name or by class.</p>
 *
 * <p>Created: 2017. 10. 25.</p>
 */
public interface CommandRegistry {

    /**
     * Returns the console commander that owns this registry.
     * @return the console commander
     */
    ConsoleCommander getConsoleCommander();

    /**
     * Retrieves a command by its name.
     * @param commandName the name of the command
     * @return the command instance, or {@code null} if not found
     */
    Command getCommand(String commandName);

    /**
     * Retrieves a command by its class.
     * @param commandClass the class of the command
     * @return the command instance, or {@code null} if not found
     */
    Command getCommand(Class<? extends Command> commandClass);

    /**
     * Returns a collection of all registered commands.
     * @return a collection of all commands
     */
    Collection<Command> getAllCommands();

}
