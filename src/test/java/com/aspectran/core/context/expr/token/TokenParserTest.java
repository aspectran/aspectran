/*
 * Copyright 2008-2017 Juho Jeong
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

import static junit.framework.TestCase.assertEquals;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Test cases that parse tokens.
 *
 * <p>Created: 2017. 3. 23.</p>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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

        //for (Token token : tokens) {
        //    System.out.println(token.stringify() + " = " + token.toString());
        //}

        assertEquals(tokens[0].stringify(), "${param1}");
        assertEquals(tokens[1].stringify(), "${param2:defaultStr}");
        assertEquals(tokens[2].stringify(), "@{attr1}");
        assertEquals(tokens[3].stringify(), "@{attr2:defaultStr}");
        assertEquals(tokens[4].stringify(), "@{attr3^invokeMethod}");
        assertEquals(tokens[5].stringify(), "%{prop1}");
        assertEquals(tokens[6].stringify(), "%{classpath:propertiesPath^getterName}");
        assertEquals(tokens[7].stringify(), "%{classpath:propertiesPath^getterName:defaultStr}");
        assertEquals(tokens[8].stringify(), "#{beanId}");
        assertEquals(tokens[9].stringify(), "#{beanId^getterName}");
        assertEquals(tokens[10].stringify(), "#{beanId^getterName:defaultStr}");
        assertEquals(tokens[11].stringify(), "#{class:beanClassName}");
        assertEquals(tokens[12].stringify(), "#{class:beanClassName^getterName}");
        assertEquals(tokens[13].stringify(), "#{class:beanClassName^getterName:defaultStr}");
        assertEquals(tokens[14].stringify(), "~{templateId}");
        assertEquals(tokens[15].stringify(), "~{templateId:defaultStr}");
    }



}