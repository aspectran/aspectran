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
package com.aspectran.utils.apon;

import com.aspectran.utils.wildcard.IncludeExcludeParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for verifying the noBrackets option in ParameterKey.
 */
class AponNoBracketsTest {

    @Test
    void testNoBracketsForStringArray() {
        IncludeExcludeParameters params = new IncludeExcludeParameters();
        params.addIncludePattern("/**");
        params.addExcludePattern("/assets/**");
        params.addExcludePattern("/favicon.ico");

        String result = params.toString();

        // Expected output with repeated keys instead of [ ]
        String expected = """
                +: /**
                -: /assets/**
                -: /favicon.ico
                """;

        assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    @Test
    void testNoBracketsInNestedStructure() {
        // Simulating WebConfig structure
        Parameters webConfig = new VariableParameters();

        // Simulating AcceptableConfig (which inherits IncludeExcludeParameters)
        IncludeExcludeParameters acceptable = new IncludeExcludeParameters();
        acceptable.addIncludePattern("/**");
        acceptable.addExcludePattern("/assets/**");
        acceptable.addExcludePattern("/favicon.ico");

        webConfig.putValue("acceptable", acceptable);

        String result = webConfig.toString();

        String expected = """
                acceptable: {
                  +: /**
                  -: /assets/**
                  -: /favicon.ico
                }
                """;

        assertEquals(expected.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

    @Test
    void testRoundTripWithRepeatedKeys() throws IOException {
        String apon = """
                item: {
                  id: 1
                }
                item: {
                  id: 2
                }
                """;

        // Parsing without schema (using VariableParameters)
        Parameters root = AponReader.read(apon);

        // Writing back should preserve repeated keys instead of creating [ ]
        String result = root.toString();

        assertEquals(apon.replace("\r\n", "\n"), result.replace("\r\n", "\n"));
    }

}
