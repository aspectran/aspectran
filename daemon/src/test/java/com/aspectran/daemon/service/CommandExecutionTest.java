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
package com.aspectran.daemon.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonExecutorConfig;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.service.CoreService;
import com.aspectran.daemon.command.AsyncCommandExecutor;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.EchoCommand;
import com.aspectran.embed.service.EmbeddedAspectran;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for command execution in {@link DefaultDaemonService}.
 */
class CommandExecutionTest {

    private static EmbeddedAspectran aspectran;
    private static DefaultDaemonService daemonService;

    @BeforeAll
    static void setup() throws Exception {
        AspectranConfig aspectranConfig = new AspectranConfig();
        aspectran = EmbeddedAspectran.run(aspectranConfig);
        daemonService = DefaultDaemonServiceBuilder.build((CoreService)aspectran);
        if (daemonService.getServiceLifeCycle().isOrphan()) {
            daemonService.start();
        }
        // Register EchoCommand for testing
        ((DaemonCommandRegistry)daemonService.getCommandRegistry()).addCommand(EchoCommand.class);
    }

    @AfterAll
    static void tearDown() {
        if (daemonService != null) {
            daemonService.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testExecuteCommand() {
        String apon = """
                command: echo
                arguments: {
                  item: [
                    { value: "Hello World" }
                  ]
                }""";
        CommandResult result = daemonService.execute(apon);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Command execution should be successful. Result: " + result.getResult());
        assertTrue(result.getResult().contains("Hello World"));
    }

    @Test
    void testExecuteUnknownCommand() {
        String apon = "command: unknown";
        CommandResult result = daemonService.execute(apon);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getResult().contains("Command not found"));
    }

    @Test
    void testMalformedApon() {
        String malformedApon = "command quit"; // missing colon
        CommandResult result = daemonService.execute(malformedApon);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getResult().contains("Malformed command data"));
    }

    @Test
    void testAsyncExecution() throws InterruptedException {
        DaemonExecutorConfig config = new DaemonExecutorConfig();
        AsyncCommandExecutor asyncExecutor = new AsyncCommandExecutor(daemonService.getCommandExecutor(), config);

        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("echo");
        ItemRule arg1 = new ItemRule();
        arg1.setValue("Async Hello");
        parameters.putArgument(arg1);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean successCalled = new AtomicBoolean(false);

        boolean accepted = asyncExecutor.execute(parameters, new AsyncCommandExecutor.Callback() {
            @Override
            public void success() {
                successCalled.set(true);
                latch.countDown();
            }

            @Override
            public void failure() {
                latch.countDown();
            }
        });

        assertTrue(accepted);
        latch.await(2, TimeUnit.SECONDS);
        assertTrue(successCalled.get(), "Async success callback should have been called. Result: " + parameters.getResult());
        assertTrue(parameters.getResult().contains("Async Hello"));

        asyncExecutor.shutdown();
    }

}
