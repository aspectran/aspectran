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
package com.aspectran.web.support.util;

import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>This class is a clone of org.springframework.web.util.JavaScriptUtils</p>
 *
 * Utility class for JavaScript escaping.
 * Escapes based on the JavaScript 1.5 recommendation.
 *
 * <p>Reference:
 * <a href="https://developer.mozilla.org/en-US/docs/JavaScript/Guide/Values,_variables,_and_literals#String_literals">
 * JavaScript Guide</a> on Mozilla Developer Network.
 */
public abstract class JavaScriptUtils {

    /**
     * Turn JavaScript special characters into escaped characters.
     * @param input the input string
     * @return the string with escaped characters
     */
    @NonNull
    public static String javaScriptEscape(@NonNull String input) {
        StringBuilder filtered = new StringBuilder(input.length());
        char prevChar = '\u0000';
        char c;
        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);
            if (c == '"') {
                filtered.append("\\\"");
            }
            else if (c == '\'') {
                filtered.append("\\'");
            }
            else if (c == '\\') {
                filtered.append("\\\\");
            }
            else if (c == '/') {
                filtered.append("\\/");
            }
            else if (c == '\t') {
                filtered.append("\\t");
            }
            else if (c == '\n') {
                if (prevChar != '\r') {
                    filtered.append("\\n");
                }
            }
            else if (c == '\r') {
                filtered.append("\\n");
            }
            else if (c == '\f') {
                filtered.append("\\f");
            }
            else if (c == '\b') {
                filtered.append("\\b");
            }
            // No '\v' in Java, use octal value for VT ascii char
            else if (c == '\013') {
                filtered.append("\\v");
            }
            else if (c == '<') {
                filtered.append("\\u003C");
            }
            else if (c == '>') {
                filtered.append("\\u003E");
            }
            // Unicode for PS (line terminator in ECMA-262)
            else if (c == '\u2028') {
                filtered.append("\\u2028");
            }
            // Unicode for LS (line terminator in ECMA-262)
            else if (c == '\u2029') {
                filtered.append("\\u2029");
            }
            else {
                filtered.append(c);
            }
            prevChar = c;

        }
        return filtered.toString();
    }

}
