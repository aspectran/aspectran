/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.aspect.pointcut;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.util.wildcard.WildcardPattern;

/**
 * The Class WildcardPointcut.
 */
public class WildcardPointcut extends AbstractPointcut implements Pointcut {

	private static final String OR_MATCH_DELIMITER = "|";
	
	private Map<String, WildcardPattern> wildcardPatternCache = new WeakHashMap<String, WildcardPattern>();
	
	public WildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}
	
	public boolean patternMatches(String pattern, String compareString) {
		if(pattern.indexOf(OR_MATCH_DELIMITER) == -1) {
			return wildcardPatternMatches(pattern, compareString);
		} else {
			StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);

			while(parser.hasMoreTokens()) {
				String patternToken = parser.nextToken();
				
				if(wildcardPatternMatches(patternToken, compareString))
					return true;
			}
			
			return false;
		}
	}
	
	public boolean patternMatches(String pattern, String compareString, char separator) {
		if(pattern.indexOf(OR_MATCH_DELIMITER) == -1) {
			return wildcardPatternMatches(pattern, compareString, separator);
		} else {
			StringTokenizer parser = new StringTokenizer(pattern, OR_MATCH_DELIMITER);

			while(parser.hasMoreTokens()) {
				String patternToken = parser.nextToken();
				
				if(wildcardPatternMatches(patternToken, compareString, separator))
					return true;
			}
			
			return false;
		}
	}
	
	public boolean wildcardPatternMatches(String pattern, String compareString) {
		if(!WildcardPattern.hasWildcards(pattern)) {
			return pattern.equals(compareString);
		}
		
		WildcardPattern wildcardPattern = wildcardPatternCache.get(pattern);
			
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern);
			wildcardPatternCache.put(pattern, wildcardPattern);
		}
		
		return wildcardPattern.matches(compareString);
	}
	
	public boolean wildcardPatternMatches(String pattern, String compareString, char separator) {
		String patternId = pattern + separator;
		
		WildcardPattern wildcardPattern = wildcardPatternCache.get(patternId);
		
		if(wildcardPattern == null) {
			wildcardPattern = new WildcardPattern(pattern, separator);
			wildcardPatternCache.put(patternId, wildcardPattern);
		}
		
		return wildcardPattern.matches(compareString);
	}
	
	public void clear() {
		wildcardPatternCache.clear();
	}
	
}
