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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A small holder for multiple precompiled {@link WildcardPattern} instances.
 * <p>
 * This type is convenient when you need to evaluate an input against a set of
 * patterns, such as include/exclude lists. Instances are immutable. Factory
 * methods are null-safe and will return {@code null} when no effective
 * patterns are provided.
 * </p>
 */
public class WildcardPatterns {

    /** The compiled patterns to evaluate in order. */
    private final WildcardPattern[] patterns;

    /**
     * Construct a new container with the given compiled patterns.
     * @param patterns the compiled patterns (must not be {@code null})
     */
    private WildcardPatterns(@NonNull WildcardPattern[] patterns) {
        this.patterns = patterns;
    }

    /**
     * Return the underlying array of compiled patterns.
     * @return the array of patterns (never {@code null})
     */
    @NonNull
    public WildcardPattern[] getPatterns() {
        return patterns;
    }

    /**
     * Whether this container has at least one pattern.
     * @return {@code true} if patterns exist; {@code false} otherwise
     */
    public boolean hasPatterns() {
        return (patterns.length > 0);
    }

    /**
     * Check if the given input matches any pattern in this container.
     * @param input the character sequence to match
     * @return {@code true} if any pattern matches; {@code false} otherwise
     */
    public boolean matches(CharSequence input) {
        for (WildcardPattern pattern : patterns) {
            if (pattern.matches(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a {@code WildcardPatterns} from an array of precompiled patterns.
     * Returns {@code null} if the input is {@code null} or empty.
     * @param patterns precompiled patterns
     * @return a new {@code WildcardPatterns} or {@code null}
     */
    @Nullable
    static WildcardPatterns of(WildcardPattern[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        return new WildcardPatterns(patterns);
    }

    /**
     * Create a {@code WildcardPatterns} from an array of pattern strings, using
     * no segment separator.
     * Returns {@code null} if the input is {@code null} or empty, or when all
     * entries are blank.
     * @param patterns the pattern strings
     * @return a new {@code WildcardPatterns} or {@code null}
     */
    @Nullable
    public static WildcardPatterns of(String[] patterns) {
        return of(patterns, Character.MIN_VALUE);
    }

    /**
     * Create a {@code WildcardPatterns} from an array of pattern strings with a
     * specific segment separator.
     * Returns {@code null} if the input is {@code null} or empty, or when all
     * entries are blank.
     * @param patterns the pattern strings
     * @param separator the segment separator character
     * @return a new {@code WildcardPatterns} or {@code null}
     */
    @Nullable
    public static WildcardPatterns of(String[] patterns, char separator) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        List<WildcardPattern> list = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            if (pattern != null && !pattern.isEmpty()) {
                WildcardPattern wildcardPattern = new WildcardPattern(pattern, separator);
                list.add(wildcardPattern);
            }
        }
        if (!list.isEmpty()) {
            return of(list.toArray(new WildcardPattern[0]));
        } else {
            return null;
        }
    }

}
