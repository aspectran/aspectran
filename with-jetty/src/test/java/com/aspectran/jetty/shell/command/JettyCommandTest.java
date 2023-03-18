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

import com.aspectran.shell.command.CommandRunner;
import com.aspectran.shell.console.ShellConsole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.aspectran.core.util.PBEncryptionUtils.ENCRYPTION_PASSWORD_KEY;

/**
 * <p>Created: 2021/05/16</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JettyCommandTest {

    private final CommandRunner runner = new TestCommandRunner();

    private ShellConsole getConsole() {
        return runner.getConsole();
    }

    @BeforeAll
    void saveProperties() {
        // System default
        System.setProperty(ENCRYPTION_PASSWORD_KEY, "encryption-password-for-test");
    }

    @Test
    void testJettyCommand() {
        JettyCommand command = new JettyCommand(runner.getCommandRegistry());
        //getConsole().writeLine(command.getDescriptor().getDescription());
        command.printHelp(getConsole());
    }

}
