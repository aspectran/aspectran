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
package com.aspectran.core.component.aspect.pointcut;

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.util.ConcurrentReferenceHashMap;
import com.aspectran.core.util.wildcard.WildcardPattern;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Pointcut using Wildcard Matching to identify joinpoints.
 */
public class WildcardPointcut extends AbstractPointcut {

    private static final String OR_MATCH_DELIMITER = "|";

    private final Map<String, WildcardPattern> cache = new ConcurrentReferenceHashMap<>(256);

    public WildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        super(pointcutPatternRuleList);
    }

    @Override
    public boolean patternMatches(String pattern, String compareString) {
        if (pattern.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                String patternToken = parser.nextToken();
                if (wildcardPatternMatches(patternToken, compareString)) {
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
        if (pattern.contains(OR_MATCH_DELIMITER)) {
            StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);
            while (parser.hasMoreTokens()) {
                String patternToken = parser.nextToken();
                if (wildcardPatternMatches(patternToken, compareString, separator)) {
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
            WildcardPattern wildcardPattern2 = cache.putIfAbsent(pattern, wildcardPattern);
            if (wildcardPattern2 != null) {
                wildcardPattern = wildcardPattern2;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    private boolean wildcardPatternMatches(String pattern, String compareString, char separator) {
        String patternKey = pattern + separator;
        WildcardPattern wildcardPattern = cache.get(patternKey);
        if (wildcardPattern == null) {
            wildcardPattern = new WildcardPattern(pattern, separator);
            WildcardPattern wildcardPattern2 = cache.putIfAbsent(patternKey, wildcardPattern);
            if (wildcardPattern2 != null) {
                wildcardPattern = wildcardPattern2;
            }
        }
        return wildcardPattern.matches(compareString);
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
