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
 * Preprocessor for Elvis Operator (?:).
 */
public class ElvisOperatorPreprocessor implements ExpressionPreprocessor {

    @Override
    public String preprocess(String expression) {
        String processed = expression;
        while (processed.contains("?:")) {
            StringBuilder sb = new StringBuilder(processed);
            int index = sb.indexOf("?:");
            if (isInsideQuotes(sb, index)) {
                int nextIndex = findNextOperatorOutsideQuotes(sb, "?:", index + 2);
                if (nextIndex == -1) break;
                index = nextIndex;
            }

            int leftStart = findLeftOperandStart(sb, index, false);
            String leftExpr = sb.substring(leftStart, index).trim();

            int rightEnd = findRightOperandEnd(sb, index + 2, false);
            String rightExpr = sb.substring(index + 2, rightEnd).trim();

            String replacement = "(#_res = (" + leftExpr + "), #_res != null ? #_res : (" + rightExpr + "))";
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
