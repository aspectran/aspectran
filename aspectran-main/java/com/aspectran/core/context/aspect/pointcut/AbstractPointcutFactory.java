package com.aspectran.core.context.aspect.pointcut;

import java.util.ArrayList;
import java.util.List;

import com.aspectran.core.context.AspectranConstant;
import com.aspectran.core.context.rule.PointcutRule;
import com.aspectran.core.context.type.PointcutPatternOperationType;
import com.aspectran.core.context.type.PointcutType;
import com.aspectran.core.util.StringUtils;

public abstract class AbstractPointcutFactory implements PointcutFactory {

	private final String includeOperationPrefix = PointcutPatternOperationType.INCLUDE + "(";
	
	private final String excludeOperationPrefix = PointcutPatternOperationType.EXCLUDE + "(";
	
	private final String operationSuffix = ")";
	
	public Pointcut createPointcut(PointcutRule pointcutRule) {
		if(pointcutRule.getPointcutType() == PointcutType.WILDCARD) {
			return createWildcardPointcut(pointcutRule);
		} else if(pointcutRule.getPointcutType() == PointcutType.REGEXP) {
			return createRegexpPointcut(pointcutRule);
		}

		return null;
	}
	
	protected abstract Pointcut createWildcardPointcut(PointcutRule pointcutRule);
	
	protected abstract Pointcut createRegexpPointcut(PointcutRule pointcutRule);
	
	protected List<PointcutPattern> parsePattern(String patternString) {
		String[] patterns = StringUtils.tokenize(patternString, "\n\t,;:| ");
		List<PointcutPattern> pointcutPatternList = null;
		
		if(patterns.length > 0) {
			pointcutPatternList = new ArrayList<PointcutPattern>();
			
			for(String pattern : patterns) {
				if(pattern.length() > 0) {
					PointcutPatternOperationType pointcutPatternOperationType = null;
					
					if(pattern.startsWith(includeOperationPrefix)) {
						pattern = patternString.substring(includeOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.INCLUDE;
					} else if(pattern.startsWith(excludeOperationPrefix)) {
						pattern = patternString.substring(excludeOperationPrefix.length());
						if(pattern.endsWith(operationSuffix))
							pattern.substring(0, pattern.length() - 1);
						
						pointcutPatternOperationType = PointcutPatternOperationType.EXCLUDE;
					} else {
						pointcutPatternOperationType = PointcutPatternOperationType.INCLUDE;
					}
					
					PointcutPattern pp = createPointcutPattern(pointcutPatternOperationType, pattern);
					pointcutPatternList.add(pp);
				}
			}
		}
		
		return pointcutPatternList;
	}
	
	private PointcutPattern createPointcutPattern(PointcutPatternOperationType pointcutPatternOperationType, String pattern) {
		PointcutPattern pointcutPattern = new PointcutPattern();
		pointcutPattern.setPointcutPatternOperationType(pointcutPatternOperationType);
		
		String transletNamePattern = null;
		String actionNamePattern = null;
		String beanMethodNamePattern = null;

		int actionDelimiterIndex = pattern.indexOf(AspectranConstant.POINTCUT_ACTION_DELIMITER);
		
		if(actionDelimiterIndex == -1)
			transletNamePattern = pattern;
		else if(actionDelimiterIndex == 0)
			actionNamePattern = pattern.substring(1);
		else {
			transletNamePattern = pattern.substring(0, actionDelimiterIndex);
			actionNamePattern = pattern.substring(actionDelimiterIndex + 1);
		}

		if(actionNamePattern != null) {
			int beanMethodDelimiterIndex = actionNamePattern.indexOf(AspectranConstant.POINTCUT_ACTION_BEAN_METHOD_DELIMITER);
			
			if(beanMethodDelimiterIndex == 0) {
				beanMethodNamePattern = actionNamePattern.substring(1);
				actionNamePattern = null;
			} else if(beanMethodDelimiterIndex > 0) {
				beanMethodNamePattern = actionNamePattern.substring(beanMethodDelimiterIndex + 1);
				actionNamePattern = actionNamePattern.substring(0, beanMethodDelimiterIndex);
			}
		}
		
		if(transletNamePattern != null)
			pointcutPattern.setTransletNamePattern(transletNamePattern);
		
		if(actionNamePattern != null)
			pointcutPattern.setActionNamePattern(actionNamePattern);

		if(beanMethodNamePattern != null)
			pointcutPattern.setBeanMethodNamePattern(beanMethodNamePattern);
		
		return pointcutPattern;
	}
}
