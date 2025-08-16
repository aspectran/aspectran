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
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Holder for include and exclude wildcard pattern sets and a convenience
 * evaluator for the common "include unless excluded" rule.
 * <p>
 * An input is considered a match when it matches any include pattern (or when
 * there are no include patterns) and does not match any exclude pattern.
 * </p>
 */
public class IncludeExcludeWildcardPatterns {

    /** Patterns that must include the input (optional). */
    private final WildcardPatterns includePatterns;

    /** Patterns that must exclude the input (optional). */
    private final WildcardPatterns excludePatterns;

    /**
     * Create a new holder with the given compiled pattern containers.
     * @param includePatterns include pattern container, may be {@code null}
     * @param excludePatterns exclude pattern container, may be {@code null}
     */
    private IncludeExcludeWildcardPatterns(WildcardPatterns includePatterns, WildcardPatterns excludePatterns) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
    }

    /**
     * Return the include patterns as an array, or {@code null} if none.
     */
    public WildcardPattern[] getIncludePatterns() {
        return (includePatterns != null ? includePatterns.getPatterns() : null);
    }

    /**
     * Whether include patterns exist.
     */
    public boolean hasIncludePatterns() {
        return (includePatterns != null && includePatterns.hasPatterns());
    }

    /**
     * Return the exclude patterns as an array, or {@code null} if none.
     */
    public WildcardPattern[] getExcludePatterns() {
        return (excludePatterns != null ? excludePatterns.getPatterns() : null);
    }

    /**
     * Whether exclude patterns exist.
     */
    public boolean hasExcludePatterns() {
        return (excludePatterns != null && excludePatterns.hasPatterns());
    }

    /**
     * Evaluate the input against include then exclude patterns.
     * @param input the character sequence to check
     * @return {@code true} if included and not excluded; {@code false} otherwise
     */
    public boolean matches(CharSequence input) {
        boolean includes = (includePatterns == null || includePatterns.matches(input));
        return (includes && (excludePatterns == null || !excludePatterns.matches(input)));
    }

    /**
     * Create a holder from {@link IncludeExcludeParameters} with no separator.
     * @param includeExcludeParameters the include/exclude parameter bean
     * @return a new holder (never {@code null})
     */
    @NonNull
    public static IncludeExcludeWildcardPatterns of(IncludeExcludeParameters includeExcludeParameters) {
        Assert.notNull(includeExcludeParameters, "includeExcludeParameters must not be null");
        String[] includePatterns = includeExcludeParameters.getIncludePatterns();
        String[] excludePatterns = includeExcludeParameters.getExcludePatterns();
        return of(includePatterns, excludePatterns);
    }

    /**
     * Create a holder from {@link IncludeExcludeParameters} with a separator.
     * @param includeExcludeParameters the include/exclude parameter bean
     * @param separator segment separator to use when compiling patterns
     * @return a new holder (never {@code null})
     */
    @NonNull
    public static IncludeExcludeWildcardPatterns of(IncludeExcludeParameters includeExcludeParameters, char separator) {
        Assert.notNull(includeExcludeParameters, "includeExcludeParameters must not be null");
        String[] includePatterns = includeExcludeParameters.getIncludePatterns();
        String[] excludePatterns = includeExcludeParameters.getExcludePatterns();
        return of(includePatterns, excludePatterns, separator);
    }

    /**
     * Create a holder from precompiled patterns.
     */
    @NonNull
    public static IncludeExcludeWildcardPatterns of(WildcardPattern[] includePatterns, WildcardPattern[] excludePatterns) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns), WildcardPatterns.of(excludePatterns));
    }

    /**
     * Create a holder from pattern strings with no separator.
     */
    @NonNull
    public static IncludeExcludeWildcardPatterns of(String[] includePatterns, String[] excludePatterns) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns), WildcardPatterns.of(excludePatterns));
    }

    /**
     * Create a holder from pattern strings with a separator.
     */
    @NonNull
    public static IncludeExcludeWildcardPatterns of(String[] includePatterns, String[] excludePatterns, char separator) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns, separator), WildcardPatterns.of(excludePatterns, separator));
    }

}
