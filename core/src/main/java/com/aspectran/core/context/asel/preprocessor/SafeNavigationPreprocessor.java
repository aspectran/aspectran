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

import static com.aspectran.core.context.asel.preprocessor.ExpressionPreprocessorUtils.findLeftOperandStart;
import static com.aspectran.core.context.asel.preprocessor.ExpressionPreprocessorUtils.findRightOperandEnd;
import static com.aspectran.core.context.asel.preprocessor.ExpressionPreprocessorUtils.isInsideQuotes;

/**
 * Preprocessor for Safe Navigation Operator (?.).
 */
public class SafeNavigationPreprocessor implements ExpressionPreprocessor {

    @Override
    public String preprocess(String expression) {
        String processed = expression;
        while (processed.contains("?.")) {
            StringBuilder sb = new StringBuilder(processed);
            int index = sb.indexOf("?.");
            if (isInsideQuotes(sb, index)) {
                int nextIndex = findNextOperatorOutsideQuotes(sb, "?.", index + 2);
                if (nextIndex == -1) break;
                index = nextIndex;
            }

            int leftStart = findLeftOperandStart(sb, index, true);
            String leftExpr = sb.substring(leftStart, index).trim();

            // The RHS should be the part immediately following the ?.
            // It could be a property name, a method call, or a collection operator
            int rightEnd = findRightOperandEnd(sb, index + 2, true);
            String rightExpr = sb.substring(index + 2, rightEnd).trim();

            if (rightExpr.isEmpty()) {
                // Should not happen in valid syntax, but safety first
                sb.replace(index, index + 2, ".");
                processed = sb.toString();
                continue;
            }

            String replacement;
            char firstChar = rightExpr.charAt(0);
            if (firstChar == '.' || firstChar == '[' || firstChar == '{') {
                // Already has a standard OGNL separator
                replacement = "(#_res = (" + leftExpr + "), #_res != null ? #_res" + rightExpr + " : null)";
            } else {
                // Property access, method call, or SpEL-style collection operators (![, ?[, etc.)
                // all need a dot separator for valid OGNL syntax after our transformation
                replacement = "(#_res = (" + leftExpr + "), #_res != null ? #_res." + rightExpr + " : null)";
            }
            
            sb.replace(leftStart, rightEnd, replacement);
            processed = sb.toString();
        }
        return processed;
    }

    private int findNextOperatorOutsideQuotes(StringBuilder sb, String op, int start) {
        int index = start;
        while ((index = sb.indexOf(op, index)) != -1) {
            if (!isInsideQuotes(sb, index)) return index;
            index += op.length();
        }
        return -1;
    }

}
