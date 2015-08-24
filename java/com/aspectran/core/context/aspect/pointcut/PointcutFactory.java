package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.context.rule.PointcutPatternRule;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.rule.type.PointcutType;

/**
 * A factory for creating Pointcut objects.
 */
public class PointcutFactory {

	public static Pointcut createPointcut(PointcutRule pointcutRule) {
		if(pointcutRule.getPointcutType() == PointcutType.REGEXP) {
			return createRegexpPointcut(pointcutRule.getPointcutPatternRuleList());
		} else {
			return createWildcardPointcut(pointcutRule.getPointcutPatternRuleList());
		}
	}
	
	private static Pointcut createWildcardPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		return new WildcardPointcut(pointcutPatternRuleList);
	}
	
	private static Pointcut createRegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		return new RegexpPointcut(pointcutPatternRuleList);
	}
	
	/*
	private Pointcut createPointcut(PointcutRule pointcutRule, Pointcut pointcut) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcutRule.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null && pointcutPatternRuleList.size() > 0) {
			for(PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
				if(pointcutPatternRule.getPatternString() != null) {
					List<PointcutPatternRule> pointcutPatternList = pointcutPatternCache.get(pointcutPatternRule.getPatternString());
				
					if(pointcutPatternList == null) {
						pointcutPatternList = parsePattern(pointcutPatternRule.getPatternString());
						pointcutPatternCache.put(pointcutPatternRule.getPatternString(), pointcutPatternList);
					} 
				
					pointcut.addPointcutPatternRule(pointcutPatternList);
				} else {
					PointcutPatternRule pointcutPattern = new PointcutPatternRule(pointcutPatternRule);
					
					List<PointcutPatternRule> minusPointcutPatternRuleList = pointcutPatternRule.getExcludePointcutPatternRuleList();
					
					if(minusPointcutPatternRuleList != null) {
						for(PointcutPatternRule wppr : minusPointcutPatternRuleList) {
							PointcutPatternRule wpp = new PointcutPatternRule(wppr);
							pointcutPattern.addMinusPointcutPatternRule(wpp);;
						}
					}

					pointcut.addPointcutPatternRule(pointcutPattern);
				}
			}
		} else if(pointcutRule.getPatternString() != null) {
			List<PointcutPatternRule> pointcutPatternList = pointcutPatternCache.get(pointcutRule.getPatternString());
			
			if(pointcutPatternList == null) {
				pointcutPatternList = parsePattern(pointcutRule.getPatternString());
				pointcutPatternCache.put(pointcutRule.getPatternString(), pointcutPatternList);
			} 
		
			pointcut.addPointcutPatternRule(pointcutPatternList);
		}
		
		return pointcut;
	}
	
	protected List<PointcutPatternRule> parsePattern(String patternString) {
		String[] patterns = StringUtils.tokenize(patternString, "\n\t,;:| ");
		List<PointcutPatternRule> pointcutPatternList = null;
		
		if(patterns.length > 0) {
			pointcutPatternList = new ArrayList<PointcutPatternRule>();
			
			PointcutPatternRule pointcutPattern = null;
			
			for(String pattern : patterns) {
				if(pattern.length() > 0) {
					PointcutPatternRuleOperationType pointcutPatternOperationType = null;
					
					if(pattern.startsWith(plusOperationPrefix)) {
						pattern = patternString.substring(plusOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternRuleOperationType.PLUS;
					} else if(pattern.startsWith(minusOperationPrefix)) {
						pattern = patternString.substring(minusOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternRuleOperationType.MINUS;
					} else {
						pointcutPatternOperationType = PointcutPatternRuleOperationType.PLUS;
					}

					if(pointcutPatternOperationType == PointcutPatternRuleOperationType.PLUS) {
						if(pointcutPattern != null) {
							pointcutPatternList.add(pointcutPattern);
							pointcutPattern = null;
						}
						
						pointcutPattern =  PointcutPatternRule.createPointcutPatternRule(pattern);
					} else if(pointcutPatternOperationType == PointcutPatternRuleOperationType.MINUS) {
						PointcutPatternRule wpp =  PointcutPatternRule.createPointcutPatternRule(pattern);

						if(pointcutPattern == null)
							pointcutPattern = new PointcutPatternRule();
						
						pointcutPattern.addMinusPointcutPatternRule(wpp);
					}
				}
			}
			
			if(pointcutPattern != null)
				pointcutPatternList.add(pointcutPattern);
		}
		
		return pointcutPatternList;
	}
	
	public void close() {
		if(pointcutPatternCache != null) {
			pointcutPatternCache.clear();
			pointcutPatternCache = null;
		}
		
		if(pointcutList != null) {
			for(Pointcut pointcut : pointcutList) {
				pointcut.clearCache();
			}
	
			pointcutList.clear();
			pointcutList = null;
		}
	}
 */
	
}
