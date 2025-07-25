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
 * Class for Wildcard Matching with multiple Include and Exclude patterns.
 *
 * <p>The comparison string must match one of the Include patterns and must
 * not match the Exclude pattern.</p>
 *
 * <p>Created: 2017. 2. 11.</p>
 *
 * @since 3.3.0
 */
public class IncludeExcludeWildcardPatterns {

    private final WildcardPatterns includePatterns;

    private final WildcardPatterns excludePatterns;

    private IncludeExcludeWildcardPatterns(WildcardPatterns includePatterns, WildcardPatterns excludePatterns) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
    }

    public WildcardPattern[] getIncludePatterns() {
        return (includePatterns != null ? includePatterns.getPatterns() : null);
    }

    public boolean hasIncludePatterns() {
        return (includePatterns != null && includePatterns.hasPatterns());
    }

    public WildcardPattern[] getExcludePatterns() {
        return (excludePatterns != null ? excludePatterns.getPatterns() : null);
    }

    public boolean hasExcludePatterns() {
        return (excludePatterns != null && excludePatterns.hasPatterns());
    }

    public boolean matches(CharSequence input) {
        boolean includes = (includePatterns == null || includePatterns.matches(input));
        return (includes && (excludePatterns == null || !excludePatterns.matches(input)));
    }

    @NonNull
    public static IncludeExcludeWildcardPatterns of(IncludeExcludeParameters includeExcludeParameters) {
        Assert.notNull(includeExcludeParameters, "includeExcludeParameters must not be null");
        String[] includePatterns = includeExcludeParameters.getIncludePatterns();
        String[] excludePatterns = includeExcludeParameters.getExcludePatterns();
        return of(includePatterns, excludePatterns);
    }

    @NonNull
    public static IncludeExcludeWildcardPatterns of(IncludeExcludeParameters includeExcludeParameters, char separator) {
        Assert.notNull(includeExcludeParameters, "includeExcludeParameters must not be null");
        String[] includePatterns = includeExcludeParameters.getIncludePatterns();
        String[] excludePatterns = includeExcludeParameters.getExcludePatterns();
        return of(includePatterns, excludePatterns, separator);
    }

    @NonNull
    public static IncludeExcludeWildcardPatterns of(WildcardPattern[] includePatterns, WildcardPattern[] excludePatterns) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns), WildcardPatterns.of(excludePatterns));
    }

    @NonNull
    public static IncludeExcludeWildcardPatterns of(String[] includePatterns, String[] excludePatterns) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns), WildcardPatterns.of(excludePatterns));
    }

    @NonNull
    public static IncludeExcludeWildcardPatterns of(String[] includePatterns, String[] excludePatterns, char separator) {
        return new IncludeExcludeWildcardPatterns(WildcardPatterns.of(includePatterns, separator), WildcardPatterns.of(excludePatterns, separator));
    }

}
