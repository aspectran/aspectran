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
package com.aspectran.daemon.command;

import com.aspectran.daemon.TestDaemon;
import com.aspectran.daemon.command.builtin.BeanActionCommand;
import com.aspectran.daemon.command.polling.CommandParameters;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;

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
        this.commandRegistry = new CommandRegistry(daemon);
    }

    @AfterAll
    void finish() {
        if (daemon != null) {
            daemon.release();
        }
    }

    @Test
    void testVerboseCommand() throws Exception {
        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("beanMethodAction");
        parameters.setBeanName("class:com.aspectran.daemon.command.CommandTestBean");
        parameters.setMethodName("command1");

        Command command = new BeanActionCommand(commandRegistry);
        String result = command.execute(parameters);
        assertEquals("<<command1>>", result);
    }

}