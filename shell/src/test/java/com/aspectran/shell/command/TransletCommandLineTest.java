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

import com.aspectran.core.activity.request.parameter.ParameterMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-01-21</p>
 */
class TransletCommandLineTest {

    @Test
    void testExtractParameters() {
        CommandLineParser lineParser = new CommandLineParser("GET /path/work1 --param1 apple --param2=strawberry --arr=a --arr=b >> abcde.txt > 12345.txt");
        TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
        assertEquals(transletCommandLine.getRequestMethod().toString(), "GET");
        assertEquals(transletCommandLine.getTransletName(), "/path/work1");
        ParameterMap params = transletCommandLine.getParameterMap();
        assertEquals(params.getParameter("param1"), "apple");
        assertEquals(params.getParameter("param2"), "strawberry");
        assertEquals("a", params.getParameterValues("arr")[0]);
        assertEquals("b", params.getParameterValues("arr")[1]);
        assertEquals(transletCommandLine.getRedirectionList().toString(), "[{operator=>>, operand=abcde.txt}, {operator=>, operand=12345.txt}]");
    }

    @Test
    void testRedirectionOperators() {
        CommandLineParser lineParser = new CommandLineParser(">> abcde > 12345");
        TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
        List<TransletOutputRedirection> list = transletCommandLine.getRedirectionList();
        assertEquals(list.get(0).getOperator(), TransletOutputRedirection.Operator.APPEND_OUT);
        assertEquals(list.get(0).getOperand(), "abcde");
        assertEquals(list.get(1).getOperator(), TransletOutputRedirection.Operator.OVERWRITE_OUT);
        assertEquals(list.get(1).getOperand(), "12345");
    }

    @Test
    void testRedirectionOperators2() {
        CommandLineParser lineParser = new CommandLineParser("> '<abcde>' >> 12345");
        TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
        List<TransletOutputRedirection> list = transletCommandLine.getRedirectionList();
        assertEquals(list.get(0).getOperator(), TransletOutputRedirection.Operator.OVERWRITE_OUT);
        assertEquals(list.get(0).getOperand(), "'<abcde>'");
        assertEquals(list.get(1).getOperator(), TransletOutputRedirection.Operator.APPEND_OUT);
        assertEquals(list.get(1).getOperand(), "12345");
    }

}