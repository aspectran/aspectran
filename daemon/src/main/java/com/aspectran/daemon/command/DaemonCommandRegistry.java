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
import com.aspectran.utils.ClassUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class DaemonCommandRegistry implements CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private final Daemon daemon;

    public DaemonCommandRegistry(Daemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public Daemon getDaemon() {
        return daemon;
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

    public void addCommand(Class<? extends Command> commandClass) {
        Object[] args = { this };
        Class<?>[] argTypes = { CommandRegistry.class };
        Command command = ClassUtils.createInstance(commandClass, args, argTypes);
        if (command.getDescriptor() == null) {
            throw new NullPointerException("A command without a descriptor");
        }
        commands.put(command.getDescriptor().getName(), command);
    }

}
