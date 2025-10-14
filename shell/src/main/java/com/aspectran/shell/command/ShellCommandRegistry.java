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

import com.aspectran.utils.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of the {@link CommandRegistry} interface.
 * <p>This class stores commands in a map, keyed by their names, and provides
 * methods to add and retrieve them.</p>
 *
 * <p>Created: 2017. 10. 25.</p>
 */
public class ShellCommandRegistry implements CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private final ConsoleCommander consoleCommander;

    /**
     * Instantiates a new shell command registry.
     * @param consoleCommander the console commander that owns this registry
     */
    public ShellCommandRegistry(ConsoleCommander consoleCommander) {
        this.consoleCommander = consoleCommander;
    }

    @Override
    public ConsoleCommander getConsoleCommander() {
        return consoleCommander;
    }

    @Override
    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    @Override
    public Command getCommand(Class<? extends Command> commandClass) {
        for (Command command : commands.values()) {
            if (command.getClass().equals(commandClass)) {
                return command;
            }
        }
        return null;
    }

    @Override
    public Collection<Command> getAllCommands() {
        return commands.values();
    }

    /**
     * Adds commands to the registry from an array of class names.
     * @param classNames the fully qualified class names of the commands to add
     */
    public void addCommand(String... classNames) {
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>)classLoader.loadClass(className);
                    addCommand(commandClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load Command class: " + className, e);
                }
            }
        }
    }

    /**
     * Instantiates and adds a command to the registry from its class.
     * <p>The command class must have a constructor that accepts a
     * {@link CommandRegistry} as its only argument.</p>
     * @param commandClass the class of the command to add
     */
    public void addCommand(Class<? extends Command> commandClass) {
        Object[] args = { this };
        Class<?>[] argTypes = { CommandRegistry.class };
        Command command = ClassUtils.createInstance(commandClass, args, argTypes);
        commands.put(command.getDescriptor().getName(), command);
    }

}
