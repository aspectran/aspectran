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
 * Preprocessor for the 'matches' operator.
 * <p>Converts 'a matches b' to '(a).matches(b)'.</p>
 */
public class MatchesOperatorPreprocessor implements ExpressionPreprocessor {

    private static final String MATCHES = " matches ";

    @Override
    public String preprocess(String expression) {
        if (!expression.contains(MATCHES)) {
            return expression;
        }

        StringBuilder sb = new StringBuilder(expression);
        int index = 0;
        while ((index = sb.indexOf(MATCHES, index)) != -1) {
            if (isInsideQuotes(sb, index)) {
                index += MATCHES.length();
                continue;
            }

            int leftStart = findLeftOperandStart(sb, index, false);
            String leftExpr = sb.substring(leftStart, index).trim();

            int rightEnd = findRightOperandEnd(sb, index + MATCHES.length(), false);
            String rightExpr = sb.substring(index + MATCHES.length(), rightEnd).trim();

            String replacement = "(" + leftExpr + ").matches(" + rightExpr + ")";
            sb.replace(leftStart, rightEnd, replacement);
            index = leftStart + replacement.length();
        }
        return sb.toString();
    }

}
