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

import com.aspectran.utils.StringUtils;

/**
 * Stateful matcher for evaluating an input {@link CharSequence} against a
 * compiled {@link WildcardPattern}. Provides convenience methods to count and
 * iterate over path segments when a separator is configured.
 */
public class WildcardMatcher {

    /** The compiled pattern to use for matching. */
    private final WildcardPattern pattern;

    /** Last input passed to {@link #matches(CharSequence)} or {@link #separate(CharSequence)}. */
    private CharSequence input;

    /** Per-character flags indicating the segment index at each separator position. */
    private int[] separatorFlags;

    /** Number of separators found in the last input (or -1 if unknown). */
    private int separatorCount = -1;

    /** Current index used by {@link #next()} and {@link #previous()}. */
    private int separatorIndex;

    /**
     * Create a new matcher bound to the given compiled pattern.
     * This instance can be reused across multiple inputs.
     * @param pattern the compiled wildcard pattern (must not be {@code null})
     */
    public WildcardMatcher(WildcardPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Evaluate whether the given input matches this matcher's compiled pattern.
     * <p>
     * This method resets internal separator tracking for the new input. When a
     * separator is configured on the pattern and the input matches, the number
     * of separators encountered is recorded and can be obtained via
     * {@link #getSeparatorCount()} or iterated using {@link #first()},
     * {@link #last()}, {@link #next()} and related methods.
     * </p>
     * @param input the input to test; if {@code null}, returns {@code false}
     * @return {@code true} if the input matches; {@code false} otherwise
     */
    public boolean matches(CharSequence input) {
        separatorCount = -1;
        separatorIndex = 0;

        if (input == null) {
            this.input = null;
            separatorFlags = null;
            return false;
        }

        this.input = input;
        separatorFlags = new int[input.length()];
        boolean result = WildcardEngine.match(pattern, input, separatorFlags);
        if (result) {
            for (int i = separatorFlags.length - 1; i >= 0; i--) {
                if (separatorFlags[i] > 0) {
                    separatorCount = separatorFlags[i];
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Scan the given input and record separator positions based on the
     * configured separator of the underlying {@link WildcardPattern}.
     * <p>
     * This method does not perform wildcard matching; it only prepares internal
     * state for segment navigation with {@link #first()}, {@link #last()},
     * {@link #next()} and {@link #previous()}.
     * </p>
     * @param input the input to separate; if {@code null}, returns {@code 0}
     * @return the number of separators found in the input
     */
    public int separate(CharSequence input) {
        separatorCount = -1;
        separatorIndex = 0;

        if (input == null) {
            this.input = null;
            separatorFlags = null;
            return 0;
        }

        this.input = input;
        int len = input.length();
        char separator = pattern.getSeparator();
        separatorFlags = new int[len];

        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == separator) {
                separatorFlags[i] = ++separatorCount;
            }
        }

        return separatorCount;
    }

    /**
     * Move the internal segment cursor to the first segment.
     * @return this matcher for method chaining
     */
    public WildcardMatcher first() {
        separatorIndex = 0;
        return this;
    }

    /**
     * Move the internal segment cursor to the last segment (if known).
     * @return this matcher for method chaining
     */
    public WildcardMatcher last() {
        if (separatorCount > -1) {
            separatorIndex = separatorCount;
        }
        return this;
    }

    /**
     * Whether a call to {@link #next()} would return a segment.
     */
    public boolean hasNext() {
        return separatorIndex <= separatorCount;
    }

    /**
     * Whether a call to {@link #previous()} would return a segment.
     */
    public boolean hasPrev() {
        return separatorIndex >= 0;
    }

    /**
     * Return the next segment based on the current cursor position and then
     * advance the cursor.
     * @return the next segment string, or {@code null} if there is none
     */
    public String next() {
        if (separatorIndex > separatorCount) {
            return null;
        }
        return find(separatorIndex++);
    }

    /**
     * Return the previous segment based on the current cursor position and then
     * move the cursor backwards.
     * @return the previous segment string, or {@code null} if there is none
     */
    public String previous() {
        if (separatorIndex < 0) {
            return null;
        }
        return find(separatorIndex--);
    }

    /**
     * Return the segment at the current cursor position without changing the
     * cursor.
     * @return the current segment, or {@code null} if unavailable
     */
    public String find() {
        return find(separatorIndex);
    }

    /**
     * Return the segment at the given group index.
     * <p>
     * Group indices range from {@code 0} (before the first separator) to
     * {@code getSeparatorCount()} (after the last separator). When the input
     * starts with a separator, the first group may be an empty string.
     * </p>
     * @param group the zero-based segment index
     * @return the segment value, or {@code null} if not available
     * @throws IndexOutOfBoundsException if {@code group} is outside the valid range
     */
    public String find(int group) {
        if (separatorCount == 0) {
            if (input == null) {
                return null;
            }
            return input.toString();
        }

        if (group < 0 || group > separatorCount) {
            throw new IndexOutOfBoundsException();
        }

        int start = 0;
        int offset = -1;

        if (group == 0) {
            for (int i = 0; i < separatorFlags.length; i++) {
                if (separatorFlags[i] == 1) {
                    offset = i;
                    break;
                }
            }

            if (offset == -1) {
                offset = separatorFlags.length;
            }
        } else {
            for (int i = 0; i < separatorFlags.length; i++) {
                if (separatorFlags[i] == group) {
                    start = i + 1;
                } else if (start > 0 && separatorFlags[i] == group + 1) {
                    offset = i;
                    break;
                }
            }

            if (start > 0 && offset == -1) {
                offset = separatorFlags.length;
            }
        }

        if (offset == -1) {
            return null;
        } else if (offset == 0) {
            return StringUtils.EMPTY;
        } else {
            return input.subSequence(start, offset).toString();
        }
    }

    /**
     * Return the number of separators found in the last processed input.
     * @return the separator count, or {@code -1} if unknown (no match yet)
     */
    public int getSeparatorCount() {
        return separatorCount;
    }

    /**
     * Return the compiled {@link WildcardPattern} used by this matcher.
     */
    public WildcardPattern getWildcardPattern() {
        return pattern;
    }

    /**
     * Static convenience to evaluate a single input against a pattern.
     * @param pattern the compiled pattern (must not be {@code null})
     * @param input the input character sequence (may be {@code null})
     * @return {@code true} if the input matches; {@code false} otherwise
     */
    public static boolean matches(WildcardPattern pattern, CharSequence input) {
        return WildcardEngine.match(pattern, input, null);
    }

}
