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
package com.aspectran.core.context.asel.preprocessor;

import org.jspecify.annotations.NonNull;

/**
 * Shared utility methods for expression pre-processing.
 * <p>Provides methods for scanning operands and detecting string literals
 * within an expression string.</p>
 */
abstract class ExpressionPreprocessorUtils {

    /**
     * Checks if the character at the specified index is inside a string literal.
     * @param s the character sequence
     * @param index the index to check
     * @return true if inside quotes; false otherwise
     */
    static boolean isInsideQuotes(@NonNull CharSequence s, int index) {
        boolean doubleQuoted = false;
        boolean singleQuoted = false;
        boolean escaped = false;
        for (int i = 0; i < index; i++) {
            char c = s.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
            } else if (c == '\"' && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
            } else if (c == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
            }
        }
        return (doubleQuoted || singleQuoted);
    }

    /**
     * Finds the start index of the left operand for an operator.
     * @param s the character sequence
     * @param end the index where the operator starts
     * @param stopAtOperators whether to stop at other operators
     * @return the start index of the left operand
     */
    static int findLeftOperandStart(@NonNull CharSequence s, int end, boolean stopAtOperators) {
        int depth = 0;
        for (int i = end - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == ')' || c == ']' || c == '}') {
                depth++;
            } else if (c == '(' || c == '[' || c == '{') {
                if (depth == 0) {
                    return i + 1;
                }
                depth--;
            } else if (depth == 0) {
                if (c == ',' || (stopAtOperators && isOperator(c))) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    /**
     * Finds the end index of the right operand for an operator.
     * @param s the character sequence
     * @param start the index where the operator ends
     * @param stopAtOperators whether to stop at other operators
     * @return the end index of the right operand
     */
    static int findRightOperandEnd(@NonNull CharSequence s, int start, boolean stopAtOperators) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                depth++;
            } else if (c == ')' || c == ']' || c == '}') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            } else if (depth == 0) {
                if (c == ',') {
                    return i;
                }
                if (stopAtOperators) {
                    if (isOperator(c)) {
                        // Check if it's a collection operator start like ![ or ?[
                        if ((c == '!' || c == '?' || c == '^' || c == '$') &&
                                i + 1 < s.length() && s.charAt(i + 1) == '[') {
                            continue;
                        }
                        return i;
                    }
                }
            }
        }
        return s.length();
    }

    /**
     * Checks if the character is a standard operator.
     */
    static boolean isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ||
                c == '=' || c == '<' || c == '>' || c == '&' || c == '|' ||
                c == '^' || c == '!' || c == '?' || c == ':');
    }

}
