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
 * <p>Created: 2020/05/29</p>
 */
class AponWriterTest {

    @Test
    void singleQuoteEscapeTest() throws IOException {
        String input = "'";

        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("param1"));
    }

    @Test
    void doubleQuoteEscapeTest() throws IOException {
        String input = "\"";

        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("param1"));
    }

    @Test
    void spacesEscapeTest() throws IOException {
        String input = " s ";

        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("param1"));
    }

    @Test
    void unicodeEscapeTest() throws IOException {
        String input = "\u2019";

        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("param1"));
    }

    @Test
    void escapedUnicodeTest() throws IOException {
        String input = "\\u2019";

        Parameters parameters = new VariableParameters();
        parameters.putValue("param1", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("param1"));
    }

    @Test
    void stringWithNewlinesWriteTest() throws IOException {
        String input = "1\n2\n3";
        input = input.replace("\n", AponFormat.SYSTEM_NEW_LINE);

        Parameters parameters = new VariableParameters();
        parameters.putValue("textParam", input);

        AponWriter writer = new AponWriter();
        writer.write(parameters);

        AponReader reader = new AponReader(writer.toString());
        Parameters output = reader.read();

        assertEquals(input, output.getString("textParam"));
    }

}
