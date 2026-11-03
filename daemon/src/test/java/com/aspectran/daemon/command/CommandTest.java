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
package com.aspectran.daemon.command;

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.type.ItemValueType;
import com.aspectran.daemon.TestDaemon;
import com.aspectran.daemon.command.builtins.InvokeActionCommand;
import com.aspectran.daemon.command.builtins.PollingIntervalCommand;
import com.aspectran.utils.apon.AponFormat;
import com.aspectran.utils.apon.AponWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testInvokeActionCommand() {
        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("invokeAction");
        parameters.setBeanName("class:com.aspectran.daemon.command.CommandTestBean");
        parameters.setMethodName("command1");

        Command command = new InvokeActionCommand(commandRegistry);
        CommandResult result = command.execute(parameters);
        assertEquals("<<command1>>", result.getResult());
    }

    @Test
    void testPollingIntervalCommand() {
        long oldInterval = daemon.getDaemonService().getFileCommander().getPollingInterval();

        ItemRule argItemRule = new ItemRule();
        argItemRule.setValue("2000");
        argItemRule.setValueType(ItemValueType.LONG);

        CommandParameters parameters = new CommandParameters();
        parameters.setCommandName("pollingInterval");
        parameters.putArgument(argItemRule);

        Command command = new PollingIntervalCommand(commandRegistry);
        CommandResult result = command.execute(parameters);
        assertEquals("The polling interval has been changed from " + oldInterval + "ms to 2000ms.", result.getResult());

        // Test fluent API with addArgument
        CommandParameters fluentParameters = new CommandParameters()
                .setCommandName("pollingInterval")
                .addArgument(3000L);

        CommandResult fluentResult = command.execute(fluentParameters);
        assertEquals("The polling interval has been changed from 2000ms to 3000ms.", fluentResult.getResult());
    }

    @Test
    void testFluentParameterBuilders() {
        CommandParameters parameters = new CommandParameters()
                .setCommandName("anatomy")
                .setParameter("mode", "data")
                .setParameter("timeout", 5000L)
                .setParameter("active", true)
                .setParameter("targets", new String[] {"node1", "node2"})
                .addArgument("mem")
                .addArgument(4000L, ItemValueType.LONG)
                .setProperty("prop1", "val1")
                .setAttribute("attr1", "val2");

        String apon = parameters.toString();
        assertTrue(apon.contains("command: anatomy"));
        assertTrue(apon.contains("name: mode"));
        assertTrue(apon.contains("value: data"));
        assertTrue(apon.contains("name: timeout"));
        assertTrue(apon.contains("valueType: long"));
        assertTrue(apon.contains("value: 5000"));
        assertTrue(apon.contains("name: active"));
        assertTrue(apon.contains("valueType: boolean"));
        assertTrue(apon.contains("value: true"));
        assertTrue(apon.contains("name: targets"));
        assertTrue(apon.contains("type: array"));
        assertTrue(apon.contains("value: mem"));
        assertTrue(apon.contains("name: prop1"));
        assertTrue(apon.contains("name: attr1"));

        ParameterMap parameterMap = parameters.getParameterMap();
        assertEquals("data", parameterMap.getParameter("mode"));
        assertEquals("5000", parameterMap.getParameter("timeout"));
        assertEquals("true", parameterMap.getParameter("active"));
        assertArrayEquals(new String[] {"node1", "node2"}, parameterMap.getParameterValues("targets"));
    }

    @Test
    void testEmptyResult() throws IOException {
        CommandParameters parameters = new CommandParameters();
        parameters.setTransletName("emptyResult");
        parameters.setResult("");

        AponWriter aponWriter = new AponWriter().nullWritable(false);
        aponWriter.write(parameters);
        aponWriter.close();

        String s1 = ("translet: emptyResult\n" + "result: (\n" + "  |\n" + ")").replace("\n", AponFormat.SYSTEM_NEW_LINE);
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

        String s1 = "translet: nullResult";
        String s2 = aponWriter.toString().trim();
        assertEquals(s1, s2);
    }

}
