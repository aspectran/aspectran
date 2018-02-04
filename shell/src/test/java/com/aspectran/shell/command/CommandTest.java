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
package com.aspectran.shell.command;

import com.aspectran.shell.command.builtin.HelpCommand;
import com.aspectran.shell.command.builtin.VerboseCommand;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import org.junit.Test;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
public class CommandTest {

    @Test
    public void testVerboseCommand() {
        Console console = new DefaultConsole();
        Command command = new VerboseCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

    @Test
    public void testHelpCommand() {
        Console console = new DefaultConsole();
        Command command = new HelpCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

}
