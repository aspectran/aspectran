package com.aspectran.core.context.aspect.pointcut;

import com.aspectran.core.rule.PointcutRule;
import com.aspectran.core.type.PointcutType;

public class ReusePointcutFactory implements PointcutFactory {

	private WildcardPointcut wildcardPointcut;
	
	private RegexpPointcut regexpPointcut;
	
	public Pointcut createPointcut(PointcutRule pointcutRule) {
		if(pointcutRule.getPointcutType() == PointcutType.WILDCARD) {
			if(wildcardPointcut == null)
				wildcardPointcut = new WildcardPointcut();
			
			wildcardPointcut.setIncludePatternList(pointcutRule.getIncludePatternList());
			wildcardPointcut.setExcludePatternList(pointcutRule.getExcludePatternList());
			
			return wildcardPointcut;
		} else if(pointcutRule.getPointcutType() == PointcutType.REGEXP) {
			if(regexpPointcut == null)
				regexpPointcut = new RegexpPointcut();
			
			regexpPointcut.setIncludePatternList(pointcutRule.getIncludePatternList());
			regexpPointcut.setExcludePatternList(pointcutRule.getExcludePatternList());
			
			return regexpPointcut;
		}

		return null;
	}
	
}
