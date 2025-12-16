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
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A low-level utility that tokenizes strings containing AsEL expressions.
 * <p>This class implements a state-machine-based scanner that iterates through a character
 * sequence and splits it into a list of {@link Token} objects. It recognizes the syntax for
 * different token types (e.g., {@code ${...}}, {@code @{...}}), including their names, default
 * values, directives, and property accessors (getters).
 *
 * <p>This is a foundational utility used by {@link TokenParser} and is generally not
 * intended for direct use by application code. To prevent excessive memory usage with
 * malformed input, the maximum length for a token name is limited to 256 characters;
 * longer names will cause the parser to revert to treating the segment as plain text.</p>
 *
 * @see Token
 * @see TokenParser
 */
public class Tokenizer {

    private static final int MAX_TOKEN_NAME_LENGTH = 256;

    private static final int AT_TEXT = 1;

    private static final int AT_SYMBOL = 2;

    private static final int AT_NAME = 3;

    private static final int AT_VALUE = 4;

    private static final char CR = '\r';

    private static final char LF = '\n';

    /**
     * Tokenizes a character sequence into a list of {@link Token} objects.
     * <p>This is the core tokenizing method that drives the parsing process. It uses a state
     * machine to differentiate between plain text and special token expressions.</p>
     * @param chars the character sequence to tokenize
     * @param textTrim if {@code true}, applies special trimming logic to plain text tokens
     *      (see {@link #extract(CharSequence, int, int, boolean)})
     * @return a list of parsed tokens
     * @throws IllegalArgumentException if the input character sequence is null
     */
    @NonNull
    public static List<Token> tokenize(CharSequence chars, boolean textTrim) {
        if (chars == null) {
            throw new IllegalArgumentException("chars must not be null");
        }

        int inputLen = chars.length();
        if (inputLen == 0) {
            List<Token> tokens = new ArrayList<>(1);
            tokens.add(new Token(""));
            return tokens;
        }

        StringBuilder nameBuf = new StringBuilder();
        StringBuilder valueBuf = new StringBuilder();
        int start = 0; // start index of chars considered a text token
        int end = 0; // end index of chars considered a text token
        int symbolStart = -1; // start index of the symbol character
        char symbol = Token.PARAMETER_SYMBOL;
        int state = AT_TEXT;
        char c;

        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < inputLen; i++) {
            c = chars.charAt(i);
            end++;
            switch (state) {
                case AT_TEXT:
                    if (Token.isTokenSymbol(c)) {
                        symbol = c;
                        state = AT_SYMBOL;
                        symbolStart = end - 1;
                    }
                    break;
                case AT_SYMBOL:
                    if (c == Token.BRACKET_OPEN) {
                        nameBuf.setLength(0);
                        state = AT_NAME;
                    } else if (Token.isTokenSymbol(c)) {
                        symbol = c;
                        symbolStart = end - 1;
                    } else {
                        state = AT_TEXT;
                    }
                    break;
                case AT_NAME:
                    if (c == Token.VALUE_DELIMITER) {
                        valueBuf.setLength(0);
                        state = AT_VALUE;
                        break;
                    }
                    if (c == Token.BRACKET_CLOSE) {
                        if (!nameBuf.isEmpty()) {
                            if (symbolStart > start) {
                                Token token = createToken(chars, start, symbolStart, textTrim);
                                tokens.add(token);
                            }
                            Token token = createToken(symbol, nameBuf, null);
                            tokens.add(token);
                            start = end;
                        }
                        state = AT_TEXT;
                    } else {
                        nameBuf.append(c);
                        // if the name is too long, it is treated as a text token
                        if (nameBuf.length() > MAX_TOKEN_NAME_LENGTH) {
                            nameBuf.setLength(0);
                            state = AT_TEXT;
                        }
                    }
                    break;
                case AT_VALUE:
                    if (c == Token.BRACKET_CLOSE) {
                        if (!valueBuf.isEmpty()) {
                            if (symbolStart > start) {
                                Token token = createToken(chars, start, symbolStart, textTrim);
                                tokens.add(token);
                            }
                            Token token = createToken(symbol, nameBuf, valueBuf);
                            tokens.add(token);
                            start = end;
                        }
                        state = AT_TEXT;
                    } else {
                        valueBuf.append(c);
                    }
                    break;
                }
        }
        // any remaining text that is not tokenized is created as a text token
        if (start < end) {
            Token token = createToken(chars, start, end, textTrim);
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * Creates a special token (e.g., parameter, attribute) from its constituent parts.
     * <p>This method is responsible for the complex logic of parsing the token's internal
     * structure, including directives, getters, and default values, from the raw name and
     * value buffers.</p>
     * @param symbol the special character that identifies the token type (e.g., '$', '@')
     * @param nameBuf the buffer containing the token's name part (which may include a getter)
     * @param valueBuf the buffer containing the token's value part (which may include a default value),
     *      or {@code null} if not present
     * @return a new {@link Token} instance representing the special token
     */
    @NonNull
    private static Token createToken(char symbol, StringBuilder nameBuf, StringBuilder valueBuf) {
        String value = null;
        if (valueBuf != null && !valueBuf.isEmpty()) {
            value = valueBuf.toString();
        }

        if (!nameBuf.isEmpty()) {
            TokenType type = Token.resolveTypeAsSymbol(symbol);
            String name = nameBuf.toString();

            int offset = name.indexOf(Token.GETTER_DELIMITER);
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
                    offset = value.indexOf(Token.GETTER_DELIMITER);
                    if (offset > -1) {
                        String value2 = value.substring(0, offset);
                        String getter2 = value.substring(offset + 1);
                        value = value2;
                        offset = getter2.indexOf(Token.VALUE_DELIMITER);
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
                    } else {
                        offset = value.indexOf(Token.VALUE_DELIMITER);
                        if (offset > -1) {
                            String value2 = value.substring(0, offset);
                            String value3 = value.substring(offset + 1);
                            value = value2;
                            if (!value3.isEmpty()) {
                                defaultValue = value3;
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
     * Creates a plain text token from a subsequence of the input characters.
     * @param chars the original character sequence
     * @param start the start index of the text segment
     * @param end the end index of a text segment
     * @param trim whether to apply special trimming logic to the text
     * @return a new text {@link Token}
     */
    @NonNull
    private static Token createToken(CharSequence chars, int start, int end, boolean trim) {
        String text = extract(chars, start, end, trim);
        return new Token(text);
    }

    /**
     * Extracts and trims text from a subsequence of characters.
     * <p>This method implements a special trimming logic. Unlike {@link String#trim()}, it preserves
     * a single newline character ({@code \n}) if the trimmed whitespace contained any newlines.
     * This is intended to retain significant line breaks in formatted text blocks while removing
     * extraneous indentation and spacing.</p>
     * <p>For example:
     * <pre>
     * "   \r\n   aaa  \r\n  bbb  "   -&gt;   "\naaa  \n  bbb"
     * "  aaa    \r\n   bbb   \r\n  "   -&gt;   "aaa\nbbb\n"
     * </pre>
     * @param chars the original character sequence
     * @param start the start index of the text segment
     * @param end the end index of a text segment
     * @param trim whether to apply this special trimming logic
     * @return the extracted and potentially trimmed string
     */
    private static String extract(CharSequence chars, int start, int end, boolean trim) {
        if (!trim) {
            return chars.subSequence(start, end).toString();
        }

        boolean leadingLF = false;
        boolean tailingLF = false;
        int subStart = start;
        int subEnd = end;

        // leading whitespace
        for (int i = start; i < end; i++) {
            char c = chars.charAt(i);
            if (c == LF || c == CR) {
                leadingLF = true;
            } else if (!Character.isWhitespace(c)) {
                subStart = i;
                break;
            }
        }

        if (leadingLF && start == subStart) {
            return String.valueOf(LF);
        }

        // trailing whitespace
        for (int i = end - 1; i > start; i--) {
            char c = chars.charAt(i);
            if (c == LF || c == CR) {
                tailingLF = true;
            } else if (!Character.isWhitespace(c)) {
                subEnd = i + 1;
                break;
            }
        }

        String str = chars.subSequence(subStart, subEnd).toString();
        if (leadingLF && tailingLF) {
            return LF + str + LF; // restore a new line character which is leading and tailing whitespace
        } else if (leadingLF) {
            return LF + str; // restore a new line character which is leading whitespace
        } else if (tailingLF) {
            return str + LF; // restore a new line character which is tailing whitespace
        } else {
            return str;
        }
    }

    /**
     * Optimizes an array of tokens by trimming leading and trailing whitespace
     * from the first and last text tokens, respectively.
     * @param tokens the array of tokens to optimize
     * @return the optimized array of tokens, or {@code null} if the input is null
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
     * Trims leading whitespace from a string.
     * @param str the string to trim
     * @return the trimmed string
     */
    @NonNull
    private static String trimLeadingWhitespace(@NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        int start = 0;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                start = i;
                break;
            }
        }
        if (start == 0) {
            return str;
        }
        return str.substring(start);
    }

    /**
     * Trims trailing whitespace from a string.
     * @param str the string to trim
     * @return the trimmed string
     */
    private static String trimTrailingWhitespace(@NonNull String str) {
        int end = 0;
        char c;
        for (int i = str.length() - 1; i >= 0; i--) {
            c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                end = i;
                break;
            }
        }
        if (end == 0) {
            return str;
        }
        return str.substring(0, end + 1);
    }

}
