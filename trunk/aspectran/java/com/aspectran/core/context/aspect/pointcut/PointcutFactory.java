package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.var.rule.PointcutPatternRule;
import com.aspectran.core.var.rule.PointcutRule;
import com.aspectran.core.var.type.PointcutPatternOperationType;
import com.aspectran.core.var.type.PointcutType;

public class PointcutFactory {

	private final String plusOperationPrefix = "+(";
	
	private final String minusOperationPrefix = "-(";
	
	private final String operationSuffix = ")";
	
	private Map<String, List<PointcutPattern>> pointcutPatternCache = new HashMap<String, List<PointcutPattern>>();
	
	private List<Pointcut> pointcutList = new ArrayList<Pointcut>();
	
	public Pointcut createPointcut(PointcutRule pointcutRule) {
		if(pointcutRule.getPointcutType() == PointcutType.WILDCARD) {
			Pointcut pointcut = createWildcardPointcut(pointcutRule);
			pointcutList.add(pointcut);
		} else if(pointcutRule.getPointcutType() == PointcutType.REGEXP) {
			Pointcut pointcut = createRegexpPointcut(pointcutRule);
			pointcutList.add(pointcut);
		}

		return null;
	}
	
	protected Pointcut createWildcardPointcut(PointcutRule pointcutRule) {
		WildcardPointcut wildcardPointcut =  new WildcardPointcut();
		
		return createPointcut(pointcutRule, wildcardPointcut);
	}
	
	protected Pointcut createRegexpPointcut(PointcutRule pointcutRule) {
		RegexpPointcut regexpPointcut =  new RegexpPointcut();
		
		return createPointcut(pointcutRule, regexpPointcut);
	}
	
	private Pointcut createPointcut(PointcutRule pointcutRule, Pointcut pointcut) {
		List<PointcutPatternRule> pointcutPatternRuleList = pointcutRule.getPointcutPatternRuleList();
		
		if(pointcutPatternRuleList != null && pointcutPatternRuleList.size() > 0) {
			for(PointcutPatternRule pointcutPatternRule : pointcutPatternRuleList) {
				if(pointcutPatternRule.getPatternString() != null) {
					List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutPatternRule.getPatternString());
				
					if(pointcutPatternList == null) {
						pointcutPatternList = parsePattern(pointcutPatternRule.getPatternString());
						pointcutPatternCache.put(pointcutPatternRule.getPatternString(), pointcutPatternList);
					} 
				
					pointcut.addPointcutPattern(pointcutPatternList);
				} else {
					PointcutPattern pointcutPattern = new PointcutPattern(pointcutPatternRule);
					
					List<PointcutPatternRule> withoutPointcutPatternRuleList = pointcutPatternRule.getMinusPointcutPatternRuleList();
					
					if(withoutPointcutPatternRuleList != null) {
						for(PointcutPatternRule wppr : withoutPointcutPatternRuleList) {
							PointcutPattern wpp = new PointcutPattern(wppr);
							pointcutPattern.addMinusPointcutPattern(wpp);;
						}
					}

					pointcut.addPointcutPattern(pointcutPattern);
				}
			}
		} else if(pointcutRule.getPatternString() != null) {
			List<PointcutPattern> pointcutPatternList = pointcutPatternCache.get(pointcutRule.getPatternString());
			
			if(pointcutPatternList == null) {
				pointcutPatternList = parsePattern(pointcutRule.getPatternString());
				pointcutPatternCache.put(pointcutRule.getPatternString(), pointcutPatternList);
			} 
		
			pointcut.addPointcutPattern(pointcutPatternList);
		}
		
		return pointcut;
	}
	
	
	protected List<PointcutPattern> parsePattern(String patternString) {
		String[] patterns = StringUtils.tokenize(patternString, "\n\t,;:| ");
		List<PointcutPattern> pointcutPatternList = null;
		
		if(patterns.length > 0) {
			pointcutPatternList = new ArrayList<PointcutPattern>();
			
			PointcutPattern pointcutPattern = null;
			
			for(String pattern : patterns) {
				if(pattern.length() > 0) {
					PointcutPatternOperationType pointcutPatternOperationType = null;
					
					if(pattern.startsWith(plusOperationPrefix)) {
						pattern = patternString.substring(plusOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.PLUS;
					} else if(pattern.startsWith(minusOperationPrefix)) {
						pattern = patternString.substring(minusOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.MINUS;
					} else {
						pointcutPatternOperationType = PointcutPatternOperationType.PLUS;
					}

					if(pointcutPatternOperationType == PointcutPatternOperationType.PLUS) {
						if(pointcutPattern != null) {
							pointcutPatternList.add(pointcutPattern);
							pointcutPattern = null;
						}
						
						pointcutPattern =  PointcutPattern.createPointcutPattern(pointcutPatternOperationType, pattern);
					} else if(pointcutPatternOperationType == PointcutPatternOperationType.MINUS) {
						PointcutPattern wpp =  PointcutPattern.createPointcutPattern(pointcutPatternOperationType, pattern);

						if(pointcutPattern == null)
							pointcutPattern = new PointcutPattern();
						
						pointcutPattern.addMinusPointcutPattern(wpp);
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
	
}
