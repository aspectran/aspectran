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

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Utility to apply a wildcard pattern as a mask over an input sequence.
 * <p>
 * Given a compiled {@link WildcardPattern} and an input, this class erases the
 * characters that are consumed by wildcard tokens and collects only the
 * remaining literal characters in their original order. If the input does not
 * match the pattern, {@code null} is returned.
 * </p>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>The behavior is separator-aware: when a separator is configured on the
 *   pattern, single-segment wildcards ("*", "?", "+") will not cross the
 *   separator while the double-star token ("**") may span across it.</li>
 *   <li>This class does not allocate intermediate Strings while masking; it
 *   writes into a character buffer and returns a new String when matching
 *   finishes successfully.</li>
 * </ul>
 */
public class WildcardMasker {

    /**
     * Apply the supplied {@link WildcardPattern} to the given input and return a
     * masked string that retains only the characters matched by literal tokens.
     * Characters consumed by wildcard tokens are omitted.
     * <p>If the input does not conform to the pattern, this method returns
     * {@code null}.</p>
     * @param pattern the precompiled wildcard pattern (must not be {@code null})
     * @param input the input character sequence to be masked (must not be {@code null})
     * @return the masked string if the input matches; {@code null} otherwise
     */
    @Nullable
    public static String mask(WildcardPattern pattern, CharSequence input) {
        return WildcardEngine.mask(pattern, input);
    }

}
