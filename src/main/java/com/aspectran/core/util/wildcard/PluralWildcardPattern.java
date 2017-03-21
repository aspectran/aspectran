/*
 * Copyright 2008-2017 Juho Jeong
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
 * The Class PluralWildcardPattern.
 *
 * <p>Created: 2017. 2. 11.</p>
 *
 * @since 3.3.0
 */
public class PluralWildcardPattern {

    private final WildcardPattern[] patterns;

    public PluralWildcardPattern(WildcardPattern[] patterns) {
        this.patterns = patterns;
    }

    public PluralWildcardPattern(String[] patterns) {
        this.patterns = compile(patterns);
    }

    public PluralWildcardPattern(String[] patterns, char separator) {
        this.patterns = compile(patterns, separator);
    }

    public boolean matches(String compareString) {
        if (patterns != null) {
            for (WildcardPattern pattern : patterns) {
                if (pattern.matches(compareString)) {
                    return true;
                }
            }
        }
        return false;
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
            return wildcardPatternList.toArray(new WildcardPattern[wildcardPatternList.size()]);
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
            return wildcardPatternList.toArray(new WildcardPattern[wildcardPatternList.size()]);
        } else {
            return null;
        }
    }

    public static PluralWildcardPattern newInstance(String[] patterns) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        WildcardPattern[] wildcardPatterns = compile(patterns);
        if (wildcardPatterns != null) {
            return new PluralWildcardPattern(wildcardPatterns);
        } else {
            return null;
        }
    }

    public static PluralWildcardPattern newInstance(String[] patterns, char separator) {
        if (patterns == null || patterns.length == 0) {
            return null;
        }
        WildcardPattern[] wildcardPatterns = compile(patterns, separator);
        if (wildcardPatterns != null) {
            return new PluralWildcardPattern(wildcardPatterns);
        } else {
            return null;
        }
    }

}
