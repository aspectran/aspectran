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

	private final String withinOperationPrefix = "+(";
	
	private final String withoutOperationPrefix = "-(";
	
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
					
					List<PointcutPatternRule> withoutPointcutPatternRuleList = pointcutPatternRule.getWithoutPointcutPatternRuleList();
					
					if(withoutPointcutPatternRuleList != null) {
						for(PointcutPatternRule wppr : withoutPointcutPatternRuleList) {
							PointcutPattern wpp = new PointcutPattern(wppr);
							pointcutPattern.addWithoutPointcutPattern(wpp);;
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

					if(pointcutPatternOperationType == PointcutPatternOperationType.WITHIN) {
						if(pointcutPattern != null) {
							pointcutPatternList.add(pointcutPattern);
							pointcutPattern = null;
						}
						
						pointcutPattern =  PointcutPattern.createPointcutPattern(pointcutPatternOperationType, pattern);
					} else if(pointcutPatternOperationType == PointcutPatternOperationType.WITHOUT) {
						PointcutPattern wpp =  PointcutPattern.createPointcutPattern(pointcutPatternOperationType, pattern);

						if(pointcutPattern == null)
							pointcutPattern = new PointcutPattern();
						
						pointcutPattern.addWithoutPointcutPattern(wpp);
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
