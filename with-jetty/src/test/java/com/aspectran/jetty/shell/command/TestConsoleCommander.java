/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.jetty.shell.command;

import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.ConsoleCommander;
import com.aspectran.shell.command.ShellCommandRegistry;
import com.aspectran.shell.console.DefaultShellConsole;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;

public class TestConsoleCommander implements ConsoleCommander {

    private final ShellConsole console = new DefaultShellConsole();

    private final CommandRegistry commandRegistry = new ShellCommandRegistry(this);

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ShellConsole> T getConsole() {
        return (T)console;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ShellService getShellService() {
        return null;
    }

}
