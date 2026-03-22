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
 * Preprocessor for SpEL-style collection operators:
 * <ul>
 *   <li>Selection: .?[selectionExpression] -> .{? selectionExpression}</li>
 *   <li>Projection: .![projectionExpression] -> .{ projectionExpression}</li>
 *   <li>First: .^[selectionExpression] -> .{^ selectionExpression}</li>
 *   <li>Last: .$[selectionExpression] -> .{$ selectionExpression}</li>
 * </ul>
 */
public class CollectionOperatorPreprocessor implements ExpressionPreprocessor {

    @Override
    public String preprocess(String expression) {
        if (!expression.contains(".?[" ) && !expression.contains(".![") &&
            !expression.contains(".^[") && !expression.contains(".$[")) {
            return expression;
        }

        StringBuilder sb = new StringBuilder(expression);
        int index = 0;
        while (index < sb.length()) {
            int foundIndex = -1;
            String op = null;
            String replacementOp = null;

            // Find the first occurrence of any collection operator
            String[] operators = {".?[", ".![", ".^[", ".$["};
            String[] replacements = {".{? ", ".{ ", ".{^ ", ".{$ "};

            for (int i = 0; i < operators.length; i++) {
                int idx = sb.indexOf(operators[i], index);
                if (idx != -1 && (foundIndex == -1 || idx < foundIndex)) {
                    if (!isInsideQuotes(sb, idx)) {
                        foundIndex = idx;
                        op = operators[i];
                        replacementOp = replacements[i];
                    }
                }
            }

            if (foundIndex != -1) {
                // Replace opening [.?[ etc. with .{? etc.
                sb.replace(foundIndex, foundIndex + op.length(), replacementOp);
                
                // Find matching closing bracket ] and replace with }
                int closingBracket = findMatchingClosingBracket(sb, foundIndex + replacementOp.length());
                if (closingBracket != -1) {
                    sb.setCharAt(closingBracket, '}');
                }
                index = foundIndex + replacementOp.length();
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private int findMatchingClosingBracket(StringBuilder sb, int start) {
        int depth = 1;
        for (int i = start; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (isInsideQuotes(sb, i)) continue;
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

}
