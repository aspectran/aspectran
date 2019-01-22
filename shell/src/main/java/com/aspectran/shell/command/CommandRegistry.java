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
package com.aspectran.shell.command;

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.shell.service.ShellService;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A registry that contains the commands known by a shell.
 *
 * <p>Created: 2017. 10. 25.</p>
 */
public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private ShellService service;

    public CommandRegistry(ShellService service) {
        this.service = service;
    }

    public ShellService getService() {
        return service;
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public Command getCommand(Class<? extends Command> commandClass) {
        for (Command command : commands.values()) {
            if (command.getClass().equals(commandClass)) {
                return command;
            }
        }
        return null;
    }

    public void addCommand(String... classNames) {
        if (classNames != null) {
            for (String className : classNames) {
                try {
                    ClassLoader classLoader = AspectranClassLoader.getDefaultClassLoader();
                    @SuppressWarnings("unchecked")
                    Class<? extends Command> commandClass = (Class<? extends Command>)classLoader.loadClass(className);
                    addCommand(commandClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Unable to load Command class: " + className, e);
                }
            }
        }
    }

    public void addCommand(Class<? extends Command> commandClass) {
        Command command = ClassUtils.createInstance(commandClass, this);
        commands.put(command.getDescriptor().getName(), command);
    }

    public Collection<Command> getAllCommands() {
        return commands.values();
    }

}
