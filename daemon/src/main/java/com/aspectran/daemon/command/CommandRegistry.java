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
package com.aspectran.daemon.command;

import com.aspectran.daemon.Daemon;

import java.util.Collection;

/**
 * Registry of daemon commands.
 * <p>
 * Provides lookup and enumeration of {@link Command} implementations associated
 * with a {@link com.aspectran.daemon.Daemon}. Implementations may support
 * dynamic registration.
 * </p>
 */
public interface CommandRegistry {

    /**
     * Returns the owning daemon instance associated with this registry.
     * @return the {@link Daemon}
     */
    Daemon getDaemon();

    /**
     * Look up a command by its descriptor name.
     * @param commandName the command name as exposed by {@link Command.Descriptor#getName()}
     * @return the matching command instance, or {@code null} if not registered
     */
    Command getCommand(String commandName);

    /**
     * Look up a command by its concrete implementation class.
     * @param commandClass the command class to match
     * @return the registered command of the given class, or {@code null} if none
     */
    Command getCommand(Class<? extends Command> commandClass);

    /**
     * Get all registered commands in this registry.
     * @return a collection view of all commands (iteration order is implementation-specific)
     */
    Collection<Command> getAllCommands();

}
