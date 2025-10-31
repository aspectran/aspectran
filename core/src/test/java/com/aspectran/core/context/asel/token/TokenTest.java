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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for the Token class.
 *
 * <p>Created: 20/11/2018</p>
 */
class TokenTest {

    @Test
    void testHasToken() {
        assertTrue(Token.hasToken("a${#{x}z"));
        assertTrue(Token.hasToken("@{name}"));
        assertTrue(Token.hasToken("#{bean.id}"));
    }

    @Test
    void testConstructors() {
        // Test text token
        Token textToken = new Token("some text");
        assertEquals(TokenType.TEXT, textToken.getType());
        assertEquals("some text", textToken.getDefaultValue());

        // Test special token
        Token paramToken = new Token(TokenType.PARAMETER, "paramName");
        assertEquals(TokenType.PARAMETER, paramToken.getType());
        assertEquals("paramName", paramToken.getName());

        // Test special token with directive
        Token beanToken = new Token(TokenType.BEAN, TokenDirectiveType.CLASS, "com.example.TestBean");
        assertEquals(TokenType.BEAN, beanToken.getType());
        assertEquals(TokenDirectiveType.CLASS, beanToken.getDirectiveType());
        assertEquals("class", beanToken.getName());
        assertEquals("com.example.TestBean", beanToken.getValue());

        // Test invalid constructor usage
        assertThrows(UnsupportedOperationException.class, () -> new Token(TokenType.TEXT, "invalid"));
        assertThrows(IllegalArgumentException.class, () -> new Token(null, "invalid"));
    }

    @Test
    void testStringify() {
        // Text
        Token textToken = new Token("just text");
        assertEquals("just text", textToken.stringify());

        // Parameter
        Token paramToken = new Token(TokenType.PARAMETER, "param1");
        assertEquals("${param1}", paramToken.stringify());
        paramToken.setDefaultValue("default");
        assertEquals("${param1:default}", paramToken.stringify());

        // Attribute
        Token attrToken = new Token(TokenType.ATTRIBUTE, "attr1");
        attrToken.setGetterName("name");
        assertEquals("@{attr1^name}", attrToken.stringify());
        attrToken.setDefaultValue("default");
        assertEquals("@{attr1^name:default}", attrToken.stringify());

        // Bean with directive
        Token beanToken = new Token(TokenType.BEAN, TokenDirectiveType.CLASS, "com.example.MyBean");
        assertEquals("#{class:com.example.MyBean}", beanToken.stringify());
        beanToken.setGetterName("prop");
        assertEquals("#{class:com.example.MyBean^prop}", beanToken.stringify());

        // Property
        Token propToken = new Token(TokenType.PROPERTY, "my.prop");
        assertEquals("%{my.prop}", propToken.stringify());

        // Template
        Token templateToken = new Token(TokenType.TEMPLATE, "myTemplate");
        assertEquals("~{myTemplate}", templateToken.stringify());
    }

    @Test
    void testEqualsAndHashCode() {
        Token token1 = new Token(TokenType.PARAMETER, "param1");
        token1.setDefaultValue("default");

        Token token2 = new Token(TokenType.PARAMETER, "param1");
        token2.setDefaultValue("default");

        Token token3 = new Token(TokenType.PARAMETER, "param2");
        token3.setDefaultValue("default");

        Token token4 = new Token(TokenType.ATTRIBUTE, "param1");
        token4.setDefaultValue("default");

        // Reflexive
        assertEquals(token1, token1);

        // Symmetric
        assertEquals(token1, token2);
        assertEquals(token2, token1);

        // Hash code
        assertEquals(token1.hashCode(), token2.hashCode());

        // Not equal
        assertNotEquals(token1, token3);
        assertNotEquals(token1, token4);
        assertNotEquals(token2, token3);
    }

    @Test
    void testReplicate() {
        Token original = new Token(TokenType.BEAN, TokenDirectiveType.CLASS, "com.example.MyBean");
        original.setGetterName("prop");
        original.setDefaultValue("defaultValue");

        Token replica = original.replicate();

        assertNotSame(original, replica);
        assertEquals(original, replica);
        assertEquals(original.hashCode(), replica.hashCode());
        assertEquals(original.stringify(), replica.stringify());
    }

}
