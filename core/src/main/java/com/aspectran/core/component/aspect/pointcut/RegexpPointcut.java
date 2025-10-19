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
import com.aspectran.utils.cache.Cache;
import com.aspectran.utils.cache.ConcurrentReferenceCache;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A concrete {@link Pointcut} implementation that uses regular expressions
 * for pattern matching to identify join points.
 * <p>This class extends {@link AbstractPointcut} and utilizes a cache for
 * compiled regular expression {@link Pattern} objects to optimize performance.
 * </p>
 */
public class RegexpPointcut extends AbstractPointcut {

    /** Cache for compiled regular expression patterns. */
    private final Cache<String, Pattern> cache = new ConcurrentReferenceCache<>(Pattern::compile);

    /**
     * Creates a new RegexpPointcut with the given list of pointcut pattern rules.
     * @param pointcutPatternRuleList the list of pointcut pattern rules
     */
    public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        super(pointcutPatternRuleList);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation uses regular expression matching to compare the string against the pattern.</p>
     * @throws IllegalArgumentException if the patternString is null
     */
    @Override
    public boolean patternMatches(String patternString, String compareString) {
        if (patternString == null) {
            throw new IllegalArgumentException("regex must not be null");
        }
        Pattern pattern = cache.get(patternString);
        Matcher matcher = pattern.matcher(compareString);
        return matcher.matches();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation delegates to {@link #patternMatches(String, String)} as separators are not
     * typically used in regular expression matching for this context.
     * </p>
     */
    @Override
    public boolean patternMatches(String patternString, String compareString, char separator) {
        return patternMatches(patternString, compareString);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation clears the internal cache of compiled regular expression patterns.</p>
     */
    @Override
    public void clear() {
        cache.clear();
    }

}
