/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context.aspect.pointcut;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aspectran.core.context.rule.PointcutPatternRule;

/**
 * The Class RegexpPointcut.
 */
public class RegexpPointcut extends AbstractPointcut implements Pointcut {
	
	private Map<String, Pattern> regexpPatternCache = new WeakHashMap<String, Pattern>();

	public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}

	protected boolean patternMatches(String regex, String input) {
		Pattern pattern = regexpPatternCache.get(regex);
			
		if(pattern == null) {
			pattern = Pattern.compile(regex);
			regexpPatternCache.put(regex, pattern);
		}

		Matcher matcher = pattern.matcher(input);

		return matcher.matches();
	}
	
	protected boolean patternMatches(String regex, String str, char separator) {
		return patternMatches(regex, str);
	}

}
