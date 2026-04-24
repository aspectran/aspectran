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
package com.aspectran.utils.wildcard;

import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Internal engine for wildcard pattern matching.
 * <p>This class is not part of the public API and is intended for internal
 * use within the package. It centralizes the complex matching algorithms
 * to improve maintainability.</p>
 */
class WildcardEngine {

    /**
     * The master implementation of the wildcard matching algorithm.
     * @param pattern the compiled pattern
     * @param input the input to match
     * @param separatorFlags an array to store separator positions, or null
     * @return true if the input matches the pattern, false otherwise
     */
    static boolean match(WildcardPattern pattern, CharSequence input, int @Nullable [] separatorFlags) {
        Assert.notNull(pattern, "pattern must not be null");
        if (input == null) {
            return matchNull(pattern);
        }
        if (separatorFlags != null) {
            Arrays.fill(separatorFlags, 0);
        }
        int[] separatorCount = new int[1];
        return matchRecursive(pattern, input, 0, 0, separatorFlags, separatorCount);
    }

    private static boolean matchRecursive(
            @NonNull WildcardPattern pattern, @NonNull CharSequence input,
            int tokenIndex, int charIndex, int[] separatorFlags, int[] separatorCount) {
        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char separator = pattern.getSeparator();
        int tokenCount = tokens.length;
        int inputLength = input.length();

        if (tokenIndex == tokenCount || types[tokenIndex] == WildcardPattern.EOT_TYPE) {
            return charIndex == inputLength;
        }

        int type = types[tokenIndex];
        if (type == WildcardPattern.LITERAL_TYPE || type == WildcardPattern.SEPARATOR_TYPE) {
            if (charIndex < inputLength && tokens[tokenIndex] == input.charAt(charIndex)) {
                if (type == WildcardPattern.SEPARATOR_TYPE && separatorFlags != null) {
                    separatorFlags[charIndex] = ++separatorCount[0];
                }
                if (matchRecursive(pattern, input, tokenIndex + 1, charIndex + 1, separatorFlags, separatorCount)) {
                    return true;
                }
                if (type == WildcardPattern.SEPARATOR_TYPE && separatorFlags != null) {
                    separatorFlags[charIndex] = 0;
                    separatorCount[0]--;
                }
            }
            return false;
        } else if (type == WildcardPattern.STAR_TYPE) {
            for (int i = 0; charIndex + i <= inputLength; i++) {
                if (i > 0 && separator > Character.MIN_VALUE && input.charAt(charIndex + i - 1) == separator) {
                    break;
                }
                if (matchRecursive(pattern, input, tokenIndex + 1, charIndex + i, separatorFlags, separatorCount)) {
                    return true;
                }
            }
            return false;
        } else if (type == WildcardPattern.STAR_STAR_TYPE) {
            // Swallowing case: if ** matches empty string and is between separators, skip next separator in pattern
            if (tokenIndex > 0 && types[tokenIndex - 1] == WildcardPattern.SEPARATOR_TYPE &&
                    tokenIndex + 1 < tokenCount && types[tokenIndex + 1] == WildcardPattern.SEPARATOR_TYPE) {
                if (matchRecursive(pattern, input, tokenIndex + 2, charIndex, separatorFlags, separatorCount)) {
                    return true;
                }
            }
            // Normal matching (crosses separators)
            for (int i = 0; charIndex + i <= inputLength; i++) {
                int sc = separatorCount[0];
                if (separatorFlags != null && separator > Character.MIN_VALUE) {
                    for (int j = 0; j < i; j++) {
                        if (input.charAt(charIndex + j) == separator) {
                            separatorFlags[charIndex + j] = ++sc;
                        }
                    }
                }
                int savedSc = separatorCount[0];
                separatorCount[0] = sc;
                if (matchRecursive(pattern, input, tokenIndex + 1, charIndex + i, separatorFlags, separatorCount)) {
                    return true;
                }
                separatorCount[0] = savedSc;
                if (separatorFlags != null && separator > Character.MIN_VALUE) {
                    for (int j = 0; j < i; j++) {
                        if (input.charAt(charIndex + j) == separator) {
                            separatorFlags[charIndex + j] = 0;
                        }
                    }
                }
            }
            return false;
        } else if (type == WildcardPattern.QUESTION_TYPE) {
            // matches 0 or 1
            if (charIndex < inputLength && (separator == Character.MIN_VALUE || input.charAt(charIndex) != separator)) {
                if (matchRecursive(pattern, input, tokenIndex + 1, charIndex + 1, separatorFlags, separatorCount)) {
                    return true;
                }
            }
            return matchRecursive(pattern, input, tokenIndex + 1, charIndex, separatorFlags, separatorCount);
        } else if (type == WildcardPattern.PLUS_TYPE) {
            // matches exactly 1
            if (charIndex < inputLength && (separator == Character.MIN_VALUE || input.charAt(charIndex) != separator)) {
                return matchRecursive(pattern, input, tokenIndex + 1, charIndex + 1, separatorFlags, separatorCount);
            }
            return false;
        }
        return false;
    }

    /**
     * The master implementation of the wildcard masking algorithm.
     * @param pattern the precompiled wildcard pattern
     * @param input the input character sequence to be masked
     * @return the masked string if the input matches; {@code null} otherwise
     */
    @Nullable
    static String mask(WildcardPattern pattern, CharSequence input) {
        Assert.notNull(pattern, "pattern must not be null");
        if (input == null) {
            return (matchNull(pattern) ? "" : null);
        }

        int[] maskFlags = new int[input.length()];
        if (maskRecursive(pattern, input, 0, 0, maskFlags)) {
            char separator = pattern.getSeparator();
            int[] types = pattern.getTypes();
            StringBuilder sb = new StringBuilder();
            int lastWildcardTokenIndex = -1;
            for (int i = 0; i < input.length(); i++) {
                int tIdx = maskFlags[i] - 1;
                if (tIdx >= 0 && isWildcard(types[tIdx])) {
                    if (lastWildcardTokenIndex != -1 && tIdx > lastWildcardTokenIndex) {
                        if (hasSeparatorBetween(types, lastWildcardTokenIndex, tIdx)) {
                            sb.append(separator);
                        }
                    }
                    sb.append(input.charAt(i));
                    lastWildcardTokenIndex = tIdx;
                }
            }
            return sb.toString();
        }
        return null;
    }

    private static boolean isWildcard(int type) {
        return type == WildcardPattern.STAR_TYPE || type == WildcardPattern.STAR_STAR_TYPE ||
               type == WildcardPattern.QUESTION_TYPE || type == WildcardPattern.PLUS_TYPE;
    }

    private static boolean hasSeparatorBetween(int[] types, int start, int end) {
        for (int i = start + 1; i < end; i++) {
            if (types[i] == WildcardPattern.SEPARATOR_TYPE) {
                return true;
            }
        }
        return false;
    }

    private static boolean maskRecursive(
            @NonNull WildcardPattern pattern, @NonNull CharSequence input,
            int tokenIndex, int charIndex, int[] maskFlags) {
        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char separator = pattern.getSeparator();
        int tokenCount = tokens.length;
        int inputLength = input.length();

        if (tokenIndex == tokenCount || types[tokenIndex] == WildcardPattern.EOT_TYPE) {
            return charIndex == inputLength;
        }

        int type = types[tokenIndex];
        if (type == WildcardPattern.LITERAL_TYPE || type == WildcardPattern.SEPARATOR_TYPE) {
            if (charIndex < inputLength && tokens[tokenIndex] == input.charAt(charIndex)) {
                return maskRecursive(pattern, input, tokenIndex + 1, charIndex + 1, maskFlags);
            }
            return false;
        } else if (type == WildcardPattern.STAR_TYPE) {
            // Try from shortest to longest to handle backtracking expectations in some test cases
            for (int i = 0; charIndex + i <= inputLength; i++) {
                if (i > 0 && separator > Character.MIN_VALUE && input.charAt(charIndex + i - 1) == separator) {
                    break;
                }
                if (maskRecursive(pattern, input, tokenIndex + 1, charIndex + i, maskFlags)) {
                    for (int j = 0; j < i; j++) {
                        maskFlags[charIndex + j] = tokenIndex + 1;
                    }
                    return true;
                }
            }
            return false;
        } else if (type == WildcardPattern.STAR_STAR_TYPE) {
            // Shortest match first for ** as well
            for (int i = 0; charIndex + i <= inputLength; i++) {
                // Swallowing case
                if (i == 0 && tokenIndex > 0 && types[tokenIndex - 1] == WildcardPattern.SEPARATOR_TYPE &&
                        tokenIndex + 1 < tokenCount && types[tokenIndex + 1] == WildcardPattern.SEPARATOR_TYPE) {
                    if (maskRecursive(pattern, input, tokenIndex + 2, charIndex, maskFlags)) {
                        return true;
                    }
                }
                if (maskRecursive(pattern, input, tokenIndex + 1, charIndex + i, maskFlags)) {
                    for (int j = 0; j < i; j++) {
                        maskFlags[charIndex + j] = tokenIndex + 1;
                    }
                    return true;
                }
            }
            return false;
        } else if (type == WildcardPattern.QUESTION_TYPE) {
            // try match 1 then match 0
            if (charIndex < inputLength && (separator == Character.MIN_VALUE || input.charAt(charIndex) != separator)) {
                if (maskRecursive(pattern, input, tokenIndex + 1, charIndex + 1, maskFlags)) {
                    maskFlags[charIndex] = tokenIndex + 1;
                    return true;
                }
            }
            return maskRecursive(pattern, input, tokenIndex + 1, charIndex, maskFlags);
        } else if (type == WildcardPattern.PLUS_TYPE) {
            if (charIndex < inputLength && (separator == Character.MIN_VALUE || input.charAt(charIndex) != separator)) {
                if (maskRecursive(pattern, input, tokenIndex + 1, charIndex + 1, maskFlags)) {
                    maskFlags[charIndex] = tokenIndex + 1;
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private static boolean matchNull(@NonNull WildcardPattern pattern) {
        int[] types = pattern.getTypes();
        for (int type : types) {
            if (type == WildcardPattern.LITERAL_TYPE ||
                    type == WildcardPattern.PLUS_TYPE ||
                    type == WildcardPattern.SEPARATOR_TYPE) {
                return false;
            }
        }
        return true;
    }

}
