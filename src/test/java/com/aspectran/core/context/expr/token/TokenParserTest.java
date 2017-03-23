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

        assertEquals(tokens[0].toString(), "{type=parameter, name=emailId}");
        assertEquals(tokens[1].toString(), "{type=text, value=@}");
        assertEquals(tokens[2].toString(), "{type=parameter, name=emailDomain}");
        assertEquals(tokens[3].toString(), "{type=text, value=#{aaaa}");

        //for (Token token : tokens) {
        //    System.out.println(token.toString());
        //}

    }

}