/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.core.util.wildcard;

import java.util.ArrayList;
import java.util.List;

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
public class PluralWildcardPattern {

    private final WildcardPattern[] includePatterns;

    private final WildcardPattern[] excludePatterns;

    public PluralWildcardPattern(WildcardPattern[] includePatterns, WildcardPattern[] excludePatterns) {
        this.includePatterns = includePatterns;
        this.excludePatterns = excludePatterns;
    }

    public PluralWildcardPattern(String[] includePatterns, String[] excludePatterns) {
        this.includePatterns = compile(includePatterns);
        this.excludePatterns = compile(excludePatterns);
    }

    public PluralWildcardPattern(String[] includePatterns, String[] excludePatterns, char separator) {
        this.includePatterns = compile(includePatterns, separator);
        this.excludePatterns = compile(excludePatterns, separator);
    }

    public boolean matches(String compareString) {
        boolean result = false;
        if (includePatterns != null) {
            for (WildcardPattern pattern : includePatterns) {
                if (pattern.matches(compareString)) {
                    result = true;
                    break;
                }
            }
        } else {
            result = true;
        }
        if (result && excludePatterns != null) {
            for (WildcardPattern pattern : excludePatterns) {
                if (pattern.matches(compareString)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    public static WildcardPattern[] compile(String[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        List<WildcardPattern> wildcardPatternList = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            if (pattern != null && pattern.length() > 0) {
                WildcardPattern wildcardPattern = new WildcardPattern(pattern);
                wildcardPatternList.add(wildcardPattern);
            }
        }
        if (wildcardPatternList.size() > 0) {
            return wildcardPatternList.toArray(new WildcardPattern[0]);
        } else {
            return null;
        }
    }

    public static WildcardPattern[] compile(String[] patterns, char separator) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        List<WildcardPattern> wildcardPatternList = new ArrayList<>(patterns.length);
        for (String pattern : patterns) {
            if (pattern != null && pattern.length() > 0) {
                WildcardPattern wildcardPattern = new WildcardPattern(pattern, separator);
                wildcardPatternList.add(wildcardPattern);
            }
        }
        if (wildcardPatternList.size() > 0) {
            return wildcardPatternList.toArray(new WildcardPattern[0]);
        } else {
            return null;
        }
    }

}
