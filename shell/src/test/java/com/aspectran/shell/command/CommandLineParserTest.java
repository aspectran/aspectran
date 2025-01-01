/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2017. 3. 5.</p>
 */
class CommandLineParserTest {

    @Test
    void testQuotes() {
        CommandLineParser lineParser = new CommandLineParser("encrypt -p=password \" a b c \" ' ? ' e 'f - g          '");
        assertEquals("[-p=password,  a b c ,  ? , e, f - g          ]", Arrays.toString(lineParser.getArgs()));
    }

    @Test
    void testShift() {
        CommandLineParser lineParser = new CommandLineParser("command arg1 arg2 arg3");
        assertEquals("command", lineParser.getCommandName());
        lineParser.shift();
        assertEquals("arg1", lineParser.getCommandName());
        lineParser.shift();
        assertEquals("arg2", lineParser.getCommandName());
        lineParser.shift();
        assertEquals("arg3", lineParser.getCommandName());
    }

    @Test
    void testRedirection() {
        CommandLineParser lineParser = new CommandLineParser("  ' encrypt ' ' arg1 ' arg2 >  ' C:\\temp\\hello.txt  '  ");
        /*
        System.out.println(lineParser.getCommandLine());
        System.out.println(lineParser.getCommandName());
        System.out.println(Arrays.toString(lineParser.getArgs()));
        System.out.println(lineParser.getRedirectionList());
        */
        List<OutputRedirection> list = lineParser.getRedirectionList();
        assertEquals("' encrypt ' ' arg1 ' arg2", lineParser.getCommandLine());
        assertEquals(" encrypt ", lineParser.getCommandName());
        assertEquals("[ arg1 , arg2]", Arrays.toString(lineParser.getArgs()));
        assertEquals("[{operator=>, operand= C:\\temp\\hello.txt  }]", list.toString());
        assertEquals(OutputRedirection.Operator.OVERWRITE_OUT, list.get(0).getOperator());
    }

    @Test
    void testRedirectionOperators() {
        CommandLineParser lineParser = new CommandLineParser(">> abcde > 12345");
        List<OutputRedirection> list = lineParser.getRedirectionList();
        assertEquals(OutputRedirection.Operator.APPEND_OUT, list.get(0).getOperator());
        assertEquals("abcde", list.get(0).getOperand());
        assertEquals(OutputRedirection.Operator.OVERWRITE_OUT, list.get(1).getOperator());
        assertEquals("12345", list.get(1).getOperand());
    }

    @Test
    void testRedirectionOperators2() {
        CommandLineParser lineParser = new CommandLineParser("> '<abcde>' >> \" 12345 \"  ");
        List<OutputRedirection> list = lineParser.getRedirectionList();
        assertEquals(OutputRedirection.Operator.OVERWRITE_OUT, list.get(0).getOperator());
        assertEquals("<abcde>", list.get(0).getOperand());
        assertEquals(OutputRedirection.Operator.APPEND_OUT, list.get(1).getOperator());
        assertEquals(" 12345 ", list.get(1).getOperand());
    }

}
