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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2020/05/30</p>
 */
class AponReaderTest {

    @Test
    void singleQuoteEscapeTest() throws AponParseException {
        String input = "name: \"she\\u2019s \"";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        //System.out.println(parameters.getString("name"));
        assertEquals("she’s ", parameters.getString("name"));
    }

    @Test
    void noEscapeTest() throws AponParseException {
        String input = "name: she\u2019s";
        AponReader reader = new AponReader(input);
        Parameters parameters = reader.read();
        //System.out.println(parameters.getString("name"));
        assertEquals("she\u2019s", parameters.getString("name"));
    }

}
