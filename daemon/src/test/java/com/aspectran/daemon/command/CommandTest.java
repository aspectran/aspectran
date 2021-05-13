/*
 * Copyright (c) 2008-2021 The Aspectran Project
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

import com.aspectran.core.util.apon.AponFormat;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.daemon.TestDaemon;
import com.aspectran.daemon.command.builtins.InvokeActionCommand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2018-12-24</p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandTest {

    private TestDaemon daemon;

    private CommandRegistry commandRegistry;

    @BeforeAll
    void ready() throws Exception {
        File baseDir = new File("./target/test-classes");
        String[] args = { baseDir.getCanonicalPath(), "config/aspectran-config.apon" };

        TestDaemon daemon = new TestDaemon();
        daemon.run(args);

        this.daemon = daemon;
        this.commandRegistry = daemon.getCommandRegistry();
    }

    @AfterAll
    void finish() {
        if (daemon != null) {
            daemon.release();
        }
    }

    @Test
    void testVerboseCommand() {
        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("invokeAction");
        parameters.setBeanName("class:com.aspectran.daemon.command.CommandTestBean");
        parameters.setMethodName("command1");

        Command command = new InvokeActionCommand(commandRegistry);
        CommandResult result = command.execute(parameters);
        assertEquals("<<command1>>", result.getMessage());
    }

    @Test
    void testEmptyResult() throws IOException {
        CommandParameters parameters = new CommandParameters();
        parameters.setTransletName("emptyResult");
        parameters.setResult("");

        AponWriter aponWriter = new AponWriter().nullWritable(false);
        aponWriter.write(parameters);
        aponWriter.close();

        String s1 = ("translet: emptyResult\n" + "result: (\n" + "  |\n" + ")").replace("\n", AponFormat.NEW_LINE);
        String s2 = aponWriter.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testNullResult() throws IOException {
        CommandParameters parameters = new CommandParameters();
        parameters.setTransletName("nullResult");
        parameters.setResult(null);

        AponWriter aponWriter = new AponWriter().nullWritable(false);
        aponWriter.write(parameters);
        aponWriter.close();

        String s1 = "translet: nullResult".replace("\n", AponFormat.NEW_LINE);
        String s2 = aponWriter.toString().trim();

        assertEquals(s1, s2);
    }

}
