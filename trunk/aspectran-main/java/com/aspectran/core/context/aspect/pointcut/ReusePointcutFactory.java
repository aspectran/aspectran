package com.aspectran.core.context.aspect.pointcut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.core.rule.PointcutRule;

public class ReusePointcutFactory extends AbstractPointcutFactory implements PointcutFactory {

	private Map<Object, Pointcut> reusablePointcutMap = new HashMap<Object, Pointcut>();
	
	protected Pointcut createWildcardPointcut(PointcutRule pointcutRule) {
		WildcardPointcut wildcardPointcut = (WildcardPointcut)reusablePointcutMap.get(pointcutRule);
		
		if(wildcardPointcut == null) {
			wildcardPointcut = new WildcardPointcut();
			reusablePointcutMap.put(pointcutRule, wildcardPointcut);
		
			List<PointcutPattern> pointcutPatternList = parsePattern(pointcutRule.getPatternString());
			
			for(PointcutPattern pointcutPattern : pointcutPatternList) {
				wildcardPointcut.addPointcutPattern(pointcutPattern);
			}
		}
		
		return wildcardPointcut;
	}
	
	protected Pointcut createRegexpPointcut(PointcutRule pointcutRule) {
		RegexpPointcut regexpPointcut = (RegexpPointcut)reusablePointcutMap.get(pointcutRule);
		
		if(regexpPointcut == null) {
			regexpPointcut = new RegexpPointcut();
			reusablePointcutMap.put(pointcutRule, regexpPointcut);
		
			List<PointcutPattern> pointcutPatternList = parsePattern(pointcutRule.getPatternString());
			
			for(PointcutPattern pointcutPattern : pointcutPatternList) {
				regexpPointcut.addPointcutPattern(pointcutPattern);
			}
		}
		
		return regexpPointcut;
	}
}
