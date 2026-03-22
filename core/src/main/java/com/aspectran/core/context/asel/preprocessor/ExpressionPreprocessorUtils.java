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

/**
 * Shared utilities for expression pre-processing.
 */
public abstract class ExpressionPreprocessorUtils {

    public static boolean isInsideQuotes(CharSequence s, int index) {
        boolean doubleQuoted = false;
        boolean singleQuoted = false;
        for (int i = 0; i < index; i++) {
            char c = s.charAt(i);
            if (c == '\"' && !singleQuoted) doubleQuoted = !doubleQuoted;
            else if (c == '\'' && !doubleQuoted) singleQuoted = !singleQuoted;
        }
        return (doubleQuoted || singleQuoted);
    }

    public static int findLeftOperandStart(CharSequence s, int end, boolean stopAtOperators) {
        int depth = 0;
        for (int i = end - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c == ')' || c == ']' || c == '}') depth++;
            else if (c == '(' || c == '[' || c == '{') {
                if (depth == 0) return i + 1;
                depth--;
            } else if (depth == 0) {
                if (c == ',' || (stopAtOperators && isOperator(c))) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    public static int findRightOperandEnd(CharSequence s, int start, boolean stopAtOperators) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' || c == '[' || c == '{') depth++;
            else if (c == ')' || c == ']' || c == '}') {
                if (depth == 0) return i;
                depth--;
            } else if (depth == 0) {
                if (c == ',') return i;
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

    public static boolean isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ||
                c == '=' || c == '<' || c == '>' || c == '&' || c == '|' ||
                c == '^' || c == '!' || c == '?' || c == ':');
    }

}
