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

import com.aspectran.core.activity.request.ParameterMap;
import org.junit.jupiter.api.Test;

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
        assertEquals(transletCommandLine.getRequestName(), "/path/work1");
        ParameterMap params = transletCommandLine.getParameterMap();
        assertEquals(params.getParameter("param1"), "apple");
        assertEquals(params.getParameter("param2"), "strawberry");
        assertEquals("a", params.getParameterValues("arr")[0]);
        assertEquals("b", params.getParameterValues("arr")[1]);
    }

}
