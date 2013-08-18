package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.rule.PointcutRule;

public class ReusePointcutFactory extends AbstractPointcutFactory implements PointcutFactory {

	private WildcardPointcut wildcardPointcut;
	
	private RegexpPointcut regexpPointcut;
	
	protected Pointcut createWildcardPointcut(PointcutRule pointcutRule) {
		if(wildcardPointcut == null)
			wildcardPointcut = new WildcardPointcut();
		
		List<PointcutPattern> pointcutPatternList = parsePattern(pointcutRule.getPatternString());
		
		for(PointcutPattern pointcutPattern : pointcutPatternList) {
			wildcardPointcut.addPointcutPattern(pointcutPattern);
		}
		
		return wildcardPointcut;
	}
	
	protected Pointcut createRegexpPointcut(PointcutRule pointcutRule) {
		if(regexpPointcut == null)
			regexpPointcut = new RegexpPointcut();
		
		List<PointcutPattern> pointcutPatternList = parsePattern(pointcutRule.getPatternString());
		
		for(PointcutPattern pointcutPattern : pointcutPatternList) {
			regexpPointcut.addPointcutPattern(pointcutPattern);
		}
		
		return regexpPointcut;
	}
}
