package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.rule.PointcutRule;
import com.aspectran.core.var.type.PointcutPatternOperationType;
import com.aspectran.core.var.type.PointcutType;

public class PointcutFactory {

	private final String withinOperationPrefix = PointcutPatternOperationType.WITHIN + "(";
	
	private final String withoutOperationPrefix = PointcutPatternOperationType.WITHOUT + "(";
	
	private final String operationSuffix = ")";
	
	private Map<String, List<PointcutPattern>> pointcutPatternCache = new HashMap<String, List<PointcutPattern>>();
	
	public Pointcut createPointcut(PointcutRule pointcutRule) {
		if(pointcutRule.getPointcutType() == PointcutType.WILDCARD) {
			return createWildcardPointcut(pointcutRule);
		} else if(pointcutRule.getPointcutType() == PointcutType.REGEXP) {
			return createRegexpPointcut(pointcutRule);
		}

		return null;
	}
	
	protected Pointcut createWildcardPointcut(PointcutRule pointcutRule) {
		WildcardPointcut wildcardPointcut =  new WildcardPointcut();
		
		List<PointcutPatternRule> pointcutPatternRuleList = pointcutRule.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null && pointcutPatternRuleList.size() > 0) {
			for(PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
				if(pointcutPatternRule.getPatternString() != null) {
					List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutPatternRule.getPatternString());
				
					if(pointcutPatternList == null) {
						pointcutPatternList = parsePattern(pointcutPatternRule.getPatternString());
						pointcutPatternCache.put(pointcutPatternRule.getPatternString(), pointcutPatternList);
					} 
				
					wildcardPointcut.addPointcutPattern(pointcutPatternList);
				} else {
					PointcutPattern pointcutPattern = new PointcutPattern(pointcutPatternRule);
					wildcardPointcut.addPointcutPattern(pointcutPattern);
				}
			}
		} else if(pointcutRule.getPatternString() != null) {
			List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutRule.getPatternString());
			
			if(pointcutPatternList == null) {
				pointcutPatternList = parsePattern(pointcutRule.getPatternString());
				pointcutPatternCache.put(pointcutRule.getPatternString(), pointcutPatternList);
			} 
		
			wildcardPointcut.addPointcutPattern(pointcutPatternList);
		}
		
		return wildcardPointcut;
	}
	
	protected Pointcut createRegexpPointcut(PointcutRule pointcutRule) {
		RegexpPointcut regexpPointcut =  new RegexpPointcut();
		
		List<PointcutPatternRule> pointcutPatternRuleList = pointcutRule.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null && pointcutPatternRuleList.size() > 0) {
			for(PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
				if(pointcutPatternRule.getPatternString() != null) {
					List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutPatternRule.getPatternString());
				
					if(pointcutPatternList == null) {
						pointcutPatternList = parsePattern(pointcutPatternRule.getPatternString());
						pointcutPatternCache.put(pointcutPatternRule.getPatternString(), pointcutPatternList);
					} 
				
					regexpPointcut.addPointcutPattern(pointcutPatternList);
				} else {
					PointcutPattern pointcutPattern = new PointcutPattern(pointcutPatternRule);
					regexpPointcut.addPointcutPattern(pointcutPattern);
				}
			}
		} else if(pointcutRule.getPatternString() != null) {
			List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutRule.getPatternString());
			
			if(pointcutPatternList == null) {
				pointcutPatternList = parsePattern(pointcutRule.getPatternString());
				pointcutPatternCache.put(pointcutRule.getPatternString(), pointcutPatternList);
			} 
		
			regexpPointcut.addPointcutPattern(pointcutPatternList);
		}
		
		return regexpPointcut;
	}
	
	protected List<PointcutPattern> parsePattern(String patternString) {
		String[] patterns = StringUtils.tokenize(patternString, "\n\t,;:| ");
		List<PointcutPattern> pointcutPatternList = null;
		
		if(patterns.length > 0) {
			pointcutPatternList = new ArrayList<PointcutPattern>();
			
			for(String pattern : patterns) {
				if(pattern.length() > 0) {
					PointcutPatternOperationType pointcutPatternOperationType = null;
					
					if(pattern.startsWith(withinOperationPrefix)) {
						pattern = patternString.substring(withinOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.WITHIN;
					} else if(pattern.startsWith(withoutOperationPrefix)) {
						pattern = patternString.substring(withoutOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.WITHOUT;
					} else {
						pointcutPatternOperationType = PointcutPatternOperationType.WITHIN;
					}
					
					PointcutPattern pp = PointcutPattern.createPointcutPattern(pointcutPatternOperationType, pattern);
					pointcutPatternList.add(pp);
				}
			}
		}
		
		return pointcutPatternList;
	}
	
}
