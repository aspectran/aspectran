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

import com.aspectran.core.context.rule.type.TokenDirectiveType;
import com.aspectran.core.context.rule.type.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Class Tokenizer.
 * 
 * <p>Created: 2008. 03. 29 AM 1:55:03</p>
 */
public class Tokenizer {

    private static final int MAX_TOKEN_NAME_LENGTH = 256;

    private static final int AT_TEXT = 1;

    private static final int AT_TOKEN_SYMBOL = 2;

    private static final int AT_TOKEN_NAME = 3;

    private static final int AT_TOKEN_VALUE = 4;

    private static final char CR = '\r';

    private static final char LF = '\n';

    /**
     * Returns a list of tokens that contains tokenized string.
     *
     * @param input the string to tokenize
     * @param textTrim whether to trim text
     * @return a list of tokens
     */
    public static List<Token> tokenize(CharSequence input, boolean textTrim) {
        if (input == null) {
            throw new IllegalArgumentException("Argument 'input' must not be null");
        }

        int inputLen = input.length();
        if (inputLen == 0) {
            List<Token> tokens = new ArrayList<>(1);
            tokens.add(new Token(""));
            return tokens;
        }

        int status = AT_TEXT;
        int tokenStartOffset = 0; // start position of token in the stringBuffer
        char symbol = Token.PARAMETER_SYMBOL; // PARAMETER_SYMBOL or ATTRIBUTE_SYMBOL
        char c;

        StringBuilder nameBuf = new StringBuilder();
        StringBuilder valueBuf = new StringBuilder();
        StringBuilder textBuf = new StringBuilder();

        List<Token> tokens = new ArrayList<>();

        for (int i = 0; i < inputLen; i++) {
            c = input.charAt(i);

            switch (status) {
            case AT_TEXT:
                textBuf.append(c);
                if (Token.isTokenSymbol(c)) {
                    symbol = c;
                    status = AT_TOKEN_SYMBOL;
                    // abc$ --> tokenStartOffset: 3
                    tokenStartOffset = textBuf.length() - 1;
                }
                break;
            case AT_TOKEN_SYMBOL:
                textBuf.append(c);
                if (c == Token.START_BRACKET) {
                    status = AT_TOKEN_NAME;
                } else {
                    if (Token.isTokenSymbol(c)) {
                        symbol = c;
                        status = AT_TOKEN_SYMBOL;
                        // abc$ --> tokenStartOffset: 3
                        tokenStartOffset = textBuf.length() - 1;
                    } else {
                        status = AT_TEXT;
                    }
                }
                break;
            case AT_TOKEN_NAME:
            case AT_TOKEN_VALUE:
                textBuf.append(c);
                if (status == AT_TOKEN_NAME) {
                    if (c == Token.VALUE_SEPARATOR) {
                        status = AT_TOKEN_VALUE;
                        break;
                    }
                }
                if (c == Token.END_BRACKET) {
                    if (nameBuf.length() > 0 || valueBuf.length() > 0) {
                        // save previous non-token string
                        if (tokenStartOffset > 0) {
                            String text = trimBuffer(textBuf, tokenStartOffset, textTrim);
                            Token token = new Token(text);
                            tokens.add(token);
                        }

                        // save token name and default value
                        Token token = createToken(symbol, nameBuf, valueBuf);
                        tokens.add(token);

                        status = AT_TEXT;
                        textBuf.setLength(0);
                        break;
                    }

                    status = AT_TEXT;
                    break;
                }
                if (status == AT_TOKEN_NAME) {
                    if (nameBuf.length() > MAX_TOKEN_NAME_LENGTH) {
                        status = AT_TEXT;
                        nameBuf.setLength(0);
                    }
                    nameBuf.append(c);
                } else {
                    valueBuf.append(c);
                }
                break;
            }
        }

        if (textBuf.length() > 0) {
            String text = trimBuffer(textBuf, textBuf.length(), textTrim);
            Token token = new Token(text);
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Create a token.
     *
     * @param symbol the token symbol
     * @param nameBuf the name buffer
     * @param valueBuf the value buffer
     * @return the token
     */
    private static Token createToken(char symbol, StringBuilder nameBuf, StringBuilder valueBuf) {
        String value = null;
        if (valueBuf.length() > 0) {
            value = valueBuf.toString();
            valueBuf.setLength(0); // empty the value buffer
        }

        if (nameBuf.length() > 0) {
            TokenType type = Token.resolveTypeAsSymbol(symbol);
            String name = nameBuf.toString();
            nameBuf.setLength(0); // empty the name buffer

            int offset = name.indexOf(Token.GETTER_SEPARATOR);
            if (offset > -1) {
                String name2 = name.substring(0, offset);
                String getter = name.substring(offset + 1);
                Token token = new Token(type, name2);
                if (!getter.isEmpty()) {
                    token.setGetterName(getter);
                }
                token.setDefaultValue(value);
                return token;
            } else if (value != null) {
                TokenDirectiveType directiveType = TokenDirectiveType.resolve(name);
                if (directiveType != null) {
                    String getter = null;
                    String defaultValue = null;
                    offset = value.indexOf(Token.GETTER_SEPARATOR);
                    if (offset > -1) {
                        String value2 = value.substring(0, offset);
                        String getter2 = value.substring(offset + 1);
                        value = value2;
                        offset = getter2.indexOf(Token.VALUE_SEPARATOR);
                        if (offset > -1) {
                            String getter3 = getter2.substring(0, offset);
                            String value3 = getter2.substring(offset + 1);
                            if (!getter3.isEmpty()) {
                                getter = getter3;
                            }
                            if (!value3.isEmpty()) {
                                defaultValue = value3;
                            }
                        } else {
                            if (!getter2.isEmpty()) {
                                getter = getter2;
                            }
                        }
                    }
                    Token token = new Token(type, directiveType, value);
                    token.setGetterName(getter);
                    token.setDefaultValue(defaultValue);
                    return token;
                } else {
                    Token token = new Token(type, name);
                    token.setDefaultValue(value);
                    return token;
                }
            } else {
                return new Token(type, name);
            }
        } else {
            // when not exists tokenName then tokenType must be TEXT type
            return new Token(value);
        }
    }

    /**
     * Returns a copy of the string, with leading and trailing whitespaces stripped.
     * <pre>
     * "   \r\n   aaa  \r\n  bbb  "   ==&gt;   "\naaa  \n  bbb"
     * "  aaa    \r\n   bbb   \r\n  "   ==&gt;   "aaa\nbbb\n"
     * </pre>
     *
     * @param textBuf the string builder object
     * @param end the ending index, exclusive.
     * @param trim whether to trim
     * @return the trimmed string
     */
    private static String trimBuffer(StringBuilder textBuf, int end, boolean trim) {
        if (!trim) {
            return textBuf.substring(0, end);
        }

        int start = 0;
        boolean leadingLF = false;
        boolean tailingLF = false;
        char c;

        // leading whitespace
        for (int i = 0; i < end; i++) {
            c = textBuf.charAt(i);

            if (c == LF || c == CR) {
                leadingLF = true;
            } else if (!Character.isWhitespace(c)) {
                start = i;
                break;
            }
        }

        if (leadingLF && start == 0) {
            return String.valueOf(LF);
        }

        // trailing whitespace
        for (int i = end - 1; i > start; i--) {
            c = textBuf.charAt(i);

            if (c == LF || c == CR) {
                tailingLF = true;
            } else if (!Character.isWhitespace(c)) {
                end = i + 1;
                break;
            }
        }

        // restore a new line character which is leading whitespace
        if (leadingLF) {
            textBuf.setCharAt(--start, LF);
        }

        // restore a new line character which is tailing whitespace
        if (tailingLF) {
            textBuf.setCharAt(end++, LF);
        }

        return textBuf.substring(start, end);
    }

    /**
     * Returns an array of tokens that is optimized.
     * Eliminates unnecessary white spaces for the first and last tokens.
     *
     * @param tokens the tokens before optimizing
     * @return the optimized tokens
     */
    public static Token[] optimize(Token[] tokens) {
        if (tokens == null) {
            return null;
        }

        String firstVal = null;
        String lastVal = null;

        if (tokens.length == 1) {
            if (tokens[0].getType() == TokenType.TEXT) {
                firstVal = tokens[0].getDefaultValue();
            }
        } else if (tokens.length > 1) {
            if (tokens[0].getType() == TokenType.TEXT) {
                firstVal = tokens[0].getDefaultValue();
            }
            if (tokens[tokens.length - 1].getType() == TokenType.TEXT) {
                lastVal = tokens[tokens.length - 1].getDefaultValue();
            }
        }

        if (firstVal != null) {
            String text = trimLeadingWhitespace(firstVal);
            if (!Objects.equals(firstVal, text)) {
                tokens[0] = new Token(text);
            }
        }

        if (lastVal != null && !lastVal.isEmpty()) {
            String text = trimTrailingWhitespace(lastVal);
            if (!Objects.equals(lastVal, text)) {
                tokens[tokens.length - 1] = new Token(text);
            }
        }

        return tokens;
    }

    /**
     * Returns a string that contains a copy of a specified string
     * without leading whitespaces.
     *
     * @param string the string to trim leading whitespaces
     * @return a string with leading whitespaces trimmed
     */
    private static String trimLeadingWhitespace(String string) {
        if (string.isEmpty()) {
            return string;
        }

        int start = 0;
        char c;

        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);
            if (!Character.isWhitespace(c)) {
                start = i;
                break;
            }
        }

        if (start == 0) {
            return string;
        }

        return string.substring(start);
    }

    /**
     * Returns a string that contains a copy of a specified string
     * without trailing whitespaces.
     *
     * @param string the string to trim trailing whitespaces
     * @return a string with trailing whitespaces trimmed
     */
    private static String trimTrailingWhitespace(String string) {
        int end = 0;
        char c;

        for (int i = string.length() - 1; i >= 0; i--) {
            c = string.charAt(i);
            if (!Character.isWhitespace(c)) {
                end = i;
                break;
            }
        }

        if (end == 0) {
            return string;
        }

        return string.substring(0, end + 1);
    }

}
