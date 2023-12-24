/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * Pointcut using Regular Expression Matching to identify joinpoints.
 */
public class RegexpPointcut extends AbstractPointcut {

    private final Cache<String, Pattern> cache =
            new ConcurrentReferenceCache<>(Pattern::compile);

    public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
        super(pointcutPatternRuleList);
    }

    @Override
    public boolean patternMatches(String regex, String compareString) {
        if (regex == null) {
            throw new IllegalArgumentException("regex must not be null");
        }
        Pattern pattern = cache.get(regex);
        Matcher matcher = pattern.matcher(compareString);
        return matcher.matches();
    }

    @Override
    public boolean patternMatches(String regex, String compareString, char separator) {
        return patternMatches(regex, compareString);
    }

    @Override
    public void clear() {
        cache.clear();
    }

}
