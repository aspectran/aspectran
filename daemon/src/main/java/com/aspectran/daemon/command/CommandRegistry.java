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

import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.daemon.Daemon;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new LinkedHashMap<>();

    private final Daemon daemon;

    public CommandRegistry(Daemon daemon) {
        this.daemon = daemon;
    }

    public Daemon getDaemon() {
        return daemon;
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
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
