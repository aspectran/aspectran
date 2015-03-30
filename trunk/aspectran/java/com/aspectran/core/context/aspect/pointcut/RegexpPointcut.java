package com.aspectran.core.context.aspect.pointcut;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aspectran.core.context.rule.PointcutPatternRule;

public class RegexpPointcut extends AbstractPointcut implements Pointcut {
	
	private Map<String, Pattern> regexpPatternCache = new WeakHashMap<String, Pattern>();

	public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}

	protected boolean patternMatches(String regex, String input) {
		Pattern pattern;
		
		synchronized(regexpPatternCache) {
			pattern = regexpPatternCache.get(regex);
			
			if(pattern == null) {
				pattern = Pattern.compile(regex);
				regexpPatternCache.put(regex, pattern);
			}
		}

		Matcher matcher = pattern.matcher(input);

		return matcher.matches();
	}
	
	protected boolean patternMatches(String regex, String str, char separator) {
		return patternMatches(regex, str);
	}

}
