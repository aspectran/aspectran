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
import org.junit.jupiter.api.Test;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
class CommandTest {

    private CommandInterpreter interpreter = new TestCommandInterpreter();

    @Test
    void testVerboseCommand() {
        VerboseCommand command = new VerboseCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
    }

    @Test
    void testHelpCommand() {
        HelpCommand command = new HelpCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
    }

    @Test
    void testSysInfoCommand() throws Exception {
        SysInfoCommand command = new SysInfoCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
        CommandLineParser lineParser = new CommandLineParser("sysinfo -mem");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testPBEncryptCommand() throws Exception {
        PBEncryptCommand command = new PBEncryptCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
        CommandLineParser lineParser = new CommandLineParser("encrypt \"aaa ccc d\" -p=bbb");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testPBDecryptCommand() throws Exception {
        PBDecryptCommand command = new PBDecryptCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
        CommandLineParser lineParser = new CommandLineParser("decrypt -p=bbb RYr6VMzCxBuY9MoXYBV64w==");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testPBEncryptCommand2() throws Exception {
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "password");

        PBEncryptCommand command = new PBEncryptCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
        CommandLineParser lineParser = new CommandLineParser("decrypt input1 input2");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testPBDecryptCommand2() throws Exception {
        System.setProperty(PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY, "password");

        PBDecryptCommand command = new PBDecryptCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        CommandLineParser lineParser = new CommandLineParser("encrypt KuSJkQVYRydcVTNdm5oTJg==");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testTestCommand() throws Exception {
        TestCommand command = new TestCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
        CommandLineParser lineParser = new CommandLineParser("test -i=aaa -D=123 -p=bbb -X -Y -Z");
        command.execute(lineParser.getParsedOptions(command.getOptions()));
    }

    @Test
    void testJettyCommand() {
        JettyCommand command = new JettyCommand(interpreter.getCommandRegistry());
        command.writeLine(command.getDescriptor().getDescription());
        command.printUsage();
    }

}
