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
package com.aspectran.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Represents an immutable pattern with a prefix and a suffix, separated by a single wildcard character.
 * <p>This class is designed to parse and use simple wildcard patterns like "prefix*suffix".
 * It is immutable and thread-safe.</p>
 *
 * <p>Created: 2016. 1. 31.</p>
 */
public class PrefixSuffixPattern {

    /**
     * The wildcard character that separates the prefix and suffix.
     */
    public static final char PREFIX_SUFFIX_SEPARATOR = '*';

    private final String prefix;

    private final String suffix;

    /**
     * Private constructor to create an immutable instance.
     * @param prefix the prefix part of the pattern; never null, can be empty
     * @param suffix the suffix part of the pattern; never null, can be empty
     */
    private PrefixSuffixPattern(@NonNull String prefix, @NonNull String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Returns the prefix part of the pattern.
     * @return the prefix, never null (can be an empty string)
     */
    @NonNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the suffix part of the pattern.
     * @return the suffix, never null (can be an empty string)
     */
    @NonNull
    public String getSuffix() {
        return suffix;
    }

    /**
     * Encloses the given infix string with the pattern's prefix and suffix.
     * @param infix the string to enclose
     * @return the fully enclosed string
     */
    public String enclose(String infix) {
        return join(this.prefix, infix, this.suffix);
    }

    /**
     * A static helper method to join a prefix, infix, and suffix.
     * Handles empty prefixes and suffixes correctly.
     * @param prefix the prefix string
     * @param infix the mandatory infix string
     * @param suffix the suffix string
     * @return the combined string
     */
    @NonNull
    public static String join(@NonNull String prefix, @NonNull String infix, @NonNull String suffix) {
        Assert.notNull(infix, "infix must not be null");
        return prefix + infix + suffix;
    }

    /**
     * Creates a {@code PrefixSuffixPattern} instance by parsing the given expression.
     * <p>Examples:
     *  - "pre*suf" results in prefix="pre", suffix="suf"
     *  - "*suf" results in prefix="", suffix="suf"
     *  - "pre*" results in prefix="pre", suffix=""
     *  - "*" results in prefix="", suffix=""
     * </p>
     * @param expression the pattern expression to parse
     * @return a new {@link PrefixSuffixPattern} instance, or {@code null} if the expression
     *      is null, empty, or does not contain the wildcard character
     * @throws IllegalArgumentException if the expression contains more than one wildcard character
     */
    @Nullable
    public static PrefixSuffixPattern of(String expression) {
        if (!StringUtils.hasLength(expression)) {
            return null;
        }

        int firstIndex = expression.indexOf(PREFIX_SUFFIX_SEPARATOR);
        if (firstIndex == -1) {
            return null;
        }

        int lastIndex = expression.lastIndexOf(PREFIX_SUFFIX_SEPARATOR);
        if (firstIndex != lastIndex) {
            throw new IllegalArgumentException("Pattern expression must contain exactly one '" +
                    PREFIX_SUFFIX_SEPARATOR + "' wildcard, but found more than one in \"" + expression + "\"");
        }

        String prefix = expression.substring(0, firstIndex);
        String suffix = expression.substring(firstIndex + 1);
        return new PrefixSuffixPattern(prefix, suffix);
    }

}
