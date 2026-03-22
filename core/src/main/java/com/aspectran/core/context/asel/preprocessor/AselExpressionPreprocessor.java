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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A state-machine based single-pass preprocessor for AsEL expressions.
 * <p>This class handles all custom operators (Safe Navigation, Elvis, Collection, etc.)
 * in a single traversal, correctly respecting string literals, escaped characters,
 * and nesting levels of parentheses, brackets, and braces.</p>
 *
 * <p>Created: 2026. 03. 22.</p>
 */
public class AselExpressionPreprocessor implements ExpressionPreprocessor {

    private final AtomicInteger varCounter = new AtomicInteger();

    /**
     * Pre-processes the given AsEL expression string.
     * @param expression the raw expression string
     * @return the transformed expression string compatible with OGNL
     */
    @Override
    public String preprocess(String expression) {
        if (expression == null || expression.isEmpty()) {
            return expression;
        }
        return processRecursive(expression);
    }

    /**
     * Recursively processes the expression to handle nested operators.
     * @param expr the expression segment to process
     * @return the processed segment
     */
    @NonNull
    private String processRecursive(String expr) {
        // Step 1: Handle T(class) first as it can be a part of other expressions
        expr = processTypeOperator(expr);

        // Step 2: Handle other operators by scanning from left to right.
        // We use a loop to ensure nested operators within generated code are also handled.
        StringBuilder sb = new StringBuilder(expr);
        boolean changed;
        do {
            changed = false;
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;
            boolean escaped = false;

            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                    continue;
                }
                if (c == '\"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                    continue;
                }
                if (inSingleQuote || inDoubleQuote) {
                    continue;
                }

                // Check for custom operators at any nesting level.
                // findLeft/RightOperandStart will respect parentheses correctly.
                if (checkAndReplace(sb, i)) {
                    changed = true;
                    break; // Re-scan the modified string to catch any remaining or newly formed patterns
                }
            }
        } while (changed);

        return sb.toString();
    }

    /**
     * Identifies and replaces custom operators at the specified index.
     * @param sb the expression buffer
     * @param i the current index
     * @return true if a replacement was made; false otherwise
     */
    private boolean checkAndReplace(StringBuilder sb, int i) {
        // Safe Navigation: ?. (allows optional space)
        if (match(sb, i, "?.") || match(sb, i, "? .")) {
            int opLen = (sb.charAt(i + 1) == ' ' ? 3 : 2);
            replaceSafeNavigation(sb, i, opLen);
            return true;
        }
        // Elvis Operator: ?: (allows optional space)
        if (match(sb, i, "?:") || match(sb, i, "? :")) {
            int opLen = (sb.charAt(i + 1) == ' ' ? 3 : 2);
            replaceElvis(sb, i, opLen);
            return true;
        }
        // SpEL-style Collection Selection/Projection: .?[ .![ .^[ .$[
        if (i > 0 && sb.charAt(i - 1) == '.') {
            if (match(sb, i, "?[") || match(sb, i, "![") || match(sb, i, "^[") || match(sb, i, "$[")) {
                replaceCollection(sb, i);
                return true;
            }
        }
        // Matches Operator: ' matches '
        if (match(sb, i, " matches ")) {
            replaceMatches(sb, i);
            return true;
        }
        return false;
    }

    /**
     * Checks if the buffer starts with the given pattern at the specified index.
     */
    private boolean match(@NonNull StringBuilder sb, int i, @NonNull String pattern) {
        if (i + pattern.length() > sb.length()) {
            return false;
        }
        for (int j = 0; j < pattern.length(); j++) {
            if (sb.charAt(i + j) != pattern.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Transforms 'obj?.prop' to '(#_res = (obj), #_res != null ? #_res.prop : null)'.
     */
    private void replaceSafeNavigation(StringBuilder sb, int index, int opLen) {
        int leftStart = ExpressionPreprocessorUtils.findLeftOperandStart(sb, index, true);
        String leftExpr = sb.substring(leftStart, index).trim();
        int rightEnd = ExpressionPreprocessorUtils.findRightOperandEnd(sb, index + opLen, true);
        String rightExpr = sb.substring(index + opLen, rightEnd).trim();

        if (rightExpr.isEmpty()) {
            sb.replace(index, index + opLen, ".");
            return;
        }

        String varName = getUniqueVarName("sn");
        String replacement;
        char firstChar = rightExpr.charAt(0);
        if (firstChar == '.' || firstChar == '[' || firstChar == '{') {
            replacement = "(#_res_" + varName + " = (" + leftExpr + "), #_res_" + varName + " != null ? #_res_" + varName + rightExpr + " : null)";
        } else {
            replacement = "(#_res_" + varName + " = (" + leftExpr + "), #_res_" + varName + " != null ? #_res_" + varName + "." + rightExpr + " : null)";
        }
        sb.replace(leftStart, rightEnd, replacement);
    }

    /**
     * Transforms 'obj ?: fallback' to '(#_res = (obj), #_res != null ? #_res : (fallback))'.
     */
    private void replaceElvis(StringBuilder sb, int index, int opLen) {
        int leftStart = ExpressionPreprocessorUtils.findLeftOperandStart(sb, index, false);
        String leftExpr = sb.substring(leftStart, index).trim();
        int rightEnd = ExpressionPreprocessorUtils.findRightOperandEnd(sb, index + opLen, false);
        String rightExpr = sb.substring(index + opLen, rightEnd).trim();

        String varName = getUniqueVarName("el");
        String replacement = "(#_res_" + varName + " = (" + leftExpr + "), #_res_" + varName + " != null ? #_res_" + varName + " : (" + rightExpr + "))";
        sb.replace(leftStart, rightEnd, replacement);
    }

    /**
     * Transforms SpEL-style .?[...] to OGNL-style .{? ...}.
     */
    private void replaceCollection(@NonNull StringBuilder sb, int index) {
        char type = sb.charAt(index);
        String replacementOp = switch (type) {
            case '?' -> "{? ";
            case '!' -> "{ ";
            case '^' -> "{^ ";
            case '$' -> "{$ ";
            default -> null;
        };
        if (replacementOp != null) {
            sb.replace(index, index + 2, replacementOp);
            int closingBracket = findMatchingClosingBracket(sb, index + replacementOp.length());
            if (closingBracket != -1) {
                sb.setCharAt(closingBracket, '}');
            }
        }
    }

    /**
     * Transforms 'a matches b' to '(a).matches(b)'.
     */
    private void replaceMatches(StringBuilder sb, int index) {
        int leftStart = ExpressionPreprocessorUtils.findLeftOperandStart(sb, index, false);
        String leftExpr = sb.substring(leftStart, index).trim();
        int rightEnd = ExpressionPreprocessorUtils.findRightOperandEnd(sb, index + 9, false);
        String rightExpr = sb.substring(index + 9, rightEnd).trim();
        String replacement = "(" + leftExpr + ").matches(" + rightExpr + ")";
        sb.replace(leftStart, rightEnd, replacement);
    }

    /**
     * Transforms 'T(package.Class)' to '@package.Class@'.
     */
    @NonNull
    private String processTypeOperator(String expr) {
        StringBuilder sb = new StringBuilder(expr);
        int index = 0;
        while ((index = sb.indexOf("T(", index)) != -1) {
            if (ExpressionPreprocessorUtils.isInsideQuotes(sb, index)) {
                index += 2;
                continue;
            }
            int closingParen = findMatchingClosingParen(sb, index + 2);
            if (closingParen != -1) {
                String className = sb.substring(index + 2, closingParen).trim();
                int replacementEnd = closingParen + 1;

                // Handle optional whitespace and dot for static member access
                int dotIndex = replacementEnd;
                while (dotIndex < sb.length() && Character.isWhitespace(sb.charAt(dotIndex))) {
                    dotIndex++;
                }
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

    private int findMatchingClosingBracket(@NonNull StringBuilder sb, int start) {
        int depth = 1;
        for (int i = start; i < sb.length(); i++) {
            if (ExpressionPreprocessorUtils.isInsideQuotes(sb, i)) continue;
            char c = sb.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                if (--depth == 0) return i;
            }
        }
        return -1;
    }

    private int findMatchingClosingParen(@NonNull StringBuilder sb, int start) {
        int depth = 1;
        for (int i = start; i < sb.length(); i++) {
            if (ExpressionPreprocessorUtils.isInsideQuotes(sb, i)) continue;
            char c = sb.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') {
                if (--depth == 0) return i;
            }
        }
        return -1;
    }

    @NonNull
    private String getUniqueVarName(String prefix) {
        return prefix + "_" + (System.nanoTime() % 1000000) + "_" + varCounter.getAndIncrement();
    }

}
