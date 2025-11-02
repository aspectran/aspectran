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
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Internal engine for wildcard pattern matching.
 * <p>This class is not part of the public API and is intended for internal
 * use within the package. It centralizes the complex matching algorithms
 * to improve maintainability.</p>
 */
class WildcardEngine {

    /**
     * The master implementation of the wildcard matching algorithm.
     * <p>IMPORTANT: The {@link #mask} method shares a very similar algorithmic structure.
     * Any bug fixes or logic changes applied here MUST be carefully mirrored
     * in the {@code mask} method as well.</p>
     * @param pattern the compiled pattern
     * @param input the input to match
     * @param separatorFlags an array to store separator positions, or null
     * @return true if the input matches the pattern, false otherwise
     */
    static boolean match(WildcardPattern pattern, CharSequence input, @Nullable int[] separatorFlags) {
        Assert.notNull(pattern, "pattern must not be null");

        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char separator = pattern.getSeparator();

        if (input == null) {
            for (int i = 0; i < tokens.length; i++) {
                if (types[i] == WildcardPattern.LITERAL_TYPE ||
                        types[i] == WildcardPattern.PLUS_TYPE ||
                        types[i] == WildcardPattern.SEPARATOR_TYPE) {
                    return false;
                }
            }
            return true;
        }

        int tokenCount = tokens.length;
        int inputLength = input.length();
        int separatorCount = 0;
        int tokenIndex = 0;
        int charIndex = 0;

        while (tokenIndex < tokenCount && charIndex < inputLength) {
            if (types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
                if (tokens[tokenIndex++] != input.charAt(charIndex++)) {
                    return false;
                }
            } else if (types[tokenIndex] == WildcardPattern.STAR_TYPE) {
                int tempTokenIndex1 = tokenIndex + 1;
                if (tempTokenIndex1 < tokenCount) {
                    int tempTokenIndex2 = tempTokenIndex1;
                    for (; tempTokenIndex2 < tokenCount; tempTokenIndex2++) {
                        if (types[tempTokenIndex2] == WildcardPattern.EOT_TYPE ||
                                types[tempTokenIndex2] != WildcardPattern.LITERAL_TYPE) {
                            break;
                        }
                    }
                    if (tempTokenIndex1 == tempTokenIndex2) {
                        // prefix*
                        for (; charIndex < inputLength; charIndex++) {
                            if (input.charAt(charIndex) == separator) {
                                break;
                            }
                        }
                        tokenIndex++;
                    } else {
                        // *suffix
                        int currentSuffixToken = tempTokenIndex1;
                        do {
                            if (input.charAt(charIndex) == separator) {
                                return false;
                            }
                            if (tokens[currentSuffixToken] != input.charAt(charIndex++)) {
                                currentSuffixToken = tempTokenIndex1;
                            }
                            else {
                                currentSuffixToken++;
                            }
                        } while (currentSuffixToken < tempTokenIndex2 && charIndex < inputLength);
                        if (currentSuffixToken < tempTokenIndex2) {
                            return false;
                        }
                        tokenIndex = tempTokenIndex2;
                    }
                } else {
                    for (; charIndex < inputLength; charIndex++) {
                        if (input.charAt(charIndex) == separator) {
                            break;
                        }
                    }
                    tokenIndex++;
                }
            } else if (types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
                if (separator > 0) {
                    int tempTokenIndex1 = -1;
                    int tempTokenIndex2 = -1;
                    for (int i = tokenIndex + 1; i < tokenCount; i++) {
                        if (tempTokenIndex1 == -1) {
                            if (types[i] == WildcardPattern.LITERAL_TYPE) {
                                tempTokenIndex1 = i;
                            }
                        }
                        else {
                            if (types[i] != WildcardPattern.LITERAL_TYPE) {
                                tempTokenIndex2 = i - 1;
                                break;
                            }
                        }
                    }
                    if (tempTokenIndex1 > -1 && tempTokenIndex2 > -1) {
                        int charRangeStart = charIndex;
                        int charRangeEnd = charIndex;
                        int currentSuffixToken = tempTokenIndex1;
                        while (currentSuffixToken <= tempTokenIndex2 && charRangeEnd < inputLength) {
                            if (input.charAt(charRangeEnd++) != tokens[currentSuffixToken]) {
                                currentSuffixToken = tempTokenIndex1;
                            }
                            else {
                                currentSuffixToken++;
                            }
                        }
                        if (currentSuffixToken <= tempTokenIndex2) {
                            tokenIndex = tempTokenIndex2;
                            if (charIndex > 0) {
                                charIndex--;
                            }
                        } else {
                            if (separatorFlags != null && charRangeStart < charRangeEnd) {
                                for (int i = charRangeStart; i < charRangeEnd; i++) {
                                    if (input.charAt(i) == separator) {
                                        separatorFlags[i] = ++separatorCount;
                                    }
                                }
                            }
                            charIndex = charRangeEnd;
                            tokenIndex = tempTokenIndex2 + 1;
                        }
                    } else {
                        tokenIndex++;
                        int separatorCountInPattern = 0;
                        for (int i = tokenIndex; i < tokenCount; i++) {
                            if (types[i] == WildcardPattern.SEPARATOR_TYPE) {
                                separatorCountInPattern++;
                            }
                        }
                        if (separatorCountInPattern > 0) {
                            int charRangeStart = charIndex;
                            int charRangeEnd = inputLength;
                            int separatorCountInInput = 0;
                            while (charRangeEnd > 0 && charRangeStart <= charRangeEnd--) {
                                if (input.charAt(charRangeEnd) == separator) {
                                    separatorCountInInput++;
                                }
                                if (separatorCountInPattern == separatorCountInInput) {
                                    break;
                                }
                            }
                            if (separatorCountInPattern == separatorCountInInput) {
                                charIndex = charRangeEnd;
                                if (separatorFlags != null) {
                                    while (charRangeStart < charRangeEnd) {
                                        if (input.charAt(charRangeStart) == separator) {
                                            separatorFlags[charRangeStart] = ++separatorCount;
                                        }
                                        charRangeStart++;
                                    }
                                }
                            }
                        } else {
                            charIndex = inputLength; //complete
                        }
                    }
                }
                else {
                    charIndex = inputLength; //complete
                    tokenIndex++;
                }
            } else if (types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
                if (tokenIndex > tokenCount - 1 ||
                        types[tokenIndex + 1] != WildcardPattern.LITERAL_TYPE ||
                        tokens[tokenIndex + 1] != input.charAt(charIndex)) {
                    if (separator > 0) {
                        if (input.charAt(charIndex) != separator) {
                            charIndex++;
                        }
                    } else {
                        charIndex++;
                    }
                }
                tokenIndex++;
            } else if (types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
                if (separator > 0) {
                    if (input.charAt(charIndex) == separator) {
                        return false;
                    }
                }
                charIndex++;
                tokenIndex++;
            } else if (types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
                if (tokens[tokenIndex++] != input.charAt(charIndex++)) {
                    return false;
                }
                if (separatorFlags != null) {
                    separatorFlags[charIndex - 1] = ++separatorCount;
                }
            } else if (types[tokenIndex] == WildcardPattern.EOT_TYPE) {
                break;
            } else {
                tokenIndex++;
            }
        }

        if (charIndex < inputLength) {
            return false;
        }

        if (tokenIndex < tokenCount) {
            for (int i = tokenIndex; i < tokenCount; i++) {
                if (types[i] == WildcardPattern.LITERAL_TYPE ||
                        types[i] == WildcardPattern.PLUS_TYPE ||
                        types[i] == WildcardPattern.SEPARATOR_TYPE) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * The master implementation of the wildcard masking algorithm.
     * <p>IMPORTANT: The {@link #match} method shares a very similar algorithmic structure.
     * Any bug fixes or logic changes applied here MUST be carefully mirrored
     * in the {@code match} method as well.</p>
     * @param pattern the precompiled wildcard pattern
     * @param input the input character sequence to be masked
     * @return the masked string if the input matches; {@code null} otherwise
     */
    @Nullable
    static String mask(WildcardPattern pattern, CharSequence input) {
        Assert.notNull(pattern, "pattern must not be null");
        Assert.notNull(input, "input must not be null");

        char[] tokens = pattern.getTokens();
        int[] types = pattern.getTypes();
        char separator = pattern.getSeparator();

        int tokenCount = tokens.length;
        int inputLength = input.length();

        char[] masks = new char[inputLength];
        char c;

        int tokenIndex = 0;
        int charIndex = 0;

        while (tokenIndex < tokenCount && charIndex < inputLength) {
            if (types[tokenIndex] == WildcardPattern.LITERAL_TYPE) {
                if (tokens[tokenIndex++] != input.charAt(charIndex++)) {
                    return null;
                }
            } else if (types[tokenIndex] == WildcardPattern.STAR_TYPE) {
                int tempTokenIndex1 = tokenIndex + 1;
                if (tempTokenIndex1 < tokenCount) {
                    int tempTokenIndex2 = tempTokenIndex1;
                    for (; tempTokenIndex2 < tokenCount; tempTokenIndex2++) {
                        if (types[tempTokenIndex2] == WildcardPattern.EOT_TYPE ||
                                types[tempTokenIndex2] != WildcardPattern.LITERAL_TYPE) {
                            break;
                        }
                    }
                    if (tempTokenIndex1 == tempTokenIndex2) {
                        // prefix*
                        for (; charIndex < inputLength; charIndex++) {
                            c = input.charAt(charIndex);
                            if (c == separator) {
                                break;
                            }
                            masks[charIndex] = c;
                        }
                        tokenIndex++;
                    } else {
                        // *suffix
                        int currentSuffixToken = tempTokenIndex1;
                        do {
                            c = input.charAt(charIndex);
                            if (c == separator) {
                                return null;
                            }
                            if (tokens[currentSuffixToken] != c) {
                                currentSuffixToken = tempTokenIndex1;
                                masks[charIndex] = c;
                            } else {
                                currentSuffixToken++;
                            }
                            charIndex++;
                        } while (currentSuffixToken < tempTokenIndex2 && charIndex < inputLength);
                        if (currentSuffixToken < tempTokenIndex2) {
                            return null;
                        }
                        tokenIndex = tempTokenIndex2;
                    }
                } else {
                    for (; charIndex < inputLength; charIndex++) {
                        c = input.charAt(charIndex);
                        if (c == separator) {
                            break;
                        }
                        masks[charIndex] = c;
                    }
                    tokenIndex++;
                }
            } else if (types[tokenIndex] == WildcardPattern.STAR_STAR_TYPE) {
                if (separator != Character.MIN_VALUE) {
                    int tempTokenIndex1 = -1;
                    int tempTokenIndex2 = -1;
                    for (int i = tokenIndex + 1; i < tokenCount; i++) {
                        if (tempTokenIndex1 == -1) {
                            if (types[i] == WildcardPattern.LITERAL_TYPE) {
                                tempTokenIndex1 = i;
                            }
                        } else {
                            if (types[i] != WildcardPattern.LITERAL_TYPE) {
                                tempTokenIndex2 = i - 1;
                                break;
                            }
                        }
                    }
                    if (tempTokenIndex1 > -1 && tempTokenIndex2 > -1) {
                        int charRangeEnd = charIndex;
                        int currentSuffixToken = tempTokenIndex1;
                        while (currentSuffixToken <= tempTokenIndex2 && charRangeEnd < inputLength) {
                            c = input.charAt(charRangeEnd);
                            if (c != tokens[currentSuffixToken]) {
                                currentSuffixToken = tempTokenIndex1;
                                masks[charRangeEnd] = c;
                            } else {
                                currentSuffixToken++;
                            }
                            charRangeEnd++;
                        }
                        if (currentSuffixToken <= tempTokenIndex2) {
                            tokenIndex = tempTokenIndex2;
                            if (charIndex > 0) {
                                charIndex--;
                                masks[charIndex] = 0; //erase
                            }
                        } else {
                            charIndex = charRangeEnd;
                            tokenIndex = tempTokenIndex2 + 1;
                        }
                    } else {
                        tokenIndex++;
                        int separatorCountInPattern = 0;
                        for (int i = tokenIndex; i < tokenCount; i++) {
                            if (types[i] == WildcardPattern.SEPARATOR_TYPE) {
                                separatorCountInPattern++;
                            }
                        }
                        if (separatorCountInPattern > 0) {
                            int charRangeStart = charIndex;
                            int charRangeEnd = inputLength;
                            int separatorCountInInput = 0;
                            while (charRangeEnd > 0 && charRangeStart <= charRangeEnd--) {
                                if (input.charAt(charRangeEnd) == separator) {
                                    separatorCountInInput++;
                                }
                                if (separatorCountInPattern == separatorCountInInput) {
                                    break;
                                }
                            }
                            if (separatorCountInPattern == separatorCountInInput) {
                                charIndex = charRangeEnd;
                                for (int i = charRangeStart; i < charRangeEnd; i++) {
                                    masks[i] = input.charAt(i);
                                }
                            }
                        } else {
                            for (; charIndex < inputLength; charIndex++) {
                                masks[charIndex] = input.charAt(charIndex);
                            }
                        }
                    }
                } else {
                    for (int i = charIndex; i < inputLength; i++) {
                        masks[i] = input.charAt(i);
                    }
                    charIndex = inputLength; //complete
                    tokenIndex++;
                }
            } else if (types[tokenIndex] == WildcardPattern.QUESTION_TYPE) {
                if (tokenIndex > tokenCount - 1 ||
                        types[tokenIndex + 1] != WildcardPattern.LITERAL_TYPE ||
                        tokens[tokenIndex + 1] != input.charAt(charIndex)) {
                    if (separator != Character.MIN_VALUE) {
                        if (input.charAt(charIndex) != separator) {
                            masks[charIndex] = input.charAt(charIndex);
                            charIndex++;
                        }
                    } else {
                        masks[charIndex] = input.charAt(charIndex);
                        charIndex++;
                    }
                }
                tokenIndex++;
            } else if (types[tokenIndex] == WildcardPattern.PLUS_TYPE) {
                if (separator != Character.MIN_VALUE) {
                    if (input.charAt(charIndex) == separator) {
                        return null;
                    }
                }
                masks[charIndex] = input.charAt(charIndex);
                charIndex++;
                tokenIndex++;
            } else if (types[tokenIndex] == WildcardPattern.SEPARATOR_TYPE) {
                if (tokens[tokenIndex] != input.charAt(charIndex)) {
                    return null;
                }
                if (tokenIndex > 0 && charIndex > 0 && masks[charIndex - 1] > 0 &&
                        (types[tokenIndex - 1] == WildcardPattern.STAR_STAR_TYPE ||
                                types[tokenIndex - 1] == WildcardPattern.STAR_TYPE)) {
                    masks[charIndex] = input.charAt(charIndex);
                }
                tokenIndex++;
                charIndex++;
            } else if (types[tokenIndex] == WildcardPattern.EOT_TYPE) {
                break;
            } else {
                tokenIndex++;
            }
        }

        if (charIndex < inputLength) {
            if (charIndex == 0 && tokenCount > 0 && types[0] == WildcardPattern.STAR_STAR_TYPE) {
                for (int end = 0; end < inputLength; end++) {
                    if (input.charAt(end) != separator) {
                        if (end > 0) {
                            return input.subSequence(end, inputLength).toString();
                        }
                        break;
                    }
                }
                return input.toString();
            }
            return null;
        }

        if (tokenIndex < tokenCount) {
            for (int i = tokenIndex; i < tokenCount; i++) {
                if (types[i] == WildcardPattern.LITERAL_TYPE ||
                        types[i] == WildcardPattern.PLUS_TYPE ||
                        types[i] == WildcardPattern.SEPARATOR_TYPE) {
                    return null;
                }
            }
        }

        StringBuilder sb = new StringBuilder(masks.length);
        for (char mask : masks) {
            if (mask > 0) {
                sb.append(mask);
            }
        }
        if (types[0] == WildcardPattern.STAR_STAR_TYPE || types[0] == WildcardPattern.STAR_TYPE) {
            for (int end = 0; end < sb.length(); end++) {
                if (sb.charAt(end) != separator) {
                    if (end > 0) {
                        sb.delete(0, end);
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

}
