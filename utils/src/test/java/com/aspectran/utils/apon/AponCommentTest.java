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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for verifying comment formatting in AponWriter.
 */
class AponCommentTest {

    @Test
    void testCommentFormatting() throws IOException {
        Parameters params = new VariableParameters();
        params.putValue("system", null);

        AponWriter writer = new AponWriter();
        writer.comment("\ncomment line-1\ncomment line-2\ncomment line-3\n");
        writer.write(params);

        String expected = """
                #
                # comment line-1
                # comment line-2
                # comment line-3
                #
                system: null
                """;

        assertEquals(expected.replace("\r\n", "\n"), writer.toString().replace("\r\n", "\n"));
    }

}
