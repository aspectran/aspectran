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
package com.aspectran.core.context.asel.token;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases that parse tokens.
 *
 * <p>Created: 2017. 3. 23.</p>
 */
class TokenParserTest {

    @Test
    void parsingTest() {
        String text = "${emailId}@${emailDomain}#{aaaa";

        Token[] tokens = TokenParser.parse(text);

        assertEquals(4, tokens.length);
        assertEquals("${emailId}", tokens[0].stringify());
        assertEquals("@", tokens[1].stringify());
        assertEquals("${emailDomain}", tokens[2].stringify());
        assertEquals("#{aaaa", tokens[3].stringify());
    }

    @Test
    void equalsTest() {
        String text = """
                ${param1}\
                ${param2:defaultStr}\
                @{attr1}\
                @{attr2:defaultStr}\
                @{attr3^invokeMethod}\
                %{prop1}\
                %{classpath:propertiesPath^getterName}\
                %{classpath:propertiesPath^getterName:defaultStr}\
                %{system:aaa.bbb.ccc}\
                %{system:aaa.bbb.ccc:defaultStr}\
                #{beanId}\
                #{beanId^getterName}\
                #{beanId^getterName:defaultStr}\
                #{class:beanClassName}\
                #{class:beanClassName^getterName}\
                #{class:beanClassName^getterName:defaultStr}\
                ~{templateId}\
                ~{templateId:defaultStr}""";

        Token[] tokens = TokenParser.parse(text);

        for (Token token : tokens) {
            //System.out.println(token.stringify() + " = " + token.toString());
            assertEquals(token, token.replicate());
            assertEquals(token.stringify(), token.replicate().stringify());
        }
    }

    @Test
    void longNameToken() {
        //260
        String text = "${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJ}"
            + "${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEF}";

        Token[] tokens = TokenParser.parse(text);

        assertEquals(2, tokens.length);
        assertEquals("{type=text, defaultValue=${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJ}}", tokens[0].toString());
        assertEquals("{type=parameter, name=ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEF}", tokens[1].toString());
    }

    @Test
    void withWhitespaces() {
        String text = " \n\r ${emailId}@${emailDomain}\n\n";

        Token[] tokens = TokenParser.parse(text, true);

        assertEquals(5, tokens.length);
        assertEquals("\n", tokens[0].stringify());
        assertEquals("${emailId}", tokens[1].stringify());
        assertEquals("@", tokens[2].stringify());
        assertEquals("${emailDomain}", tokens[3].stringify());
        assertEquals("\n", tokens[4].stringify());
    }

    @Test
    void testSystemProperties() {
        String text = "%{system:aaa.bbb.ccc:ddd}";

        Token[] tokens = TokenParser.parse(text);

        assertEquals(1, tokens.length);
        assertEquals("%{system:aaa.bbb.ccc:ddd}", tokens[0].stringify());
        assertEquals("ddd", tokens[0].getDefaultValue());
    }

}
