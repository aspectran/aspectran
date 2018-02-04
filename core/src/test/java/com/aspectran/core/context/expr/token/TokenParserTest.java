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
package com.aspectran.core.context.expr.token;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Test cases that parse tokens.
 *
 * <p>Created: 2017. 3. 23.</p>
 */
public class TokenParserTest {

    @Test
    public void parsingTest() {
        String text = "${emailId}@${emailDomain}#{aaaa";

        Token[] tokens = TokenParser.parse(text);

        //for (Token token : tokens) {
        //    System.out.println(token.stringify() + " = " + token.toString());
        //}

        assertEquals(tokens[0].stringify(), "${emailId}");
        assertEquals(tokens[1].stringify(), "@");
        assertEquals(tokens[2].stringify(), "${emailDomain}");
        assertEquals(tokens[3].stringify(), "#{aaaa");
    }

    @Test
    public void equalsTest() {
        String text = "${param1}" +
                "${param2:defaultStr}" +
                "@{attr1}" +
                "@{attr2:defaultStr}" +
                "@{attr3^invokeMethod}" +
                "%{prop1}" +
                "%{classpath:propertiesPath^getterName}" +
                "%{classpath:propertiesPath^getterName:defaultStr}" +
                "#{beanId}" +
                "#{beanId^getterName}" +
                "#{beanId^getterName:defaultStr}" +
                "#{class:beanClassName}" +
                "#{class:beanClassName^getterName}" +
                "#{class:beanClassName^getterName:defaultStr}" +
                "~{templateId}" +
                "~{templateId:defaultStr}";

        Token[] tokens = TokenParser.parse(text);

        for (Token token : tokens) {
            //System.out.println(token.stringify() + " = " + token.toString());
            assertTrue(token.equals(token.replicate()));
            assertEquals(token.stringify(), token.replicate().stringify());
        }
    }

}