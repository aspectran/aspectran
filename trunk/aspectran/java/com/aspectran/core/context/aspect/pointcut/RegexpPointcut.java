package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.var.rule.PointcutPatternRule;

public class RegexpPointcut extends AbstractPointcut implements Pointcut {

	public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}

	public boolean matches(String transletName) {
		return false;
	}

	public boolean matches(String transletName, String beanOrActionId) {
		return false;
	}

	public boolean matches(String transletName, String beanOrActionId, String beanMethodName) {
		return false;
	}
	
	public boolean strictMatches(String transletName) {
		return false;
	}

	public boolean strictMatches(String transletName, String beanOrActionId) {
		return false;
	}

	public boolean strictMatches(String transletName, String beanOrActionId, String beanMethodName) {
		return false;
	}
	
	public void clearCache() {
		
	}

}
