/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * <p>Created: 2017. 3. 5.</p>
 */
public class CommandLineParserTest {

    @Test
    public void testExtractParameters() {
        CommandLineParser parser = CommandLineParser.parse("GET /path/work1 --param1 apple --param2=strawberry --arr=a --arr=b >> abcde.txt > 12345.txt");
        Assert.assertEquals(parser.getRequestMethod().toString(), "GET");
        Assert.assertEquals(parser.getCommandName(), "/path/work1");
        ParameterMap params = parser.extractParameters();
        Assert.assertEquals(params.getParameter("param1"), "apple");
        Assert.assertEquals(params.getParameter("param2"), "strawberry");
        Assert.assertEquals("a", params.getParameterValues("arr")[0]);
        Assert.assertEquals("b", params.getParameterValues("arr")[1]);
        Assert.assertEquals(parser.getRedirectionList().toString(), "[{operator=>>, operand=abcde.txt}, {operator=>, operand=12345.txt}]");
    }

    @Test
    public void testRedirectionOperators() {
        List<CommandLineRedirection> list = CommandLineParser.parse(">> abcde > 12345").getRedirectionList();
        Assert.assertEquals(list.get(0).getOperator(), CommandLineRedirection.Operator.APPEND_OUT);
        Assert.assertEquals(list.get(0).getOperand(), "abcde");
        Assert.assertEquals(list.get(1).getOperator(), CommandLineRedirection.Operator.OVERWRITE_OUT);
        Assert.assertEquals(list.get(1).getOperand(), "12345");
    }

    @Test
    public void testRedirectionOperators2() {
        List<CommandLineRedirection> list = CommandLineParser.parse("> '<abcde>' >> 12345").getRedirectionList();
        Assert.assertEquals(list.get(0).getOperator(), CommandLineRedirection.Operator.OVERWRITE_OUT);
        Assert.assertEquals(list.get(0).getOperand(), "'<abcde>'");
        Assert.assertEquals(list.get(1).getOperator(), CommandLineRedirection.Operator.APPEND_OUT);
        Assert.assertEquals(list.get(1).getOperand(), "12345");
    }

}
