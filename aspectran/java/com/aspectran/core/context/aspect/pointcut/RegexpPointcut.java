package com.aspectran.core.context.aspect.pointcut;

import java.util.List;

import com.aspectran.core.context.rule.PointcutPatternRule;

public class RegexpPointcut extends AbstractPointcut implements Pointcut {

	public RegexpPointcut(List<PointcutPatternRule> pointcutPatternRuleList) {
		super(pointcutPatternRuleList);
	}

	public boolean matches(String transletName) {
		return false;
	}

	public boolean matches(String transletName, String beanId) {
		return false;
	}

	public boolean matches(String transletName, String beanId, String beanMethodName) {
		return false;
	}
	
	public boolean exists(String transletName) {
		return false;
	}

	public boolean exists(String transletName, String beanId) {
		return false;
	}

	public boolean exists(String transletName, String beanId, String beanMethodName) {
		return false;
	}
	
	public void clearCache() {
		
	}

}
