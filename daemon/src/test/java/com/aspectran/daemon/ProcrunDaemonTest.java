/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.daemon;

import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandParameters;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProcrunDaemonTest {

    @Test
    @Order(1)
    void startProcrunDaemon() throws IOException {
        File baseDir = new File("./target/test-classes");
        String[] args = { baseDir.getCanonicalPath(), "config/aspectran-config.apon" };
        ProcrunDaemon.start(args, 500);
    }

    @Test
    @Order(2)
    void executeEchoCommand() {
        DefaultDaemon defaultDaemon = ProcrunDaemon.getDefaultDaemon();
        CommandExecutor commandExecutor = defaultDaemon.getCommandExecutor();
        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("echo");
        ItemRule arg1 = new ItemRule();
        arg1.setValue("Hello");
        parameters.putArgument(arg1);
        commandExecutor.execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                assertEquals(arg1.getValue(), parameters.getResult());
            }

            @Override
            public void failure() {
                assertEquals(arg1.getValue(), parameters.getResult());
            }
        });
    }

    @Test
    @Order(3)
    void executeSysInfoCommand() {
        DefaultDaemon defaultDaemon = ProcrunDaemon.getDefaultDaemon();
        CommandExecutor commandExecutor = defaultDaemon.getCommandExecutor();
        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("sysinfo");
        ItemRule arg1 = new ItemRule();
        arg1.setValue("props");
        parameters.putArgument(arg1);
        commandExecutor.execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                System.out.println(parameters.getResult());
            }

            @Override
            public void failure() {
                System.out.println(parameters.getResult());
            }
        });
    }

    @Test
    @Order(4)
    void stopProcrunDaemon() {
        ProcrunDaemon.stop();
    }

}
