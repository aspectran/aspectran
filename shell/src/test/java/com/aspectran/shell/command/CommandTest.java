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

import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.shell.command.builtins.HelpCommand;
import com.aspectran.shell.command.builtins.JettyCommand;
import com.aspectran.shell.command.builtins.PBDecryptCommand;
import com.aspectran.shell.command.builtins.PBEncryptCommand;
import com.aspectran.shell.command.builtins.SysInfoCommand;
import com.aspectran.shell.command.builtins.VerboseCommand;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.console.DefaultConsole;
import org.junit.jupiter.api.Test;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
class CommandTest {

    @Test
    void testVerboseCommand() {
        Console console = new DefaultConsole();
        Command command = new VerboseCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

    @Test
    void testHelpCommand() {
        Console console = new DefaultConsole();
        Command command = new HelpCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

    @Test
    void testSysInfoCommand() {
        Console console = new DefaultConsole();
        Command command = new SysInfoCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

    @Test
    void testPBEncryptCommand() throws Exception {
        Console console = new DefaultConsole();
        PBEncryptCommand command = new PBEncryptCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
        command.execute(new String[] {"aaa ccc d", "-p=bbb"});
    }

    @Test
    void testPBDecryptCommand() throws Exception {
        Console console = new DefaultConsole();
        PBDecryptCommand command = new PBDecryptCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
        command.execute(new String[] {"-i=RYr6VMzCxBuY9MoXYBV64w==", "-p=bbb"});
    }

    @Test
    void testPBEncryptCommand2() throws Exception {
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "password");

        Console console = new DefaultConsole();
        PBEncryptCommand command = new PBEncryptCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
        command.execute(new String[] {"-i", "input1", "input2"});
    }

    @Test
    void testPBDecryptCommand2() throws Exception {
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "password");

        Console console = new DefaultConsole();
        PBDecryptCommand command = new PBDecryptCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
        command.execute(new String[] {"-i=KuSJkQVYRydcVTNdm5oTJg=="});
    }

    @Test
    void testTestCommand() throws Exception {
        Console console = new DefaultConsole();
        TestCommand command = new TestCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
        command.execute(new String[] {"-D=123", "-i=aaa", "-p=bbb", "-X", "-Y", "-Z"});
    }

    @Test
    void testJettyCommand() {
        Console console = new DefaultConsole();
        JettyCommand command = new JettyCommand(null);
        console.writeLine(command.getDescriptor().getDescription());
        command.printUsage(console);
    }

}
