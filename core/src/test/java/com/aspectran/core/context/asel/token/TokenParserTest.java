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

import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases that parse tokens.
 *
 * <p>Created: 2017. 3. 23.</p>
 */
class TokenParserTest {

    @Test
    void testSimpleParsing() {
        String text = "${emailId}@${emailDomain}#{aaaa";

        Token[] tokens = TokenParser.parse(text);

        assertEquals(4, tokens.length);
        assertEquals("${emailId}", tokens[0].stringify());
        assertEquals("@", tokens[1].stringify());
        assertEquals("${emailDomain}", tokens[2].stringify());
        assertEquals("#{aaaa", tokens[3].stringify());
    }

    @Test
    void testEquals() {
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
    void testLongNameToken() {
        //260
        String text = "${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJ}"
            + "${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEF}";

        Token[] tokens = TokenParser.parse(text);

        assertEquals(2, tokens.length);
        assertEquals("{type=text, value=${ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJ}}", tokens[0].toString());
        assertEquals("{type=parameter, name=ABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEFGHIJABCDEF}", tokens[1].toString());
    }

    @Test
    void testWithWhitespaces() {
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

    @Test
    void testParseAsList() {
        String text = "value1 @{attr1} #{bean.prop}";
        List<Token[]> tokensList = TokenParser.parseAsList(text);
        assertNotNull(tokensList);
        assertEquals(3, tokensList.size());
        assertEquals("value1", tokensList.get(0)[0].getDefaultValue());
        assertEquals("attr1", tokensList.get(1)[0].getName());
        assertEquals("bean.prop", tokensList.get(2)[0].getName());
    }

    @Test
    void testParseAsMap() {
        // "key1:value1" is not a token format and is ignored.
        // The token "@{attr2}" has a name but no value, so it is ignored.
        String text1 = "key1:value1 @{attr2}";
        Map<String, Token[]> tokensMap1 = TokenParser.parseAsMap(text1);
        assertNull(tokensMap1);

        // The token "%{system:java.version}" has a name and a value, but no default value, so it is ignored.
        String text2 = "%{system:java.version}";
        Map<String, Token[]> tokensMap2 = TokenParser.parseAsMap(text2);
        assertNull(tokensMap2);
    }

    @Test
    void testParsePathSafely() {
        // Case 1: Safe tokens
        Token[] tokens1 = TokenParser.parsePathSafely("/path/${param}/and/@{attr}");
        assertNotNull(tokens1);
        assertEquals(4, tokens1.length);
        assertEquals(TokenType.TEXT, tokens1[0].getType());
        assertEquals(TokenType.PARAMETER, tokens1[1].getType());
        assertEquals(TokenType.TEXT, tokens1[2].getType());
        assertEquals(TokenType.ATTRIBUTE, tokens1[3].getType());

        // Case 2: Unsafe tokens
        Token[] tokens2 = TokenParser.parsePathSafely("/path/#{bean}/and/%{prop}");
        assertNull(tokens2);

        // Case 3: No tokens
        Token[] tokens3 = TokenParser.parsePathSafely("/path/static/string");
        assertNull(tokens3);

        // Case 4: Empty/Null
        assertNull(TokenParser.parsePathSafely(null));
        assertNull(TokenParser.parsePathSafely(""));
    }

    @Test
    void testComplexTokenParsing() {
        String text = "%{classpath:com/example/config.properties^db.password:defaultPassword}";
        Token[] tokens = TokenParser.parse(text);
        assertEquals(1, tokens.length);
        Token token = tokens[0];

        assertEquals(TokenType.PROPERTY, token.getType());
        assertEquals(TokenDirectiveType.CLASSPATH, token.getDirectiveType());
        assertEquals("com/example/config.properties", token.getValue());
        assertEquals("db.password", token.getGetterName());
        assertEquals("defaultPassword", token.getDefaultValue());
    }

}
