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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.utils.ConcurrentReferenceHashMap;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.wildcard.WildcardPattern;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A {@link Pointcut} implementation that uses wildcard matching.
 *
 * <p>This class supports wildcard characters (`*`, `?`) and OR-matching (`|`)
 * within patterns. It uses a cache for compiled wildcard patterns to optimize
 * performance.
 * </p>
 */
public class WildcardPointcut extends AbstractPointcut {

    private static final String OR_MATCH_DELIMITER = "|";

    /** Cache for compiled wildcard patterns. */
    private final Map<String, WildcardPattern> cache = new ConcurrentReferenceHashMap<>();

    /**
     * Creates a new WildcardPointcut with the given list of pointcut pattern rules.
     * @param pointcutPatternRuleList the list of pointcut pattern rules
     */
    public WildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        super(pointcutPatternRuleList);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation supports wildcard characters (`*`, `?`) and OR-matching (`|`).</p>
     * @throws IllegalArgumentException if the patternString is null
     */
    @Override
    public boolean patternMatches(String patternString, String compareString) {
        if (patternString == null) {
            throw new IllegalArgumentException("patternString must not be null");
        }
        if (patternString.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(patternString, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                if (wildcardPatternMatches(parser.nextToken(), compareString)) {
                    return true;
                }
            }
            return false;
        } else {
            return wildcardPatternMatches(patternString, compareString);
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation supports wildcard characters (`*`, `?`) and OR-matching (`|`),
     * and can use a specified separator for path-like patterns.
     * </p>
     * @throws IllegalArgumentException if the patternString is null
     */
    @Override
    public boolean patternMatches(String patternString, String compareString, char separator) {
        if (patternString == null) {
            throw new IllegalArgumentException("patternString must not be null");
        }
        if (patternString.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(patternString, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                if (wildcardPatternMatches(parser.nextToken(), compareString, separator)) {
                    return true;
                }
            }
            return false;
        } else {
            return wildcardPatternMatches(patternString, compareString, separator);
        }
    }

    /**
     * Performs wildcard pattern matching without considering OR-delimiters.
     * @param patternString the wildcard pattern string
     * @param compareString the string to compare
     * @return true if the string matches the pattern, false otherwise
     */
    private boolean wildcardPatternMatches(String patternString, String compareString) {
        if (!WildcardPattern.hasWildcards(patternString)) {
            return patternString.equals(compareString);
        }

        WildcardPattern wildcardPattern = cache.get(patternString);
        if (wildcardPattern == null) {
            wildcardPattern = new WildcardPattern(patternString);
            WildcardPattern existing = cache.putIfAbsent(patternString, wildcardPattern);
            if (existing != null) {
                wildcardPattern = existing;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    /**
     * Performs wildcard pattern matching with a specified separator.
     * @param patternString the wildcard pattern string
     * @param compareString the string to compare
     * @param separator the separator character
     * @return true if the string matches the pattern, false otherwise
     */
    private boolean wildcardPatternMatches(@NonNull String patternString, String compareString, char separator) {
        if (patternString.indexOf(separator) == -1 && !WildcardPattern.hasWildcards(patternString)) {
            return patternString.equals(compareString);
        }

        String patternKey = patternString + separator;
        WildcardPattern wildcardPattern = cache.get(patternKey);
        if (wildcardPattern == null) {
            wildcardPattern = new WildcardPattern(patternString, separator);
            WildcardPattern existing = cache.putIfAbsent(patternKey, wildcardPattern);
            if (existing != null) {
                wildcardPattern = existing;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation clears the internal cache of compiled wildcard patterns.</p>
     */
    @Override
    public void clear() {
        cache.clear();
    }

}
