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

import static com.aspectran.core.context.asel.preprocessor.ExpressionPreprocessorUtils.isInsideQuotes;

/**
 * Preprocessor for the Type operator T(class).
 * <p>Converts 'T(java.lang.Math)' to '@java.lang.Math@'.</p>
 */
public class TypeOperatorPreprocessor implements ExpressionPreprocessor {

    @Override
    public String preprocess(String expression) {
        if (!expression.contains("T(")) {
            return expression;
        }

        StringBuilder sb = new StringBuilder(expression);
        int index = 0;
        while ((index = sb.indexOf("T(", index)) != -1) {
            if (isInsideQuotes(sb, index)) {
                index += 2;
                continue;
            }

            int closingParen = findMatchingClosingParen(sb, index + 2);
            if (closingParen != -1) {
                String className = sb.substring(index + 2, closingParen).trim();
                int replacementEnd = closingParen + 1;
                
                // Skip optional whitespace after ')'
                int dotIndex = replacementEnd;
                while (dotIndex < sb.length() && Character.isWhitespace(sb.charAt(dotIndex))) {
                    dotIndex++;
                }
                
                // If followed by a dot (e.g., T(class).member), remove the dot for OGNL static access
                if (dotIndex < sb.length() && sb.charAt(dotIndex) == '.') {
                    replacementEnd = dotIndex + 1; 
                }
                
                String replacement = "@" + className + "@";
                sb.replace(index, replacementEnd, replacement);
                index += replacement.length();
            } else {
                index += 2;
            }
        }
        return sb.toString();
    }

    private int findMatchingClosingParen(StringBuilder sb, int start) {
        int depth = 1;
        for (int i = start; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (isInsideQuotes(sb, i)) continue;
            if (c == '(') depth++;
            else if (c == ')') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

}
