/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.utils.ConcurrentReferenceHashMap;
import com.aspectran.utils.wildcard.WildcardPattern;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Pointcut using Wildcard Matching to identify joinpoints.
 */
public class WildcardPointcut extends AbstractPointcut {

    private static final String OR_MATCH_DELIMITER = "|";

    private final Map<String, WildcardPattern> cache = new ConcurrentReferenceHashMap<>();

    public WildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        super(pointcutPatternRuleList);
    }

    @Override
    public boolean patternMatches(String pattern, String compareString) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        if (pattern.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                if (wildcardPatternMatches(parser.nextToken(), compareString)) {
                    return true;
                }
            }
            return false;
        } else {
            return wildcardPatternMatches(pattern, compareString);
        }
    }

    @Override
    public boolean patternMatches(String pattern, String compareString, char separator) {
        if (pattern == null) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        if (pattern.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                if (wildcardPatternMatches(parser.nextToken(), compareString, separator)) {
                    return true;
                }
            }
            return false;
        } else {
            return wildcardPatternMatches(pattern, compareString, separator);
        }
    }

    private boolean wildcardPatternMatches(String pattern, String compareString) {
        if (!WildcardPattern.hasWildcards(pattern)) {
            return pattern.equals(compareString);
        }

        WildcardPattern wildcardPattern = cache.get(pattern);
        if (wildcardPattern == null) {
            wildcardPattern = new WildcardPattern(pattern);
            WildcardPattern existing = cache.putIfAbsent(pattern, wildcardPattern);
            if (existing != null) {
                wildcardPattern = existing;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    private boolean wildcardPatternMatches(String pattern, String compareString, char separator) {
        if (pattern.indexOf(separator) == -1 && !WildcardPattern.hasWildcards(pattern)) {
            return pattern.equals(compareString);
        }

        String patternKey = pattern + separator;
        WildcardPattern wildcardPattern = cache.get(patternKey);
        if (wildcardPattern == null) {
            wildcardPattern = new WildcardPattern(pattern, separator);
            WildcardPattern existing = cache.putIfAbsent(patternKey, wildcardPattern);
            if (existing != null) {
                wildcardPattern = existing;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
